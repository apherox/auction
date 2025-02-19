package com.auction.api.controller;

import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repository.RoleRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @BeforeEach
    void setUp() {
        // Create role "USER"
        Role userRole = new Role();
        userRole.setRoleName("user");
        roleRepository.save(userRole);

        // Create a user and assign the "USER" role
        User user = new User();
        user.setUsername("testuser");
        user.setPassword(new BCryptPasswordEncoder().encode("password123"));
        user.setEmail("testuser@domain.com");
        user.setFullName("Test User");
        user.setRoles(Collections.singletonList(userRole));
        user.setCreatedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    @Test
    void testLoginShouldReturnOk() throws Exception {
        // Arrange
        String loginRequest = "{\"username\":\"testuser\", \"password\":\"password123\"}";

        // Act
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isOk())
                .andExpect(content().string(""));
    }

    @Test
    void testLoginShouldFailWithWrongPassword() throws Exception {
        // Arrange
        String loginRequest = "{\"username\":\"testuser\", \"password\":\"wrongpassword\"}";

        // Act
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginShouldFailWithNonExistingUser() throws Exception {
        // Arrange:
        String loginRequest = "{\"username\":\"nonexistentuser\", \"password\":\"password123\"}";

        // Act
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginRequest))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLoginShouldReturnUnauthorizedForInvalidCredentials() throws Exception {
        // Arrange
        String invalidLoginRequest = "{\"username\":\"invaliduser\", \"password\":\"invalidpassword\"}";

        // Act
        mockMvc.perform(post("/v1/api/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLoginRequest))
                .andExpect(status().isUnauthorized());
    }
}


