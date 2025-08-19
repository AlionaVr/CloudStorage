package com.example.cloudservice.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RenameFileRequest {
    @NotBlank(message = "name must not be blank")
    private String name;
}