package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.exception.InvalidBidException;
import com.auction.model.Auction;
import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.RoleRepository;
import com.auction.repository.UserRepository;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OptimisticLockTest extends AbstractServiceTest {

    @MockitoBean
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private BidService bidService;

    @MockitoBean
    private Authentication authentication;

    private User user;
    private BidRequest bidRequest;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setRoleName("USER");
        roleRepository.save(userRole);

        user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        user.setEmail("johndoe@example.com");
        user.setFullName("John Doe");
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        bidRequest = new BidRequest();
        bidRequest.setAmount(12000.00);

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(user.getUsername());
        authentication = new UsernamePasswordAuthenticationToken(userDetails, user.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void testPlaceBid_shouldRetryOnOptimisticLockException() {
        // Given
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10.00);
        when(auction.getHighestBidUser()).thenReturn(user);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");

        // Simulate OptimisticLockException on the first save attempt, then succeed on the second attempt
        when(auctionRepository.save(any(Auction.class)))
                .thenThrow(OptimisticLockException.class)
                .thenReturn(auction);

        // When
        BidResponse bidResponse = bidService.placeBid(authentication, auction.getAuctionId(), bidRequest);

        // Then
        assertNotNull(bidResponse);
        verify(auctionRepository, times(2)).save(any(Auction.class));
    }

    @Test
    void testPlaceBid_shouldThrowInvalidBidExceptionAfterRetriesExhausted() {
        // Given
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10.00);
        when(auction.getHighestBidUser()).thenReturn(user);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");

        // Simulate OptimisticLockException on all retry attempts
        doThrow(OptimisticLockException.class).when(auctionRepository).save(any(Auction.class));

        // When & Then
        assertThrowsExactly(InvalidBidException.class,
                () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest)
        );

        // Verify that the save method was called 3 times (initial attempt + 2 retries)
        verify(auctionRepository, times(3)).save(any(Auction.class));
    }

    @Test
    void testPlaceBid_shouldSucceedWithoutRetry() {
        // Given
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10.00);
        when(auction.getHighestBidUser()).thenReturn(user);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");

        // Simulate successful save
        when(auctionRepository.save(any(Auction.class)))
                .thenReturn(auction);

        // When
        BidResponse bidResponse = bidService.placeBid(authentication, auction.getAuctionId(), bidRequest);

        // Then
        assertNotNull(bidResponse);
        verify(auctionRepository, times(1)).save(any(Auction.class));
    }

    @Test
    void testPlaceBid_shouldFailOnValidation() {
        // Given
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(anyLong())).thenReturn(Optional.of(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10000.00);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");

        // Bid amount lower than current highest bid
        bidRequest.setAmount(5000.00);

        // When & Then
        assertThrowsExactly(InvalidBidException.class,
                () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest)
        );

        verify(auctionRepository, never()).save(any(Auction.class));
    }
}

