package com.auction.service;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.exception.ResourceNotFoundException;
import com.auction.exception.UserConflictException;
import com.auction.exception.UserCreationException;
import com.auction.mapper.UserMapper;
import com.auction.model.User;
import com.auction.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SequenceService sequenceService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, SequenceService sequenceService) {
        this.userRepository = userRepository;
        this.userMapper = UserMapper.INSTANCE;
        this.passwordEncoder = passwordEncoder;
        this.sequenceService = sequenceService;
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public UserResponse createUser(UserRequest userRequest) {
        validateUserData(userRequest);

        Optional.ofNullable(userRequest.getPassword())
                .map(passwordEncoder::encode)
                .ifPresent(userRequest::setPassword);

        return Optional.of(userRequest)
                .map(userMapper::toUserEntity)
                .map(userRepository::save)
                .map(savedUser -> {
                    log.info("Created user with username: {}", userRequest.getUsername());
                    return userMapper.toUserApiModel(savedUser);
                })
                .orElseThrow(() -> new UserCreationException("Error creating user"));
    }

    public UserResponse getUserById(Long userId) {
        log.info("Get {} with id: {}", UserResponse.class.getSimpleName(), userId);
        return userRepository.findById(userId)
                .map(userMapper::toUserApiModel)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("User with id %d not found", userId)));
    }

    private void validateUserData(UserRequest userRequest) {
        if (userRepository.existsByUsername(userRequest.getUsername())) {
            throw new UserConflictException(
                    String.format("User with the username '%s' already exists.", userRequest.getUsername()));
        }

        if (userRepository.existsByEmail(userRequest.getEmail())) {
            throw new UserConflictException(
                    String.format("User with the email '%s' already exists.", userRequest.getEmail()));
        }
    }

    @PostConstruct
    public void resetSequences() {
        sequenceService.resetUserSequence();
    }
}
