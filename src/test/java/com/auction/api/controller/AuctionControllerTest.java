package com.auction.api.controller;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.auction.AuctionUpdateRequest;
import com.auction.exception.ResourceNotFoundException;
import com.auction.model.Auction;
import com.auction.repository.AuctionRepository;
import com.auction.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuctionControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private AuctionService auctionService;

    private static final String USERNAME = "test_user";
    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_USER = "user";


    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testCreateAuctionShouldReturnCreated() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase dating back to the Ming Dynasty.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Antique Vase"))
                .andExpect(jsonPath("$.startingPrice").value(150.00));
    }

    @Test
    @WithMockUser(username = USERNAME)
    void testCreateAuctionShouldReturn403ForMissingRole() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase dating back to the Ming Dynasty.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_USER)
    void testCreateAuctionShouldReturn403ForNonPrivilegedRole() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase dating back to the Ming Dynasty.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testCreateAuctionShouldReturnBadRequestForInvalidData() throws Exception {
        // Arrange
        AuctionRequest invalidAuctionRequest = new AuctionRequest();
        invalidAuctionRequest.setTitle(null);
        invalidAuctionRequest.setDescription("An ancient vase.");
        invalidAuctionRequest.setStartingPrice(100.00);
        invalidAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidAuctionRequest))
                )
                .andExpect(status().isBadRequest());

    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testGetAuctionByIdShouldReturnAuction() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        String createdAuction = mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuctionResponse savedAuction = objectMapper.readValue(createdAuction, AuctionResponse.class);

        // Act
        mockMvc.perform(get("/v1/api/auctions/{id}", savedAuction.getAuctionId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.auctionId").value(savedAuction.getAuctionId()))
                .andExpect(jsonPath("$.title").value("Antique Vase"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testGetAuctionByIdShouldReturnNotFound() throws Exception {
        // Arrange
        Long nonExistentAuctionId = 999L;

        // Act
        mockMvc.perform(get("/v1/api/auctions/{id}", nonExistentAuctionId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Auction with id " + nonExistentAuctionId + " not found"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testUpdateAuctionShouldReturnUpdatedAuction() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        String createdAuction = mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuctionResponse savedAuction = objectMapper.readValue(createdAuction, AuctionResponse.class);

        // Create an auction update request
        AuctionUpdateRequest auctionUpdateRequest = new AuctionUpdateRequest();
        auctionUpdateRequest.setTitle("Updated Antique Vase");
        auctionUpdateRequest.setDescription("An updated description for the vase.");
        auctionUpdateRequest.setStartingPrice(200.00);
        auctionUpdateRequest.setExpirationTime(LocalDateTime.now().plusDays(10));

        // Act
        mockMvc.perform(put("/v1/api/auctions/{auctionId}", savedAuction.getAuctionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auctionUpdateRequest))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Antique Vase"))
                .andExpect(jsonPath("$.startingPrice").value(200.00));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testUpdateAuctionShouldReturnForbiddenIfAuctionHasBidders() throws Exception {
        // Arrange
        AuctionRequest validAuctionRequest = new AuctionRequest();
        validAuctionRequest.setTitle("Antique Vase");
        validAuctionRequest.setDescription("An ancient porcelain vase.");
        validAuctionRequest.setStartingPrice(150.00);
        validAuctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        String createdAuction = mockMvc.perform(post("/v1/api/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuctionResponse savedAuction = objectMapper.readValue(createdAuction, AuctionResponse.class);

        // update the auction directly with the repository method since it can't be done via REST
        Auction auction = auctionRepository.findById(savedAuction.getAuctionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Auction with id %d not found", savedAuction.getAuctionId())));
        auction.setHighestBid(150.00);
        auction.setStatus("OPEN");

        mockMvc.perform(put("/v1/api/auctions/{auctionId}", savedAuction.getAuctionId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validAuctionRequest))
                )
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Auction with id " + savedAuction.getAuctionId() + " can't be modified because it contains bidders"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_ADMIN)
    void testUpdateAuctionShouldReturnNotFoundWhenAuctionDoesNotExist() throws Exception {
        // Arrange
        Long nonExistentAuctionId = 999L;
        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle("Antique Vase");
        auctionRequest.setDescription("An ancient porcelain vase.");
        auctionRequest.setStartingPrice(150.00);
        auctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(put("/v1/api/auctions/{auctionId}", nonExistentAuctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auctionRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Auction with id " + nonExistentAuctionId + " not found"));
    }

    @Test
    @WithMockUser(username = USERNAME, roles = ROLE_USER)
    void testUpdateAuctionShouldReturnForbiddenWhenUserIsNotAdmin() throws Exception {
        // Arrange
        Long nonExistentAuctionId = 999L;
        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle("Antique Vase");
        auctionRequest.setDescription("An ancient porcelain vase.");
        auctionRequest.setStartingPrice(150.00);
        auctionRequest.setExpirationTime(LocalDateTime.now().plusDays(7));

        // Act
        mockMvc.perform(put("/v1/api/auctions/{auctionId}", nonExistentAuctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(auctionRequest)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Only users with 'admin' role are allowed to create auctions"));
    }
}