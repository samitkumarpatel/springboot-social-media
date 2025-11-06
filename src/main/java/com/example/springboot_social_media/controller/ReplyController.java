package com.example.springboot_social_media.controller;

import com.example.springboot_social_media.dto.CreateReplyRequest;
import com.example.springboot_social_media.dto.LikeResponse;
import com.example.springboot_social_media.dto.UpdateReplyRequest;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.entity.Reply;
import com.example.springboot_social_media.services.LikeService;
import com.example.springboot_social_media.services.ReplyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts/{postId}/comments/{commentId}/replies")
@Validated
@RequiredArgsConstructor
@CrossOrigin
public class ReplyController {

    final ReplyService replyService;

    final LikeService likeService;

    @GetMapping
    public ResponseEntity<List<Reply>> getRepliesByComment(
            @PathVariable Long postId,
            @PathVariable Long commentId) {
        try {
            List<Reply> replies = replyService.findRepliesByComment(commentId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Reply> createReplyToComment(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @Valid @RequestBody CreateReplyRequest request) {
        try {
            Reply reply = replyService.createReplyToComment(postId, commentId, request.getAuthorId(), request.getContent());
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/{replyId}/replies")
    public ResponseEntity<Reply> createReplyToReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @Valid @RequestBody CreateReplyRequest request) {
        try {
            Reply reply = replyService.createReplyToReply(postId, replyId, request.getAuthorId(), request.getContent());
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{replyId}")
    public ResponseEntity<Reply> getReplyById(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId) {
        try {
            Reply reply = replyService.findByIdWithLikes(replyId);
            if (reply != null) {
                return ResponseEntity.ok(reply);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{replyId}")
    public ResponseEntity<Reply> updateReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @Valid @RequestBody UpdateReplyRequest request) {
        try {
            Reply reply = replyService.updateReply(replyId, request.getContent());
            return ResponseEntity.ok(reply);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{replyId}")
    public ResponseEntity<Void> deleteReply(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId) {
        try {
            replyService.deleteReply(replyId);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{replyId}/like")
    public ResponseEntity<LikeResponse> toggleReplyLike(
            @PathVariable Long postId,
            @PathVariable Long commentId,
            @PathVariable Long replyId,
            @RequestParam Long userId) {
        try {
            boolean liked = likeService.toggleLike(userId, LikeableType.REPLY, replyId);
            long likeCount = likeService.getLikeCount(LikeableType.REPLY, replyId);
            LikeResponse response = new LikeResponse(liked, likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
