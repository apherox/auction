package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.AuctionTimeExpiredException;
import com.auction.exception.AuthenticationException;
import com.auction.exception.InvalidBidException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.mapper.BidMapper;
import com.auction.model.Auction;
import com.auction.model.AuctionStatus;
import com.auction.model.Bid;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.BidRepository;
import com.auction.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class BidService {

    private final AuctionRepository auctionRepository;
    private final BidRepository bidRepository;
    private final UserRepository userRepository;
    private final BidMapper bidMapper;
    private final SequenceService sequenceService;

    public BidService(AuctionRepository auctionRepository, UserRepository userRepository, SequenceService sequenceService, BidRepository bidRepository) {
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
        this.sequenceService = sequenceService;
        this.bidMapper = BidMapper.INSTANCE;
        this.bidRepository = bidRepository;
    }

    @Transactional
    public BidResponse placeBid(final Authentication authentication,
                                final Long auctionId, final BidRequest bidRequest) {
        String username = getUsernameFromAuthentication(authentication);
        Auction auction = getAuctionById(auctionId);
        User user = getUserByUsername(username);

        validateAuctionStatus(auction);
        validateBidAmount(auction, bidRequest);

        int retryAttempts = 3;
        while (retryAttempts > 0) {
            try {
                Bid savedBid = saveBid(bidRequest, auction, user);
                updateAuctionWithHighestBid(auction, bidRequest, user);
                return bidMapper.toBidApiModel(savedBid);
            } catch (OptimisticLockException e) {
                retryAttempts--;
                if (retryAttempts == 0) {
                    throw new InvalidBidException("Failed to place bid due to concurrent modifications. Please try again.");
                }
                auction = getAuctionById(auctionId);
            }
        }
        throw new InvalidBidException("Failed to place bid after multiple attempts.");
    }

    private String getUsernameFromAuthentication(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userDetails.getUsername();
        } else {
            throw new AuthenticationException("Invalid authentication principal");
        }
    }

    private Auction getAuctionById(Long auctionId) {
        return auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Auction with id %d not found", auctionId)));
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with username %s not found", username)));
    }

    private void validateAuctionStatus(Auction auction) {
        if (auction.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AuctionTimeExpiredException(
                    String.format("Bids on auction with id %d can't be made since it is expired", auction.getAuctionId()));
        }

        if (AuctionStatus.CLOSED.equals(AuctionStatus.valueOf(auction.getStatus()))) {
            throw new AuctionClosedException(
                    String.format("Bids on auction with id %d can't be made since it is closed", auction.getAuctionId()));
        }
    }

    private void validateBidAmount(Auction auction, BidRequest bidRequest) {
        Double highestBid = auction.getHighestBid();
        Double startingPrice = auction.getStartingPrice();

        if (highestBid == null && bidRequest.getAmount().compareTo(startingPrice) < 0) {
            throw new InvalidBidException(
                    String.format("Bid amount %s must be greater than or equal to the starting price %s",
                            bidRequest.getAmount(), startingPrice));
        }

        if (highestBid != null && bidRequest.getAmount().compareTo(highestBid) <= 0) {
            throw new InvalidBidException(
                    String.format("Bid amount %s must be higher than the current highest bid amount %s",
                            bidRequest.getAmount(), highestBid));
        }
    }

    private Bid saveBid(BidRequest bidRequest, Auction auction, User user) {
        Bid bid = bidMapper.toBidEntity(bidRequest);
        bid.setAuction(auction);
        bid.setUser(user);
        bid.setBidTime(LocalDateTime.now());
        return bidRepository.save(bid);
    }

    private void updateAuctionWithHighestBid(Auction auction, BidRequest bidRequest, User user) {
        auction.setHighestBid(bidRequest.getAmount());
        auction.setHighestBidUser(user);
        auction.setUpdatedAt(LocalDateTime.now());
        auctionRepository.save(auction);
    }

    @PostConstruct
    public void resetSequences() {
        sequenceService.resetBidSequence();
    }
}
