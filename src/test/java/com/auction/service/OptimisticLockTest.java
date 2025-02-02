package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.model.Auction;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OptimisticLockTest extends AbstractServiceTest {

    @MockitoBean
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidService bidService;

    @MockitoBean
    private Authentication authentication;

    private User user;
    private BidRequest bidRequest;

    @BeforeEach
    void setUp() {
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
    void testPlaceBid_shouldThrowException() {
        // When
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.ofNullable(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10.00);
        when(auction.getHighestBidUser()).thenReturn(user);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");
        doThrow(OptimisticLockingFailureException.class).when(auctionRepository).save(any(Auction.class));

        assertThrowsExactly(OptimisticLockingFailureException.class,
                () -> bidService.placeBid(authentication, auction.getAuctionId(), bidRequest)
        );
    }
}
