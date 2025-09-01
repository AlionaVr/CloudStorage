package com.example.fileservice.service;

import com.example.fileservice.controller.FileController;
import com.example.fileservice.dto.FileDownloadResponse;
import com.example.fileservice.dto.FileListResponse;
import com.example.fileservice.dto.request.RenameFileRequest;
import com.example.fileservice.exception.FileNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileControllerTests {

    private static final String TEST_TOKEN = "test-jwt-token";
    private static final String TEST_FILENAME = "test.txt";
    private static final String NEW_FILENAME = "new.txt";
    @Mock
    private FileService fileService;
    @InjectMocks
    private FileController fileController;

    @Test
    void uploadFile_whenValidRequest_shouldReturnSuccessResponse() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", TEST_FILENAME,
                "text/plain", "Hello World".getBytes());
        doNothing().when(fileService).uploadFile(TEST_TOKEN, TEST_FILENAME, file);

        // when
        ResponseEntity<?> response = fileController.uploadFile(TEST_TOKEN, TEST_FILENAME, file);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isInstanceOf(Map.class);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("File uploaded successfully");

        verify(fileService).uploadFile(TEST_TOKEN, TEST_FILENAME, file);
    }

    @Test
    void uploadFile_whenServiceThrowsException_shouldPropagateException() throws IOException {
        // given
        MockMultipartFile file = new MockMultipartFile("file", TEST_FILENAME,
                "text/plain", "Hello".getBytes());
        doThrow(new IllegalArgumentException("File already exists"))
                .when(fileService).uploadFile(TEST_TOKEN, TEST_FILENAME, file);

        // when & then
        assertThatThrownBy(() -> fileController.uploadFile(TEST_TOKEN, TEST_FILENAME, file))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File already exists");
    }

    @Test
    void deleteFile_whenValidRequest_shouldReturnSuccessResponse() {
        // given
        doNothing().when(fileService).deleteFile(TEST_TOKEN, TEST_FILENAME);

        // when
        ResponseEntity<?> response = fileController.deleteFile(TEST_TOKEN, TEST_FILENAME);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("File deleted successfully");

        verify(fileService).deleteFile(TEST_TOKEN, TEST_FILENAME);
    }

    @Test
    void deleteFile_whenFileNotFound_shouldPropagateException() {
        // given
        doThrow(new FileNotFoundException("File not found: " + TEST_FILENAME))
                .when(fileService).deleteFile(TEST_TOKEN, TEST_FILENAME);

        // when & then
        assertThatThrownBy(() -> fileController.deleteFile(TEST_TOKEN, TEST_FILENAME))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File not found: " + TEST_FILENAME);
    }

    @Test
    void downloadFile_whenValidRequest_shouldReturnFileWithHeaders() {
        // given
        byte[] fileContent = "Hello World".getBytes();
        ByteArrayResource resource = new ByteArrayResource(fileContent);

        FileDownloadResponse downloadResponse = FileDownloadResponse.builder()
                .resource(resource)
                .fileName(TEST_FILENAME)
                .contentType("text/plain")
                .size((long) fileContent.length)
                .build();

        when(fileService.downloadFile(TEST_TOKEN, TEST_FILENAME)).thenReturn(downloadResponse);

        // when
        ResponseEntity<?> response = fileController.downloadFile(TEST_TOKEN, TEST_FILENAME);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(resource);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("text/plain");
        assertThat(response.getHeaders().getContentLength()).isEqualTo(fileContent.length);
        assertThat(response.getHeaders().get("Content-Disposition"))
                .contains("attachment; filename=\"" + TEST_FILENAME + "\"");

        verify(fileService).downloadFile(TEST_TOKEN, TEST_FILENAME);
    }

    @Test
    void downloadFile_whenFileNotFound_shouldPropagateException() {
        // given
        when(fileService.downloadFile(TEST_TOKEN, TEST_FILENAME))
                .thenThrow(new FileNotFoundException("File not found: " + TEST_FILENAME));

        // when & then
        assertThatThrownBy(() -> fileController.downloadFile(TEST_TOKEN, TEST_FILENAME))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File not found: " + TEST_FILENAME);
    }

    @Test
    void renameFile_whenValidRequest_shouldReturnSuccessResponse() {
        // given
        RenameFileRequest request = new RenameFileRequest();
        request.setNewFilename(NEW_FILENAME);
        doNothing().when(fileService).renameFile(TEST_TOKEN, TEST_FILENAME, NEW_FILENAME);

        // when
        ResponseEntity<?> response = fileController.renameFile(TEST_TOKEN, TEST_FILENAME, request);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

        @SuppressWarnings("unchecked")
        Map<String, String> body = (Map<String, String>) response.getBody();
        assertThat(body.get("message")).isEqualTo("File renamed successfully");

        verify(fileService).renameFile(TEST_TOKEN, TEST_FILENAME, NEW_FILENAME);
    }

    @Test
    void renameFile_whenNewNameAlreadyExists_shouldPropagateException() {
        // given
        RenameFileRequest request = new RenameFileRequest();
        request.setNewFilename(NEW_FILENAME);
        doThrow(new IllegalArgumentException("File with name '" + NEW_FILENAME + "' already exists"))
                .when(fileService).renameFile(TEST_TOKEN, TEST_FILENAME, NEW_FILENAME);

        // when & then
        assertThatThrownBy(() -> fileController.renameFile(TEST_TOKEN, TEST_FILENAME, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File with name '" + NEW_FILENAME + "' already exists");
    }

    @Test
    void getFileList_whenValidRequest_shouldReturnFileList() {
        // given
        int limit = 5;
        List<FileListResponse> expectedFiles = List.of(
                FileListResponse.builder()
                        .filename("file1.txt")
                        .size(100L)
                        .uploadDate(LocalDateTime.now())
                        .contentType("text/plain")
                        .build(),
                FileListResponse.builder()
                        .filename("file2.txt")
                        .size(200L)
                        .uploadDate(LocalDateTime.now())
                        .contentType("application/pdf")
                        .build()
        );

        when(fileService.getAllFiles(TEST_TOKEN, limit)).thenReturn(expectedFiles);

        // when
        ResponseEntity<?> response = fileController.getFileList(TEST_TOKEN, limit);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedFiles);

        verify(fileService).getAllFiles(TEST_TOKEN, limit);
    }

    @Test
    void getFileList_whenNoFiles_shouldReturnEmptyList() {
        // given
        int limit = 10;
        when(fileService.getAllFiles(TEST_TOKEN, limit)).thenReturn(List.of());

        // when
        ResponseEntity<?> response = fileController.getFileList(TEST_TOKEN, limit);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(List.of());

        verify(fileService).getAllFiles(TEST_TOKEN, limit);
    }
}
