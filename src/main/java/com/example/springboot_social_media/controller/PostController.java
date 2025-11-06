package com.example.springboot_social_media.controller;

import com.example.springboot_social_media.dto.CreatePostRequest;
import com.example.springboot_social_media.dto.LikeResponse;
import com.example.springboot_social_media.dto.PostSummaryResponse;
import com.example.springboot_social_media.dto.UpdatePostRequest;
import com.example.springboot_social_media.entity.Like;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.entity.Post;
import com.example.springboot_social_media.services.LikeService;
import com.example.springboot_social_media.services.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
@Validated
@RequiredArgsConstructor
@CrossOrigin
public class PostController {

    final PostService postService;

    final LikeService likeService;

    @GetMapping
    public ResponseEntity<List<PostSummaryResponse>> getAllPosts() {
        try {
            List<PostSummaryResponse> posts = postService.findAllPublishedPostsSummary();
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
        try {
            Post post = postService.findByIdWithDetails(id);
            if (post != null) {
                return ResponseEntity.ok(post);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping
    public ResponseEntity<Post> createPost(@Valid @RequestBody CreatePostRequest request) {
        try {
            Post post = postService.createPost(
                    request.getTitle(),
                    request.getContent(),
                    request.getAuthorId()
            );
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Post> updatePost(@PathVariable Long id, @Valid @RequestBody UpdatePostRequest request) {
        try {
            Post post = postService.updatePost(id, request.getTitle(), request.getContent());
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponse> toggleLike(@PathVariable Long id, @RequestParam Long userId) {
        try {
            boolean liked = likeService.toggleLike(userId, LikeableType.POST, id);
            long likeCount = likeService.getLikeCount(LikeableType.POST, id);
            LikeResponse response = new LikeResponse(liked, likeCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @GetMapping("/{id}/likes")
    public ResponseEntity<List<Like>> getPostLikes(@PathVariable Long id) {
        try {
            List<Like> likes = likeService.getLikesForItem(LikeableType.POST, id);
            return ResponseEntity.ok(likes);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
