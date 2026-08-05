package org.example;

import org.bson.Document;
import org.bson.types.ObjectId;
import com.mongodb.client.FindIterable;

public class PostService {
    private PostDAO postDAO;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public void createPost(ObjectId authorId, String title, String body) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        postDAO.createPost(authorId, title, body);
    }

    public FindIterable<Document> getAllPosts() {
        return postDAO.findAllPosts();
    }

    public Document getPostById(ObjectId id) {
        return postDAO.findPostById(id);
    }

    public void updatePostTitle(ObjectId postId, String newTitle) {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        postDAO.updatePostTitle(postId, newTitle);
    }

    public void deletePost(ObjectId postId) {
        postDAO.deletePost(postId);
    }
}