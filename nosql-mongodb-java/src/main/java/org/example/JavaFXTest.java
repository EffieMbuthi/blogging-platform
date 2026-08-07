package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class JavaFXTest extends Application {

    @Override
    public void start(Stage primaryStage) {
        Label message = new Label("JavaFX is working!");
        StackPane root = new StackPane(message);
        Scene scene = new Scene(root, 400, 300);

        primaryStage.setTitle("JavaFX Test");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}