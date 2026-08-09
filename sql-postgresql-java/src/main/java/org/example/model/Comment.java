package org.example.model;

public class Comment {
    private int id;
    private int postId;
    private int userId;
    private String body;

    public Comment(int id, int postId, int userId, String body) {
        this.id = id;
        this.postId = postId;
        this.userId = userId;
        this.body = body;
    }

    public int getId() { return id; }
    public int getPostId() { return postId; }
    public int getUserId() { return userId; }
    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }
}