package com.example.securitylib;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
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
                byte[] bytes = Base64.getDecoder().decode(secret);
                cachedKey = Keys.hmacShaKeyFor(bytes);
            } catch (IllegalArgumentException e) {
                throw new IllegalStateException("Invalid JWT secret key format. Must be base64 encoded.", e);
            }
        }
        return cachedKey;
    }

    public String generateAccessToken(String username, List<String> roles) {

        Instant now = Instant.now();
        Instant exp = now.plusSeconds(ttlMinutes * SECONDS_IN_MINUTE);

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
        return Jwts.parserBuilder()
                .setSigningKey(key())
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token);
    }

    public String getUsername(String token) {
        return parse(token).getBody().getSubject();
    }
}