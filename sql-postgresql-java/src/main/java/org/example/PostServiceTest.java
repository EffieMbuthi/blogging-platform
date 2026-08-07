package org.example;

import java.sql.SQLException;
import java.util.List;

public class PostServiceTest {
    public static void main(String[] args) {
        PostService postService = new PostService();

        try {
            postService.createPost(1, "My First SQL Post", "Testing through the Service layer.");
            System.out.println("Post created.");

            List<Post> posts = postService.getAllPosts();
            for (Post post : posts) {
                System.out.println(post.getId() + " - " + post.getTitle());
            }

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("Validation error: " + e.getMessage());
        }
    }
}