package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import java.sql.ResultSet;
import java.sql.SQLException;

public class BlogApp extends Application {

    private PostService postService = new PostService();
    private ListView<String> postListView = new ListView<>();

    @Override
    public void start(Stage primaryStage) {
        refreshPostList();

        TextField titleField = new TextField();
        titleField.setPromptText("Post title");

        TextField bodyField = new TextField();
        bodyField.setPromptText("Post body");

        Button createButton = new Button("Create Post");
        Label statusLabel = new Label();

        createButton.setOnAction(event -> {
            try {
                int authorId = 1; // Effie, hardcoded for now
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

        VBox root = new VBox(10, postListView, titleField, bodyField, createButton, statusLabel);
        Scene scene = new Scene(root, 500, 500);

        primaryStage.setTitle("Blogging Platform (PostgreSQL)");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshPostList() {
        postListView.getItems().clear();
        try {
            ResultSet rs = postService.getAllPosts();
            while (rs.next()) {
                postListView.getItems().add(rs.getString("title"));
            }
        } catch (SQLException e) {
            postListView.getItems().add("Error loading posts: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}