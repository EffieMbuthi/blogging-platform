package org.example.model;

public class Review {
    private int id;
    private int postId;
    private int userId;
    private int rating;

    public Review(int id, int postId, int userId, int rating) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.rating = rating;
    }

    public int getId() { return id; }
    public int getPostId() { return postId; }
    public int getUserId() { return userId; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
}