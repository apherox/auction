package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.AuctionTimeExpiredException;
import com.auction.exception.InvalidBidException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.model.Auction;
import com.auction.model.AuctionStatus;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.BidRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BidServiceTest extends AbstractServiceTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidService bidService;

    @Autowired
    private BidRepository bidRepository;

    private Auction auction;
    private User user;
    private BidRequest bidRequest;

    @BeforeEach
    void setUp() {
        auction = new Auction();
        auction.setTitle("Vintage Car");
        auction.setDescription("A classic vintage car");
        auction.setStartingPrice(10000.00);
        auction.setExpirationTime(LocalDateTime.now().plusDays(3));
        auction.setStatus("OPEN");
        auctionRepository.save(auction);

        user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        user.setEmail("johndoe@example.com");
        user.setFullName("John Doe");
        user.setRoles("USER");
        userRepository.save(user);

        bidRequest = new BidRequest();
        bidRequest.setUserId(user.getUserId());
        bidRequest.setAmount(12000.00);
    }

    @Test
    void testPlaceBid_shouldPlaceBidSuccessfully() {
        // When
        BidResponse bidResponse = bidService.placeBid(auction.getAuctionId(), bidRequest);

        // Then
        assertNotNull(bidResponse);
        assertEquals(user.getUserId(), bidResponse.getUserId());
        assertEquals(auction.getAuctionId(), bidResponse.getAuctionId());
        assertEquals(bidRequest.getAmount(), bidResponse.getAmount());
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionNotFound() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bidService.placeBid(999L, bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenUserNotFound() {
        // Given non-existent user
        bidRequest.setUserId(999L);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> bidService.placeBid(auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionTimeExpired() {
        // Given
        auction.setExpirationTime(LocalDateTime.now().minusDays(1));
        auctionRepository.save(auction);

        // When & Then
        assertThrows(AuctionTimeExpiredException.class, () -> bidService.placeBid(auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionIsClosed() {
        // Given
        auction.setStatus(AuctionStatus.CLOSED.name());
        auctionRepository.save(auction);

        // When & Then
        assertThrows(AuctionClosedException.class, () -> bidService.placeBid(auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenBidAmountIsLowerThanHighestBid() {
        // Given
        auction.setHighestBid(15000.00);
        auctionRepository.save(auction);

        // When & Then
        bidRequest.setAmount(14000.00);
        assertThrows(InvalidBidException.class, () -> bidService.placeBid(auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldUpdateAuctionHighestBid() {
        // When
        bidService.placeBid(auction.getAuctionId(), bidRequest);

        // Then
        Auction updatedAuction = auctionRepository.findById(auction.getAuctionId()).orElseThrow(RuntimeException::new);
        assertEquals(bidRequest.getAmount(), updatedAuction.getHighestBid());
        assertEquals(user.getUserId(), updatedAuction.getHighestBidUser().getUserId());
    }
}
