package com.inventorylite.stock.dto;

import com.inventorylite.stock.MovementType;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record RecordMovementRequest(
        @NotNull MovementType type,
        @NotNull @Positive Integer quantity,
        @Size(max = 500) String note) {}
