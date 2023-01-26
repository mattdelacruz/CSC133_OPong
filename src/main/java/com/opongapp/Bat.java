package com.opongapp;

import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Bat extends Rectangle {
    double batVelocity;

    Bat(Point2D pos, int width, int height, Color color) {
        super(width, height, color);
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
        batVelocity = 0;
    }

    void updateVelocity(Point2D lastBatPos, double elapsedTime,
            double initTime, double ft) {
        batVelocity = (getTranslateX() - lastBatPos.getX()) /
                ((elapsedTime + 1) - initTime);
    }

    double getVelocity() {
        return batVelocity;
    }
}