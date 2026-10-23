package com.inventorylite.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Create and update share one shape. Quantity is deliberately absent: it changes only through stock movements.
 * {@code reorderLevel} defaults to 10 when omitted; {@code version} is required on update.
 */
public record ProductRequest(
        @NotBlank @Size(max = 64)
        @Pattern(regexp = "^[A-Z0-9-]+$", message = "must contain only uppercase letters, digits and hyphens")
        String sku,
        @NotBlank @Size(max = 200) String name,
        @NotNull @DecimalMin("0.00") @Digits(integer = 17, fraction = 2) BigDecimal unitPrice,
        @PositiveOrZero Integer reorderLevel,
        @NotNull Long supplierId,
        Long version) {}
