package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.bson.Document;
import org.bson.types.ObjectId;

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
                ObjectId authorId = new ObjectId("6a6dcb285515732ba4b4c7ac"); // Effie, hardcoded for now
                postService.createPost(authorId, titleField.getText(), bodyField.getText());
                statusLabel.setText("Post created successfully.");
                titleField.clear();
                bodyField.clear();
                refreshPostList();
            } catch (IllegalArgumentException e) {
                statusLabel.setText("Error: " + e.getMessage());
            }
        });

        VBox root = new VBox(10, postListView, titleField, bodyField, createButton, statusLabel);
        Scene scene = new Scene(root, 500, 500);

        primaryStage.setTitle("Blogging Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void refreshPostList() {
        postListView.getItems().clear();
        for (Document post : postService.getAllPosts()) {
            postListView.getItems().add(post.getString("title"));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}