package com.inventorylite.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.product.dto.ProductRequest;
import com.inventorylite.product.dto.ProductResponse;
import com.inventorylite.supplier.Supplier;
import com.inventorylite.supplier.SupplierService;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class ProductServiceTest {

    private final ProductRepository repository = mock(ProductRepository.class);
    private final SupplierService supplierService = mock(SupplierService.class);
    private final ProductService service = new ProductService(repository, supplierService, new ProductMapper());
    private final Supplier supplier = new Supplier("Acme", null, null);

    @Test
    void create_withoutReorderLevel_defaultsTo10AndZeroQuantity() {
        when(supplierService.require(1L)).thenReturn(supplier);
        when(repository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        ProductResponse created = service.create(request("ABC-1", null, null));

        assertThat(created.reorderLevel()).isEqualTo(10);
        assertThat(created.quantity()).isZero();
        assertThat(created.lowStock()).isTrue();
        assertThat(created.supplierName()).isEqualTo("Acme");
    }

    @Test
    void create_duplicateSku_throwsConflict() {
        when(repository.existsBySku("ABC-1")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request("ABC-1", 5, null))).isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_staleVersion_throwsOptimisticLock() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Product("ABC-1", "W", BigDecimal.ONE, 10, supplier)));

        assertThatThrownBy(() -> service.update(1L, request("ABC-1", 5, 7L)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void update_skuTakenByAnotherProduct_throwsConflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Product("ABC-1", "W", BigDecimal.ONE, 10, supplier)));
        when(repository.existsBySkuAndIdNot("XYZ-9", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, request("XYZ-9", 5, null))).isInstanceOf(ConflictException.class);
    }

    @Test
    void update_valid_changesFieldsButNotQuantity() {
        Product product = new Product("ABC-1", "W", BigDecimal.ONE, 10, supplier);
        product.adjustQuantity(4);
        when(repository.findById(1L)).thenReturn(Optional.of(product));
        when(supplierService.require(1L)).thenReturn(supplier);
        when(repository.saveAndFlush(product)).thenReturn(product);

        ProductResponse updated = service.update(1L, request("ABC-2", 2, null));

        assertThat(updated.sku()).isEqualTo("ABC-2");
        assertThat(updated.reorderLevel()).isEqualTo(2);
        assertThat(updated.quantity()).isEqualTo(4);
        assertThat(updated.lowStock()).isFalse();
    }

    @Test
    void delete_productWithMovements_throwsConflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Product("ABC-1", "W", BigDecimal.ONE, 10, supplier)));
        when(repository.hasMovements(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("stock movements");
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_productWithoutMovements_deletes() {
        Product product = new Product("ABC-1", "W", BigDecimal.ONE, 10, supplier);
        when(repository.findById(1L)).thenReturn(Optional.of(product));

        service.delete(1L);

        verify(repository).delete(product);
    }

    @Test
    void list_blankQuery_passesNullPattern() {
        when(repository.search(isNull(), eq(3L), any())).thenReturn(new PageImpl<>(List.of()));

        assertThat(service.list("  ", 3L, PageRequest.of(0, 20))).isEmpty();
    }

    @Test
    void get_unknown_throwsNotFound() {
        when(repository.findWithSupplierById(5L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(5L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.lockForUpdate(5L)).isInstanceOf(NotFoundException.class);
        assertThatThrownBy(() -> service.requireExists(5L)).isInstanceOf(NotFoundException.class);
    }

    @Test
    void totalStockValue_isScaledToCents() {
        when(repository.totalStockValue()).thenReturn(BigDecimal.ZERO);

        assertThat(service.totalStockValue()).isEqualByComparingTo("0").hasToString("0.00");
    }

    private static ProductRequest request(String sku, Integer reorderLevel, Long version) {
        return new ProductRequest(sku, "Widget", new BigDecimal("9.99"), reorderLevel, 1L, version);
    }
}
