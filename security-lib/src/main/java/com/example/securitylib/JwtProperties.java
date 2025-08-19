package com.example.securitylib;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;


@ConfigurationProperties(prefix = "security.jwt")
@Data
@RequiredArgsConstructor
public class JwtProperties {
    private String secret;
    private String issuer = "cloud-storage-diploma";
    private long accessTtlMinutes = 30;

    private String header = "auth-token";
}
