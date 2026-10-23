package com.inventorylite.supplier;

import com.inventorylite.supplier.dto.SupplierResponse;

import org.springframework.stereotype.Component;

@Component
class SupplierMapper {

    SupplierResponse toResponse(Supplier supplier) {
        return new SupplierResponse(supplier.getId(), supplier.getName(), supplier.getEmail(), supplier.getPhone(),
                supplier.getVersion(), supplier.getCreatedAt(), supplier.getUpdatedAt());
    }
}
