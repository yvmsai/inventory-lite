package com.inventorylite.supplier;

import com.inventorylite.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "supplier")
public class Supplier extends BaseEntity {

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 200)
    private String email;

    @Column(length = 30)
    private String phone;

    protected Supplier() {
    }

    public Supplier(String name, String email, String phone) {
        update(name, email, phone);
    }

    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    final void update(String name, String email, String phone) {
        this.name = name;
        this.email = email;
        this.phone = phone;
    }
}
