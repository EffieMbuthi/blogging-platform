-- ============================================================
-- Blogging Platform — PostgreSQL (SQL) Implementation Script
-- Database: blogging_platform
-- ============================================================
-- Run these statements in order (pgAdmin Query Tool or psql),
-- against a fresh 'blogging_platform' database.

-- ============================================================
-- 1. USERS
-- ============================================================
CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 2. POSTS
-- ============================================================
-- user_id references users(id): many posts -> one user (author).
CREATE TABLE posts (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 3. COMMENTS
-- ============================================================
-- References BOTH posts and users: many comments -> one post,
-- many comments -> one user (author of the comment).
CREATE TABLE comments (
    id SERIAL PRIMARY KEY,
    post_id INTEGER REFERENCES posts(id),
    user_id INTEGER REFERENCES users(id),
    body TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 4. REVIEWS
-- ============================================================
-- rating is constrained to 1-5 at the database level (CHECK),
-- the SQL equivalent of the $jsonSchema validator used on the
-- MongoDB implementation's reviews collection.
CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    post_id INTEGER REFERENCES posts(id),
    user_id INTEGER REFERENCES users(id),
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    created_at TIMESTAMP DEFAULT NOW()
);

-- ============================================================
-- 5. TAGS
-- ============================================================
CREATE TABLE tags (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

-- ============================================================
-- 6. POST_TAGS (junction table — many-to-many Posts <-> Tags)
-- ============================================================
-- Composite primary key (post_id, tag_id) prevents the same
-- post/tag pairing from being inserted more than once.
CREATE TABLE post_tags (
    post_id INTEGER REFERENCES posts(id),
    tag_id INTEGER REFERENCES tags(id),
    PRIMARY KEY (post_id, tag_id)
);

-- ============================================================
-- 7. INDEXES (User Story 1.2)
-- ============================================================
-- Primary keys (id columns above) are indexed automatically by
-- PostgreSQL. The following are added explicitly, matching the
-- fields the application frequently queries/filters by.

-- Frequently filtered: "posts by this author"
CREATE INDEX idx_posts_user_id ON posts(user_id);

-- Frequently searched: case-insensitive title search (ILIKE)
CREATE INDEX idx_posts_title ON posts(title);

-- Frequently filtered: "comments for this post"
CREATE INDEX idx_comments_post_id ON comments(post_id);

-- Frequently filtered: "reviews for this post"
CREATE INDEX idx_reviews_post_id ON reviews(post_id);

-- Frequently searched: look up a tag by name
CREATE INDEX idx_tags_name ON tags(name);

-- ============================================================
-- 8. SAMPLE DATA (matches data used during development/testing)
-- ============================================================
INSERT INTO users (name, email) VALUES ('Effie', 'effie@gmail.com');
INSERT INTO users (name, email) VALUES ('James', 'james@gmail.com');

INSERT INTO posts (user_id, title, body) VALUES (1, 'Voting', 'women voting.....');

INSERT INTO comments (post_id, user_id, body) VALUES (1, 2, 'Great post!');

INSERT INTO reviews (post_id, user_id, rating) VALUES (1, 2, 5);

INSERT INTO tags (name) VALUES ('politics');

INSERT INTO post_tags (post_id, tag_id) VALUES (1, 1);
