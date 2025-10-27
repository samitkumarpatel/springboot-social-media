-- PostgreSQL Schema for Posts, Comments, and Nested Replies
-- This schema supports unlimited nesting of replies

-- Create the database (optional - uncomment if needed)
-- CREATE DATABASE social_media;

-- Use the database
-- \c social_media;

-- Posts table
CREATE TABLE posts (
       id SERIAL PRIMARY KEY,
       title VARCHAR(255) NOT NULL,
       content TEXT NOT NULL,
       author_id INTEGER NOT NULL, -- Assuming you have a users table
       created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
       updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
       is_published BOOLEAN DEFAULT true,
       view_count INTEGER DEFAULT 0
);

-- Comments table (first level comments on posts)
CREATE TABLE comments (
      id SERIAL PRIMARY KEY,
      post_id INTEGER NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
      author_id INTEGER NOT NULL, -- Assuming you have a users table
      content TEXT NOT NULL,
      created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
      is_deleted BOOLEAN DEFAULT false
);

-- Replies table (handles all levels of replies - to comments and to other replies)
CREATE TABLE replies (
     id SERIAL PRIMARY KEY,
     post_id INTEGER NOT NULL REFERENCES posts(id) ON DELETE CASCADE,
     parent_comment_id INTEGER REFERENCES comments(id) ON DELETE CASCADE,
     parent_reply_id INTEGER REFERENCES replies(id) ON DELETE CASCADE,
     author_id INTEGER NOT NULL, -- Assuming you have a users table
     content TEXT NOT NULL,
     created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
     is_deleted BOOLEAN DEFAULT false,
     depth_level INTEGER DEFAULT 1, -- Track nesting level for easier queries
     path TEXT, -- Materialized path for efficient hierarchical queries

-- Ensure a reply has either a parent comment or parent reply, but not both
     CONSTRAINT check_parent CHECK (
         (parent_comment_id IS NOT NULL AND parent_reply_id IS NULL) OR
         (parent_comment_id IS NULL AND parent_reply_id IS NOT NULL)
         )
);

-- Likes table - tracks who liked what (polymorphic relationship)
CREATE TABLE likes (
   id SERIAL PRIMARY KEY,
   user_id INTEGER NOT NULL, -- Assuming you have a users table
   likeable_type VARCHAR(20) NOT NULL, -- 'post', 'comment', or 'reply'
   likeable_id INTEGER NOT NULL, -- ID of the liked item
   created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    -- Ensure likeable_type is valid
   CONSTRAINT check_likeable_type CHECK (likeable_type IN ('post', 'comment', 'reply')),

    -- Ensure a user can only like the same item once
   CONSTRAINT unique_like_per_user UNIQUE (user_id, likeable_type, likeable_id)
);

-- Indexes for performance
CREATE INDEX idx_posts_author ON posts(author_id);
CREATE INDEX idx_posts_created_at ON posts(created_at);
CREATE INDEX idx_posts_published ON posts(is_published);

CREATE INDEX idx_comments_post ON comments(post_id);
CREATE INDEX idx_comments_author ON comments(author_id);
CREATE INDEX idx_comments_created_at ON comments(created_at);

CREATE INDEX idx_replies_post ON replies(post_id);
CREATE INDEX idx_replies_parent_comment ON replies(parent_comment_id);
CREATE INDEX idx_replies_parent_reply ON replies(parent_reply_id);
CREATE INDEX idx_replies_author ON replies(author_id);
CREATE INDEX idx_replies_created_at ON replies(created_at);
CREATE INDEX idx_replies_depth ON replies(depth_level);
CREATE INDEX idx_replies_path ON replies(path);

-- Likes table indexes
CREATE INDEX idx_likes_user ON likes(user_id);
CREATE INDEX idx_likes_likeable ON likes(likeable_type, likeable_id);
CREATE INDEX idx_likes_created_at ON likes(created_at);

-- Function to update the materialized path for replies
CREATE OR REPLACE FUNCTION update_reply_path()
RETURNS TRIGGER AS $$
DECLARE
parent_path TEXT;
    parent_depth INTEGER;
BEGIN
    -- If replying to a comment
    IF NEW.parent_comment_id IS NOT NULL THEN
        NEW.depth_level := 1;
        NEW.path := NEW.parent_comment_id::TEXT;
    -- If replying to another reply
    ELSIF NEW.parent_reply_id IS NOT NULL THEN
SELECT path, depth_level INTO parent_path, parent_depth
FROM replies WHERE id = NEW.parent_reply_id;

NEW.depth_level := parent_depth + 1;
        NEW.path := parent_path || '.' || NEW.parent_reply_id::TEXT;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger to automatically update path and depth when inserting replies
CREATE TRIGGER trigger_update_reply_path
    BEFORE INSERT ON replies
    FOR EACH ROW
    EXECUTE FUNCTION update_reply_path();


-- Function to get all replies in a hierarchical structure
CREATE OR REPLACE FUNCTION get_replies_hierarchy(post_id_param INTEGER)
RETURNS TABLE (
    id INTEGER,
    parent_comment_id INTEGER,
    parent_reply_id INTEGER,
    author_id INTEGER,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    depth_level INTEGER,
    path TEXT,
    like_count BIGINT
) AS $$
BEGIN
RETURN QUERY
SELECT
    r.id,
    r.parent_comment_id,
    r.parent_reply_id,
    r.author_id,
    r.content,
    r.created_at,
    r.depth_level,
    r.path,
    COALESCE(lr.like_count, 0) as like_count
FROM replies r
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'reply'
    GROUP BY likeable_id
) lr ON r.id = lr.likeable_id
WHERE r.post_id = post_id_param
  AND r.is_deleted = false
ORDER BY r.path, r.created_at;
END;
$$ LANGUAGE plpgsql;

-- Function to get comment thread (comment + all its replies)
CREATE OR REPLACE FUNCTION get_comment_thread(comment_id_param INTEGER)
RETURNS TABLE (
    id INTEGER,
    type TEXT, -- 'comment' or 'reply'
    parent_comment_id INTEGER,
    parent_reply_id INTEGER,
    author_id INTEGER,
    content TEXT,
    created_at TIMESTAMP WITH TIME ZONE,
    depth_level INTEGER,
    path TEXT,
    like_count BIGINT
) AS $$
BEGIN
RETURN QUERY
-- Get the comment itself
SELECT
    c.id,
    'comment'::TEXT as type,
    NULL::INTEGER as parent_comment_id,
    NULL::INTEGER as parent_reply_id,
    c.author_id,
    c.content,
    c.created_at,
    0 as depth_level,
    c.id::TEXT as path,
    COALESCE(lc.like_count, 0) as like_count
FROM comments c
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'comment'
    GROUP BY likeable_id
) lc ON c.id = lc.likeable_id
WHERE c.id = comment_id_param
  AND c.is_deleted = false

UNION ALL

-- Get all replies to this comment
SELECT
    r.id,
    'reply'::TEXT as type,
    r.parent_comment_id,
    r.parent_reply_id,
    r.author_id,
    r.content,
    r.created_at,
    r.depth_level,
    r.path,
    COALESCE(lr.like_count, 0) as like_count
FROM replies r
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'reply'
    GROUP BY likeable_id
) lr ON r.id = lr.likeable_id
WHERE r.parent_comment_id = comment_id_param
  AND r.is_deleted = false

ORDER BY path, created_at;
END;
$$ LANGUAGE plpgsql;

-- Function to get like count for any likeable item
CREATE OR REPLACE FUNCTION get_like_count(likeable_type_param VARCHAR(20), likeable_id_param INTEGER)
RETURNS INTEGER AS $$
DECLARE
count_result INTEGER;
BEGIN
SELECT COUNT(*) INTO count_result
FROM likes
WHERE likeable_type = likeable_type_param
  AND likeable_id = likeable_id_param;

RETURN count_result;
END;
$$ LANGUAGE plpgsql;

-- Function to check if a user has liked an item
CREATE OR REPLACE FUNCTION has_user_liked(user_id_param INTEGER, likeable_type_param VARCHAR(20), likeable_id_param INTEGER)
RETURNS BOOLEAN AS $$
DECLARE
like_exists BOOLEAN;
BEGIN
SELECT EXISTS(
    SELECT 1 FROM likes
    WHERE user_id = user_id_param
      AND likeable_type = likeable_type_param
      AND likeable_id = likeable_id_param
) INTO like_exists;

RETURN like_exists;
END;
$$ LANGUAGE plpgsql;

-- Function to toggle a like (like if not liked, unlike if already liked)
CREATE OR REPLACE FUNCTION toggle_like(user_id_param INTEGER, likeable_type_param VARCHAR(20), likeable_id_param INTEGER)
RETURNS BOOLEAN AS $$
DECLARE
like_exists BOOLEAN;
    new_like_state BOOLEAN;
BEGIN
    -- Check if like already exists
SELECT has_user_liked(user_id_param, likeable_type_param, likeable_id_param) INTO like_exists;

IF like_exists THEN
        -- Unlike: remove the like
DELETE FROM likes
WHERE user_id = user_id_param
  AND likeable_type = likeable_type_param
  AND likeable_id = likeable_id_param;
new_like_state := false;
ELSE
        -- Like: add the like
        INSERT INTO likes (user_id, likeable_type, likeable_id)
        VALUES (user_id_param, likeable_type_param, likeable_id_param);
        new_like_state := true;
END IF;

RETURN new_like_state;
END;
$$ LANGUAGE plpgsql;

-- Sample data for testing
INSERT INTO posts (title, content, author_id) VALUES
      ('My First Post', 'This is the content of my first post!', 1),
      ('Another Post', 'This is another post with some interesting content.', 2);

INSERT INTO comments (post_id, author_id, content) VALUES
       (1, 2, 'Great post! I really enjoyed reading this.'),
       (1, 3, 'I have a different perspective on this topic.'),
       (2, 1, 'Thanks for sharing this information.');

INSERT INTO replies (post_id, parent_comment_id, author_id, content) VALUES
     (1, 1, 1, 'Thank you! I''m glad you found it interesting.'),
     (1, 1, 3, 'I agree with the author''s response.'),
     (1, 2, 1, 'I''d love to hear your perspective. Could you elaborate?');

-- Reply to a reply (nested reply)
INSERT INTO replies (post_id, parent_reply_id, author_id, content) VALUES
       (1, 1, 2, 'You''re welcome! Looking forward to more posts like this.'),
       (1, 3, 2, 'Sure! I think the main issue is...');

-- Reply to a reply to a reply (deeper nesting)
INSERT INTO replies (post_id, parent_reply_id, author_id, content) VALUES
    (1, 4, 1, 'That makes sense. Thanks for explaining!');

-- Sample likes data
INSERT INTO likes (user_id, likeable_type, likeable_id) VALUES
-- Likes for posts
    (2, 'post', 1),
    (3, 'post', 1),
    (1, 'post', 2),
    -- Likes for comments
    (1, 'comment', 1),
    (3, 'comment', 1),
    (2, 'comment', 2),
    (1, 'comment', 3),
    -- Likes for replies
    (2, 'reply', 1),
    (3, 'reply', 1),
    (1, 'reply', 2),
    (2, 'reply', 3),
    (1, 'reply', 4),
    (3, 'reply', 5);

-- Example queries to test the schema

-- Get all posts with their comment counts and like counts
SELECT
    p.id,
    p.title,
    p.created_at,
    COUNT(DISTINCT c.id) as comment_count,
    COALESCE(pl.like_count, 0) as like_count
FROM posts p
         LEFT JOIN comments c ON p.id = c.post_id AND c.is_deleted = false
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'post'
    GROUP BY likeable_id
) pl ON p.id = pl.likeable_id
GROUP BY p.id, p.title, p.created_at, pl.like_count
ORDER BY p.created_at DESC;

-- Get a specific post with all its comments and replies with like counts
SELECT
    p.title,
    p.content,
    c.content as comment_content,
    c.created_at as comment_date,
    COALESCE(cl.like_count, 0) as comment_likes,
    r.content as reply_content,
    r.created_at as reply_date,
    r.depth_level,
    r.path,
    COALESCE(rl.like_count, 0) as reply_likes
FROM posts p
         LEFT JOIN comments c ON p.id = c.post_id AND c.is_deleted = false
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'comment'
    GROUP BY likeable_id
) cl ON c.id = cl.likeable_id
         LEFT JOIN replies r ON c.id = r.parent_comment_id AND r.is_deleted = false
         LEFT JOIN (
    SELECT likeable_id, COUNT(*) as like_count
    FROM likes
    WHERE likeable_type = 'reply'
    GROUP BY likeable_id
) rl ON r.id = rl.likeable_id
WHERE p.id = 1
ORDER BY c.created_at, r.path;

-- Get hierarchical structure for a specific comment thread with like counts
SELECT * FROM get_comment_thread(1);

-- Get all replies in hierarchical order for a post
SELECT * FROM get_replies_hierarchy(1);

-- Get like count for a specific item
SELECT get_like_count('post', 1) as post_likes;
SELECT get_like_count('comment', 1) as comment_likes;
SELECT get_like_count('reply', 1) as reply_likes;

-- Check if a user has liked an item
SELECT has_user_liked(1, 'post', 1) as user_1_liked_post_1;
SELECT has_user_liked(2, 'comment', 1) as user_2_liked_comment_1;

-- Toggle a like (returns true if liked, false if unliked)
SELECT toggle_like(1, 'post', 1) as like_toggled;

-- Get all users who liked a specific post
SELECT
    l.user_id,
    l.created_at as liked_at
FROM likes l
WHERE l.likeable_type = 'post' AND l.likeable_id = 1
ORDER BY l.created_at;

-- Get all items liked by a specific user
SELECT
    l.likeable_type,
    l.likeable_id,
    l.created_at as liked_at
FROM likes l
WHERE l.user_id = 1
ORDER BY l.created_at DESC;
