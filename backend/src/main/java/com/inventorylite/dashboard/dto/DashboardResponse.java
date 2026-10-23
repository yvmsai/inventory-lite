package com.inventorylite.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.stock.dto.StockMovementResponse;

public record DashboardResponse(
        Totals totals,
        long lowStockCount,
        List<ProductResponse> lowStock,
        List<StockMovementResponse> recentMovements) {

    public record Totals(long productCount, long supplierCount, BigDecimal totalStockValue) {}
}
