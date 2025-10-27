package com.example.springboot_social_media.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateReplyRequest {
    @NotBlank
    private String content;
}
