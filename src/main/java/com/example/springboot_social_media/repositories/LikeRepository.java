package com.example.springboot_social_media.repositories;

import com.example.springboot_social_media.entity.Like;
import com.example.springboot_social_media.entity.LikeableType;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface LikeRepository extends ListCrudRepository<Like, Long> {
    List<Like> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Like> findByLikeableTypeAndLikeableId(LikeableType likeableType, Long likeableId);
    boolean existsByUserIdAndLikeableTypeAndLikeableId(Long userId, LikeableType likeableType, Long likeableId);
    void deleteByUserIdAndLikeableTypeAndLikeableId(Long userId, LikeableType likeableType, Long likeableId);
    void deleteByLikeableTypeAndLikeableId(LikeableType likeableType, Long likeableId);
    long countByLikeableTypeAndLikeableId(LikeableType likeableType, Long likeableId);
    List<Like> findByLikeableType(LikeableType likeableType);
}
