package com.auction.service;

import com.auction.api.model.bid.BidRequest;
import com.auction.api.model.bid.BidResponse;
import com.auction.exception.AuctionClosedException;
import com.auction.exception.AuctionOptimisticLockException;
import com.auction.exception.AuctionTimeExpiredException;
import com.auction.exception.BidCreationException;
import com.auction.exception.InvalidBidException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.mapper.BidMapper;
import com.auction.model.Auction;
import com.auction.model.AuctionStatus;
import com.auction.model.User;
import com.auction.repository.AuctionRepository;
import com.auction.repository.BidRepository;
import com.auction.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.OptimisticLockException;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

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
    public BidResponse placeBid(final Long auctionId, final BidRequest bidRequest) {
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Auction with id %d not found", auctionId)));

        User user = userRepository.findById(bidRequest.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %d not found", bidRequest.getUserId())));

        if (auction.getExpirationTime().isBefore(LocalDateTime.now())) {
            throw new AuctionTimeExpiredException(
                    String.format("Bids on auction with id %d can't be made since it is expired", auction.getAuctionId()));
        }

        if (auction.getStatus().equals(AuctionStatus.CLOSED.name())) {
            throw new AuctionClosedException(
                    String.format("Bids on auction with id %d can't be made since it is closed", auction.getAuctionId()));
        }

        Double highestBid = auction.getHighestBid();
        if (highestBid != null && bidRequest.getAmount().compareTo(highestBid) <= 0) {
            throw new InvalidBidException(
                    String.format("Bid amount %s must be higher than the current highest bid amount %s", bidRequest.getAmount(), highestBid));
        }

        try {
            return Optional.of(bidRequest)
                    .map(bidMapper::toBidEntity)
                    .map(bid -> {
                        bid.setAuction(auction);
                        bid.setUser(user);
                        bid.setBidTime(LocalDateTime.now());
                        return bid;
                    })
                    .map(bidRepository::save)
                    .map(savedBid -> {
                        log.info("Created bid for auction with id: {}", auctionId);
                        auction.setHighestBid(bidRequest.getAmount());
                        auction.setHighestBidUser(user);
                        auction.setUpdatedAt(LocalDateTime.now());
                        auctionRepository.save(auction);
                        return bidMapper.toBidApiModel(savedBid);
                    })
                    .orElseThrow(() -> new BidCreationException("Error creating bid for auction with id " + auctionId));
        } catch (OptimisticLockException | OptimisticLockingFailureException e) {
            throw new AuctionOptimisticLockException(
                    String.format("Bid failed due to concurrent updates on auction with id %d. Please retry", auctionId));
        }
    }

    @PostConstruct
    public void resetSequences() {
        sequenceService.resetBidSequence();
    }
}
