package com.example.springboot_social_media.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PostSummaryResponse {
    private Long id;
    private String title;
    private String content;
    private Long authorId;
    private LocalDateTime createdAt;
    private long likeCount;
    private int commentCount;
}
