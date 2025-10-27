package com.example.springboot_social_media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCommentRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;
}
