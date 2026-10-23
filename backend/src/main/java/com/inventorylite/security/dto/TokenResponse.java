package com.inventorylite.security.dto;

public record TokenResponse(String accessToken, long expiresIn) {}
