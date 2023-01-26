package com.opongapp;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.AnimationTimer;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

class Pong extends Group {

    public static final int GAME_HEIGHT = 600;
    public static final int GAME_WIDTH = 400;
    public static final int BAT_WIDTH = GAME_WIDTH / 3;
    public static final int BAT_HEIGHT = GAME_HEIGHT / 25;
    public static final int BALL_SIZE = GAME_WIDTH / 10;
    public static final int BASE_SPEED = 4;
    public static final int BASE_ANGLE = 5;
    public static final int UPDATE_TIME = 1500;
    public static final int BAT_REDUCTION_VALUE = BAT_WIDTH / 50;
    public static final int MIN_BAT_WIDTH = BAT_REDUCTION_VALUE;
    public static final Color BAT_COLOR = Color.TEAL;
    public static final Color BALL_COLOR = Color.BLUE;

    Ball ball;
    Bat bat;
    Bounds topBounds, leftBounds, rightBounds;

    GameTimer gameTimer = new GameTimer();
    ScoreDisplay scoreDisplay = new ScoreDisplay(GAME_WIDTH, GAME_WIDTH);
    Random r = new Random();
    BinkBonkSound sound = new BinkBonkSound();
    ArrayList<Double> fTimesAvg = new ArrayList<Double>();
    ArrayList<Double> frameAvg = new ArrayList<Double>();

    boolean fpsPaneFlag = false;

    double gravityX = 0;
    double gravityY = 0;
    double angle = 0;
    int points = 0;

    public Pong() {
        ball = new Ball(new Point2D(r.nextInt(GAME_WIDTH - BALL_SIZE), 0),
                BALL_SIZE, BALL_SIZE, BALL_COLOR, BASE_SPEED);
        bat = new Bat(new Point2D(0, GAME_HEIGHT - BAT_HEIGHT),
                BAT_WIDTH, BAT_HEIGHT, BAT_COLOR);

        topBounds = new Bounds(new Point2D(0, 0), GAME_WIDTH, 0);
        leftBounds = new Bounds(new Point2D(0, 0), 0, GAME_HEIGHT);
        rightBounds = new Bounds(new Point2D(GAME_WIDTH, 0), 0, GAME_HEIGHT);

        gravityY = ball.getVelocity();

        getChildren().addAll(ball, bat, scoreDisplay, gameTimer);
        startAnimation();
    }

    public void startAnimation() {
        AnimationTimer loop = new AnimationTimer() {
            double old = -1;
            double elapsedTime = 0;
            double speed = 0;
            double acceleration = 0.1;
            double initTime = 0;
            double rotationValue = 5;
            double ft = 0;
            int difficulty = 5;
            boolean bouncedBack = false;
            int rotation = 0;
            Point2D lastBatPos = new Point2D(0, 0);
            Point2D lastBallPos = new Point2D(0, 0);

            @Override
            public void handle(long now) {

                updateGameTimerValues(now);
                updateGameTimer(now);
                updateLastPos(now);
                updateRotation();
                updateBallPos();

                stuckBall();
                batOutOfBounds();

                if (collisionDetection(ball, bat)) {
                    bat.updateVelocity(lastBatPos, elapsedTime, initTime, ft);
                    bounceBat(lastBallPos, speed);
                    gravityX *= -1;
                    gravityY *= -1;
                }

                if (collisionDetection(ball, topBounds)) {
                    bounceWall(lastBallPos, speed);
                    gravityX *= -1;
                    gravityY *= -1;
                }

                if (collisionDetection(ball, leftBounds) ||
                        collisionDetection(ball, rightBounds)) {
                    bounceWall(lastBallPos, speed);
                    rotation += rotationValue;
                }

                if (ball.getTranslateY() + ball.getHeight() >= GAME_HEIGHT ||
                        ball.getTranslateY() + ball.getHeight() < 0) {
                    ballHasFallen();
                }
            }

            private void updateBallPos() {
                ball.setTranslateX(ball.getTranslateX() + gravityX);
                ball.setTranslateY(ball.getTranslateY() + gravityY);
            }

            private void updateLastPos(long now) {
                if (now % (UPDATE_TIME / 2) == 0) {
                    lastBatPos = new Point2D(bat.getTranslateX(),
                            bat.getTranslateY());
                    lastBallPos = new Point2D(ball.getTranslateX(),
                            ball.getTranslateY());
                    initTime = elapsedTime;
                }
            }

            private void updateGameTimerValues(long now) {
                if (old < 0)
                    old = now;
                double delta = (now - old) / 1e9;

                old = now;
                elapsedTime += delta;

                double frames = 1 / delta;
                ft = (1 / frames) * 1000;

                fTimesAvg.add(ft);
                frameAvg.add(frames);

            }

            private void updateGameTimer(long now) {
                if (now % UPDATE_TIME == 0) {

                    int i = 0;
                    double ftAdder = 0;
                    double fAdder = 0;

                    for (Double t : fTimesAvg) {
                        ftAdder += fTimesAvg.get(i);
                        i++;
                    }

                    i = 0;
                    for (Double t : frameAvg) {
                        fAdder += frameAvg.get(i);
                        i++;
                    }

                    ftAdder /= fTimesAvg.size();
                    fAdder /= frameAvg.size();

                    updateGameTimerDescription(fAdder, ftAdder, elapsedTime);

                    fTimesAvg.clear();
                    frameAvg.clear();
                }
            }

            private void updateGameTimerDescription(double frameAvg,
                    double frameTimeAvg, double elapsedTime) {
                updateFpsDescription(String.format(
                        String.format("%.2f FPS (avg)", frameAvg)), 0);
                updateFpsDescription(String.format(
                        String.format("FT=%.2f (ms avg)", frameTimeAvg)), 1);
                updateFpsDescription(String.format(
                        String.format("GT=%.2f (s)", elapsedTime)), 2);
            }

            private void batOutOfBounds() {
                if (collide(bat, rightBounds) || collide(bat, leftBounds)) {
                    bat.setFill(Color.RED);
                } else {
                    bat.setFill(BAT_COLOR);
                }
            }

            private boolean collisionDetection(Rectangle a, Rectangle b) {
                if (collide(a, b)) {
                    sound.play(true);
                    pointScore();
                    return true;
                }
                return false;
            }

            private void pointScore() {
                if (points % difficulty == 0)
                    shrinkBat();

                bouncedBack = true;
                points++;
                speed += acceleration;
                rotation += rotationValue;
                addNewScore(points);
                bounceWall(lastBallPos, speed);
            }

            private void updateRotation() {
                ball.setRotate(rotation);

                if (bouncedBack) {
                    rotation += bat.getVelocity();

                    if (bat.getVelocity() < 0)
                        ball.setRotate(rotation);
                    else
                        ball.setRotate(-rotation);
                }
            }

            private void shrinkBat() {
                if (bat.getWidth() > MIN_BAT_WIDTH)
                    bat.setWidth(bat.getWidth() - BAT_REDUCTION_VALUE);
                else {
                    bat.setWidth(MIN_BAT_WIDTH);
                }
            }

            private void stuckBall() {
                double ballH = ball.getTranslateY() + ball.getHeight();
                double ballW = ball.getTranslateX() + ball.getWidth();
                double batW = bat.getTranslateX() + bat.getWidth();
                double batH = bat.getTranslateY() + bat.getHeight();

                if (ballH > bat.getTranslateY() &&
                        ball.getTranslateY() < batH &&
                        ballW > bat.getTranslateX() &&
                        ball.getTranslateX() < batW) {
                    ball.setTranslateY(GAME_HEIGHT);
                }
            }

            private void ballHasFallen() {
                bouncedBack = false;
                points = 0;
                speed = 0;
                bat.setWidth(BAT_WIDTH);
                rotation = 0;
                addNewScore(points);
                gravityX = 0;
                gravityY = ball.getVelocity();
                ball.setTranslateX(r.nextInt(GAME_WIDTH - BALL_SIZE));
                ball.setTranslateY(0);

            }

            private void bounceWall(Point2D lastPos, double speed) {
                if (lastPos.getX() > ball.getTranslateX()) {
                    gravityX = Math.cos(Math.toRadians(BASE_ANGLE));
                    gravityX += (ball.getVelocity() + speed);
                } else {
                    gravityX = -Math.cos(Math.toRadians(BASE_ANGLE));
                    gravityX += (-ball.getVelocity() + (-speed));
                }
            }

            private void bounceBat(Point2D lastPos, double speed) {
                if (bat.getVelocity() == 0) {
                    gravityX = Math.cos(Math.toRadians(90));
                    gravityY += (speed);
                } else if (bat.getVelocity() < 0) {
                    gravityX = Math.cos(Math.toRadians(BASE_ANGLE));
                    gravityX += (ball.getVelocity() + speed);
                } else {
                    gravityX = -Math.cos(Math.toRadians(BASE_ANGLE));
                    gravityX += (-ball.getVelocity() + (-speed));
                }
            }

            private boolean collide(Rectangle a, Rectangle b) {
                if (a.getTranslateY() + a.getHeight() >= b.getTranslateY() &&
                        a.getTranslateY() <= b.getTranslateY() + b.getHeight() &&
                        a.getTranslateX() + a.getWidth() >= b.getTranslateX() &&
                        a.getTranslateX() <= b.getTranslateX() + b.getWidth()) {
                    return true;
                }
                return false;
            }

            private void addNewScore(int points) {
                scoreDisplay.update(points);
            }

            private void updateFpsDescription(String description, int pos) {
                gameTimer.update(description, pos);
            }
        };
        loop.start();
    }

    void handleMouseMove(MouseEvent e) {
        bat.setTranslateX(e.getX() - (bat.getWidth() / 2));
    }

    void handleFpsPaneShow() {
        gameTimer.show();
    }

    void handleSound() {
        sound.toggleSound();
    }
}