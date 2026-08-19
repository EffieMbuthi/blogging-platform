package org.example.model;

public class PostEngagement {
    private final String title;
    private final String authorName;
    private final int commentCount;
    private final double avgRating;
    private final int tagCount;

    public PostEngagement(String title, String authorName, int commentCount, double avgRating, int tagCount) {
        this.title = title;
        this.authorName = authorName;
        this.commentCount = commentCount;
        this.avgRating = avgRating;
        this.tagCount = tagCount;
    }

    public String getTitle() { return title; }
    public String getAuthorName() { return authorName; }
    public int getCommentCount() { return commentCount; }
    public double getAvgRating() { return avgRating; }
    public int getTagCount() { return tagCount; }
}
