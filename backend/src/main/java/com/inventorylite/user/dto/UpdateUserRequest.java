package com.inventorylite.user.dto;

import com.inventorylite.user.Role;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Partial update: null fields are left unchanged. */
public record UpdateUserRequest(
        @Size(max = 150) @Pattern(regexp = ".*\\S.*", message = "must not be blank") String fullName,
        Role role) {}
