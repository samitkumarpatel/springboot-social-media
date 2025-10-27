package com.example.springboot_social_media.repositories;

import com.example.springboot_social_media.entity.Comment;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface CommentRepository extends ListCrudRepository<Comment, Long> {
    List<Comment> findByPostIdAndIsDeletedFalseOrderByCreatedAt(Long postId);
    List<Comment> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId);
}
