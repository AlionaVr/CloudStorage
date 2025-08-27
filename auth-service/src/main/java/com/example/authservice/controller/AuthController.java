package com.example.authservice.controller;

import com.example.authservice.entity.Role;
import com.example.authservice.service.AuthService;
import com.example.securitylib.dto.LoginRequest;
import com.example.securitylib.dto.LoginResponse;
import com.example.securitylib.dto.UserRegistrationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cloud")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Attempt login for user '{}'", request.getLogin());

        LoginResponse response = authService.login(request.getLogin(), request.getPassword());

        log.info("Login successful for user '{}'", request.getLogin());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("User is logging out");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody UserRegistrationRequest request) {
        log.info("Attempt registration for login '{}'", request.getLogin());

        authService.register(request.getLogin(), request.getPassword(), Role.USER);

        log.info("User '{}' registered successfully", request.getLogin());
        return ResponseEntity.ok().build();
    }
}
