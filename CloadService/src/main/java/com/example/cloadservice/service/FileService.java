package com.example.cloadservice.service;

import com.example.cloadservice.dto.FileDownloadResponse;
import com.example.cloadservice.dto.FileListResponse;
import com.example.cloadservice.exception.FileNotFoundException;
import com.example.cloadservice.model.FileDocument;
import com.example.cloadservice.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FileService {

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; //10MB
    private final FileRepository fileRepository;

    public void uploadFile(String userName, String fileName, MultipartFile file) throws IOException {
        Optional<FileDocument> existingFile = fileRepository.findByOwnerName(userName).stream()
                .filter(f -> f.getFileName().equals(fileName))
                .findFirst();

        if (existingFile.isPresent()) {
            throw new IllegalArgumentException("File already exists");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size cannot exceed 10MB");
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
        if (limit != null && limit > 0) {
            files.stream()
                    .sorted((f1, f2) -> f2.getUploadDate().compareTo(f1.getUploadDate()))
                    .limit(limit)
                    .toList();
        }
        return files.stream().map(doc -> FileListResponse.builder()
                        .filename(doc.getFileName())
                        .size(doc.getSize())
                        .uploadDate(doc.getUploadDate())
                        .contentType(doc.getContentType())
                        .build())
                .toList();
    }
}

