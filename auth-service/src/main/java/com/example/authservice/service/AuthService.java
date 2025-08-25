package com.example.authservice.service;

import com.example.authservice.entity.Role;
import com.example.authservice.entity.User;
import com.example.authservice.repository.UserRepository;
import com.example.securitylib.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String login(String login, String rawPassword) {
        log.info("User '{}' is logging in", login);
        User user = userRepository.findByLogin(login)
                .orElseThrow(() -> {
                    log.error("User with login '{}' not found", login);
                    return new UsernameNotFoundException("User not found");
                });

        if (!encoder.matches(rawPassword, user.getPasswordHash())) {
            log.error("Bad credentials for user '{}'", login);
            throw new IllegalArgumentException("Bad credentials");
        }
        return jwt.generateAccessToken(user.getLogin(), List.of(user.getRole().name()));
    }

    public void register(String login, String rawPassword, Role role) {
        log.info("User '{}' is registering", login);
        User user = new User();
        user.setLogin(login);
        user.setPasswordHash(encoder.encode(rawPassword));
        user.setRole(role);
        user.setCreatedAt(LocalDateTime.now());

        userRepository.save(user);
        log.info("User '{}' registered successfully", login);
    }
}
