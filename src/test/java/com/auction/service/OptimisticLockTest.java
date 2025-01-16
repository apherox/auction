package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.exception.AuctionOptimisticLockException;
import com.auction.model.Auction;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
        bidRequest.setUserId(user.getUserId());
        bidRequest.setAmount(12000.00);
    }

    @Test
    void testPlaceBid_shouldPlaceBidSuccessfully() {
        // When
        Auction auction = mock(Auction.class);

        when(auctionRepository.findById(any())).thenReturn(Optional.ofNullable(auction));
        when(auction.getAuctionId()).thenReturn(999L);
        when(auction.getHighestBid()).thenReturn(10.00);
        when(auction.getHighestBidUser()).thenReturn(user);
        when(auction.getExpirationTime()).thenReturn(LocalDateTime.now().plusDays(3));
        when(auction.getStatus()).thenReturn("OPEN");
        doThrow(OptimisticLockingFailureException.class).when(auctionRepository).save(any(Auction.class));

        AuctionOptimisticLockException exception = assertThrows(AuctionOptimisticLockException.class,
                () -> bidService.placeBid(auction.getAuctionId(), bidRequest)
        );

        assertEquals(
                String.format("Bid failed due to concurrent updates on auction with id %d. Please retry",
                        auction.getAuctionId()), exception.getMessage());
    }
}
