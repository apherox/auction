package com.auction.api.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "User response model")
public class UserResponse {

    @Schema(description = "ID of the user", example = "111")
    private Long userId;

    @NotBlank(message = "Username is mandatory.")
    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @NotBlank(message = "Email is mandatory.")
    @Schema(description = "Email of the user", example = "john.doe@auction.com")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Full name is mandatory.")
    @Schema(description = "Full name of the user", example = "John Doe")
    private String fullName;

    @NotBlank(message = "Roles is mandatory.")
    @Schema(description = "Roles of the user", example = "USER")
    @Pattern(regexp = "^(user|admin)(,\\s*(user|admin))*$", message = "Roles must be a comma-separated list of 'user' and 'admin'.")
    private String roles;

    @Schema(description = "Timestamp of the creation of the user", example = "2021-03-24 16:34:26.666")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last login of the user", example = "2021-03-24 16:34:26.666")
    private LocalDateTime lastLogin;

}
