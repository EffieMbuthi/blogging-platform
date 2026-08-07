package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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

    public List<Post> findAllPosts() throws SQLException {
        // loops through ResultSet internally, builds Post objects, returns a List<Post>(for updates)
        String sql = "SELECT * FROM posts";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            //still creates that exact same ResultSet internally, loops through it itself,
            // converts each row into a Post object — and only then hands back the finished List<Post>
            while (rs.next()) {// stops automatically when rs.next() returns false (no more rows left).
                posts.add(new Post(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("title"),
                        rs.getString("body")
                ));
            }
        }
        return posts;
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