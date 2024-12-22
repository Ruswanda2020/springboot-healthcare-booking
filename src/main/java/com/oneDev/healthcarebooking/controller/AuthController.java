package com.oneDev.healthcarebooking.controller;

import com.oneDev.healthcarebooking.model.UserInfo;
import com.oneDev.healthcarebooking.model.request.AuthRequest;
import com.oneDev.healthcarebooking.model.request.UserRegisterRequest;
import com.oneDev.healthcarebooking.model.response.AuthResponse;
import com.oneDev.healthcarebooking.model.response.UserResponse;
import com.oneDev.healthcarebooking.service.AuthService;
import com.oneDev.healthcarebooking.service.JwtService;
import com.oneDev.healthcarebooking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final AuthService authService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest authRequest) {
        UserInfo userInfo = authService.authenticate(authRequest);
        String token = jwtService.generateToken(userInfo);
        AuthResponse authResponse = AuthResponse.from(userInfo, token);
        return ResponseEntity.ok(authResponse);

    }


    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRegisterRequest UserRegisterRequest) {
        UserResponse userResponse = userService.register(UserRegisterRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }


}
