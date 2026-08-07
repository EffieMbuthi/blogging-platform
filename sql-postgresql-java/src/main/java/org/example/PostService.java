package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostService {
    private PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public void createPost(int authorId, String title, String body) throws SQLException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        postDAO.createPost(authorId, title, body);
    }

    public List<Post> getAllPosts() throws SQLException {
        return postDAO.findAllPosts();
    }

    public void updatePostTitle(int postId, String newTitle) throws SQLException {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        postDAO.updatePostTitle(postId, newTitle);
    }

    public void deletePost(int postId) throws SQLException {
        postDAO.deletePost(postId);
    }
}