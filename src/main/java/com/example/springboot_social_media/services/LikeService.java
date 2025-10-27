package com.example.springboot_social_media.services;

import com.example.springboot_social_media.entity.Like;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.entity.Post;
import com.example.springboot_social_media.repositories.LikeRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
@RequiredArgsConstructor
public class LikeService {

    final LikeRepository likeRepository;

    public boolean toggleLike(Long userId, LikeableType likeableType, Long likeableId) {
        boolean exists = likeRepository.existsByUserIdAndLikeableTypeAndLikeableId(
                userId, likeableType, likeableId);

        if (exists) {
            likeRepository.deleteByUserIdAndLikeableTypeAndLikeableId(
                    userId, likeableType, likeableId);
            return false; // Unliked
        } else {
            Like like = new Like(userId, likeableType, likeableId);
            likeRepository.save(like);
            return true; // Liked
        }
    }

    public long getLikeCount(LikeableType likeableType, Long likeableId) {
        return likeRepository.countByLikeableTypeAndLikeableId(likeableType, likeableId);
    }

    public boolean hasUserLiked(Long userId, LikeableType likeableType, Long likeableId) {
        return likeRepository.existsByUserIdAndLikeableTypeAndLikeableId(
                userId, likeableType, likeableId);
    }

    public List<Like> getLikesByUser(Long userId) {
        return likeRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Like> getLikesForItem(LikeableType likeableType, Long likeableId) {
        return likeRepository.findByLikeableTypeAndLikeableId(likeableType, likeableId);
    }

    public void populateLikeCounts(Post post) {
        if (post != null) {
            post.setLikeCount(getLikeCount(LikeableType.POST, post.getId()));

            if (post.getComments() != null) {
                post.getComments().forEach(comment -> {
                    comment.setLikeCount(getLikeCount(LikeableType.COMMENT, comment.getId()));

                    if (comment.getReplies() != null) {
                        comment.getReplies().forEach(reply -> {
                            reply.setLikeCount(getLikeCount(LikeableType.REPLY, reply.getId()));
                        });
                    }
                });
            }
        }
    }

    public void removeAllLikesForItem(LikeableType likeableType, Long likeableId) {
        likeRepository.deleteByLikeableTypeAndLikeableId(likeableType, likeableId);
    }

    public Map<Long, Long> getLikeCountsForItems(LikeableType likeableType, List<Long> itemIds) {
        Map<Long, Long> likeCounts = new HashMap<>();
        for (Long itemId : itemIds) {
            likeCounts.put(itemId, getLikeCount(likeableType, itemId));
        }
        return likeCounts;
    }
}
