package com.example.fileservice.service;

import com.example.fileservice.dto.FileDownloadResponse;
import com.example.fileservice.dto.FileListResponse;
import com.example.fileservice.exception.FileNotFoundException;
import com.example.fileservice.model.FileDocument;
import com.example.fileservice.repository.FileRepository;
import com.example.securitylib.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    private final JwtService jwtService;

    private final FileRepository fileRepository;
    @Value("${spring.servlet.multipart.max-file-size}")
    private long MaxFileSize; //10MB

    public void uploadFile(String token, String fileName, MultipartFile file) throws IOException {
        String username = extractUsernameFromToken(token);
        Optional<FileDocument> existingFile = fileRepository.findByOwnerNameAndFileName(username, fileName);

        if (existingFile.isPresent()) {
            throw new IllegalArgumentException("File already exists");
        }

        if (file.getSize() > MaxFileSize) {
            throw new IllegalArgumentException("File too large");
        }

        FileDocument doc = FileDocument.builder()
                .fileName(fileName)
                .contentType(file.getContentType())
                .ownerName(username)
                .size(file.getSize())
                .uploadDate(LocalDateTime.now())
                .fileData(file.getBytes())
                .build();

        fileRepository.save(doc);
    }

    public void deleteFile(String token, String fileName) {
        String username = extractUsernameFromToken(token);

        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(username, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));

        fileRepository.delete(fileDoc);
    }

    public FileDownloadResponse downloadFile(String token, String fileName) {
        String username = extractUsernameFromToken(token);

        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(username, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));

        Resource resource = new ByteArrayResource(fileDoc.getFileData());
        return FileDownloadResponse.builder()
                .resource(resource)
                .contentType(fileDoc.getContentType())
                .fileName(fileDoc.getFileName())
                .size(fileDoc.getSize())
                .build();
    }

    public void renameFile(String token, String oldName, String newName) {
        String username = extractUsernameFromToken(token);
        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(username, oldName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + oldName));

        if (fileRepository.findByOwnerNameAndFileName(username, newName).isPresent()) {
            throw new IllegalArgumentException("File with name '" + newName + "' already exists");
        }

        fileDoc.setFileName(newName);
        fileRepository.save(fileDoc);
    }

    public List<FileListResponse> getAllFiles(String token, Integer limit) {
        String username = extractUsernameFromToken(token);

        List<FileDocument> files = fileRepository.findByOwnerName(username);

        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        return files.stream()
                .sorted((f1, f2) -> f2.getUploadDate().compareTo(f1.getUploadDate()))
                .limit(limit != null && limit > 0 ? limit : files.size())
                .map(doc -> FileListResponse.builder()
                        .filename(doc.getFileName())
                        .size(doc.getSize())
                        .uploadDate(doc.getUploadDate())
                        .contentType(doc.getContentType())
                        .build())
                .toList();
    }

    private String extractUsernameFromToken(String token) {
        return jwtService.getUsername(token);
    }
}

