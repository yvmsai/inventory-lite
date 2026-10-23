package com.inventorylite.stock;

import com.inventorylite.stock.dto.StockMovementResponse;

import org.springframework.stereotype.Component;

@Component
class StockMovementMapper {

    StockMovementResponse toResponse(StockMovement movement) {
        return new StockMovementResponse(movement.getId(), movement.getProduct().getId(),
                movement.getProduct().getSku(), movement.getProduct().getName(), movement.getType(),
                movement.getQuantity(), movement.getQuantityAfter(), movement.getNote(),
                movement.getCreatedBy().getId(), movement.getCreatedBy().getFullName(), movement.getCreatedAt());
    }
}
