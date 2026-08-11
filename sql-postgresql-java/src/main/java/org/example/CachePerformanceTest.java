package org.example;

import org.example.model.Post;
import org.example.service.PostService;
import java.sql.SQLException;
import java.util.List;

public class CachePerformanceTest {
    public static void main(String[] args) throws SQLException {
        PostService postService = new PostService();

        long start1 = System.nanoTime();
        List<Post> firstCall = postService.getAllPosts();
        long end1 = System.nanoTime();
        System.out.println("First call (cache miss): " + (end1 - start1) / 1_000_000.0 + " ms");

        long start2 = System.nanoTime();
        List<Post> secondCall = postService.getAllPosts();
        long end2 = System.nanoTime();
        System.out.println("Second call (cache hit): " + (end2 - start2) / 1_000_000.0 + " ms");
    }
}