package com.example.fileservice.controller;

import com.example.fileservice.client.AuthServiceClient;
import com.example.fileservice.dto.FileDownloadResponse;
import com.example.fileservice.dto.FileListResponse;
import com.example.fileservice.dto.request.RenameFileRequest;
import com.example.fileservice.service.FileService;
import com.example.securitylib.dto.LoginRequest;
import com.example.securitylib.dto.LoginResponse;
import com.example.securitylib.dto.UserRegistrationRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/cloud")
@Slf4j
public class FileController {

    private final FileService fileService;
    private final AuthServiceClient authServiceClient;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        log.info("Proxying login request for user '{}'", request.getLogin());
        return authServiceClient.login(request);
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody UserRegistrationRequest request) {
        log.info("Proxying registration request for user '{}'", request.getLogin());
        return authServiceClient.register(request);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        log.info("Proxying logout request");
        return authServiceClient.logout();
    }

    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                        @RequestParam("filename") @NotBlank(message = "filename is required") String filename,
                                        @RequestPart("file") MultipartFile file) throws IOException {
        log.info("Upload request: filename='{}', size={} bytes", filename, file.getSize());
        fileService.uploadFile(token, filename, file);
        log.info("File '{}' uploaded successfully", filename);
        return ResponseEntity.ok(Map.of("message", "File uploaded successfully"));
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
            @RequestParam("filename") @NotBlank(message = "filename is required") String filename) {
        log.info("Delete request: filename='{}'", filename);
        fileService.deleteFile(token, filename);
        log.info("File '{}' deleted successfully", filename);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                          @RequestParam("filename") @NotBlank(message = "filename is required") String filename) {
        log.info("Download request: filename='{}'", filename);
        FileDownloadResponse resp = fileService.downloadFile(token, filename);
        log.info("File '{}' successfully downloaded", filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resp.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resp.getFileName() + "\"")
                .contentLength(resp.getSize())
                .body(resp.getResource());
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                        @RequestParam("filename") @NotBlank(message = "filename is required") String oldName,
                                        @RequestBody @Valid RenameFileRequest request) {
        String newName = request.getFilename();
        log.info("Rename request: filename='{}' to '{}'", oldName, newName);
        fileService.renameFile(token, oldName, newName);
        log.info("File '{}' renamed to '{}'", oldName, newName);
        return ResponseEntity.ok(Map.of("message", "File renamed successfully"));

    }

    @GetMapping("/list")
    public ResponseEntity<?> getFileList(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                         @RequestParam("limit") @Min(value = 1, message = "limit must be >= 1") int limit) {
        log.info("List request: limit={}", limit);
        List<FileListResponse> list = fileService.getAllFiles(token, limit);
        log.info("Returning {} files", list.size());
        return ResponseEntity.ok(list);
    }
}