package com.inventorylite.user;

import com.inventorylite.common.persistence.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "app_user")
public class AppUser extends BaseEntity {

    /** Always stored lower-cased; uniqueness is case-insensitive. */
    @Column(nullable = false, length = 200)
    private String email;

    @Column(nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, length = 100)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;

    protected AppUser() {
    }

    public AppUser(String email, String fullName, String passwordHash, Role role) {
        this.email = email;
        this.fullName = fullName;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
    public String getPasswordHash() { return passwordHash; }
    public Role getRole() { return role; }
    public boolean isActive() { return active; }

    void rename(String fullName) { this.fullName = fullName; }
    void changeRole(Role role) { this.role = role; }
    void deactivate() { this.active = false; }
    void activate() { this.active = true; }
}
