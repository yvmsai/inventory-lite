package com.inventorylite.stock;

import java.time.Instant;

import com.inventorylite.product.Product;
import com.inventorylite.user.AppUser;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/** An immutable stock change. It has no version or updated_* columns because it is never modified. */
@Entity
@Immutable
@Table(name = "stock_movement")
@EntityListeners(AuditingEntityListener.class)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false, updatable = false)
    private Product product;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 32)
    private MovementType type;

    @Column(nullable = false, updatable = false)
    private int quantity;

    @Column(nullable = false, updatable = false)
    private int quantityAfter;

    @Column(length = 500, updatable = false)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false, updatable = false)
    private AppUser createdBy;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected StockMovement() {
    }

    StockMovement(Product product, MovementType type, int quantity, int quantityAfter, String note, AppUser createdBy) {
        this.product = product;
        this.type = type;
        this.quantity = quantity;
        this.quantityAfter = quantityAfter;
        this.note = note;
        this.createdBy = createdBy;
    }

    public Long getId() { return id; }
    public Product getProduct() { return product; }
    public MovementType getType() { return type; }
    public int getQuantity() { return quantity; }
    public int getQuantityAfter() { return quantityAfter; }
    public String getNote() { return note; }
    public AppUser getCreatedBy() { return createdBy; }
    public Instant getCreatedAt() { return createdAt; }
}
