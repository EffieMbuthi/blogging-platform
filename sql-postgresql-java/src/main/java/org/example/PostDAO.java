package org.example;

import java.sql.*;

public class PostDAO {

    public void createPost(int authorId, String title, String body) throws SQLException {
        String sql = "INSERT INTO posts (user_id, title, body) VALUES (?, ?, ?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, authorId);
            stmt.setString(2, title);
            stmt.setString(3, body);
            stmt.executeUpdate();
        }
    }

    public ResultSet findAllPosts() throws SQLException {
        String sql = "SELECT * FROM posts";
        Connection conn = PostgresConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        return stmt.executeQuery();
    }

    public void updatePostTitle(int postId, String newTitle) throws SQLException {
        String sql = "UPDATE posts SET title = ? WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setInt(2, postId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows updated: " + rowsAffected);
        }
    }

    public void deletePost(int postId) throws SQLException {
        String sql = "DELETE FROM posts WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            int rowsAffected = stmt.executeUpdate();
            System.out.println("Rows deleted: " + rowsAffected);
        }
    }
}