package com.example.authservice.service;

import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.securitylib.JwtService;
import com.example.securitylib.dto.LoginResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTests {

    private final String TEST_LOGIN = "testuser";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_TOKEN = "jwt.test.token";
    @Mock
    private UserRepository userRepository;
    @Mock
    private JwtService jwtService;
    @InjectMocks
    private AuthService authService;
    private BCryptPasswordEncoder encoder;
    private User testUser;

    @BeforeEach
    void setUp() {
        encoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, jwtService, encoder);

        testUser = new User();
        testUser.setId(1L);
        testUser.setLogin(TEST_LOGIN);
        testUser.setPasswordHash(encoder.encode(TEST_PASSWORD));
        testUser.setRole(Role.USER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void whenLoginSuccess_thenReturnJwtToken() {
        // Given
        when(userRepository.findByLogin(TEST_LOGIN)).thenReturn(Optional.of(testUser));
        when(jwtService.generateAccessToken(eq(TEST_LOGIN), eq(List.of("USER")))).thenReturn(TEST_TOKEN);

        // When
        LoginResponse response = authService.login(TEST_LOGIN, TEST_PASSWORD);

        // Then
        assertNotNull(response);
        assertEquals(TEST_TOKEN, response.getAuthToken());
        verify(userRepository).findByLogin(TEST_LOGIN);
        verify(jwtService).generateAccessToken(TEST_LOGIN, List.of("USER"));
    }

    @Test
    void whenUserDoesNotExist_thenThrowUsernameNotFoundException() {
        // Given
        when(userRepository.findByLogin(TEST_LOGIN)).thenReturn(Optional.empty());

        // When & Then
        UsernameNotFoundException exception = assertThrows(
                UsernameNotFoundException.class,
                () -> authService.login(TEST_LOGIN, TEST_PASSWORD)
        );

        assertEquals("User not found", exception.getMessage());
        verify(userRepository).findByLogin(TEST_LOGIN);
        verifyNoInteractions(jwtService);
    }

    @Test
    void whenPasswordIsIncorrect_thenThrowIllegalArgumentException() {
        // Given
        when(userRepository.findByLogin(TEST_LOGIN)).thenReturn(Optional.of(testUser));

        // When & Then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.login(TEST_LOGIN, "wrongpassword")
        );

        assertEquals("Bad credentials", exception.getMessage());
        verify(userRepository).findByLogin(TEST_LOGIN);
        verifyNoInteractions(jwtService);
    }

    @Test
    void whenValidRegistrationDataProvided_thenSaveUserWithEncodedPassword() {
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        authService.register(TEST_LOGIN, TEST_PASSWORD, Role.USER);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());

        User saved = captor.getValue();
        assertThat(saved.getLogin()).isEqualTo(TEST_LOGIN);
        assertThat(encoder.matches(TEST_PASSWORD, saved.getPasswordHash())).isTrue();
        assertThat(saved.getRole()).isEqualTo(Role.USER);
    }
}
