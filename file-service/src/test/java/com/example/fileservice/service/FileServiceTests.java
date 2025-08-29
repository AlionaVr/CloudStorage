package com.example.fileservice.service;

import com.example.fileservice.exception.FileNotFoundException;
import com.example.fileservice.model.FileDocument;
import com.example.fileservice.repository.FileRepository;
import com.example.securitylib.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTests {
    @Mock
    private FileRepository fileRepository;

    @Mock
    private MultipartFile multipartFile;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private FileService fileService;

    private static final String TEST_TOKEN = "test-jwt-token";
    private static final String TEST_USERNAME = "testuser";
    private static final String TEST_FILENAME = "test.txt";
    private static final String TEST_CONTENT_TYPE = "text/plain";
    private static final long TEST_FILE_SIZE = 1024L;
    private static final long MAX_FILE_SIZE = 10485760; // 10MB;
    private static final byte[] TEST_FILE_DATA = "test file content".getBytes();

    @BeforeEach
    public void setUp() throws Exception {
        when(jwtService.getUsername(TEST_TOKEN)).thenReturn(TEST_USERNAME);

        Field maxFileSizeField = FileService.class.getDeclaredField("MaxFileSize");
        maxFileSizeField.setAccessible(true);
        maxFileSizeField.set(fileService, MAX_FILE_SIZE);

    }

    @ParameterizedTest
    @ValueSource(longs = {TEST_FILE_SIZE, MAX_FILE_SIZE})
    void uploadFile_whenFileNotExist_thenReturnSuccess(long fileSize) throws IOException {
        // given
        when(multipartFile.getSize()).thenReturn(fileSize);
        when(multipartFile.getContentType()).thenReturn(TEST_CONTENT_TYPE);
        when(multipartFile.getBytes()).thenReturn(TEST_FILE_DATA);

        when(fileRepository.findByOwnerNameAndFileName(TEST_USERNAME, TEST_FILENAME))
                .thenReturn(Optional.empty());

        // when
        fileService.uploadFile(TEST_TOKEN, TEST_FILENAME, multipartFile);

        // then
        ArgumentCaptor<FileDocument> fileCaptor = ArgumentCaptor.forClass(FileDocument.class);
        verify(fileRepository).save(fileCaptor.capture());

        FileDocument savedFile = fileCaptor.getValue();
        assertThat(savedFile.getFileName()).isEqualTo(TEST_FILENAME);
        assertThat(savedFile.getOwnerName()).isEqualTo(TEST_USERNAME);
        assertThat(savedFile.getContentType()).isEqualTo(TEST_CONTENT_TYPE);
        assertThat(savedFile.getSize()).isEqualTo(fileSize);
        assertThat(savedFile.getFileData()).isEqualTo(TEST_FILE_DATA);
        assertThat(savedFile.getUploadDate()).isNotNull();
    }

    @Test
    void whenUploadExistingFile_thenThrowException() {
        // given
        FileDocument existingFile = createTestFileDocument();
        when(fileRepository.findByOwnerNameAndFileName(TEST_USERNAME, TEST_FILENAME))
                .thenReturn(Optional.of(existingFile));

        // when & then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_TOKEN, TEST_FILENAME, multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File already exists");

        verify(fileRepository, never()).save(any());
    }

    @Test
    void whenUploadTooLargeFile_thenThrowException() {
        // given
        when(multipartFile.getSize()).thenReturn(MAX_FILE_SIZE + 1);
        when(fileRepository.findByOwnerNameAndFileName(TEST_USERNAME, TEST_FILENAME))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.uploadFile(TEST_TOKEN, TEST_FILENAME, multipartFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("File too large");

        verify(fileRepository, never()).save(any());
    }

    @Test
    void whenDeleteExistingFile_ThenReturnSuccess() {
        // given
        FileDocument file = createTestFileDocument();
        when(fileRepository.findByOwnerNameAndFileName(TEST_USERNAME, TEST_FILENAME))
                .thenReturn(Optional.of(file));

        // when
        fileService.deleteFile(TEST_TOKEN, TEST_FILENAME);

        // then
        verify(fileRepository).delete(file);
    }

    @Test
    void whenDeleteNonExistingFile_ThenReturnNotFound() {
        // given
        when(fileRepository.findByOwnerNameAndFileName(TEST_USERNAME, TEST_FILENAME))
                .thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> fileService.deleteFile(TEST_TOKEN, TEST_FILENAME))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("File not found: " + TEST_FILENAME);

        verify(fileRepository, never()).delete(any());
    }

    private FileDocument createTestFileDocument() {
        return FileDocument.builder()
                .fileName(TEST_FILENAME)
                .contentType(TEST_CONTENT_TYPE)
                .ownerName(TEST_USERNAME)
                .size(TEST_FILE_SIZE)
                .uploadDate(LocalDateTime.now())
                .fileData(TEST_FILE_DATA)
                .build();
    }
}