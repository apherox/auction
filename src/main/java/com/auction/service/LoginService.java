package com.auction.service;

import com.auction.api.model.LoginRequest;
import com.auction.exception.InvalidCredentialsException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.model.User;
import com.auction.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;

    public void login(LoginRequest loginRequest) {
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.username(), loginRequest.password());
        try {
            Authentication authentication = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            updateLastLogin(loginRequest.username());
            log.info("User {} logged in", loginRequest.username());
        } catch (BadCredentialsException e) {
            log.warn("Invalid credentials for user: {}", loginRequest.username());
            throw new InvalidCredentialsException(
                    String.format("Invalid credentials for user %s", loginRequest.username()));
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.username(), e);
            throw new com.auction.exception.AuthenticationException(
                    String.format("Authentication failed for user %s", loginRequest.username()));
        }
    }

    private void updateLastLogin(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(("User with username '" + username + "' not found")));
        user.setLastLogin(LocalDateTime.now());
    }
}
