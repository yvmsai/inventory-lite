package com.inventorylite.stock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.product.Product;
import com.inventorylite.product.ProductService;
import com.inventorylite.stock.dto.RecordMovementRequest;
import com.inventorylite.stock.dto.StockMovementResponse;
import com.inventorylite.supplier.Supplier;
import com.inventorylite.user.AppUser;
import com.inventorylite.user.Role;
import com.inventorylite.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StockMovementServiceTest {

    private final StockMovementRepository repository = mock(StockMovementRepository.class);
    private final ProductService productService = mock(ProductService.class);
    private final UserService userService = mock(UserService.class);
    private final StockMovementService service =
            new StockMovementService(repository, productService, userService, new StockMovementMapper());
    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product("ABC-1", "Widget", BigDecimal.ONE, 10, new Supplier("S", null, null));
        when(productService.lockForUpdate(1L)).thenReturn(product);
        when(userService.getReference(7L)).thenReturn(new AppUser("u@x.com", "User", "h", Role.STAFF));
        when(repository.save(any(StockMovement.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void record_in_addsToQuantity() {
        StockMovementResponse response = service.record(1L, new RecordMovementRequest(MovementType.IN, 12, "  delivery "), 7L);

        assertThat(product.getQuantity()).isEqualTo(12);
        assertThat(response.quantityAfter()).isEqualTo(12);
        assertThat(response.note()).isEqualTo("delivery");
        assertThat(response.createdByName()).isEqualTo("User");
    }

    @Test
    void record_out_subtractsFromQuantity() {
        product.adjustQuantity(5);

        StockMovementResponse response = service.record(1L, new RecordMovementRequest(MovementType.OUT, 5, " "), 7L);

        assertThat(product.getQuantity()).isZero();
        assertThat(response.type()).isEqualTo(MovementType.OUT);
        assertThat(response.note()).isNull();
    }

    @Test
    void record_outMoreThanStock_throwsAndSavesNothing() {
        product.adjustQuantity(2);

        assertThatThrownBy(() -> service.record(1L, new RecordMovementRequest(MovementType.OUT, 3, null), 7L))
                .isInstanceOf(BusinessRuleException.class);
        assertThat(product.getQuantity()).isEqualTo(2);
        verify(repository, never()).save(any());
    }

    @Test
    void history_checksProductExists() {
        when(repository.findHistory(any(), any())).thenReturn(org.springframework.data.domain.Page.empty());

        service.history(1L, org.springframework.data.domain.PageRequest.of(0, 20));

        verify(productService).requireExists(1L);
    }
}
