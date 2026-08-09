package org.example.model;

public class Post {
    private int id;
    private int authorId;
    private String title;
    private String body;

    public Post(int id, int authorId, String title, String body) {
        this.id = id;
        this.authorId = authorId;
        this.title = title;
        this.body = body;
    }

    public int getId() { return id; }
    public int getAuthorId() { return authorId; }
    public String getTitle() { return title; }
    public String getBody() { return body; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBody(String body) {
        this.body = body;
    }
}