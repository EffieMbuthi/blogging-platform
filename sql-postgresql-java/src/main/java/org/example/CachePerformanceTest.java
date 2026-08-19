package org.example;

import org.example.dao.CommentDAO;
import org.example.dao.PostDAO;
import org.example.model.Post;
import org.example.service.PostService;
import org.example.service.ReportService;
import java.sql.SQLException;
import java.util.List;

// Run this manually (right-click -> Run) against a seeded database, and
// paste the printed timings into performance-report-comparison.md
// (raw measurements) / analysis-report.md (what they imply).
// Covers: PostService in-memory caching (posts), idx_comments_post_id
// (comments), idx_tags_name (tag search), and the multi-table JOIN/GROUP BY
// used by the Post Engagement analytics report.
public class CachePerformanceTest {
    public static void main(String[] args) throws SQLException {
        PostService postService = new PostService();

        long start1 = System.nanoTime();
        List<Post> firstCall = postService.getAllPosts();
        long end1 = System.nanoTime();
        System.out.println("Posts - first call (cache miss): " + (end1 - start1) / 1_000_000.0 + " ms");

        long start2 = System.nanoTime();
        List<Post> secondCall = postService.getAllPosts();
        long end2 = System.nanoTime();
        System.out.println("Posts - second call (cache hit): " + (end2 - start2) / 1_000_000.0 + " ms");

        CommentDAO commentDAO = new CommentDAO();
        long start3 = System.nanoTime();
        commentDAO.findCommentsByPostId(1);
        long end3 = System.nanoTime();
        System.out.println("Comments for post_id=1 (idx_comments_post_id): "
                + (end3 - start3) / 1_000_000.0 + " ms");

        PostDAO postDAO = new PostDAO();
        long start4 = System.nanoTime();
        postDAO.searchPostsByTag("politics");
        long end4 = System.nanoTime();
        System.out.println("Posts by tag 'politics' (idx_tags_name): "
                + (end4 - start4) / 1_000_000.0 + " ms");

        ReportService reportService = new ReportService();
        long start5 = System.nanoTime();
        reportService.getPostEngagementReport();
        long end5 = System.nanoTime();
        System.out.println("Post Engagement report (4-table JOIN + GROUP BY): "
                + (end5 - start5) / 1_000_000.0 + " ms");
    }
}
