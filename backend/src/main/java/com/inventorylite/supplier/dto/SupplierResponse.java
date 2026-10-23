package com.inventorylite.supplier.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;

public record SupplierResponse(
        Long id, String name,
        @Schema(types = {"string", "null"}) String email,
        @Schema(types = {"string", "null"}) String phone, Long version, Instant createdAt, Instant updatedAt) {}
