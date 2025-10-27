package com.example.springboot_social_media.services;

import com.example.springboot_social_media.entity.Comment;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.entity.Post;
import com.example.springboot_social_media.repositories.CommentRepository;
import com.example.springboot_social_media.repositories.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class CommentService {

    final CommentRepository commentRepository;

    final PostRepository postRepository;

    final LikeService likeService;

    public Comment createComment(Long postId, Long authorId, String content) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            Comment comment = new Comment(post, authorId, content);
            return commentRepository.save(comment);
        }
        throw new RuntimeException("Post not found with id: " + postId);
    }

    public Optional<Comment> findById(Long id) {
        return commentRepository.findById(id);
    }

    public Comment findByIdWithLikes(Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setLikeCount(likeService.getLikeCount(LikeableType.COMMENT, comment.getId()));
            return comment;
        }
        return null;
    }

    public List<Comment> findCommentsByPost(Long postId) {
        List<Comment> comments = commentRepository.findByPostIdAndIsDeletedFalseOrderByCreatedAt(postId);
        comments.forEach(comment ->
                comment.setLikeCount(likeService.getLikeCount(LikeableType.COMMENT, comment.getId()))
        );
        return comments;
    }

    public List<Comment> findCommentsByAuthor(Long authorId) {
        List<Comment> comments = commentRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(authorId);
        comments.forEach(comment ->
                comment.setLikeCount(likeService.getLikeCount(LikeableType.COMMENT, comment.getId()))
        );
        return comments;
    }

    public Comment updateComment(Long id, String content) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setContent(content);
            return commentRepository.save(comment);
        }
        throw new RuntimeException("Comment not found with id: " + id);
    }

    public void deleteComment(Long id) {
        Optional<Comment> commentOpt = commentRepository.findById(id);
        if (commentOpt.isPresent()) {
            Comment comment = commentOpt.get();
            comment.setIsDeleted(true);
            commentRepository.save(comment);
        }
    }

    public void hardDeleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
