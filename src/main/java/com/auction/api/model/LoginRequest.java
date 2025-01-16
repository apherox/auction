package com.auction.api.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Login request model")
public record LoginRequest(
        @Schema(description = "Username of the user", example = "john_doe")
        String username,
        @Schema(description = "Password of the user", example = "password123")
        String password) {}
