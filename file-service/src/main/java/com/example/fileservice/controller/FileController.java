package com.example.fileservice.controller;

import com.example.fileservice.dto.FileDownloadResponse;
import com.example.fileservice.dto.FileListResponse;
import com.example.fileservice.dto.request.RenameFileRequest;
import com.example.fileservice.service.FileService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
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
public class FileController {

    private final FileService fileService;

    @PostMapping(path = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                        @RequestParam("filename") @NotBlank(message = "filename is required") String filename,
                                        @RequestPart("file") MultipartFile file) throws IOException {
        fileService.uploadFile(token, filename, file);
        return ResponseEntity.ok(Map.of("message", "File uploaded successfully"));
    }

    @DeleteMapping("/file")
    public ResponseEntity<?> deleteFile(
            @RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
            @RequestParam("filename") @NotBlank(message = "filename is required") String filename) {
        fileService.deleteFile(token, filename);
        return ResponseEntity.ok(Map.of("message", "File deleted successfully"));
    }

    @GetMapping("/file")
    public ResponseEntity<?> downloadFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                          @RequestParam("filename") @NotBlank(message = "filename is required") String filename) throws IOException {
        FileDownloadResponse resp = fileService.downloadFile(token, filename);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resp.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resp.getFileName() + "\"")
                .contentLength(resp.getSize())
                .body(resp.getResource());
    }

    @PutMapping("/file")
    public ResponseEntity<?> renameFile(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                        @RequestParam("filename") @NotBlank(message = "filename is required") String oldName,
                                        @RequestBody @Valid RenameFileRequest request) throws IOException {
        String newName = request.getName();
        fileService.renameFile(token, oldName, newName);
        return ResponseEntity.ok(Map.of("message", "File renamed successfully"));

    }

    @GetMapping("/list")
    public ResponseEntity<?> getFileList(@RequestHeader("auth-token") @NotBlank(message = "auth-token is required") String token,
                                         @RequestParam("limit") @Min(value = 1, message = "limit must be >= 1") int limit) throws IOException {
        List<FileListResponse> list = fileService.getAllFiles(token, limit);
        return ResponseEntity.ok(list);
    }
}