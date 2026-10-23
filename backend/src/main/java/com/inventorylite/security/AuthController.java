package com.inventorylite.security;

import com.inventorylite.security.dto.LoginRequest;
import com.inventorylite.security.dto.TokenResponse;
import com.inventorylite.user.UserService;
import com.inventorylite.user.dto.UserResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
class AuthController {

    private final AuthService authService;
    private final UserService userService;

    AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Log in with email and password and receive a bearer token")
    TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    @Operation(summary = "The logged-in user's profile")
    UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.get(CurrentUser.id(jwt));
    }
}
