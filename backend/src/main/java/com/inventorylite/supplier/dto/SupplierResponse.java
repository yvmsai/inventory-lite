package com.inventorylite.supplier.dto;

import java.time.Instant;

public record SupplierResponse(
        Long id, String name, String email, String phone, Long version, Instant createdAt, Instant updatedAt) {}
