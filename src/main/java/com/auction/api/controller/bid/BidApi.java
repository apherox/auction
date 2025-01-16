package com.auction.api.controller.bid;

import com.auction.api.Api;
import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api
public interface BidApi {

    @Operation(
            summary = "Place a bid",
            description = "This endpoint is used to place a bid on an auction",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bid placed successfully", content = @Content(schema = @Schema(implementation = BidResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Auction not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<BidResponse> placeBid(@PathVariable Long auctionId, @RequestBody BidRequest bidRequest);
}