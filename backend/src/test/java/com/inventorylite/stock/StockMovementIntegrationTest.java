package com.inventorylite.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.product.ProductService;
import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.stock.dto.RecordMovementRequest;
import com.inventorylite.support.IntegrationTest;
import com.inventorylite.supplier.SupplierService;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.user.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

class StockMovementIntegrationTest extends IntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockMovementService stockMovementService;

    @Test
    void record_inAndOutAsStaff_updatesQuantityAndHistoryIsNewestFirst() throws Exception {
        ProductResponse product = product();
        String staff = createUser(Role.STAFF);
        String token = bearerFor(staff);

        record(token, product.id(), "IN", 20, "Initial delivery").andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantityAfter").value(20))
                .andExpect(jsonPath("$.createdById").value(userId(staff)));
        record(token, product.id(), "OUT", 7, null).andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantityAfter").value(13));

        mvc.perform(get("/api/v1/products/{id}", product.id()).header("Authorization", token))
                .andExpect(jsonPath("$.quantity").value(13))
                .andExpect(jsonPath("$.version").value(2));
        mvc.perform(get("/api/v1/products/{id}/movements", product.id()).header("Authorization", token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].type").value("OUT"))
                .andExpect(jsonPath("$.content[1].type").value("IN"))
                .andExpect(jsonPath("$.content[1].note").value("Initial delivery"));
    }

    @Test
    void record_outBeyondStock_returns422AndLeavesQuantityUnchanged() throws Exception {
        ProductResponse product = product();
        String token = bearer(Role.STAFF);
        record(token, product.id(), "IN", 5, null);

        record(token, product.id(), "OUT", 6, null)
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("Insufficient stock")));

        assertThat(productService.get(product.id()).quantity()).isEqualTo(5);
    }

    @Test
    void record_nonPositiveQuantity_returns400() throws Exception {
        ProductResponse product = product();
        String token = bearer(Role.STAFF);

        record(token, product.id(), "IN", 0, null).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].field").value("quantity"));
        record(token, product.id(), "IN", -3, null).andExpect(status().isBadRequest());
    }

    @Test
    void record_unknownProduct_returns404() throws Exception {
        record(bearer(Role.STAFF), 999999L, "IN", 1, null).andExpect(status().isNotFound());
        mvc.perform(get("/api/v1/products/999999/movements").header("Authorization", bearer(Role.STAFF)))
                .andExpect(status().isNotFound());
    }

    @Test
    void movements_cannotBeEditedOrDeleted() throws Exception {
        ProductResponse product = product();
        String token = bearer(Role.ADMIN);

        mvc.perform(put("/api/v1/products/{id}/movements", product.id()).header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed());
        mvc.perform(delete("/api/v1/products/{id}/movements", product.id()).header("Authorization", token))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void deleteProduct_withMovements_returns409() throws Exception {
        ProductResponse product = product();
        String token = bearer(Role.ADMIN);
        record(token, product.id(), "IN", 1, null);

        mvc.perform(delete("/api/v1/products/{id}", product.id()).header("Authorization", token))
                .andExpect(status().isConflict());
    }

    @Test
    void concurrentOuts_neverOversellAndNeverLoseUpdates() throws Exception {
        ProductResponse product = product();
        Long userId = userId(createUser(Role.STAFF));
        stockMovementService.record(product.id(), new RecordMovementRequest(MovementType.IN, 10, null), userId);

        List<Callable<Boolean>> outs = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            outs.add(() -> {
                try {
                    stockMovementService.record(product.id(), new RecordMovementRequest(MovementType.OUT, 1, null), userId);
                    return true;
                } catch (BusinessRuleException e) {
                    return false;
                }
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(8);
        long succeeded;
        try {
            succeeded = pool.invokeAll(outs).stream().filter(StockMovementIntegrationTest::await).count();
        } finally {
            pool.shutdown();
        }

        assertThat(succeeded).isEqualTo(10);
        assertThat(productService.get(product.id()).quantity()).isZero();
        assertThat(stockMovementService.history(product.id(), org.springframework.data.domain.PageRequest.of(0, 50))
                .getTotalElements()).isEqualTo(11);
    }

    @Test
    void concurrentIns_allApplied() throws Exception {
        ProductResponse product = product();
        Long userId = userId(createUser(Role.STAFF));
        List<Callable<Boolean>> ins = new ArrayList<>();
        for (int i = 0; i < 25; i++) {
            ins.add(() -> {
                stockMovementService.record(product.id(), new RecordMovementRequest(MovementType.IN, 2, null), userId);
                return true;
            });
        }
        ExecutorService pool = Executors.newFixedThreadPool(8);
        try {
            pool.invokeAll(ins).forEach(StockMovementIntegrationTest::await);
        } finally {
            pool.shutdown();
        }

        assertThat(productService.get(product.id()).quantity()).isEqualTo(50);
    }

    private ResultActions record(String token, Long productId, String type, int quantity, String note) throws Exception {
        String noteJson = note == null ? "null" : "\"" + note + "\"";
        return mvc.perform(post("/api/v1/products/{id}/movements", productId).header("Authorization", token)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"%s\",\"quantity\":%d,\"note\":%s}".formatted(type, quantity, noteJson)));
    }

    private ProductResponse product() {
        Long supplierId = supplierService.create(new SupplierRequest(unique("Supplier"), null, null, null)).id();
        return productService.create(new ProductRequest(unique("SKU").toUpperCase(), "Widget",
                new BigDecimal("3.00"), 5, supplierId, null));
    }

    private static boolean await(Future<Boolean> future) {
        try {
            return future.get();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
