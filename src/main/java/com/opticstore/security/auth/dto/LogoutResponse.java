package com.opticstore.security.auth.dto;


public record LogoutResponse(
        String message,
        boolean success
) {}