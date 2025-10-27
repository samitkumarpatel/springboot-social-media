package com.example.springboot_social_media.entity;

public enum LikeableType {
    POST("post"),
    COMMENT("comment"),
    REPLY("reply");

    private final String value;

    LikeableType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
