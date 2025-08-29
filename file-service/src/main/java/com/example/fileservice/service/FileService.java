package com.example.fileservice.service;

import com.example.fileservice.dto.FileDownloadResponse;
import com.example.fileservice.dto.FileListResponse;
import com.example.fileservice.exception.FileNotFoundException;
import com.example.fileservice.model.FileDocument;
import com.example.fileservice.repository.FileRepository;
import com.example.securitylib.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileService {

    private final JwtService jwtService;

    private final FileRepository fileRepository;
    @Value("${spring.servlet.multipart.max-file-size}")
    private long MaxFileSize;

    public void uploadFile(String token, String fileName, MultipartFile file) throws IOException {
        String username = extractUsernameFromToken(token);
        log.info("User '{}' is uploading file '{}'", username, fileName);

        validateFileUpload(file, username, fileName);

        FileDocument doc = buildFileDocument(fileName, file, username);

        fileRepository.save(doc);
        log.info("File '{}' uploaded successfully", fileName);
    }

    public void deleteFile(String token, String fileName) {
        String username = extractUsernameFromToken(token);
        log.info("User '{}' is deleting file '{}'", username, fileName);

        FileDocument fileDoc = findFileOrThrow(username, fileName, "Delete");
        fileRepository.delete(fileDoc);

        log.info("File '{}' deleted successfully by user '{}'", fileName, username);
    }

    public FileDownloadResponse downloadFile(String token, String fileName) {
        String username = extractUsernameFromToken(token);
        log.info("User '{}' is downloading file '{}'", username, fileName);

        FileDocument fileDoc = findFileOrThrow(username, fileName, "Download");

        log.info("File '{}' successfully prepared for download by '{}'", fileName, username);
        return buildDownloadResponse(fileDoc);
    }

    public void renameFile(String token, String oldName, String newName) {
        String username = extractUsernameFromToken(token);
        log.info("User '{}' is renaming file '{}' to '{}'", username, oldName, newName);

        FileDocument fileDoc = findFileOrThrow(username, oldName, "Rename");

        if (fileRepository.findByOwnerNameAndFileName(username, newName).isPresent()) {
            log.warn("Rename failed: file '{}' already exists for user '{}'", newName, username);
            throw new IllegalArgumentException("File with name '" + newName + "' already exists");
        }

        fileDoc.setFileName(newName);
        fileRepository.save(fileDoc);
        log.info("File '{}' renamed to '{}' by user '{}'", oldName, newName, username);
    }

    public List<FileListResponse> getAllFiles(String token, Integer limit) {
        String username = extractUsernameFromToken(token);
        log.info("User '{}' is requesting all files", username);

        List<FileDocument> files = fileRepository.findByOwnerName(username);

        if (files == null || files.isEmpty()) {
            log.info("User '{}' has no files", username);
            return Collections.emptyList();
        }

        List<FileListResponse> result = files.stream()
                .sorted((f1, f2) -> f2.getUploadDate().compareTo(f1.getUploadDate()))
                .limit(limit != null && limit > 0 ? limit : files.size())
                .map(doc -> FileListResponse.builder()
                        .filename(doc.getFileName())
                        .size(doc.getSize())
                        .uploadDate(doc.getUploadDate())
                        .contentType(doc.getContentType())
                        .build())
                .toList();

        log.info("User '{}' has {} files", username, result.size());
        return result;
    }

    private String extractUsernameFromToken(String token) {
        return jwtService.getUsername(token);
    }

    private void validateFileUpload(MultipartFile file, String username, String fileName) {

        if (fileRepository.findByOwnerNameAndFileName(username, fileName).isPresent()) {
            log.warn("Upload failed: file '{}' already exists for user '{}'", fileName, username);
            throw new IllegalArgumentException("File already exists");
        }

        if (file.getSize() > MaxFileSize) {
            log.warn("Upload failed: file '{}' is too large for user '{}'", fileName, username);
            throw new IllegalArgumentException("File too large");
        }
    }

    private FileDocument buildFileDocument(String fileName, MultipartFile file, String username) throws IOException {
        return FileDocument.builder()
                .fileName(fileName)
                .contentType(file.getContentType())
                .ownerName(username)
                .size(file.getSize())
                .uploadDate(LocalDateTime.now())
                .fileData(file.getBytes())
                .build();
    }

    private FileDocument findFileOrThrow(String username, String fileName, String operation) {
        return fileRepository.findByOwnerNameAndFileName(username, fileName)
                .orElseThrow(() -> {
                    log.warn("{} failed: file '{}' not found for user '{}'", operation, fileName, username);
                    return new FileNotFoundException("File not found: " + fileName);
                });
    }

    private FileDownloadResponse buildDownloadResponse(FileDocument fileDoc) {
        Resource resource = new ByteArrayResource(fileDoc.getFileData());
        return FileDownloadResponse.builder()
                .resource(resource)
                .contentType(fileDoc.getContentType())
                .fileName(fileDoc.getFileName())
                .size(fileDoc.getSize())
                .build();
    }
}