package com.inventorylite.dashboard;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import com.inventorylite.dashboard.dto.DashboardResponse;
import com.inventorylite.product.ProductService;
import com.inventorylite.stock.StockMovementService;
import com.inventorylite.supplier.SupplierService;

import org.junit.jupiter.api.Test;

class DashboardServiceTest {

    private final ProductService productService = mock(ProductService.class);
    private final SupplierService supplierService = mock(SupplierService.class);
    private final StockMovementService stockMovementService = mock(StockMovementService.class);
    private final DashboardService service = new DashboardService(productService, supplierService, stockMovementService);

    @Test
    void get_combinesTotalsLowStockAndTenRecentMovements() {
        when(productService.count()).thenReturn(4L);
        when(supplierService.count()).thenReturn(2L);
        when(productService.totalStockValue()).thenReturn(new BigDecimal("123.45"));
        when(productService.countLowStock()).thenReturn(1L);
        when(productService.lowStock(DashboardService.LOW_STOCK_LIMIT)).thenReturn(List.of());
        when(stockMovementService.recent(10)).thenReturn(List.of());

        DashboardResponse response = service.get();

        assertThat(response.totals().productCount()).isEqualTo(4);
        assertThat(response.totals().supplierCount()).isEqualTo(2);
        assertThat(response.totals().totalStockValue()).isEqualByComparingTo("123.45");
        assertThat(response.lowStockCount()).isEqualTo(1);
    }
}
