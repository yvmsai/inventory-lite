package com.inventorylite.product;

import com.inventorylite.product.dto.ProductResponse;

import org.springframework.stereotype.Component;

@Component
class ProductMapper {

    ProductResponse toResponse(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getUnitPrice(),
                product.getQuantity(), product.getReorderLevel(), product.isLowStock(),
                product.getSupplier().getId(), product.getSupplier().getName(),
                product.getVersion(), product.getCreatedAt(), product.getUpdatedAt());
    }
}
