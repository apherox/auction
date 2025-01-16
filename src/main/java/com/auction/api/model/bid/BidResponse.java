package com.auction.api.model.bid;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Schema(description = "Bid response model")
public class BidResponse extends BidRequest {

    @Schema(description = "ID of the bid", example = "111")
    private Long bidId;

    @Schema(description = "ID of the auction", example = "9")
    private Long auctionId;

    @Schema(description = "Title of the auction", example = "Antique Vase")
    private String auctionTitle;

    @Schema(description = "ID of the user", example = "66")
    private Long userId;

    @Schema(description = "Username of the user", example = "john_doe")
    private String username;

    @Schema(description = "Time of the created bid", example = "2025-01-14T16:34:26.666")
    private LocalDateTime bidTime;
}
