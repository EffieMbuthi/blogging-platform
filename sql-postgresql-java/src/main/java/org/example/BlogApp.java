package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import org.example.model.Post;
import org.example.service.PostService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogApp extends Application {

    private PostService postService = new PostService();
    private ListView<String> postListView = new ListView<>();
    private Map<String, Post> titleToPostMap = new HashMap<>();
    private int currentPage = 1;
    private final int PAGE_SIZE = 20;
    private Label pageLabel = new Label("Page 1");


    @Override
    public void start(Stage primaryStage) {
        TextField titleField = new TextField();
        titleField.setPromptText("Post title");

        TextField bodyField = new TextField();
        bodyField.setPromptText("Post body");

        TextField searchField= new TextField();
        searchField.setPromptText("Search by title..");

        Button createButton = new Button("Create Post");
        Button updateButton = new Button("Update Selected Post");
        Button deleteButton = new Button("Delete Selected");
        Label statusLabel = new Label();
        Button searchButton= new Button("Search");

        Button prevButton = new Button("Previous");
        Button nextButton = new Button("Next");

        loadPage();

        // Pre-fill fields when a post is selected
        postListView.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null) {
                Post post = titleToPostMap.get(newValue);
                titleField.setText(newValue);
                bodyField.setText(post.getBody());
            }
        });

        createButton.setOnAction(event -> {
            try {
                int authorId = 1;
                postService.createPost(authorId, titleField.getText(), bodyField.getText());
                statusLabel.setText("Post created successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        updateButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                postService.updatePost(postId, titleField.getText(), bodyField.getText());
                statusLabel.setText("Post updated successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        searchButton.setOnAction(event -> {
            try {
                List<Post> results = postService.searchPosts(searchField.getText());
                postListView.getItems().clear();
                titleToPostMap.clear();
                for (Post post : results) {
                    postListView.getItems().add(post.getTitle());
                    titleToPostMap.put(post.getTitle(), post);
                }
            } catch (SQLException e) {
                statusLabel.setText("Search error: " + e.getMessage());
            }
        });

        //listening to text field (#live filtering)
        searchField.textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                List<Post> results = postService.searchPosts(newValue);
                postListView.getItems().clear();
                titleToPostMap.clear();
                for (Post post : results) {
                    postListView.getItems().add(post.getTitle());
                    titleToPostMap.put(post.getTitle(), post);
                }
            } catch (SQLException e) {
                statusLabel.setText("Search error: " + e.getMessage());
            }
        });

        nextButton.setOnAction(event -> {
            currentPage++;
            loadPage();
        });

        prevButton.setOnAction(event -> {
            if (currentPage > 1) {
                currentPage--;
                loadPage();
            }
        });

        deleteButton.setOnAction(event -> {
            String selectedTitle = postListView.getSelectionModel().getSelectedItem();
            if (selectedTitle == null) {
                statusLabel.setText("Select a post first.");
                return;
            }
            try {
                int postId = titleToPostMap.get(selectedTitle).getId();
                postService.deletePost(postId);
                statusLabel.setText("Post deleted successfully.");
                titleField.clear();
                bodyField.clear();
                loadPage();
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        HBox searchRow = new HBox(10, searchField, searchButton);
        HBox buttonRow = new HBox(10, createButton, updateButton, deleteButton);
        HBox paginationRow = new HBox(10, prevButton, pageLabel, nextButton);
        VBox root = new VBox(10, searchRow, postListView, paginationRow, titleField, bodyField, buttonRow, statusLabel);
        Scene scene = new Scene(root, 550, 500);

        primaryStage.setTitle("Blogging Platform (PostgreSQL)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

             // for displaying everything in the list
//    private void refreshPostList() {
//        postListView.getItems().clear();
//        titleToPostMap.clear();
//        try {
//            List<Post> posts = postService.getAllPosts();
//            for (Post post : posts) {
//                postListView.getItems().add(post.getTitle());
//                titleToPostMap.put(post.getTitle(), post);
//            }
//        } catch (SQLException e) {
//            postListView.getItems().add("Error loading posts: " + e.getMessage());
//        }
//    }


    private void loadPage() {
        postListView.getItems().clear();
        titleToPostMap.clear();
        try {
            List<Post> posts = postService.getPostsPage(currentPage, PAGE_SIZE);
            for (Post post : posts) {
                postListView.getItems().add(post.getTitle());
                titleToPostMap.put(post.getTitle(), post);
            }
            pageLabel.setText("Page " + currentPage);
        } catch (SQLException e) {
            postListView.getItems().add("Error loading posts: " + e.getMessage());
        }
    }


    public static void main(String[] args) {
        launch(args);
    }
}