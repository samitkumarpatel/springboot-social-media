package com.example.springboot_social_media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePostRequest {
    @NotBlank
    private String title;

    @NotBlank
    private String content;
}
