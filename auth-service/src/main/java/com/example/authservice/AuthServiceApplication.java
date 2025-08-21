package com.example.authservice;

import com.example.securitylib.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)
//@EntityScan(basePackages = {
//        "com.example.authservice", // auth-service
//        "com.example.securitylib" // security-lib
//})
@ComponentScan(basePackages = {
        "com.example.authservice", // auth-service
        "com.example.securitylib" // security-lib
})
public class AuthServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }
}
