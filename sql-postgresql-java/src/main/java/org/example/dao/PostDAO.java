package org.example.dao;

import org.example.connection.PostgresConnection;
import org.example.model.Post;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PostDAO {

    public int createPost(int authorId, String title, String body) throws SQLException {
        String sql = "INSERT INTO posts (user_id, title, body) VALUES (?, ?, ?)";
        try (Connection conn = PostgresConnection.getConnection();
             //when you prepare your INSERT statement, you can tell it "after this runs,
             // I want you to hand me back whatever id got generated.(RETURN GENERATED KEYS)
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            //When this statement runs, keep track of any auto-generated column values, so I can ask for them afterward.
            //Without this flag, JDBC doesn't bother tracking that information at all — it's an opt-in feature, not automatic

            stmt.setInt(1, authorId);
            stmt.setString(2, title);
            stmt.setString(3, body);
            stmt.executeUpdate();

            //After running the insert, ask for the generated key
            ResultSet generatedKeys= stmt.getGeneratedKeys(); //just this time it doesn't hold rows of your posts table; it holds exactly one row, one column: the newly generated id.
            if (generatedKeys.next()) {
                return generatedKeys.getInt(1);
            } else {
                throw new SQLException("Failed to retrieve generated post id.");
            }
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

    public void updatePost(int postId, String newTitle, String newBody) throws SQLException {
        String sql = "UPDATE posts SET title = ?, body = ? WHERE id = ?";
        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, newTitle);
            stmt.setString(2, newBody);
            stmt.setInt(3, postId);
            stmt.executeUpdate();
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

    //ILIKE= case insensitive
    public List<Post> searchPostsByTitle(String keyword) throws SQLException {
        String sql = "SELECT * FROM posts WHERE title ILIKE ?";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            //anything can come before/after this." So searching "vot"
            // becomes the pattern "%vot%", matching "Voting", "devoted",
            // anything containing "vot" anywhere in the string
            stmt.setString(1, "%" + keyword + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("body")
                    ));
                }
            }
        }
        return posts;
    }

    public List<Post> findPostsPaginated(int pageNumber, int pageSize) throws SQLException {
        String sql = "SELECT * FROM posts ORDER BY id LIMIT ? OFFSET ?";
        List<Post> posts = new ArrayList<>();
        int offset = (pageNumber - 1) * pageSize;

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, pageSize);
            stmt.setInt(2, offset);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("body")
                    ));
                }
            }
        }
        return posts;
    }

    public List<Post> searchPostsByAuthor(String authorName) throws SQLException {
        String sql = "SELECT posts.* FROM posts JOIN users ON posts.user_id = users.id WHERE users.name ILIKE ?";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + authorName + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("body")
                    ));
                }
            }
        }
        return posts;
    }

    public List<Post> searchPostsByTag(String tagName) throws SQLException {
        String sql = "SELECT posts.* FROM posts " +
                "JOIN post_tags ON posts.id = post_tags.post_id " +
                "JOIN tags ON post_tags.tag_id = tags.id " +
                "WHERE tags.name ILIKE ?";
        List<Post> posts = new ArrayList<>();

        try (Connection conn = PostgresConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, "%" + tagName + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    posts.add(new Post(
                            rs.getInt("id"),
                            rs.getInt("user_id"),
                            rs.getString("title"),
                            rs.getString("body")
                    ));
                }
            }
        }
        return posts;
    }
}