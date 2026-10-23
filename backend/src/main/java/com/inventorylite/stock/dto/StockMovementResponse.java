package com.inventorylite.stock.dto;

import java.time.Instant;

import com.inventorylite.stock.MovementType;

public record StockMovementResponse(
        Long id, Long productId, String productSku, String productName, MovementType type, int quantity,
        int quantityAfter, String note, Long createdById, String createdByName, Instant createdAt) {}
