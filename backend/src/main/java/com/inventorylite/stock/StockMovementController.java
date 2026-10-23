package com.inventorylite.stock;

import com.inventorylite.common.web.PageResponse;
import com.inventorylite.security.CurrentUser;
import com.inventorylite.stock.dto.RecordMovementRequest;
import com.inventorylite.stock.dto.StockMovementResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Movements are immutable, so there are deliberately no update or delete endpoints. */
@RestController
@RequestMapping("/api/v1/products/{productId}/movements")
@Tag(name = "Stock movements")
class StockMovementController {

    private final StockMovementService service;

    StockMovementController(StockMovementService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Movement history for a product, newest first")
    PageResponse<StockMovementResponse> history(@PathVariable Long productId,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return PageResponse.from(service.history(productId, pageable));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Record a stock movement (IN or OUT); updates the product quantity")
    StockMovementResponse record(@PathVariable Long productId, @Valid @RequestBody RecordMovementRequest request,
                                 @AuthenticationPrincipal Jwt jwt) {
        return service.record(productId, request, CurrentUser.id(jwt));
    }
}
