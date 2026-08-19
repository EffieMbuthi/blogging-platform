package org.example.service;

import org.example.dao.PostDAO;
import org.example.model.Post;

import java.sql.SQLException;
import java.util.List;

public class PostService {
    private static final int TITLE_MIN_LENGTH = 3;
    private static final int TITLE_MAX_LENGTH = 255;
    private static final int BODY_MIN_LENGTH = 10;
    private static final int BODY_MAX_LENGTH = 5000;

    private PostDAO postDAO;
    private List<Post> cachedPosts= null;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public void createPost(int authorId, String title, String body) throws SQLException {
        validateTitle(title);
        validateBody(body);
        int newId= postDAO.createPost(authorId, title, body);
        Post newPost= new Post(newId, authorId, title, body);
        if(cachedPosts!=null){
            cachedPosts.add(newPost);
        }

    }

    public List<Post> getAllPosts() throws SQLException {
        if (cachedPosts==null){
            cachedPosts= postDAO.findAllPosts();
        }
        return cachedPosts;

    }

    public void updatePost(int postId, String newTitle, String newBody) throws SQLException {
        validateTitle(newTitle);
        validateBody(newBody);
        postDAO.updatePost(postId, newTitle, newBody);
        if (cachedPosts != null) {
            for (Post post : cachedPosts) {
                if (post.getId() == postId) {
                    post.setTitle(newTitle);
                    post.setBody(newBody);
                }
            }
        }
    }

    public void deletePost(int postId) throws SQLException {
        postDAO.deletePost(postId);
        if (cachedPosts!=null){
            cachedPosts.removeIf(post->post.getId()==postId);
        }
    }

    public List<Post> getPostsPage(int pageNumber, int pageSize) throws SQLException {
        return postDAO.findPostsPaginated(pageNumber, pageSize);
    }

    public int getTotalPages(int pageSize) throws SQLException {
        int totalPosts = postDAO.countAllPosts();
        return Math.max(1, (int) Math.ceil(totalPosts / (double) pageSize));
    }

    public List<Post> searchPosts(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByTitle(keyword);
    }

    public List<Post> searchPostsByAuthor(String authorName) throws SQLException {
        if (authorName == null || authorName.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByAuthor(authorName);
    }

    public List<Post> searchPostsByTag(String tagName) throws SQLException {
        if (tagName == null || tagName.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByTag(tagName);
    }

    private void validateTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        int length = title.trim().length();
        if (length < TITLE_MIN_LENGTH || length > TITLE_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Title must be between " + TITLE_MIN_LENGTH + " and " + TITLE_MAX_LENGTH + " characters.");
        }
    }

    private void validateBody(String body) {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        int length = body.trim().length();
        if (length < BODY_MIN_LENGTH || length > BODY_MAX_LENGTH) {
            throw new IllegalArgumentException(
                    "Body must be between " + BODY_MIN_LENGTH + " and " + BODY_MAX_LENGTH + " characters.");
        }
    }
}