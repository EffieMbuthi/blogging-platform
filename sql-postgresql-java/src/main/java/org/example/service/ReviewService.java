package org.example.service;

import org.example.dao.ReviewDAO;
import org.example.model.Review;
import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private ReviewDAO reviewDAO;

    public ReviewService() {
        this.reviewDAO = new ReviewDAO();
    }

    public void createReview(int postId, int userId, int rating) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5.");
        }
        reviewDAO.createReview(postId, userId, rating);
    }

    public List<Review> getReviewsForPost(int postId) throws SQLException {
        return reviewDAO.findReviewsByPostId(postId);
    }

    public void deleteReview(int reviewId) throws SQLException {
        reviewDAO.deleteReview(reviewId);
    }
}