package com.auction.service;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.exception.ResourceNotFoundException;
import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repository.RoleRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceTest extends AbstractServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private UserRequest userRequest;

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setRoleName("USER");
        roleRepository.save(userRole);

        userRequest = new UserRequest();
        userRequest.setUsername("john_doe");
        userRequest.setPassword("password123");
        userRequest.setEmail("john.doe@auction.com");
        userRequest.setFullName("John Doe");
        userRequest.setRoles(Collections.singletonList("USER"));
    }

    @Test
    void testCreateUser_shouldCreateUser() {
        // Given
        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        userRequest.setRoles(Collections.singletonList(userRole.getRoleName()));

        // When
        UserResponse createdUserResponse = userService.createUser(userRequest);

        // Then
        assertNotNull(createdUserResponse);
        assertEquals(userRequest.getUsername(), createdUserResponse.getUsername());
        assertEquals(userRequest.getEmail(), createdUserResponse.getEmail());
        assertEquals(userRequest.getFullName(), createdUserResponse.getFullName());
        assertEquals(userRequest.getRoles(), createdUserResponse.getRoles());
    }

    @Test
    void testCreateUser_shouldThrowExceptionWhenUsernameExists() {
        // Given
        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        User user = new User();
        user.setUsername(userRequest.getUsername());
        user.setPassword("existingPassword");
        user.setEmail("existing@auction.com");
        user.setFullName("Existing User");
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        // When & Then
        assertThrows(Exception.class, () -> {
            userService.createUser(userRequest);
        });
    }

    @Test
    void testCreateUser_shouldThrowExceptionWhenEmailExists() {
        // Given
        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        User user = new User();
        user.setUsername("new_username");
        user.setPassword("password123");
        user.setEmail(userRequest.getEmail());
        user.setFullName("Existing User");
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        // When & Then
        assertThrows(RuntimeException.class, () -> userService.createUser(userRequest));
    }

    @Test
    void testGetUserById_shouldReturnUser() {
        // Given
        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail("john.doe@auction.com");
        user.setFullName("John Doe");
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        // When
        UserResponse retrievedUserResponse = userService.getUserById(user.getUserId());

        // Then
        assertNotNull(retrievedUserResponse);
        assertEquals(user.getUserId(), retrievedUserResponse.getUserId());
        assertEquals(user.getUsername(), retrievedUserResponse.getUsername());
        assertEquals(user.getEmail(), retrievedUserResponse.getEmail());
    }

    @Test
    void testGetUserById_shouldThrowExceptionWhenUserNotFound() {
        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(999L);  // ID that doesn't exist
        });
    }

    @Test
    void testFindUserByUsername_shouldReturnUser() {
        // Given
        Role userRole = roleRepository.findByRoleName("USER").orElseThrow();
        User user = new User();
        user.setUsername("john_doe");
        user.setPassword("password123");
        user.setEmail("john.doe@auction.com");
        user.setFullName("John Doe");
        user.setRoles(Collections.singletonList(userRole));
        userRepository.save(user);

        // When
        Optional<User> retrievedUser = userService.findUserByUsername("john_doe");

        // Then
        assertTrue(retrievedUser.isPresent());
        assertEquals(user.getUsername(), retrievedUser.get().getUsername());
    }

    @Test
    void testFindUserByUsername_shouldReturnEmptyWhenUserNotFound() {
        // When
        Optional<User> retrievedUser = userService.findUserByUsername("non_existing_user");

        // Then
        assertFalse(retrievedUser.isPresent());
    }
}

