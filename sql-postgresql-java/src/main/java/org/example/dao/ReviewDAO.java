package org.example.dao;

import org.example.connection.PostgresConnection;
import org.example.model.Review;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    public int createReview(int postId, int userId, int rating) throws SQLException {
        String sql = "INSERT INTO reviews (post_id, user_id, rating) VALUES (?, ?, ?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, userId);
            stmt.setInt(3, rating);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated review id.");
            }
        }
    }

    public List<Review> findReviewsByPostId(int postId) throws SQLException {
        String sql = "SELECT * FROM reviews WHERE post_id = ?";
        List<Review> reviews = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reviews.add(new Review(
                            rs.getInt("id"),
                            rs.getInt("post_id"),
                            rs.getInt("user_id"),
                            rs.getInt("rating")
                    ));
                }
            }
        }
        return reviews;
    }

    public void deleteReview(int reviewId) throws SQLException {
        String sql = "DELETE FROM reviews WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, reviewId);
            stmt.executeUpdate();
        }
    }
}