package com.example.fileservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RenameFileRequest {
    @NotBlank(message = "name must not be blank")
    private String newFilename;
}