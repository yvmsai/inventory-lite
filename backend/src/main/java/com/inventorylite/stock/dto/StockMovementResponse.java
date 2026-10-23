package com.inventorylite.stock.dto;

import java.time.Instant;

import com.inventorylite.stock.MovementType;

import io.swagger.v3.oas.annotations.media.Schema;

public record StockMovementResponse(
        Long id, Long productId, String productSku, String productName, MovementType type, int quantity,
        int quantityAfter, @Schema(types = {"string", "null"}) String note, Long createdById, String createdByName, Instant createdAt) {}
