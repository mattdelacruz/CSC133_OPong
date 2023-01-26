package com.opongapp;

import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class ScoreDisplay extends Pane {

    private static final int SCORE_FONT_SIZE = 25;
    private static final FontWeight SCORE_FONT_WEIGHT = FontWeight.BOLD;
    private static final Color SCORE_FONT_COLOR = Color.BLUE;

    ScoreDisplay(int displayW, int displayH) {
        setTranslateX(displayW / 2 - SCORE_FONT_SIZE / 2);
        setTranslateY(displayH / 2 - SCORE_FONT_SIZE / 2);
        update(0);
    }

    void update(int score) {
        if (!getChildren().isEmpty())
            getChildren().remove(0);

        getChildren().add(createLabel(Integer.toString(score)));

    }

    Label createLabel(String score) {
        Label l = new Label(score);
        l.setTextFill(SCORE_FONT_COLOR);
        l.setFont(Font.font("Arial", SCORE_FONT_WEIGHT, SCORE_FONT_SIZE));
        return l;
    }
}