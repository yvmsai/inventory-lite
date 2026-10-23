package com.inventorylite.supplier.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Create and update share one shape; {@code version} is required on update for optimistic locking. */
public record SupplierRequest(
        @NotBlank @Size(max = 150) String name,
        @Email @Size(max = 200) String email,
        @Size(max = 30) String phone,
        Long version) {}
