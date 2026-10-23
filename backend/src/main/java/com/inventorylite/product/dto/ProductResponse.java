package com.inventorylite.product.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record ProductResponse(
        Long id, String sku, String name, BigDecimal unitPrice, int quantity, int reorderLevel, boolean lowStock,
        Long supplierId, String supplierName, Long version, Instant createdAt, Instant updatedAt) {}
