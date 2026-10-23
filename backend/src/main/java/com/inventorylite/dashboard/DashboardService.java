package com.inventorylite.dashboard;

import com.inventorylite.dashboard.dto.DashboardResponse;
import com.inventorylite.product.ProductService;
import com.inventorylite.stock.StockMovementService;
import com.inventorylite.supplier.SupplierService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    /** The low-stock list is capped; {@code lowStockCount} tells the UI how many there are in total. */
    static final int LOW_STOCK_LIMIT = 50;
    static final int RECENT_MOVEMENTS = 10;

    private final ProductService productService;
    private final SupplierService supplierService;
    private final StockMovementService stockMovementService;

    public DashboardService(ProductService productService, SupplierService supplierService,
                            StockMovementService stockMovementService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.stockMovementService = stockMovementService;
    }

    public DashboardResponse get() {
        DashboardResponse.Totals totals = new DashboardResponse.Totals(
                productService.count(), supplierService.count(), productService.totalStockValue());
        return new DashboardResponse(totals, productService.countLowStock(),
                productService.lowStock(LOW_STOCK_LIMIT), stockMovementService.recent(RECENT_MOVEMENTS));
    }
}
