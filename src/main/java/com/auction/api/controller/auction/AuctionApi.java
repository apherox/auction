package com.auction.api.controller.auction;

import com.auction.api.Api;
import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionStatusResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Api
public interface AuctionApi {

    @Operation(
            summary = "Create a new auction",
            description = "This endpoint is used to create a new auction",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Auction created successfully", content = @Content(schema = @Schema(implementation = AuctionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<AuctionResponse> createAuction(@Valid @RequestBody AuctionRequest auctionRequest);

    @Operation(
            summary = "Get auction by ID",
            description = "Fetches the auction by its unique identifier",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Auction found", content = @Content(schema = @Schema(implementation = AuctionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid ID format"),
                    @ApiResponse(responseCode = "404", description = "Auction not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @GetMapping("/{id}")
    ResponseEntity<AuctionResponse> getAuctionById(@PathVariable Long id);

    @Operation(
            summary = "Get all auctions",
            description = "Fetches all auctions with pagination support",
            responses = {
                    @ApiResponse(responseCode = "200", description = "List of auctions retrieved successfully", content = @Content(schema = @Schema(implementation = AuctionStatusResponse.class))),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @GetMapping
    ResponseEntity<Page<AuctionStatusResponse>> getAllAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size);

    @Operation(
            summary = "Update auction",
            description = "This endpoint is used to update an existing auction",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Auction updated successfully", content = @Content(schema = @Schema(implementation = AuctionResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Invalid input data"),
                    @ApiResponse(responseCode = "404", description = "Auction not found"),
                    @ApiResponse(responseCode = "500", description = "Internal Server Error")
            }
    )
    @PutMapping("/{auctionId}")
    ResponseEntity<AuctionResponse> updateAuction(@PathVariable Long auctionId,
                                                  @Valid @RequestBody AuctionUpdateRequest auctionUpdateRequest);

}