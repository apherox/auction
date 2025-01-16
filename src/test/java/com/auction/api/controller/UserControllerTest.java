package com.auction.api.controller;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UserRequest createValidUserRequest() {
        UserRequest userRequest = new UserRequest();
        userRequest.setUsername("john_doe");
        userRequest.setPassword("password123");
        userRequest.setEmail("john.doe@auction.com");
        userRequest.setFullName("John Doe");
        userRequest.setRoles("user");
        return userRequest;
    }

    @Test
    void testCreateUserShouldReturnCreated() throws Exception {
        // Arrange
        UserRequest userRequest = createValidUserRequest();

        // Act
        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest))
                )
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andDo(result -> {
                    String responseContent = result.getResponse().getContentAsString();
                    UserResponse createdUser = objectMapper.readValue(responseContent, UserResponse.class);
                    assertNotNull(createdUser.getUserId());
                    assertEquals(userRequest.getUsername(), createdUser.getUsername());
                });
    }

    @Test
    void testCreateUserShouldReturnBadRequestForMissingUsername() throws Exception {
        // Arrange
        UserRequest invalidUserRequest = new UserRequest();
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("password123");
        userRequest.setEmail("john.doe@auction.com");
        userRequest.setFullName("John Doe");
        userRequest.setRoles("user");

        // Act
        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserShouldReturnBadRequestForMissingEmail() throws Exception {
        // Arrange
        UserRequest invalidUserRequest = new UserRequest();
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("password123");
        userRequest.setUsername("john_doe");
        userRequest.setFullName("John Doe");
        userRequest.setRoles("user");

        // Act
        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateUserShouldReturnBadRequestForInvalidEmail() throws Exception {
        // Arrange
        UserRequest invalidUserRequest = new UserRequest();
        UserRequest userRequest = new UserRequest();
        userRequest.setPassword("password123");
        userRequest.setEmail("john_doe_auction.com");
        userRequest.setUsername("john_doe");
        userRequest.setFullName("John Doe");
        userRequest.setRoles("user");

        // Act
        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidUserRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetUserByIdShouldReturnUser() throws Exception {
        // Arrange
        UserRequest validUserRequest = createValidUserRequest();

        String createdUser = mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest))
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserResponse savedUser = objectMapper.readValue(createdUser, UserResponse.class);

        // Act
        mockMvc.perform(get("/v1/api/users/{id}", savedUser.getUserId())
                        .with(user(validUserRequest.getUsername()).password(validUserRequest.getPassword()).roles("USER"))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(savedUser.getUserId()))
                .andExpect(jsonPath("$.username").value(validUserRequest.getUsername()));
    }

    @Test
    void testGetUserByIdShouldReturnNotFound() throws Exception {
        // Arrange
        Long nonExistentUserId = 999L;
        UserRequest validUserRequest = createValidUserRequest();
        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest))
                )
                .andExpect(status().isCreated());

        // Act
        mockMvc.perform(get("/v1/api/users/{id}", nonExistentUserId)
                        .with(user(validUserRequest.getUsername()).password(validUserRequest.getPassword()).roles("USER"))
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with id " + nonExistentUserId + " not found"));
    }

    @Test
    void testCreateUserShouldReturnBadRequestForExistingUsername() throws Exception {
        // Arrange
        UserRequest validUserRequest = createValidUserRequest();

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest))
                )
                .andExpect(status().isCreated());

        // Act
        UserRequest userWithDuplicateUsernameRequest = new UserRequest();
        userWithDuplicateUsernameRequest.setUsername("john_doe");
        userWithDuplicateUsernameRequest.setPassword("password123");
        userWithDuplicateUsernameRequest.setEmail("johnny_doe@auction.com");
        userWithDuplicateUsernameRequest.setFullName("John Doe");
        userWithDuplicateUsernameRequest.setRoles("user");

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithDuplicateUsernameRequest))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format("User with the username '%s' already exists.", userWithDuplicateUsernameRequest.getUsername())));
    }

    @Test
    void testCreateUserShouldReturnBadRequestForExistingEmail() throws Exception {
        // Arrange
        UserRequest validUserRequest = createValidUserRequest();

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest))
                )
                .andExpect(status().isCreated());

        // Act
        UserRequest userWithDuplicateEmailRequest = new UserRequest();
        userWithDuplicateEmailRequest.setUsername("jim_carrey");
        userWithDuplicateEmailRequest.setPassword("password123");
        userWithDuplicateEmailRequest.setEmail("john.doe@auction.com");
        userWithDuplicateEmailRequest.setFullName("John Doe");
        userWithDuplicateEmailRequest.setRoles("user");

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithDuplicateEmailRequest))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        String.format("User with the email '%s' already exists.", userWithDuplicateEmailRequest.getEmail())));
    }

    @Test
    void testCreateUserShouldReturnBadRequestForInvalidRole() throws Exception {
        // Arrange
        UserRequest validUserRequest = createValidUserRequest();

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest))
                )
                .andExpect(status().isCreated());

        // Act
        UserRequest userWithDuplicateEmailRequest = new UserRequest();
        userWithDuplicateEmailRequest.setUsername("jim_carrey");
        userWithDuplicateEmailRequest.setPassword("password123");
        userWithDuplicateEmailRequest.setEmail("john.doe@auction.com");
        userWithDuplicateEmailRequest.setFullName("John Doe");
        userWithDuplicateEmailRequest.setRoles("invalid_role");

        mockMvc.perform(post("/v1/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userWithDuplicateEmailRequest))
                )
                .andExpect(status().isBadRequest());
    }
}
