package com.auction.service;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionStatusResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import com.auction.exception.AuctionModificationException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.model.Auction;
import com.auction.repository.AuctionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuctionServiceTest extends AbstractServiceTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    private static final String USERNAME = "test_user";
    private static final String ROLE_ADMIN = "admin";

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testCreateAuction() {
        // Given
        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle("Vintage Watch");
        auctionRequest.setDescription("A rare vintage wristwatch");
        auctionRequest.setStartingPrice(Double.valueOf("100.00"));
        auctionRequest.setExpirationTime(LocalDateTime.now().plusDays(3));

        // When
        AuctionResponse createdAuctionResponse = auctionService.createAuction(auctionRequest);

        // Then
        assertNotNull(createdAuctionResponse);
        assertNotNull(createdAuctionResponse.getAuctionId());
        assertEquals(auctionRequest.getTitle(), createdAuctionResponse.getTitle());
        assertEquals(auctionRequest.getDescription(), createdAuctionResponse.getDescription());
        assertEquals(auctionRequest.getStartingPrice(), createdAuctionResponse.getStartingPrice());
        assertEquals("OPEN", createdAuctionResponse.getStatus());
    }

    @Test
    void testGetAuctionById_shouldThrowResourceNotFoundException() {
        // Given
        Long invalidAuctionId = 999L;

        // When
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            auctionService.getAuctionById(invalidAuctionId);
        });

        // Then
        assertEquals("Auction with id 999 not found", exception.getMessage());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testUpdateAuction_shouldThrowExceptionWhenAuctionIsAlreadyBiddedOn() {
        // Given
        Auction auction = new Auction();
        auction.setTitle("Title 1");
        auction.setDescription("Description 1");
        auction.setStartingPrice(100.00);
        auction.setExpirationTime(LocalDateTime.now().plusDays(3));
        auction.setHighestBid(150.00);
        auction.setStatus("OPEN");
        final Auction savedAuction = auctionRepository.save(auction);

        AuctionUpdateRequest auctionUpdateRequest = new AuctionUpdateRequest();
        auctionUpdateRequest.setTitle("Updated Title");

        // When
        AuctionModificationException exception = assertThrows(AuctionModificationException.class, () -> {
            auctionService.updateAuction(savedAuction.getAuctionId(), auctionUpdateRequest);
        });

        // Then
        assertEquals(String.format("Auction with id %d can't be modified because it contains bidders", savedAuction
                .getAuctionId()), exception.getMessage());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testUpdateAuction_shouldUpdateSuccessfullyWhenNoBids() {
        // Given: an auction without any bids
        Auction auction = new Auction();
        auction.setTitle("Title 1");
        auction.setDescription("Description 1");
        auction.setStartingPrice(100.00);
        auction.setExpirationTime(LocalDateTime.now().plusDays(3));
        auction.setStatus("OPEN");
        auction.setHighestBid(null);
        final Auction savedAuction = auctionRepository.save(auction);

        AuctionUpdateRequest auctionUpdateRequest = new AuctionUpdateRequest();
        auctionUpdateRequest.setTitle("Updated Title");
        auctionUpdateRequest.setDescription("Updated Description");
        auctionUpdateRequest.setStartingPrice(888.99);
        auctionUpdateRequest.setExpirationTime(LocalDateTime.of(2025, 11, 12, 13, 15));
        auctionUpdateRequest.setStatus("CLOSED");

        // When
        AuctionResponse updatedAuctionResponse = auctionService.updateAuction(savedAuction
                .getAuctionId(), auctionUpdateRequest);

        // Then
        assertNotNull(updatedAuctionResponse);
        assertEquals("Updated Title", updatedAuctionResponse.getTitle());
        assertEquals("Updated Description", updatedAuctionResponse.getDescription());
        assertEquals(888.99, updatedAuctionResponse.getStartingPrice());
        assertEquals(LocalDateTime.of(2025, 11, 12, 13, 15), updatedAuctionResponse.getExpirationTime());
        assertEquals("CLOSED", updatedAuctionResponse.getStatus());
    }

    @Test
    void testGetAllAuctions_shouldReturnPagedResults() {
        // Given
        Auction auction1 = new Auction();
        auction1.setTitle("Title 1");
        auction1.setDescription("Description 1");
        auction1.setStartingPrice(100.00);
        auction1.setExpirationTime(LocalDateTime.now().plusDays(3));
        auction1.setStatus("OPEN");
        auctionRepository.save(auction1);

        Auction auction2 = new Auction();
        auction2.setTitle("Title 2");
        auction2.setDescription("Description 2");
        auction2.setStartingPrice(200.00);
        auction2.setExpirationTime(LocalDateTime.now().plusDays(5));
        auction2.setStatus("OPEN");
        auctionRepository.save(auction2);

        Auction auction3 = new Auction();
        auction3.setTitle("Title 3");
        auction3.setDescription("Description 3");
        auction3.setStartingPrice(300.00);
        auction3.setExpirationTime(LocalDateTime.now().plusDays(7));
        auction3.setStatus("OPEN");
        auctionRepository.save(auction3);

        // When
        Page<AuctionStatusResponse> auctionPage = auctionService.getAllAuctions(0, 2);

        // Then
        assertNotNull(auctionPage);
        assertEquals(2, auctionPage.getContent().size());
        assertEquals(3, auctionPage.getTotalElements());
    }

}
