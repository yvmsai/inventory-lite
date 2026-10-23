package com.inventorylite.supplier;

import java.util.Objects;

import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.common.persistence.LikePatterns;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.supplier.dto.SupplierResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class SupplierService {

    private static final Logger log = LoggerFactory.getLogger(SupplierService.class);

    private final SupplierRepository repository;
    private final SupplierMapper mapper;

    public SupplierService(SupplierRepository repository, SupplierMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public Page<SupplierResponse> list(String query, Pageable pageable) {
        Page<Supplier> page = StringUtils.hasText(query)
                ? repository.search(LikePatterns.contains(query), pageable)
                : repository.findAll(pageable);
        return page.map(mapper::toResponse);
    }

    public SupplierResponse get(Long id) {
        return mapper.toResponse(find(id));
    }

    public long count() {
        return repository.count();
    }

    /** Loads a supplier for another feature to reference; 404 if it doesn't exist. */
    public Supplier require(Long id) {
        return find(id);
    }

    @Transactional
    public SupplierResponse create(SupplierRequest request) {
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw duplicateName(name);
        }
        Supplier saved = repository.save(new Supplier(name, blankToNull(request.email()), blankToNull(request.phone())));
        log.info("Supplier created id={}", saved.getId());
        return mapper.toResponse(saved);
    }

    @Transactional
    public SupplierResponse update(Long id, SupplierRequest request) {
        Supplier supplier = find(id);
        if (!Objects.equals(request.version(), supplier.getVersion())) {
            throw new ObjectOptimisticLockingFailureException(Supplier.class, id);
        }
        String name = request.name().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw duplicateName(name);
        }
        supplier.update(name, blankToNull(request.email()), blankToNull(request.phone()));
        log.info("Supplier updated id={}", id);
        return mapper.toResponse(repository.saveAndFlush(supplier));
    }

    @Transactional
    public void delete(Long id) {
        Supplier supplier = find(id);
        if (repository.hasProducts(id)) {
            throw new ConflictException("Supplier '%s' has products and cannot be deleted".formatted(supplier.getName()));
        }
        repository.delete(supplier);
        log.info("Supplier deleted id={}", id);
    }

    private Supplier find(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Supplier", id));
    }

    private static ConflictException duplicateName(String name) {
        return new ConflictException("A supplier named '%s' already exists".formatted(name));
    }

    private static String blankToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
