package com.auction.service;

import com.auction.api.model.LoginRequest;
import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repository.RoleRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LoginServiceTest extends AbstractServiceTest {

    @Autowired
    private LoginService loginService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @MockitoBean
    private AuthenticationManager authenticationManager;

    private User user;
    private LoginRequest loginRequest;

    private static final String ROLE_USER = "USER";

    @BeforeEach
    void setUp() {
        Role userRole = new Role();
        userRole.setRoleName(ROLE_USER);
        roleRepository.save(userRole);

        user = new User();
        user.setUsername("johndoe");
        user.setPassword("password123");
        user.setEmail("johndoe@example.com");
        user.setFullName("John Doe");
        user.setRoles(Collections.singletonList(userRole));
        user.setLastLogin(LocalDateTime.now().minusDays(1));
        userRepository.save(user);

        loginRequest = new LoginRequest("johndoe", "password123");
    }

    @Test
    void testLogin_shouldLoginSuccessfully() {
        // Mock successful authentication
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("johndoe", "password123");
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(token)).thenReturn(authentication);
        LocalDateTime lastLogin = user.getLastLogin();

        // When
        loginService.login(loginRequest);

        // Then
        User updatedUser = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertNotNull(updatedUser.getLastLogin());
        assertTrue(updatedUser.getLastLogin().isAfter(lastLogin));
        verify(authenticationManager, times(1)).authenticate(token);
    }

    @Test
    void testLogin_shouldThrowExceptionWhenUserNotFound() {
        // Given
        loginRequest = new LoginRequest("nonexistentuser", "password123");

        // When & Then
        assertThrows(RuntimeException.class, () -> loginService.login(loginRequest));
    }

    @Test
    void testLogin_shouldThrowExceptionWhenInvalidPassword() {
        // Given
        loginRequest = new LoginRequest("johndoe", "wrongpassword");

        // Mock invalid authentication
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("johndoe", "wrongpassword");
        when(authenticationManager.authenticate(token)).thenThrow(new BadCredentialsException("Invalid username or password"));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginService.login(loginRequest));
    }

    @Test
    void testLogin_shouldNotUpdateLastLoginIfAuthenticationFails() {
        // Given
        loginRequest = new LoginRequest("johndoe", "wrongpassword");

        // Mock invalid authentication
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("johndoe", "wrongpassword");
        when(authenticationManager.authenticate(token)).thenThrow(new BadCredentialsException("Invalid username or password"));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginService.login(loginRequest));
        User updatedUser = userRepository.findByUsername(user.getUsername()).orElseThrow();
        assertEquals(user.getLastLogin(), updatedUser.getLastLogin());
    }

    @Test
    void testLogin_shouldThrowExceptionWhenAuthenticationFails() {
        // Given:
        loginRequest = new LoginRequest("johndoe", "wrongpassword");

        // Mock authentication failure
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken("johndoe", "wrongpassword");
        when(authenticationManager.authenticate(token)).thenThrow(new BadCredentialsException("Authentication failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> loginService.login(loginRequest));
    }
}


