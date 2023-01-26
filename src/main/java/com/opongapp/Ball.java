package com.opongapp;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Ball extends Rectangle {
    int baseVelocity;

    Ball(Point2D pos, int width, int height, Color color, int v) {
        super(width, height, color);
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
        baseVelocity = v;
    }

    int getVelocity() {
        return baseVelocity;
    }
}