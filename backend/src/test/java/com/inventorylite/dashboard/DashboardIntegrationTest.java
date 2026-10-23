package com.inventorylite.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;

import com.inventorylite.dashboard.dto.DashboardResponse;
import com.inventorylite.product.ProductService;
import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.stock.MovementType;
import com.inventorylite.stock.StockMovementService;
import com.inventorylite.stock.dto.RecordMovementRequest;
import com.inventorylite.support.IntegrationTest;
import com.inventorylite.supplier.SupplierService;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.user.Role;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class DashboardIntegrationTest extends IntegrationTest {

    @Autowired
    private SupplierService supplierService;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockMovementService stockMovementService;

    @Autowired
    private DashboardService dashboardService;

    @Test
    void get_reflectsNewProductsMovementsAndStockValue() {
        DashboardResponse before = dashboardService.get();
        Long supplierId = supplierService.create(new SupplierRequest(unique("Dash"), null, null, null)).id();
        ProductResponse healthy = product(supplierId, "12.50", 2);
        ProductResponse low = product(supplierId, "1.00", 10);
        Long userId = userId(createUser(Role.STAFF));
        stockMovementService.record(healthy.id(), new RecordMovementRequest(MovementType.IN, 4, null), userId);
        stockMovementService.record(low.id(), new RecordMovementRequest(MovementType.IN, 3, null), userId);

        DashboardResponse after = dashboardService.get();

        assertThat(after.totals().productCount()).isEqualTo(before.totals().productCount() + 2);
        assertThat(after.totals().supplierCount()).isEqualTo(before.totals().supplierCount() + 1);
        // 4 x 12.50 + 3 x 1.00
        assertThat(after.totals().totalStockValue().subtract(before.totals().totalStockValue()))
                .isEqualByComparingTo(new BigDecimal("53.00"));
        assertThat(after.lowStockCount()).isEqualTo(before.lowStockCount() + 1);
        assertThat(after.recentMovements()).hasSizeLessThanOrEqualTo(10);
        assertThat(after.recentMovements().getFirst().productId()).isEqualTo(low.id());
        assertThat(after.recentMovements().get(1).productId()).isEqualTo(healthy.id());
        List<Integer> quantities = after.lowStock().stream().map(ProductResponse::quantity).toList();
        assertThat(quantities).isSorted();
        assertThat(after.lowStock()).allMatch(ProductResponse::lowStock);
    }

    @Test
    void endpoint_asStaff_returns200_withoutToken_returns401() throws Exception {
        mvc.perform(get("/api/v1/dashboard").header("Authorization", bearer(Role.STAFF)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totals.totalStockValue").isString())
                .andExpect(jsonPath("$.recentMovements").isArray());
        mvc.perform(get("/api/v1/dashboard")).andExpect(status().isUnauthorized());
    }

    private ProductResponse product(Long supplierId, String price, int reorderLevel) {
        return productService.create(new ProductRequest(unique("SKU").toUpperCase(), "Item",
                new BigDecimal(price), reorderLevel, supplierId, null));
    }
}
