package com.example.springboot_social_media.services;

import com.example.springboot_social_media.entity.Post;
import com.example.springboot_social_media.repositories.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {

    final PostRepository postRepository;

    final LikeService likeService;

    public Post createPost(String title, String content, Long authorId) {
        Post post = new Post(title, content, authorId);
        return postRepository.save(post);
    }

    public Optional<Post> findById(Long id) {
        return postRepository.findById(id);
    }

    public Post findByIdWithDetails(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            likeService.populateLikeCounts(post);
            return post;
        }
        return null;
    }

    public List<Post> findAllPublishedPosts() {
        List<Post> posts = postRepository.findByIsPublishedTrueOrderByCreatedAtDesc();
        //posts.forEach(likeService::populateLikeCounts);
        return posts;
    }

    public List<Post> findPostsByAuthor(Long authorId) {
        List<Post> posts = postRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
        posts.forEach(likeService::populateLikeCounts);
        return posts;
    }

    public Post updatePost(Long id, String title, String content) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setTitle(title);
            post.setContent(content);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post not found with id: " + id);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Post publishPost(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsPublished(true);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post not found with id: " + id);
    }

    public Post unpublishPost(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setIsPublished(false);
            return postRepository.save(post);
        }
        throw new RuntimeException("Post not found with id: " + id);
    }

    public void incrementViewCount(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        }
    }
}
