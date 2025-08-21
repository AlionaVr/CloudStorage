package com.example.fileservice.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class FileListResponse {
    private String filename;
    private Long size;
    private LocalDateTime uploadDate;
    private String contentType;

}
