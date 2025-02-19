package com.auction.service;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.exception.InvalidRoleException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.exception.UserConflictException;
import com.auction.exception.UserCreationException;
import com.auction.mapper.UserMapper;
import com.auction.model.Role;
import com.auction.model.User;
import com.auction.repository.RoleRepository;
import com.auction.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SequenceService sequenceService;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, SequenceService sequenceService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
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

        List<String> roleNames = userRequest.getRoles();
        List<Role> roles = roleRepository.findByRoleNameIn(roleNames);
        if (roles.size() != roleNames.size()) {
            throw new InvalidRoleException("One or more specified roles are invalid.");
        }

        User user = userMapper.toUserEntity(userRequest);
        user.setRoles(roles);

        return Optional.of(user)
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


