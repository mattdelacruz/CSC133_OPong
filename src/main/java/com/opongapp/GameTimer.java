package com.opongapp;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

class GameTimer extends HBox {

    private static final Color FPS_FONT_COLOR = Color.RED;
    private static final FontWeight FPS_FONT_WEIGHT = FontWeight.NORMAL;
    private static final int FPS_FONT_SIZE = 15;
    private static final Insets MARGINS = new Insets(0, 5, 0, 0);

    boolean showFlag = false;

    GameTimer() {
        Label l1, l2, l3;
        l1 = new Label();
        l2 = new Label();
        l3 = new Label();

        setTranslateX(0);
        setTranslateY(-1000);
        getChildren().addAll(l1, l2, l3);
    }

    void update(String desc, int pos) {

        getChildren().remove(pos);

        Pane pane = new Pane(createLabel(desc));

        getChildren().add(pos, pane);
        setMargin(getChildren().get(pos), MARGINS);
    }

    Label createLabel(String s) {
        Label l = new Label(s);
        l.setTextFill(FPS_FONT_COLOR);
        l.setFont(Font.font("Arial", FPS_FONT_WEIGHT, FPS_FONT_SIZE));
        return l;
    }

    void show() {
        if (showFlag) {
            setTranslateY(-1000);
            showFlag = false;
        } else {
            setTranslateY(0);
            showFlag = true;
        }
    }
}