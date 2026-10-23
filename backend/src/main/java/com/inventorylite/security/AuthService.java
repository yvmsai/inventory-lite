package com.inventorylite.security;

import java.util.Optional;

import com.inventorylite.security.dto.LoginRequest;
import com.inventorylite.security.dto.TokenResponse;
import com.inventorylite.user.AppUser;
import com.inventorylite.user.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    // Compared against when the email is unknown, so every failure path costs one BCrypt check.
    private final String dummyHash;

    public AuthService(UserService userService, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.dummyHash = passwordEncoder.encode("timing-equalizer");
    }

    public TokenResponse login(LoginRequest request) {
        Optional<AppUser> user = userService.findByEmail(request.email());
        String hash = user.map(AppUser::getPasswordHash).orElse(dummyHash);
        boolean passwordMatches = passwordEncoder.matches(request.password(), hash);
        if (user.isEmpty() || !passwordMatches || !user.get().isActive()) {
            log.warn("Login failed");
            throw new BadCredentialsException("Invalid email or password");
        }
        log.info("Login succeeded userId={}", user.get().getId());
        return new TokenResponse(jwtService.issue(user.get()), jwtService.ttlSeconds());
    }
}
