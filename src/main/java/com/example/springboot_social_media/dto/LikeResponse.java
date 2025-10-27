package com.example.springboot_social_media.dto;

import lombok.Data;

@Data
public class LikeResponse {
    private boolean liked;
    private long likeCount;

    public LikeResponse(boolean liked, long likeCount) {
        this.liked = liked;
        this.likeCount = likeCount;
    }
}
