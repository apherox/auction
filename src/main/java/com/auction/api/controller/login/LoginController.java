package com.auction.api.controller.login;

import com.auction.api.model.LoginRequest;
import com.auction.service.LoginService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/api")
public class LoginController implements LoginApi {

    private final LoginService loginService;

    public ResponseEntity<Void> login(LoginRequest loginRequest) {
        loginService.login(loginRequest);
        return ResponseEntity.ok().build();
    }

}
