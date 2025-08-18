package com.example.cloudservice.service;

import com.example.cloudservice.dto.FileDownloadResponse;
import com.example.cloudservice.dto.FileListResponse;
import com.example.cloudservice.exception.FileNotFoundException;
import com.example.cloudservice.model.FileDocument;
import com.example.cloudservice.repository.FileRepository;
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

    @Value("${app.upload.max-size-bytes}")
    private long MAX_FILE_SIZE; //10MB

    private final FileRepository fileRepository;

    public void uploadFile(String userName, String fileName, MultipartFile file) throws IOException {
        Optional<FileDocument> existingFile = fileRepository.findByOwnerName(userName).stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst();

        if (existingFile.isPresent()) {
            throw new IllegalArgumentException("File already exists");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File too large");
        }

        FileDocument doc = FileDocument.builder()
                .fileName(fileName)
                .contentType(file.getContentType())
                .size(file.getSize())
                .ownerName(userName)
                .uploadDate(LocalDateTime.now())
                .fileData(file.getBytes())
                .build();

        fileRepository.save(doc);
    }

    public void deleteFile(String userName, String fileName) throws IOException {
        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(userName, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));

        fileRepository.delete(fileDoc);
    }

    public FileDownloadResponse downloadFile(String userName, String fileName) throws IOException {
        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(userName, fileName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + fileName));

        Resource resource = new ByteArrayResource(fileDoc.getFileData());
        return FileDownloadResponse.builder()
                .resource(resource)
                .contentType(fileDoc.getContentType())
                .fileName(fileDoc.getFileName())
                .size(fileDoc.getSize())
                .build();
    }

    public void renameFile(String userName, String oldName, String newName) throws IOException {
        FileDocument fileDoc = fileRepository.findByOwnerNameAndFileName(userName, oldName)
                .orElseThrow(() -> new FileNotFoundException("File not found: " + oldName));

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("New file name cannot be empty");
        }

        if (fileRepository.findByOwnerNameAndFileName(userName, newName).isPresent()) {
            throw new IllegalArgumentException("File with name '" + newName + "' already exists");
        }

        fileDoc.setFileName(newName);
        fileRepository.save(fileDoc);
    }

    public List<FileListResponse> getAllFiles(String userName, Integer limit) throws IOException {
        List<FileDocument> files = fileRepository.findByOwnerName(userName);

        if (files == null || files.isEmpty()) {
            return Collections.emptyList();
        }

        List<FileDocument> sortedFiles = List.of();
        if (limit != null && limit > 0) {
            sortedFiles = files.stream()
                    .sorted((f1, f2) -> f2.getUploadDate().compareTo(f1.getUploadDate()))
                    .limit(limit)
                    .toList();
        }
        return sortedFiles.stream()
                .map(doc -> FileListResponse.builder()
                        .filename(doc.getFileName())
                        .size(doc.getSize())
                        .uploadDate(doc.getUploadDate())
                        .contentType(doc.getContentType())
                        .build())
                .toList();
    }


}

