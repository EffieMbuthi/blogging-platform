package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.layout.StackPane;
import org.bson.Document;

public class BlogApp extends Application {

    private PostService postService = new PostService();

    @Override
    public void start(Stage primaryStage) {
        ListView<String> postListView = new ListView<>();

        for (Document post : postService.getAllPosts()) {
            postListView.getItems().add(post.getString("title"));
        }

        StackPane root = new StackPane(postListView);
        Scene scene = new Scene(root, 500, 400);

        primaryStage.setTitle("Blogging Platform");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}