package org.example.dao;

import org.example.connection.PostgresConnection;
import org.example.model.Tag;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TagDAO {

    public int createTag(String name) throws SQLException {
        String sql = "INSERT INTO tags (name) VALUES (?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, name);
            stmt.executeUpdate();

            ResultSet generatedKeys = stmt.getGeneratedKeys();
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated tag id.");
            }
        }
    }

    public List<Tag> findAllTags() throws SQLException {
        String sql = "SELECT * FROM tags";
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
            }
        }
        return tags;
    }

    //DAO method for inserting into your post_tags junction table, handling the many-to-many relationship.
    public void linkTagToPost(int postId, int tagId) throws SQLException {
        String sql = "INSERT INTO post_tags (post_id, tag_id) VALUES (?, ?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        }
    }

    public void unlinkTagFromPost(int postId, int tagId) throws SQLException {
        String sql = "DELETE FROM post_tags WHERE post_id = ? AND tag_id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        }
    }

    public List<Tag> findTagsForPost(int postId) throws SQLException {
        String sql = "SELECT tags.* FROM tags " +
                "JOIN post_tags ON tags.id = post_tags.tag_id " +
                "WHERE post_tags.post_id = ?";
        List<Tag> tags = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, postId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    tags.add(new Tag(rs.getInt("id"), rs.getString("name")));
                }
            }
        }
        return tags;
    }

    public Tag findTagByNameIgnoreCase(String name) throws SQLException {
        String sql = "SELECT * FROM tags WHERE LOWER(name) = LOWER(?)";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Tag(rs.getInt("id"), rs.getString("name"));
                }
            }
        }
        return null;
    }

    public void updateTagName(int tagId, String newName) throws SQLException {
        String sql = "UPDATE tags SET name = ? WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newName);
            stmt.setInt(2, tagId);
            stmt.executeUpdate();
        }
    }

    public void deleteTag(int tagId) throws SQLException {
        String sql = "DELETE FROM tags WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, tagId);
            stmt.executeUpdate();
        }
    }
}