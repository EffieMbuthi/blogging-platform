package org.example.service;

import org.example.dao.CommentDAO;
import org.example.model.Comment;
import java.sql.SQLException;
import java.util.List;

public class CommentService {
    private CommentDAO commentDAO;

    public CommentService() {
        this.commentDAO = new CommentDAO();
    }

    public void createComment(int postId, int userId, String body) throws SQLException {
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Comment body cannot be empty.");
        }
        commentDAO.createComment(postId, userId, body);
    }

    public List<Comment> getCommentsForPost(int postId) throws SQLException {
        return commentDAO.findCommentsByPostId(postId);
    }

    public void deleteComment(int commentId) throws SQLException {
        commentDAO.deleteComment(commentId);
    }
}