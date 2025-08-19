package com.example.cloudservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;


@Document(collection = "files")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDocument {
    @Id
    private String id;
    @Indexed
    private String ownerName;
    @Indexed
    private LocalDateTime uploadDate;

    private String fileName;
    private String contentType;
    private long size;

    private byte[] fileData;
}
