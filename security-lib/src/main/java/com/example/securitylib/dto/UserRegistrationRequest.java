package com.example.securitylib.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserRegistrationRequest {
    private String login;
    private String password;
}
