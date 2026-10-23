package com.inventorylite.user.dto;

import com.inventorylite.user.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank @Email @Size(max = 200) String email,
        @NotBlank @Size(max = 150) String fullName,
        // BCrypt only uses the first 72 bytes.
        @NotBlank @Size(min = 8, max = 72) String password,
        @NotNull Role role) {}
