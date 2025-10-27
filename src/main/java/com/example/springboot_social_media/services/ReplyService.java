package com.example.springboot_social_media.services;

import com.example.springboot_social_media.entity.Comment;
import com.example.springboot_social_media.entity.LikeableType;
import com.example.springboot_social_media.entity.Post;
import com.example.springboot_social_media.entity.Reply;
import com.example.springboot_social_media.repositories.CommentRepository;
import com.example.springboot_social_media.repositories.PostRepository;
import com.example.springboot_social_media.repositories.ReplyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReplyService {

    final ReplyRepository replyRepository;

    final PostRepository postRepository;

    final CommentRepository commentRepository;

    final LikeService likeService;

    public Reply createReplyToComment(Long postId, Long commentId, Long authorId, String content) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<Comment> commentOpt = commentRepository.findById(commentId);

        if (postOpt.isPresent() && commentOpt.isPresent()) {
            Post post = postOpt.get();
            Comment comment = commentOpt.get();
            Reply reply = new Reply(post, comment, authorId, content);
            return replyRepository.save(reply);
        }
        throw new RuntimeException("Post or Comment not found");
    }

    public Reply createReplyToReply(Long postId, Long parentReplyId, Long authorId, String content) {
        Optional<Post> postOpt = postRepository.findById(postId);
        Optional<Reply> parentReplyOpt = replyRepository.findById(parentReplyId);

        if (postOpt.isPresent() && parentReplyOpt.isPresent()) {
            Post post = postOpt.get();
            Reply parentReply = parentReplyOpt.get();
            Reply reply = new Reply(post, parentReply, authorId, content);
            return replyRepository.save(reply);
        }
        throw new RuntimeException("Post or Parent Reply not found");
    }

    public Optional<Reply> findById(Long id) {
        return replyRepository.findById(id);
    }

    public Reply findByIdWithLikes(Long id) {
        Optional<Reply> replyOpt = replyRepository.findById(id);
        if (replyOpt.isPresent()) {
            Reply reply = replyOpt.get();
            reply.setLikeCount(likeService.getLikeCount(LikeableType.REPLY, reply.getId()));
            return reply;
        }
        return null;
    }

    public List<Reply> findRepliesByPost(Long postId) {
        List<Reply> replies = replyRepository.findByPostIdAndIsDeletedFalseOrderByPath(postId);
        replies.forEach(reply ->
                reply.setLikeCount(likeService.getLikeCount(LikeableType.REPLY, reply.getId()))
        );
        return replies;
    }

    public List<Reply> findRepliesByComment(Long commentId) {
        List<Reply> replies = replyRepository.findByParentCommentIdAndIsDeletedFalseOrderByCreatedAt(commentId);
        replies.forEach(reply ->
                reply.setLikeCount(likeService.getLikeCount(LikeableType.REPLY, reply.getId()))
        );
        return replies;
    }

    public List<Reply> findRepliesByParentReply(Long parentReplyId) {
        List<Reply> replies = replyRepository.findByParentReplyIdAndIsDeletedFalseOrderByCreatedAt(parentReplyId);
        replies.forEach(reply ->
                reply.setLikeCount(likeService.getLikeCount(LikeableType.REPLY, reply.getId()))
        );
        return replies;
    }

    public List<Reply> findRepliesByAuthor(Long authorId) {
        List<Reply> replies = replyRepository.findByAuthorIdAndIsDeletedFalseOrderByCreatedAtDesc(authorId);
        replies.forEach(reply ->
                reply.setLikeCount(likeService.getLikeCount(LikeableType.REPLY, reply.getId()))
        );
        return replies;
    }

    public Reply updateReply(Long id, String content) {
        Optional<Reply> replyOpt = replyRepository.findById(id);
        if (replyOpt.isPresent()) {
            Reply reply = replyOpt.get();
            reply.setContent(content);
            return replyRepository.save(reply);
        }
        throw new RuntimeException("Reply not found with id: " + id);
    }

    public void deleteReply(Long id) {
        Optional<Reply> replyOpt = replyRepository.findById(id);
        if (replyOpt.isPresent()) {
            Reply reply = replyOpt.get();
            reply.setIsDeleted(true);
            replyRepository.save(reply);
        }
    }

    public void hardDeleteReply(Long id) {
        replyRepository.deleteById(id);
    }

    public List<Reply> getHierarchicalReplies(Long postId) {
        // This would use the PostgreSQL function get_replies_hierarchy
        return replyRepository.findByPostIdAndIsDeletedFalseOrderByPath(postId);
    }
}
