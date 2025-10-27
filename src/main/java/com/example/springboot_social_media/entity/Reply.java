package com.example.springboot_social_media.entity;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "replies")
public class Reply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_reply_id")
    private Reply parentReply;

    @Column(name = "author_id", nullable = false)
    private Long authorId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    private Boolean isDeleted = false;

    @Column(name = "depth_level")
    private Integer depthLevel = 1;

    @Column(columnDefinition = "TEXT")
    private String path;

    @OneToMany(mappedBy = "parentReply", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Reply> childReplies = new ArrayList<>();

    // Transient field for like count - calculated via service
    @Transient
    private Long likeCount;

    // Constructors, getters, setters
    public Reply() {}

    public Reply(Post post, Comment parentComment, Long authorId, String content) {
        this.post = post;
        this.parentComment = parentComment;
        this.authorId = authorId;
        this.content = content;
    }

    public Reply(Post post, Reply parentReply, Long authorId, String content) {
        this.post = post;
        this.parentReply = parentReply;
        this.authorId = authorId;
        this.content = content;
    }

    // Getters and setters...
}
