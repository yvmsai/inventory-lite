package com.inventorylite.product;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;

import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.support.IntegrationTest;
import com.inventorylite.supplier.SupplierService;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.user.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

class ProductIntegrationTest extends IntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Test
    void create_asAdmin_returns201WithZeroQuantityAndMoneyAsString() throws Exception {
        Long supplierId = supplier();
        String sku = sku();

        // "quantity" in the body is ignored: stock only changes through movements.
        mvc.perform(post("/api/v1/products").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"%s\",\"name\":\"Bolt\",\"unitPrice\":\"19.90\",\"quantity\":500,\"supplierId\":%d}"
                                .formatted(sku, supplierId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value(sku))
                .andExpect(jsonPath("$.unitPrice").value("19.90"))
                .andExpect(jsonPath("$.quantity").value(0))
                .andExpect(jsonPath("$.reorderLevel").value(10))
                .andExpect(jsonPath("$.lowStock").value(true));
    }

    @Test
    void create_duplicateSku_returns409() throws Exception {
        ProductResponse existing = product(supplier(), "Nut");

        mvc.perform(post("/api/v1/products").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"%s\",\"name\":\"Other\",\"unitPrice\":\"1.00\",\"supplierId\":%d}"
                                .formatted(existing.sku(), existing.supplierId())))
                .andExpect(status().isConflict());
    }

    @Test
    void create_unknownSupplier_returns404() throws Exception {
        mvc.perform(post("/api/v1/products").header("Authorization", bearer(Role.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sku\":\"%s\",\"name\":\"X\",\"unitPrice\":\"1.00\",\"supplierId\":999999}".formatted(sku())))
                .andExpect(status().isNotFound());
    }

    @Test
    void list_searchByNameOrSkuAndFilterBySupplier() throws Exception {
        Long supplierA = supplier();
        Long supplierB = supplier();
        String marker = unique("gizmo");
        ProductResponse a = product(supplierA, "Blue " + marker);
        product(supplierB, "Red " + marker);
        String token = bearer(Role.STAFF);

        mvc.perform(get("/api/v1/products").param("q", marker.toUpperCase()).header("Authorization", token))
                .andExpect(jsonPath("$.totalElements").value(2));
        mvc.perform(get("/api/v1/products").param("q", marker).param("supplierId", supplierA.toString())
                        .header("Authorization", token))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].supplierId").value(supplierA));
        mvc.perform(get("/api/v1/products").param("q", a.sku().toLowerCase()).header("Authorization", token))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].id").value(a.id()));
        mvc.perform(get("/api/v1/products").param("supplierId", supplierB.toString()).header("Authorization", token))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void delete_withoutMovements_returns204() throws Exception {
        ProductResponse product = product(supplier(), "Temp");

        mvc.perform(delete("/api/v1/products/{id}", product.id()).header("Authorization", bearer(Role.ADMIN)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_asStaff_returns403() throws Exception {
        ProductResponse product = product(supplier(), "Kept");

        mvc.perform(delete("/api/v1/products/{id}", product.id()).header("Authorization", bearer(Role.STAFF)))
                .andExpect(status().isForbidden());
    }

    private Long supplier() {
        return supplierService.create(new SupplierRequest(unique("Supplier"), null, null, null)).id();
    }

    private ProductResponse product(Long supplierId, String name) {
        return productService.create(new ProductRequest(sku(), name, new BigDecimal("2.50"), null, supplierId, null));
    }

    private static String sku() {
        return unique("SKU").toUpperCase();
    }
}
