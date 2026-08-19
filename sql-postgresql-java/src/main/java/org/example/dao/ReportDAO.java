package org.example.dao;

import org.example.connection.PostgresConnection;
import org.example.model.PostEngagement;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportDAO {

    // One query, four tables: JOIN (author) + LEFT JOIN (comments, reviews, tags)
    // + GROUP BY + COUNT/AVG aggregates, so every post is included even with
    // zero comments/reviews/tags.
    public List<PostEngagement> findPostEngagement() throws SQLException {
        String sql = "SELECT p.title AS title, u.name AS author_name, " +
                "COUNT(DISTINCT c.id) AS comment_count, " +
                "COALESCE(AVG(r.rating), 0) AS avg_rating, " +
                "COUNT(DISTINCT pt.tag_id) AS tag_count " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.id " +
                "LEFT JOIN comments c ON c.post_id = p.id " +
                "LEFT JOIN reviews r ON r.post_id = p.id " +
                "LEFT JOIN post_tags pt ON pt.post_id = p.id " +
                "GROUP BY p.id, p.title, u.name " +
                "ORDER BY comment_count DESC";

        List<PostEngagement> report = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                report.add(new PostEngagement(
                        rs.getString("title"),
                        rs.getString("author_name"),
                        rs.getInt("comment_count"),
                        rs.getDouble("avg_rating"),
                        rs.getInt("tag_count")
                ));
            }
        }
        return report;
    }
}
