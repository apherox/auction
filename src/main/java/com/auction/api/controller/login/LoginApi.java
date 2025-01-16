package com.auction.api.controller.login;

import com.auction.api.Api;
import com.auction.api.model.LoginRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api
public interface LoginApi {

    @Operation(
            summary = "Login user",
            description = "This endpoint is used to authenticate a user and start a session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid login request, missing or invalid credentials"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized, invalid username or password"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @PostMapping("/login")
    ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest);
}