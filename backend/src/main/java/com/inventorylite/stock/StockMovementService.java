package com.inventorylite.stock;

import java.util.List;

import com.inventorylite.product.Product;
import com.inventorylite.product.ProductService;
import com.inventorylite.stock.dto.RecordMovementRequest;
import com.inventorylite.stock.dto.StockMovementResponse;
import com.inventorylite.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class StockMovementService {

    private static final Logger log = LoggerFactory.getLogger(StockMovementService.class);

    private final StockMovementRepository repository;
    private final ProductService productService;
    private final UserService userService;
    private final StockMovementMapper mapper;

    public StockMovementService(StockMovementRepository repository, ProductService productService,
                                UserService userService, StockMovementMapper mapper) {
        this.repository = repository;
        this.productService = productService;
        this.userService = userService;
        this.mapper = mapper;
    }

    /**
     * Records a movement and updates the product's quantity in one transaction. The product row is locked
     * first, so concurrent movements on the same product are applied one after another (ADR 0005).
     */
    @Transactional
    public StockMovementResponse record(Long productId, RecordMovementRequest request, Long currentUserId) {
        Product product = productService.lockForUpdate(productId);
        int quantityAfter = product.adjustQuantity(request.type().signed(request.quantity()));
        String note = StringUtils.hasText(request.note()) ? request.note().trim() : null;
        StockMovement saved = repository.save(new StockMovement(product, request.type(), request.quantity(),
                quantityAfter, note, userService.getReference(currentUserId)));
        log.info("Stock movement recorded id={} productId={} type={} quantity={} quantityAfter={}",
                saved.getId(), productId, request.type(), request.quantity(), quantityAfter);
        return mapper.toResponse(saved);
    }

    public Page<StockMovementResponse> history(Long productId, Pageable pageable) {
        productService.requireExists(productId);
        // The order is fixed (newest first), so any client-supplied sort is ignored.
        Pageable page = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        return repository.findHistory(productId, page).map(mapper::toResponse);
    }

    public List<StockMovementResponse> recent(int limit) {
        return repository.findRecent(PageRequest.ofSize(limit)).stream().map(mapper::toResponse).toList();
    }
}
