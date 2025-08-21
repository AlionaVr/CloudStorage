package com.example.fileservice.dto;


import lombok.Builder;
import lombok.Data;
import org.springframework.core.io.Resource;

@Data
@Builder
public class FileDownloadResponse {
    private Resource resource;
    private String contentType;
    private String fileName;
    private Long size;
}
