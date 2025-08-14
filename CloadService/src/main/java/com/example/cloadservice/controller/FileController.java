package com.example.cloadservice.controller;

import com.example.cloadservice.dto.ErrorResponse;
import com.example.cloadservice.exception.FileNotFoundException;
import com.example.cloadservice.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cloud")
public class FileController {

    private final FileService fileService;

    @PostMapping("/file")
    public ResponseEntity<?> uploadFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("filename") String filename,
                                        @RequestPart("file") MultipartFile file) {
        try {
            String username = extractUsernameFromToken(token);
            fileService.uploadFile(username, filename, file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Upload failed", 500));
        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), 400));
        }
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") String token,
            @RequestParam("fileName") String filename) {
        try {
            String username = extractUsernameFromToken(token);
            fileService.deleteFile(username, filename);
            return ResponseEntity.ok("File deleted successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Delete failed", 500));
        }
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestHeader("auth-token") String token,
                                          @RequestParam("filename") String filename) {
        try {
            String username = extractUsernameFromToken(token);
            fileService.downloadFile(username, filename);
            return ResponseEntity.ok("File downloaded successfully");
        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), 400));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Download failed");
        }
    }

    @PutMapping("/file")
    public ResponseEntity<?> updateFile(@RequestHeader("auth-token") String token,
                                        @RequestParam("filename") String oldName,
                                        @RequestBody Map<String, String> body) {
        try {
            String username = extractUsernameFromToken(token);
            String newName = body.get("name");
            fileService.renameFile(username, oldName, newName);
            return ResponseEntity.ok("File renamed successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Rename failed");
        } catch (FileNotFoundException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage(), 400));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getFileList(@RequestHeader("auth-token") String token,
                                         @RequestParam("limit") int limit) {
        try {
            String username = extractUsernameFromToken(token);
            fileService.getAllFiles(username, limit);
            return ResponseEntity.ok("Success get list");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error getting file list");
        }
    }

    //TODO
    private String extractUsernameFromToken(String token) {
        return "user"; // mock logic
    }
}