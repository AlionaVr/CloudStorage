package com.example.cloudservice.repository;

import com.example.cloudservice.model.FileDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends MongoRepository<FileDocument, Long> {
    List<FileDocument> findByOwnerName(String username);

    Optional<FileDocument> findByOwnerNameAndFileName(String username, String fileName);

}
