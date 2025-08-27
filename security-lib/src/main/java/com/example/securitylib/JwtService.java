package com.example.securitylib;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.issuer}")
    private String issuer;

    @Value("${security.jwt.access-ttl-minutes}")
    private long ttlMinutes;

    private final static int SECONDS_IN_MINUTE = 60;

    private SecretKey cachedKey;

    private SecretKey key() {
        if (cachedKey == null) {
            try {
                log.debug("Decoding JWT secret key...");
                byte[] bytes = Base64.getDecoder().decode(secret);
                cachedKey = Keys.hmacShaKeyFor(bytes);
                log.debug("JWT secret key successfully decoded.");
            } catch (IllegalArgumentException e) {
                log.error("Invalid JWT secret key format. Must be base64 encoded.", e);
                throw new IllegalStateException("Invalid JWT secret key format. Must be base64 encoded.", e);
            }
        }
        return cachedKey;
    }

    public String generateAccessToken(String username, List<String> roles) {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * SECONDS_IN_MINUTE);

        log.debug("Generating JWT token for user '{}' with roles '{}', expires at '{}'", username, roles, exp);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuer(issuer)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(exp))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        log.debug("Parsing JWT token...");
        try {
            if (token.startsWith("Bearer ")) token = token.substring(7);
            Jws<Claims> claims = Jwts.parserBuilder()
                    .setSigningKey(key())
                    .requireIssuer(issuer)
                    .build()
                    .parseClaimsJws(token);
            log.debug("JWT token parsed successfully for user '{}'", claims.getBody().getSubject());
            return claims;
        } catch (Exception e) {
            log.warn("Failed to parse JWT token: {}", e.getMessage());
            throw e;
        }
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }
}