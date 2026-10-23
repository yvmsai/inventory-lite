package com.inventorylite.user.dto;

import java.time.Instant;

import com.inventorylite.user.Role;

public record UserResponse(
        Long id, String email, String fullName, Role role, boolean active, Instant createdAt, Instant updatedAt) {}
