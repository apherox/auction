package com.auction.api.controller.login;

import com.auction.api.Api;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;

@Api
public interface LogoutApi {


    @Operation(
            summary = "Logout user",
            description = "This endpoint is used to log out the currently authenticated user and invalidate the session",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Logout successful"),
                    @ApiResponse(responseCode = "400", description = "Invalid logout request"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized, no active session found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @PostMapping("/logout")
    ResponseEntity<String> logout();
}