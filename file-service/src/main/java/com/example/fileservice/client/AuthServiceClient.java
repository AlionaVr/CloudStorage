package com.example.fileservice.client;

import com.example.securitylib.dto.LoginRequest;
import com.example.securitylib.dto.LoginResponse;
import com.example.securitylib.dto.UserRegistrationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "auth-service", url = "${auth-service.url:http://localhost:8081}")
public interface AuthServiceClient {

    @PostMapping("/cloud/login")
    ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request);

    @PostMapping("/cloud/register")
    ResponseEntity<Void> register(@RequestBody UserRegistrationRequest request);

    @PostMapping("/cloud/logout")
    ResponseEntity<Void> logout();
}