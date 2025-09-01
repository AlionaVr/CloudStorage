package com.example.fileservice.service;

import com.example.fileservice.model.FileDocument;
import com.example.fileservice.repository.FileRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ComponentScan(basePackages = {
        "com.example.fileservice", // file-service
        "com.example.securitylib" // security-lib
})
class FileServiceIntegrationTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:7");
    @Autowired
    private FileRepository fileRepository;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongo::getReplicaSetUrl);
    }

    @Test
    void whenSaveFile_thenItCanBeQueried() {
        FileDocument file = FileDocument.builder()
                .fileName("integration.txt")
                .ownerName("integrationUser")
                .contentType("text/plain")
                .size(100L)
                .uploadDate(LocalDateTime.now())
                .fileData("hello".getBytes())
                .build();

        fileRepository.save(file);

        List<FileDocument> files = fileRepository.findByOwnerName("integrationUser");

        assertThat(files).hasSize(1);
        assertThat(files.get(0).getFileName()).isEqualTo("integration.txt");
    }
}

