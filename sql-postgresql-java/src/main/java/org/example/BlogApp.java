package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlogApp extends Application {

    private PostService postService = new PostService();
    private ListView<String> postListView = new ListView<>();
    private Map<String, Post> titleToPostMap = new HashMap<>();

    @Override
    public void start(Stage primaryStage) {
        TextField titleField = new TextField();
        titleField.setPromptText("Post title");

        TextField bodyField = new TextField();
        bodyField.setPromptText("Post body");

        Button createButton = new Button("Create Post");
        Button updateButton = new Button("Update Selected Post");
        Button deleteButton = new Button("Delete Selected");
        Label statusLabel = new Label();

        refreshPostList();

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
                refreshPostList();
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
                refreshPostList();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
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
                refreshPostList();
            } catch (SQLException e) {
                statusLabel.setText("Database error: " + e.getMessage());
            }
        });

        HBox buttonRow = new HBox(10, createButton, updateButton, deleteButton);
        VBox root = new VBox(10, postListView, titleField, bodyField, buttonRow, statusLabel);
        Scene scene = new Scene(root, 550, 500);

        primaryStage.setTitle("Blogging Platform (PostgreSQL)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshPostList() {
        postListView.getItems().clear();
        titleToPostMap.clear();
        try {
            List<Post> posts = postService.getAllPosts();
            for (Post post : posts) {
                postListView.getItems().add(post.getTitle());
                titleToPostMap.put(post.getTitle(), post);
            }
        } catch (SQLException e) {
            postListView.getItems().add("Error loading posts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}