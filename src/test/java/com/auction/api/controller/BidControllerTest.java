package com.auction.api.controller;

import com.auction.api.model.auction.AuctionRequest;
import com.auction.api.model.auction.AuctionResponse;
import com.auction.api.model.bid.BidResponse;
import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.model.Auction;
import com.auction.model.AuctionStatus;
import com.auction.repository.AuctionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BidControllerTest  extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuctionRepository auctionRepository;

    @MockitoBean
    private Authentication authentication;

    private Long userId;
    private Long auctionId;

    private static final String USERNAME = "john_doe";
    private static final String PASSWORD = "password123";

    @BeforeEach
    public void setup() throws Exception {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername(USERNAME);
        userRequest.setPassword(PASSWORD);
        userRequest.setEmail("john.doe@auction.com");
        userRequest.setFullName("John Doe");
        userRequest.setRoles("admin");

        String userJson = objectMapper.writeValueAsString(userRequest);

        String userResponseString = mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(userRequest.getUsername()))
                .andExpect(jsonPath("$.email").value(userRequest.getEmail()))
                .andExpect(jsonPath("$.fullName").value(userRequest.getFullName()))
                .andExpect(jsonPath("$.roles").value(userRequest.getRoles()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse userResponse = objectMapper.readValue(userResponseString, UserResponse.class);
        userId = userResponse.getUserId();

        AuctionRequest auctionRequest = new AuctionRequest();
        auctionRequest.setTitle("Antique Vase");
        auctionRequest.setDescription("An ancient porcelain vase dating back to the Ming Dynasty.");
        auctionRequest.setStartingPrice(150.00);
        auctionRequest.setExpirationTime(LocalDateTime.now().plusDays(1));

        String auctionJson = objectMapper.writeValueAsString(auctionRequest);

        String auctionResponseString = mockMvc.perform(post("/v1/api/auctions")
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(auctionJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value(auctionRequest.getTitle()))
                .andExpect(jsonPath("$.description").value(auctionRequest.getDescription()))
                .andExpect(jsonPath("$.startingPrice").value(auctionRequest.getStartingPrice()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        AuctionResponse auctionResponse = objectMapper.readValue(auctionResponseString, AuctionResponse.class);
        auctionId = auctionResponse.getAuctionId();

        UserDetails userDetails = Mockito.mock(UserDetails.class);
        Mockito.when(userDetails.getUsername()).thenReturn(userRequest.getUsername());
        authentication = new UsernamePasswordAuthenticationToken(userDetails, userRequest.getPassword());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String createBidRequest(double amount) {
        return String.format("{\"amount\": %.2f}", amount);
    }

    @Test
    void testPlaceBidSuccessfully() throws Exception {
        // Arrange
        String bidRequest = createBidRequest(250.00);

        // Act
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidRequest)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.userId").value(userId));
    }

    @Test
    void testPlaceTwoBidsBySingleUserShouldIncreaseHighestBidInAuction() throws Exception {
        // Arrange: create bid with already created user
        String firstBidRequest = createBidRequest(250.00);
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBidRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String secondBidRequest = createBidRequest(300.00);

        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondBidRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn()
                .getResponse()
                .getContentAsString();


        // Act: Fetch the auction after the bids to verify the highest bid
        mockMvc.perform(get("/v1/api/auctions/{auctionId}", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestBid").value(300.00));
    }

    @Test
    void testPlaceBidByTwoDifferentUsersShouldIncreaseHighestBidInAuction() throws Exception {
        // Arrange: create bid with already created user
        String firstBidRequest = createBidRequest(250.00);
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBidRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(250.00))
                .andExpect(jsonPath("$.userId").value(userId))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // Create second user and place a higher bid
        UserRequest secondUserRequest = new UserRequest();
        secondUserRequest.setUsername("jim_carrey");
        secondUserRequest.setPassword("password123");
        secondUserRequest.setEmail("jim_carrey@auction.com");
        secondUserRequest.setFullName("Jim Carrey");
        secondUserRequest.setRoles("user");

        String userJson = objectMapper.writeValueAsString(secondUserRequest);

        String userResponseString = mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(secondUserRequest.getUsername()))
                .andExpect(jsonPath("$.email").value(secondUserRequest.getEmail()))
                .andExpect(jsonPath("$.fullName").value(secondUserRequest.getFullName()))
                .andExpect(jsonPath("$.roles").value(secondUserRequest.getRoles()))
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse secondUserResponse = objectMapper.readValue(userResponseString, UserResponse.class);
        String secondBidRequest = createBidRequest(300.00);

        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString("jim_carrey:password123".getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondBidRequest))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(300.00))
                .andExpect(jsonPath("$.userId").value(secondUserResponse.getUserId()))
                .andExpect(jsonPath("$.username").value(secondUserResponse.getUsername()))
                .andReturn()
                .getResponse()
                .getContentAsString();


        // Act: Fetch the auction after the bids to verify the highest bid
        mockMvc.perform(get("/v1/api/auctions/{auctionId}", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString("jim_carrey:password123".getBytes()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.highestBid").value(300.00))
                .andExpect(jsonPath("$.highestBidUsername").value(secondUserRequest.getUsername()));
    }

    @Test
    void testPlaceBidWithInvalidAmount() throws Exception {
        // Arrange
        String firstBidRequest = createBidRequest(150.00);
        String response = mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBidRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BidResponse firstBidResponse = objectMapper.readValue(response, BidResponse.class);

        // Arrange second bid request
        String secondBidRequest = createBidRequest(50.00);

        // Act
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondBidRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Bid amount %s must be higher than the current highest bid amount %s", 50.00, firstBidResponse.getAmount())));
    }

    @Test
    void testPlaceBidWithInvalidAmountEqualToTheHighestBidAmount() throws Exception {
        // Arrange
        String firstBidRequest = createBidRequest(150.00);
        String response = mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstBidRequest))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BidResponse firstBidResponse = objectMapper.readValue(response, BidResponse.class);

        // Arrange second bid request
        String secondBidRequest = createBidRequest(150.00);

        // Act
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(secondBidRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Bid amount %s must be higher than the current highest bid amount %s", 150.00, firstBidResponse.getAmount())));

    }

    @Test
    void testPlaceBidOnClosedAuction() throws Exception {
        // Arrange
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(RuntimeException::new);
        auction.setStatus(AuctionStatus.CLOSED.name());
        auctionRepository.save(auction);
        String bidRequest = createBidRequest(150.00);

        // Act
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Bids on auction with id %d can't be made since it is closed", auctionId)));
    }

    @Test
    void testPlaceBidOnAnExpiredAuction() throws Exception {
        // Arrange
        Auction auction = auctionRepository.findById(auctionId).orElseThrow(RuntimeException::new);
        auction.setExpirationTime(LocalDateTime.now().minusSeconds(1));
        auctionRepository.save(auction);
        String bidRequest = createBidRequest(150.00);

        // Act
        mockMvc.perform(post("/v1/api/auctions/{auctionId}/bids", auctionId)
                        .header(HttpHeaders.AUTHORIZATION, "Basic " +
                                java.util.Base64.getEncoder().encodeToString((USERNAME + ":" + PASSWORD).getBytes()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidRequest))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message")
                        .value(String.format("Bids on auction with id %d can't be made since it is expired", auctionId)));
    }
}