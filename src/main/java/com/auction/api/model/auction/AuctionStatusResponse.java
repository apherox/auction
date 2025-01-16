package com.auction.api.model.auction;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@Schema(description = "Auction status model")
public class AuctionStatusResponse {

    @Schema(description = "Title of the auction", example = "Antique Vase")
    private String title;

    @Schema(description = "Highest bid of the auction", example = "250.00")
    private Double highestBid;

    @Pattern(regexp = "OPEN|CLOSED", message = "Status must be either 'OPEN' or 'CLOSED'.")
    @Schema(description = "Status of the auction", example = "CLOSED")
    private String status;
}
