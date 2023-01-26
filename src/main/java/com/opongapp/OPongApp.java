package com.opongapp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.stage.Stage;
import java.io.IOException;

import javax.sound.midi.*;

public class OPongApp extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        Pong root = new Pong();
        scene = new Scene(root, Pong.GAME_WIDTH, Pong.GAME_HEIGHT);
        stage.setScene(scene);

        scene.setOnMouseMoved(e -> root.handleMouseMove(e));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) {
                root.handleFpsPaneShow();
            } else if (e.getCode() == KeyCode.S) {
                root.handleSound();
            }
        });

        stage.setTitle("PONG!");
        stage.setResizable(false);
        stage.show();

    }

    public static void main(String[] args) {
        launch();
    }
}