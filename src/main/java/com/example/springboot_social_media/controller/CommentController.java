package com.example.springboot_social_media.controller;

import com.example.springboot_social_media.dto.CreateCommentRequest;
import com.example.springboot_social_media.dto.LikeResponse;
import com.example.springboot_social_media.dto.UpdateCommentRequest;
import com.example.springboot_social_media.entity.Comment;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.services.CommentService;
import com.example.springboot_social_media.services.LikeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments")
@Validated
@RequiredArgsConstructor
public class CommentController {

    final CommentService commentService;

    final LikeService likeService;

    @GetMapping
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postId) {
        try {
            List<Comment> comments = commentService.findCommentsByPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommentRequest request) {
        try {
            Comment comment = commentService.createComment(postId, request.getAuthorId(), request.getContent());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<Comment> getCommentById(@PathVariable Long postId, @PathVariable Long commentId) {
        try {
            Comment comment = commentService.findByIdWithLikes(commentId);
            if (comment != null) {
                return ResponseEntity.ok(comment);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request) {
        try {
            Comment comment = commentService.updateComment(commentId, request.getContent());
            return ResponseEntity.ok(comment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId) {
        try {
            commentService.deleteComment(commentId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{commentId}/like")
    public ResponseEntity<LikeResponse> toggleCommentLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @RequestParam Long userId) {
        try {
            boolean liked = likeService.toggleLike(userId, LikeableType.COMMENT, commentId);
            long likeCount = likeService.getLikeCount(LikeableType.COMMENT, commentId);
            LikeResponse response = new LikeResponse(liked, likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
