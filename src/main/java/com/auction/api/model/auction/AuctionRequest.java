package com.auction.api.model.auction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Auction request model")
public class AuctionRequest {

    @NotBlank(message = "Title is mandatory.")
    @Schema(description = "Title of the auction", example = "Antique Vase")
    private String title;

    @NotBlank
    @Schema(description = "Description of the auction", example = "An ancient porcelain vase dating back to the Ming Dynasty.")
    private String description;

    @NotNull(message = "Starting price is mandatory.")
    @Positive(message = "Starting price must be greater than zero.")
    @Schema(description = "Starting price of the auction", example = "150.00")
    private Double startingPrice;

    @NotNull(message = "Expiration time is mandatory.")
    @Schema(description = "Expiration time of the auction", example = "2025-01-12T16:34:26.666")
    private LocalDateTime expirationTime;
}
