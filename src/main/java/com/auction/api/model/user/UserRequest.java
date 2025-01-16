package com.auction.api.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "User request model")
public class UserRequest extends UserResponse {

    @NotBlank(message = "Password must not be null or empty")
    @Schema(description = "Password of the user", example = "password")
    private String password;
}

