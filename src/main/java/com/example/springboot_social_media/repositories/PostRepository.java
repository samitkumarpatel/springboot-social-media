package com.example.springboot_social_media.repositories;

import com.example.springboot_social_media.entity.Post;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface PostRepository extends ListCrudRepository<Post, Long> {
    List<Post> findByIsPublishedTrueOrderByCreatedAtDesc();
    List<Post> findByAuthorIdOrderByCreatedAtDesc(Long authorId);
}
