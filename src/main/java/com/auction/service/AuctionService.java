package com.auction.service;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionStatusResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import com.auction.exception.AuctionCreationException;
import com.auction.exception.AuctionModificationException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.mapper.AuctionMapper;
import com.auction.model.Auction;
import com.auction.repository.AuctionRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Consumer;

@Service
@Slf4j
public class AuctionService {

    private final AuctionRepository auctionRepository;
    private final AuctionMapper auctionMapper;
    private final SequenceService sequenceService;

    public AuctionService(AuctionRepository auctionRepository, SequenceService sequenceService) {
        this.auctionRepository = auctionRepository;
        this.auctionMapper = AuctionMapper.INSTANCE;
        this.sequenceService = sequenceService;
    }

    public AuctionResponse createAuction(AuctionRequest auctionRequest) {
        verifyAdminRole();
        return Optional.of(auctionRequest)
                .map(auctionMapper::toAuctionEntity)
                .map(auctionRepository::save)
                .map(savedAuction -> {
                    log.info("Created auction with title: {}", auctionRequest.getTitle());
                    return auctionMapper.toAuctionApiModel(savedAuction);
                })
                .orElseThrow(() -> new AuctionCreationException("Error creating auction"));
    }

    public AuctionResponse getAuctionById(Long auctionId) {
        log.info("Get {} with id: {}", AuctionResponse.class.getSimpleName(), auctionId);
        return auctionRepository.findById(auctionId)
                .map(auctionMapper::toAuctionApiModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Auction with id %d not found", auctionId)));
    }

    public AuctionResponse updateAuction(Long auctionId, AuctionUpdateRequest auctionUpdateRequest) {
        verifyAdminRole();
        log.info("Update {} with id: {}", AuctionResponse.class.getSimpleName(), auctionId);
        final Auction auction  = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Auction with id %d not found", auctionId)));

        updateAuction(auction, auctionUpdateRequest);
        return Optional.of(auction)
                .map(auctionRepository::save)
                .map(auctionMapper::toAuctionApiModel)
                .orElseThrow(() -> new RuntimeException("Error updating auction"));
    }

    public Page<AuctionStatusResponse> getAllAuctions(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Auction> auctionPage = auctionRepository.findAll(pageable);

        return auctionPage.map(auctionMapper::toAuctionStatusApiModel);
    }

    private void updateAuction(Auction auction, AuctionUpdateRequest auctionUpdateRequest) {
        if(auction.getHighestBid() != null || auction.getHighestBidUser() != null) {
            throw new AuctionModificationException(
                    String.format("Auction with id %d can't be modified because it contains bidders", auction.getAuctionId()));
        }
        updateIfNotNull(auction::setTitle, auctionUpdateRequest.getTitle());
        updateIfNotNull(auction::setDescription, auctionUpdateRequest.getDescription());
        updateIfNotNull(auction::setStartingPrice, auctionUpdateRequest.getStartingPrice());
        updateIfNotNull(auction::setExpirationTime, auctionUpdateRequest.getExpirationTime());
        updateIfNotNull(auction::setStatus, auctionUpdateRequest.getStatus());
        auction.setUpdatedAt(LocalDateTime.now());
    }

    private <T> void updateIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    /**
     *  Only users with 'admin' roles can create auctions, handle it explicitly here instead of in SecurityConfig
     */
    private void verifyAdminRole() {
        boolean hasAdminRole = SecurityContextHolder.getContext().getAuthentication()
                .getAuthorities()
                .contains(new SimpleGrantedAuthority("ROLE_admin"));

        if (!hasAdminRole) {
            throw new AccessDeniedException("Only users with 'admin' role are allowed to create auctions");
        }
    }

    @PostConstruct
    public void resetSequences() {
        sequenceService.resetAuctionSequence();
    }
}


