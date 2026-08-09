package org.example.service;

import org.example.dao.PostDAO;
import org.example.model.Post;

import java.sql.SQLException;
import java.util.List;

public class PostService {
    private PostDAO postDAO;
    private List<Post> cachedPosts= null;

    public PostService() {
        this.postDAO = new PostDAO();
    }

    public void createPost(int authorId, String title, String body) throws SQLException {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (body == null || body.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        int newId= postDAO.createPost(authorId, title, body);
        Post newPost= new Post(newId, authorId, title, body);
        if(cachedPosts!=null){
            cachedPosts.add(newPost);
        }

    }

    public List<Post> getAllPosts() throws SQLException {
        if (cachedPosts==null){
            cachedPosts= postDAO.findAllPosts();
        }
        return cachedPosts;

    }

    public void updatePost(int postId, String newTitle, String newBody) throws SQLException {
        if (newTitle == null || newTitle.isBlank()) {
            throw new IllegalArgumentException("Title cannot be empty.");
        }
        if (newBody == null || newBody.isBlank()) {
            throw new IllegalArgumentException("Body cannot be empty.");
        }
        postDAO.updatePost(postId, newTitle, newBody);
        if (cachedPosts != null) {
            for (Post post : cachedPosts) {
                if (post.getId() == postId) {
                    post.setTitle(newTitle);
                    post.setBody(newBody);
                }
            }
        }
    }

    public void deletePost(int postId) throws SQLException {
        postDAO.deletePost(postId);
        if (cachedPosts!=null){
            cachedPosts.removeIf(post->post.getId()==postId);
        }
    }

    public List<Post> getPostsPage(int pageNumber, int pageSize) throws SQLException {
        return postDAO.findPostsPaginated(pageNumber, pageSize);
    }

    public List<Post> searchPosts(String keyword) throws SQLException {
        if (keyword == null || keyword.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByTitle(keyword);
    }

    public List<Post> searchPostsByAuthor(String authorName) throws SQLException {
        if (authorName == null || authorName.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByAuthor(authorName);
    }

    public List<Post> searchPostsByTag(String tagName) throws SQLException {
        if (tagName == null || tagName.isBlank()) {
            return getAllPosts();
        }
        return postDAO.searchPostsByTag(tagName);
    }
}