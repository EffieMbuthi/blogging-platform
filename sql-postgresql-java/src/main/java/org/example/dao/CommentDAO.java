package org.example.dao;

import org.example.connection.PostgresConnection;
import org.example.model.Comment;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CommentDAO {

    public int createComment(int postId, int userId, String body) throws SQLException {
        String sql = "INSERT INTO comments (post_id, user_id, body) VALUES (?, ?, ?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setString(3, body);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated comment id.");
            }
        }
    }

    public List<Comment> findCommentsByPostId(int postId) throws SQLException {
        String sql = "SELECT * FROM comments WHERE post_id = ?";
        List<Comment> comments = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    comments.add(new Comment(
                            rs.getInt("id"),
                            rs.getInt("post_id"),
                            rs.getInt("user_id"),
                            rs.getString("body")
                    ));
                }
            }
        }
        return comments;
    }

    public void deleteComment(int commentId) throws SQLException {
        String sql = "DELETE FROM comments WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, commentId);
            stmt.executeUpdate();
        }
    }
}