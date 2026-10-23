package com.inventorylite.product;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.supplier.Supplier;

import org.junit.jupiter.api.Test;

class ProductTest {

    private final Product product = new Product("ABC-1", "Widget", new BigDecimal("2.50"), 10, new Supplier("S", null, null));

    @Test
    void adjustQuantity_inThenOut_tracksBalance() {
        assertThat(product.adjustQuantity(15)).isEqualTo(15);
        assertThat(product.adjustQuantity(-15)).isZero();
    }

    @Test
    void adjustQuantity_belowZero_throwsAndLeavesQuantityUnchanged() {
        product.adjustQuantity(3);

        assertThatThrownBy(() -> product.adjustQuantity(-4))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("3 available, 4 requested");
        assertThat(product.getQuantity()).isEqualTo(3);
    }

    @Test
    void adjustQuantity_overflow_throws() {
        product.adjustQuantity(Integer.MAX_VALUE);

        assertThatThrownBy(() -> product.adjustQuantity(1)).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void isLowStock_atOrBelowReorderLevel() {
        assertThat(product.isLowStock()).isTrue();
        product.adjustQuantity(10);
        assertThat(product.isLowStock()).isTrue();
        product.adjustQuantity(1);
        assertThat(product.isLowStock()).isFalse();
    }
}
