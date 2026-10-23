package com.inventorylite.supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.inventorylite.common.error.ConflictException;
import com.inventorylite.common.error.NotFoundException;
import com.inventorylite.supplier.dto.SupplierRequest;
import com.inventorylite.supplier.dto.SupplierResponse;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

class SupplierServiceTest {

    private final SupplierRepository repository = mock(SupplierRepository.class);
    private final SupplierService service = new SupplierService(repository, new SupplierMapper());

    @Test
    void create_trimsAndStoresBlankOptionalFieldsAsNull() {
        when(repository.save(any(Supplier.class))).thenAnswer(i -> i.getArgument(0));

        SupplierResponse created = service.create(new SupplierRequest("  Acme  ", " ", "", null));

        assertThat(created.name()).isEqualTo("Acme");
        assertThat(created.email()).isNull();
        assertThat(created.phone()).isNull();
    }

    @Test
    void create_duplicateName_throwsConflict() {
        when(repository.existsByNameIgnoreCase("Acme")).thenReturn(true);

        assertThatThrownBy(() -> service.create(new SupplierRequest("Acme", null, null, null)))
                .isInstanceOf(ConflictException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_staleVersion_throwsOptimisticLock() {
        Supplier supplier = new Supplier("Acme", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        assertThatThrownBy(() -> service.update(1L, new SupplierRequest("Acme 2", null, null, 3L)))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }

    @Test
    void update_nameTakenByAnotherSupplier_throwsConflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Supplier("Acme", null, null)));
        when(repository.existsByNameIgnoreCaseAndIdNot("Globex", 1L)).thenReturn(true);

        assertThatThrownBy(() -> service.update(1L, new SupplierRequest("Globex", null, null, null)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_valid_changesFields() {
        Supplier supplier = new Supplier("Acme", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));
        when(repository.saveAndFlush(supplier)).thenReturn(supplier);

        SupplierResponse updated = service.update(1L, new SupplierRequest("Acme Ltd", "a@acme.test", "123", null));

        assertThat(updated.name()).isEqualTo("Acme Ltd");
        assertThat(updated.email()).isEqualTo("a@acme.test");
    }

    @Test
    void delete_supplierWithProducts_throwsConflict() {
        when(repository.findById(1L)).thenReturn(Optional.of(new Supplier("Acme", null, null)));
        when(repository.hasProducts(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("has products");
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_supplierWithoutProducts_deletes() {
        Supplier supplier = new Supplier("Acme", null, null);
        when(repository.findById(1L)).thenReturn(Optional.of(supplier));

        service.delete(1L);

        verify(repository).delete(supplier);
    }

    @Test
    void list_withQuery_searchesWithEscapedLowercasePattern() {
        Pageable pageable = PageRequest.of(0, 20);
        Page<Supplier> empty = new PageImpl<>(List.of());
        when(repository.search(any(), any())).thenReturn(empty);

        service.list(" 50%_Off ", pageable);

        ArgumentCaptor<String> pattern = ArgumentCaptor.forClass(String.class);
        verify(repository).search(pattern.capture(), any());
        assertThat(pattern.getValue()).isEqualTo("%50\\%\\_off%");
    }

    @Test
    void list_withoutQuery_listsAll() {
        Pageable pageable = PageRequest.of(0, 20);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(new Supplier("Acme", null, null))));

        assertThat(service.list(null, pageable).getContent()).hasSize(1);
    }

    @Test
    void require_unknownId_throwsNotFound() {
        when(repository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.require(9L)).isInstanceOf(NotFoundException.class);
    }
}
