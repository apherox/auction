package com.auction.api.model.auction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString(callSuper = true)
@Schema(description = "Auction request update model")
public class AuctionUpdateRequest {

    @Schema(description = "Title of the auction", example = "Antique Vase")
    private String title;

    @Schema(description = "Description of the auction", example = "An ancient porcelain vase dating back to the Ming Dynasty.")
    private String description;

    @Positive(message = "Starting price must be greater than zero.")
    @Schema(description = "Starting price of the auction", example = "150.00")
    private Double startingPrice;

    @Schema(description = "Expiration time of the auction", example = "2025-01-12T16:34:26.666")
    private LocalDateTime expirationTime;

    @Pattern(regexp = "OPEN|CLOSED", message = "Status must be either 'OPEN' or 'CLOSED'.")
    @Schema(description = "Status of the auction", example = "CLOSED")
    private String status;

}
