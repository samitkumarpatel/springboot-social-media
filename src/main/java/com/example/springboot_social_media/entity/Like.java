package com.example.springboot_social_media.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "likeable_type", "likeable_id"}))
@Data
public class Like {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "likeable_type", nullable = false)
    private LikeableType likeableType;

    @Column(name = "likeable_id", nullable = false)
    private Long likeableId;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    // Constructors, getters, setters
    public Like() {}

    public Like(Long userId, LikeableType likeableType, Long likeableId) {
        this.userId = userId;
        this.likeableType = likeableType;
        this.likeableId = likeableId;
    }

    // Getters and setters...
}
