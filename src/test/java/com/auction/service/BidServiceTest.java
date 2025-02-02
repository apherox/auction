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
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

class BidServiceTest extends AbstractServiceTest {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidService bidService;

    @MockitoBean
    private Authentication authentication;

    private Auction auction;
    private User user;
    private BidRequest bidRequest;

    private static final String USERNAME = "test_user";
    private static final String ROLE_ADMIN = "admin";

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
        bidRequest.setAmount(12000.00);

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(user.getUsername());
        authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testPlaceBid_shouldPlaceBidSuccessfully() {
        // When
        BidResponse bidResponse = bidService.placeBid(authentication, auction.getAuctionId(), bidRequest);

        // Then
        assertNotNull(bidResponse);
        assertEquals(user.getUserId(), bidResponse.getUserId());
        assertEquals(auction.getAuctionId(), bidResponse.getAuctionId());
        assertEquals(bidRequest.getAmount(), bidResponse.getAmount());
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionNotFound() {
        // When & Then
        SecurityContextHolder.getContext().setAuthentication(authentication);
        assertThrows(ResourceNotFoundException.class, () -> bidService.placeBid(authentication, 999L, bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionTimeExpired() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        auction.setExpirationTime(LocalDateTime.now().minusDays(1));
        auctionRepository.save(auction);

        // When & Then
        assertThrows(AuctionTimeExpiredException.class, () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenAuctionIsClosed() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        auction.setStatus(AuctionStatus.CLOSED.name());
        auctionRepository.save(auction);

        // When & Then
        assertThrowsExactly(AuctionClosedException.class, () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldThrowExceptionWhenBidAmountIsLowerThanHighestBid() {
        // Given
        SecurityContextHolder.getContext().setAuthentication(authentication);
        auction.setHighestBid(15000.00);
        auctionRepository.save(auction);

        // When & Then
        bidRequest.setAmount(14000.00);
        assertThrowsExactly(InvalidBidException.class, () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest));
    }

    @Test
    void testPlaceBid_shouldUpdateAuctionHighestBid() {
        // When
        SecurityContextHolder.getContext().setAuthentication(authentication);
        bidService.placeBid(authentication, auction.getAuctionId(), bidRequest);

        // Then
        Auction updatedAuction = auctionRepository.findById(auction.getAuctionId()).orElseThrow(RuntimeException::new);
        assertEquals(bidRequest.getAmount(), updatedAuction.getHighestBid());
        assertEquals(user.getUserId(), updatedAuction.getHighestBidUser().getUserId());
    }
}
