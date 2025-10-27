package com.example.springboot_social_media.repositories;

import com.example.springboot_social_media.entity.Reply;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ReplyRepository extends ListCrudRepository<Reply, Long> {
    List<Reply> findByPostIdAndIsDeletedFalseOrderByPath(Long postId);
    List<Reply> findByParentCommentIdAndIsDeletedFalseOrderByCreatedAt(Long commentId);
    List<Reply> findByParentReplyIdAndIsDeletedFalseOrderByCreatedAt(Long replyId);
    List<Reply> findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(Long authorId);
}
