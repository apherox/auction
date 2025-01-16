package com.auction.api.model.auction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Auction response model")
public class AuctionResponse extends AuctionRequest {

    @Schema(description = "ID of the auction", example = "111")
    private Long auctionId;

    @NotBlank(message = "Status is mandatory.")
    @Pattern(regexp = "OPEN|CLOSED", message = "Status must be either 'OPEN' or 'CLOSED'.")
    @Schema(description = "Status of the auction", example = "OPEN")
    private String status;

    @Schema(description = "Highest bid of the auction", example = "250.00")
    private Double highestBid;

    @Schema(description = "Username of the highest bidder of the auction", example = "john_doe")
    private String highestBidUsername;

    @Schema(description = "Timestamp of the creation of the auction", example = "2025-01-01T12:00:00.000")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp of the last update to the auction", example = "2025-01-05T12:30:00.000")
    private LocalDateTime updatedAt;
}

