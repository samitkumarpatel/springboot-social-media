package com.example.springboot_social_media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateReplyRequest {
    @NotNull
    private Long authorId;

    @NotBlank
    private String content;
}
