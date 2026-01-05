package com.opticstore.security.auth.dto;

public record LoginRequest(
        String username,
        String password
) {}