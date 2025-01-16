package com.auction.api.controller.user;

import com.auction.api.model.user.UserRequest;
import com.auction.api.model.user.UserResponse;
import com.auction.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/v1/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;


    public ResponseEntity<UserResponse> createUser(UserRequest userRequest) {
        UserResponse createdUser = userService.createUser(userRequest);
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(createdUser.getUserId())
                .toUri();
        return ResponseEntity.created(location).body(createdUser);
    }

    public ResponseEntity<UserResponse> getUserById(Long id) {
        UserResponse user = userService.getUserById(id);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
