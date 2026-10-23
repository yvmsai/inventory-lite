package com.inventorylite.product;

import java.math.BigDecimal;

import com.inventorylite.common.error.BusinessRuleException;
import com.inventorylite.common.persistence.BaseEntity;
import com.inventorylite.supplier.Supplier;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    public static final int DEFAULT_REORDER_LEVEL = 10;

    @Column(nullable = false, length = 64)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal unitPrice;

    /** Changed only through {@link #adjustQuantity(int)}, i.e. by stock movements. */
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private int reorderLevel = DEFAULT_REORDER_LEVEL;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    protected Product() {
    }

    public Product(String sku, String name, BigDecimal unitPrice, int reorderLevel, Supplier supplier) {
        update(sku, name, unitPrice, reorderLevel, supplier);
    }

    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public int getQuantity() { return quantity; }
    public int getReorderLevel() { return reorderLevel; }
    public Supplier getSupplier() { return supplier; }

    public boolean isLowStock() {
        return quantity <= reorderLevel;
    }

    /** Applies a signed stock change and returns the new quantity; stock can never go below zero. */
    public int adjustQuantity(int delta) {
        long next = (long) quantity + delta;
        if (next < 0) {
            throw new BusinessRuleException("Insufficient stock for %s: %d available, %d requested"
                    .formatted(sku, quantity, -delta));
        }
        if (next > Integer.MAX_VALUE) {
            throw new BusinessRuleException("Stock for %s would exceed the maximum quantity".formatted(sku));
        }
        quantity = (int) next;
        return quantity;
    }

    final void update(String sku, String name, BigDecimal unitPrice, int reorderLevel, Supplier supplier) {
        this.sku = sku;
        this.name = name;
        this.unitPrice = unitPrice;
        this.reorderLevel = reorderLevel;
        this.supplier = supplier;
    }
}
