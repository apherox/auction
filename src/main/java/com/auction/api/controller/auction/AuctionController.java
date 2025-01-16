package com.auction.api.controller.auction;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionStatusResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import com.auction.service.AuctionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/api/auctions")
@RequiredArgsConstructor
@Slf4j
public class AuctionController implements AuctionApi {

    private final AuctionService auctionService;

    public ResponseEntity<AuctionResponse> createAuction(AuctionRequest auctionRequest) {
         AuctionResponse createdAuction = auctionService.createAuction(auctionRequest);
         URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdAuction.getAuctionId())
                .toUri();
         return ResponseEntity.created(location).body(createdAuction);
    }

    public ResponseEntity<AuctionResponse> getAuctionById(Long id) {
        AuctionResponse auction = auctionService.getAuctionById(id);
        return new ResponseEntity<>(auction, HttpStatus.OK);
    }

    public ResponseEntity<Page<AuctionStatusResponse>> getAllAuctions(int page, int size) {

        Page<AuctionStatusResponse> auctionPage = auctionService.getAllAuctions(page, size);
        return ResponseEntity.ok(auctionPage);
    }

    public ResponseEntity<AuctionResponse> updateAuction(Long auctionId, AuctionUpdateRequest auctionUpdateRequest) {
        log.info("Update Auction with id {}, with values: {}", auctionId, auctionUpdateRequest);
        return ResponseEntity.ok().body(auctionService.updateAuction(auctionId, auctionUpdateRequest));
    }
}


