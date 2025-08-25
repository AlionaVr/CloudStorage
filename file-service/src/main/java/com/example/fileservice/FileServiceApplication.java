package com.example.fileservice;

import com.example.securitylib.SecurityConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@Slf4j
@SpringBootApplication
@EnableFeignClients
@Import(SecurityConfig.class)
//@EntityScan(basePackages = {
//        "com.example.fileservice", // file-service
//        "com.example.securitylib" // security-lib
//})
@ComponentScan(basePackages = {
        "com.example.fileservice", // file-service
        "com.example.securitylib" // security-lib
})
public class FileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileServiceApplication.class, args);
    }

}
