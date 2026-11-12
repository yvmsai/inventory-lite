package com.inventorylite.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void normalizeOrigins_strayWhitespaceQuotesAndSlashes_areRemoved() {
        List<String> result = SecurityConfig.normalizeOrigins(List.of(
                " https://app.example.com/ ", "\"https://web.example.com\"", "http://localhost:3000"));

        assertThat(result).containsExactly(
                "https://app.example.com", "https://web.example.com", "http://localhost:3000");
    }

    @Test
    void normalizeOrigins_blankEntries_areDropped() {
        assertThat(SecurityConfig.normalizeOrigins(List.of("https://app.example.com", " ", "")))
                .containsExactly("https://app.example.com");
    }
}
