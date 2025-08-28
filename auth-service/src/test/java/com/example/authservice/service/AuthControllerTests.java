package com.example.authservice.service;

import com.example.authservice.controller.AuthController;
import com.example.authservice.entity.Role;
import com.example.authservice.exception.GlobalExceptionHandler;
import com.example.securitylib.dto.LoginRequest;
import com.example.securitylib.dto.LoginResponse;
import com.example.securitylib.dto.UserRegistrationRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTests {

    private final String TEST_TOKEN = "jwt.test.token";
    @Mock
    private AuthService authService;
    @InjectMocks
    private AuthController authController;
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private LoginRequest loginRequest;
    private UserRegistrationRequest registrationRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();

        loginRequest = new LoginRequest();
        loginRequest.setLogin("testuser");
        loginRequest.setPassword("password123");

        registrationRequest = new UserRegistrationRequest();
        registrationRequest.setLogin("newuser");
        registrationRequest.setPassword("password123");
    }

    @Test
    void whenValidLoginRequestReceived_thenReturnJwtTokenResponse() throws Exception {
        // Given
        LoginResponse loginResponse = new LoginResponse(TEST_TOKEN);
        when(authService.login(loginRequest.getLogin(), loginRequest.getPassword()))
                .thenReturn(loginResponse);

        // When & Then
        mockMvc.perform(post("/cloud/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.auth-token").value(TEST_TOKEN));

        verify(authService).login(loginRequest.getLogin(), loginRequest.getPassword());
    }

    @Test
    void whenRegistrationRequestReceived_thenCreateUserSuccessfully() throws Exception {
        // Given
        doNothing().when(authService).register(anyString(), anyString(), eq(Role.USER));

        // When & Then
        mockMvc.perform(post("/cloud/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isOk());

        verify(authService).register(
                registrationRequest.getLogin(),
                registrationRequest.getPassword(),
                Role.USER
        );
    }

    @Test
    void whenLoginInvalidCredentials_thenReturnBadRequest() throws Exception {
        // Given
        when(authService.login(anyString(), anyString()))
                .thenThrow(new IllegalArgumentException("Bad credentials"));
        // When & Then
        mockMvc.perform(post("/cloud/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("Bad credentials"));
        verify(authService).login(loginRequest.getLogin(), loginRequest.getPassword());
    }

    @Test
    void whenUserNotFoundDuringLogin_thenReturnBadRequest() throws Exception {
        // Given
        when(authService.login(anyString(), anyString()))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When & Then
        mockMvc.perform(post("/cloud/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("400"))
                .andExpect(jsonPath("$.message").value("User not found"));

        verify(authService).login(loginRequest.getLogin(), loginRequest.getPassword());
    }

    @Test
    void whenServerErrorOccurs_thenReturnInternalServerError() throws Exception {
        // Given
        when(authService.login(anyString(), anyString()))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When & Then
        mockMvc.perform(post("/cloud/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("500"))
                .andExpect(jsonPath("$.message").value("Database connection failed"));

        verify(authService).login(loginRequest.getLogin(), loginRequest.getPassword());
    }

    @Test
    void whenLogout_thenReturnSuccess() throws Exception {
        mockMvc.perform(post("/cloud/logout"))
                .andExpect(status().isOk());
    }
}
