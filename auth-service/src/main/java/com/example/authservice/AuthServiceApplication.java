package com.example.authservice;

import com.example.securitylib.SecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import(SecurityConfig.class)
// Is not needed here for now, but let's leave it until we have entities in shared service 'securitylib'
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
