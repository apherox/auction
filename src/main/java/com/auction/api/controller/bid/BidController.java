package com.auction.api.controller.bid;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/api/auctions/{auctionId}/bids")
@RequiredArgsConstructor
public class BidController implements BidApi {

    private final BidService bidService;

    @Override
    public ResponseEntity<BidResponse> placeBid(Authentication authentication,
                                                Long auctionId,
                                                BidRequest bidRequest) {
        BidResponse createdBid = bidService.placeBid(authentication, auctionId, bidRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdBid.getBidId())
                .toUri();
        return ResponseEntity.created(location).body(createdBid);
    }
}