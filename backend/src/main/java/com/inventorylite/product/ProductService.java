package com.inventorylite.product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.common.persistence.LikePatterns;
import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.supplier.Supplier;
import com.inventorylite.supplier.SupplierService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository repository;
    private final SupplierService supplierService;
    private final ProductMapper mapper;

    public ProductService(ProductRepository repository, SupplierService supplierService, ProductMapper mapper) {
        this.repository = repository;
        this.supplierService = supplierService;
        this.mapper = mapper;
    }

    public Page<ProductResponse> list(String query, Long supplierId, Pageable pageable) {
        String pattern = StringUtils.hasText(query) ? LikePatterns.contains(query) : null;
        return repository.search(pattern, supplierId, pageable).map(mapper::toResponse);
    }

    public ProductResponse get(Long id) {
        return mapper.toResponse(repository.findWithSupplierById(id)
                .orElseThrow(() -> new NotFoundException("Product", id)));
    }

    public long count() {
        return repository.count();
    }

    public BigDecimal totalStockValue() {
        return repository.totalStockValue().setScale(2);
    }

    public long countLowStock() {
        return repository.countLowStock();
    }

    public List<ProductResponse> lowStock(int limit) {
        return repository.findLowStock(PageRequest.ofSize(limit)).stream().map(mapper::toResponse).toList();
    }

    /** Loads the product with a row lock; the caller must be in a write transaction. */
    public Product lockForUpdate(Long id) {
        return repository.findForUpdate(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    public void requireExists(Long id) {
        if (!repository.existsById(id)) {
            throw new NotFoundException("Product", id);
        }
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        if (repository.existsBySku(request.sku())) {
            throw duplicateSku(request.sku());
        }
        Supplier supplier = supplierService.require(request.supplierId());
        Product saved = repository.save(new Product(request.sku(), request.name().trim(), request.unitPrice(),
                reorderLevel(request), supplier));
        log.info("Product created id={} sku={}", saved.getId(), saved.getSku());
        return mapper.toResponse(saved);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product product = find(id);
        if (!Objects.equals(request.version(), product.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Product.class, id);
        }
        if (repository.existsBySkuAndIdNot(request.sku(), id)) {
            throw duplicateSku(request.sku());
        }
        Supplier supplier = supplierService.require(request.supplierId());
        product.update(request.sku(), request.name().trim(), request.unitPrice(), reorderLevel(request), supplier);
        log.info("Product updated id={} sku={}", id, request.sku());
        return mapper.toResponse(repository.saveAndFlush(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = find(id);
        if (repository.hasMovements(id)) {
            throw new ConflictException("Product '%s' has stock movements and cannot be deleted"
                    .formatted(product.getSku()));
        }
        repository.delete(product);
        log.info("Product deleted id={}", id);
    }

    private Product find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    private static int reorderLevel(ProductRequest request) {
        return request.reorderLevel() != null ? request.reorderLevel() : Product.DEFAULT_REORDER_LEVEL;
    }

    private static ConflictException duplicateSku(String sku) {
        return new ConflictException("A product with SKU '%s' already exists".formatted(sku));
    }
}
