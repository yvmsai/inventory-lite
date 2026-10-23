package com.inventorylite.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.inventorylite.security.dto.LoginRequest;
import com.inventorylite.security.dto.TokenResponse;
import com.inventorylite.user.AppUser;
import com.inventorylite.user.Role;
import com.inventorylite.user.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

class AuthServiceTest {

    private final UserService userService = mock(UserService.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
    private final JwtService jwtService = mock(JwtService.class);
    private AuthService service;

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenReturn("dummy-hash");
        service = new AuthService(userService, passwordEncoder, jwtService);
    }

    @Test
    void login_validCredentials_returnsToken() {
        AppUser user = new AppUser("a@x.com", "A", "hash", Role.ADMIN);
        when(userService.findByEmail("a@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret", "hash")).thenReturn(true);
        when(jwtService.issue(user)).thenReturn("token");
        when(jwtService.ttlSeconds()).thenReturn(1800L);

        TokenResponse response = service.login(new LoginRequest("a@x.com", "secret"));

        assertThat(response.accessToken()).isEqualTo("token");
        assertThat(response.expiresIn()).isEqualTo(1800L);
    }

    @Test
    void login_unknownEmail_throwsBadCredentialsAfterCheckingDummyHash() {
        when(userService.findByEmail("n@x.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.login(new LoginRequest("n@x.com", "secret")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
        verify(passwordEncoder).matches("secret", "dummy-hash");
    }

    @Test
    void login_wrongPassword_throwsBadCredentials() {
        AppUser user = new AppUser("a@x.com", "A", "hash", Role.ADMIN);
        when(userService.findByEmail("a@x.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("bad", "hash")).thenReturn(false);

        assertThatThrownBy(() -> service.login(new LoginRequest("a@x.com", "bad")))
                .isInstanceOf(BadCredentialsException.class)
                .hasMessage("Invalid email or password");
    }
}
