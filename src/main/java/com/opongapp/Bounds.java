package com.opongapp;

import javafx.geometry.Point2D;
import javafx.scene.shape.Rectangle;

class Bounds extends Rectangle {
    Bounds(Point2D pos, int width, int height) {
        super(width, height);
        setTranslateX(pos.getX());
        setTranslateY(pos.getY());
    }
}