package org.example;

import java.sql.ResultSet;
import java.sql.SQLException;

public class PostServiceTest {
    public static void main(String[] args) {
        PostService postService = new PostService();

        try {
            // Test create
            postService.createPost(1, "My First SQL Post", "Testing through the Service layer.");
            System.out.println("Post created.");

            // Test read
            ResultSet rs = postService.getAllPosts();
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " - " + rs.getString("title"));
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}