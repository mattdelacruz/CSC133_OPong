package com.opongapp;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javax.sound.midi.*;
import java.io.IOException;
import java.util.Random;

class BinkBonkSound {

    private static final int MAX_PITCH_BEND = 16383;
    private static final int MIN_PITCH_BEND = 0;
    private static final int REVERB_LEVEL_CONTROLLER = 91;
    private static final int MIN_REVERB_LEVEL = 0;
    private static final int DRUM_MIDI_CHANNEL = 9;
    private static final int CLAVES_NOTE = 76;
    private static final int NORMAL_VELOCITY = 100;

    Instrument[] instrument;
    MidiChannel[] midiChannels;
    boolean playSound;

    public BinkBonkSound(){
        playSound=true;
        try{
            Synthesizer gmSynthesizer = MidiSystem.getSynthesizer();
            gmSynthesizer.open();
            instrument = gmSynthesizer.getDefaultSoundbank().getInstruments();
            midiChannels = gmSynthesizer.getChannels();

        } catch (MidiUnavailableException e) {
            e.printStackTrace();
        }
    }

    void play(boolean hiPitch){
        if(playSound) {

            midiChannels[DRUM_MIDI_CHANNEL]
                    .setPitchBend(hiPitch ? MAX_PITCH_BEND : MIN_PITCH_BEND);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .controlChange(REVERB_LEVEL_CONTROLLER, MIN_REVERB_LEVEL);

            midiChannels[DRUM_MIDI_CHANNEL]
                    .noteOn(CLAVES_NOTE, NORMAL_VELOCITY);
        }
    }

    public void toggleSound() {
        playSound = !playSound;
    }
}

class Bounds extends Rectangle {

    public Bounds(Point2D p, double w, double h) {
        
        new Rectangle(p.getX(), p.getY(), w, h);
    }
}

class Bat extends Rectangle {

    Rectangle bat;

    public Bat(Point2D p, Color c, double w, double h) {
        
        bat = new Rectangle(w, h);
        bat.setFill(c);
        bat.setTranslateX(p.getX());
        bat.setTranslateY(p.getY());

    }

    public void handleMouseMove(MouseEvent e) {
        bat.setTranslateX(e.getX() - (getWidth() / 2)); 
    }
}

class Ball extends Rectangle {

    Rectangle ball;

    Ball(Point2D p, Color c, double w, double h) {

        ball = new Rectangle(w, h);
        ball.setFill(c);
        ball.setTranslateX(p.getX());
        ball.setTranslateY(p.getY());
    }

}

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

        Label l = createLabel(Integer.toString(score));
        getChildren().add(l);
    
    }

    Label createLabel(String score) {

        Label l = new Label(score);
        l.setTextFill(SCORE_FONT_COLOR);
        l.setFont(Font.font("Arial", SCORE_FONT_WEIGHT, SCORE_FONT_SIZE));
        return l;
    }
}

class GameTimer extends HBox {

    private static final Color FPS_FONT_COLOR = Color.RED;
    private static final FontWeight FPS_FONT_WEIGHT = FontWeight.NORMAL;
    private static final int FPS_FONT_SIZE = 15;
    private static final Insets MARGINS = new Insets(0, 5, 0,0);
 
    boolean showFlag = false;

    Label l1, l2, l3;
    

    GameTimer() {
        l1 = new Label("");
        l2 = new Label("");
        l3 = new Label("");
        setTranslateX(0);
        setTranslateY(-1000);
        getChildren().addAll(l1, l2, l3);
    }

    void update(double info, String desc, int pos) {

        String s = String.format(String.format("%.2f ", info) + desc);
        Label label = createLabel(s);
        Pane pane = new Pane(label);
        getChildren().add(pos, pane);
        setMargin(getChildren().get(pos), MARGINS);
    }

    void remove(int pos) {
        getChildren().remove(pos);
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

class Pong extends Group{

    public static int UPDATE_TIME = 2500;
    public static int BASE_SPEED = 4;
    public static int BASE_ANGLE = 5;
    public static int MIN_BAT_WIDTH = 5;
    public static int BAT_REDUCTION_VALUE = 5;
    public static Color BAT_COLOR = Color.TEAL;

    Bounds topBounds, leftBounds, rightBounds;
    Bat bat;
    Ball ball;
    ScoreDisplay scoreDisplay;
    GameTimer gameTimer;
    BinkBonkSound sound = new BinkBonkSound();
    Random r = new Random();

    double gravity_x = 0;
    double gravity_y = BASE_SPEED;
    double batVelocity = 0;
    double angle = 0;
    int points = 0;
    int display_w = 0;
    int display_h = 0;

    Pong(int d_w, int d_h, Bat b, Ball ba,
            ScoreDisplay sd, GameTimer gt, Bounds tBounds,
            Bounds lBounds, Bounds rBounds) {
        bat = b;
        ball = ba;
        scoreDisplay = sd;
        gameTimer = gt;
        display_w = d_w;
        display_h = d_h;
        topBounds = tBounds;
        leftBounds = lBounds;
        rightBounds = rBounds;
        this.getChildren().addAll(bat, ball, scoreDisplay, gameTimer);
        start();
    }

    void start() {
        AnimationTimer loop = new AnimationTimer() {

            double old = -1;
            double elapsedTime = 0;
            double speed = 0;
            double accleration = 0.1;
            double initTime = 0;
            double rotationValue = 5;
            int difficulty = 5;
            boolean bouncedBack = false;
            double ft = 0;
            int rotation = 0;
            Point2D lastBatPos = new Point2D(0,0);
            Point2D lastBallPos = new Point2D(0,0);

            @Override
            public void handle(long now) {
                if (old < 0) 
                    old = now;
                double delta = (now - old) / 1e9;

                old = now;
                elapsedTime += delta;

                double frames = 1 / delta;
                ft = (1 / frames) * 1000;

                if (now % UPDATE_TIME == 0) {

                    gameTimer.remove(0);
                    gameTimer.update(frames, "FPS (avg)", 0);
                    gameTimer.remove(1);
                    gameTimer.update(ft, "FT (ms avg)", 1);
                    gameTimer.remove(2);
                    gameTimer.update(elapsedTime, "GT (s)", 2);
                }

                if (now % (UPDATE_TIME / 2) == 0) {
                    lastBatPos = new Point2D(bat.getTranslateX(), 
                            bat.getTranslateY());
                    lastBallPos = new Point2D(ball.getTranslateX(),
                           ball.getTranslateY());
                    initTime = elapsedTime;
                }

                updateRotation();

                ball.setTranslateX(ball.getTranslateX() + gravity_x);
                ball.setTranslateY(ball.getTranslateY() + gravity_y);

                stuckBall();
                batOutofBounds();
                
                if (collisionDetection(ball, bat)) {
                    System.out.println("touched bat!");
                    updateVelocity();
                    bounceBat(lastBallPos, speed);
                    gravity_x *= -1;
                    gravity_y *= -1;
                }

                if (collisionDetection(ball, topBounds)) {
                    bounceWall(lastBallPos, speed);
                    gravity_x *= -1;
                    gravity_y *= -1;
                }

                if (collisionDetection(ball, leftBounds) || 
                collisionDetection(ball,rightBounds)) {
                    bounceWall(lastBallPos, speed);
                    rotation += rotationValue;
                }

                if (ball.getTranslateY() >= display_h) {
                    System.out.println("fallen");
                    ballHasFallen();
                }
            }

            private void ballHasFallen() {
                bouncedBack = false;
                points = 0;
                speed = 0;
                bat.setWidth(display_w / 3);
                rotation = 0;
                addNewScore(points);
                gravity_x = 0;
                gravity_y = BASE_SPEED;
                ball.setTranslateX(r.nextInt(display_w));
                ball.setTranslateY(0);
            }

            private void addNewScore(int points) {

                scoreDisplay.update(points);

            }

            private void bounceWall(Point2D lastPos, double speed) {
                if (lastPos.getX() > ball.getTranslateX()) {
                    gravity_x = Math.cos(Math.toRadians(BASE_ANGLE));
                    gravity_x += (BASE_SPEED + speed);
                } else {
                    gravity_x = -Math.cos(Math.toRadians(BASE_ANGLE));
                    gravity_x += (-BASE_SPEED + (-speed));
                }
            }

            private void bounceBat(Point2D lastBallPos, double speed) {
                
                if (batVelocity == 0) {
                    gravity_x = Math.cos(Math.toRadians(90));
                    gravity_y += (BASE_SPEED + speed);
                } else if (batVelocity < 0) {
                    gravity_x = Math.cos(Math.toRadians(BASE_ANGLE));
                    gravity_x += (BASE_SPEED + speed);

                } else {
                    gravity_x = -Math.cos(Math.toRadians(BASE_ANGLE));
                    gravity_x = (-BASE_SPEED + (-speed));
                }
            }

            public void shrinkBat() {
                if (bat.getWidth() > MIN_BAT_WIDTH)
                    bat.setWidth(bat.getWidth() - BAT_REDUCTION_VALUE);
                else {
                    bat.setWidth(MIN_BAT_WIDTH);
                }
            }

            private void updateVelocity() {
                batVelocity = (bat.getTranslateX() - lastBatPos.getX()) / 
                            ((elapsedTime + 1) - initTime);
                batVelocity *= ft;
                
            }

            private boolean collisionDetection(Rectangle o1, Rectangle o2) {
                if (collide(o1, o2)) {
                    scoredAPoint();
                    sound.play(true);
                    return true;
                }
                return false;
            }

            private void scoredAPoint() {
                if (points % difficulty == 0) 
                    shrinkBat();

                bouncedBack = true;
                points++;
                speed+= accleration;
                rotation += rotationValue;
                addNewScore(points);
                bounceWall(lastBallPos, speed);

            }

            private boolean collide(Rectangle o1, Rectangle o2) {
                if (o1.getTranslateY() + o1.getHeight() >= o2.getTranslateY() &&
                o1.getTranslateY() <= o2.getTranslateY() + o2.getHeight() &&
                o1.getTranslateX() + o1.getWidth() >= o2.getTranslateX() &&
                o1.getTranslateX() <= o2.getTranslateX() + o2.getWidth()) {
                    return true;
                }

                return false;

            }
            private void batOutofBounds() {
                if (collide(bat, rightBounds) || collide(bat,leftBounds)) {
                    bat.setFill(Color.RED);
                } else {
                    bat.setFill(BAT_COLOR);
                }

            }

            void updateRotation() {
                ball.setRotate(rotation);
                
                if (bouncedBack) {
                    rotation += batVelocity;

                    if (batVelocity < 0) 
                        ball.setRotate(rotation);
                    else
                        ball.setRotate(-rotation);
                }
            }

            void stuckBall() {
                double ballH = ball.getTranslateY() + ball.getHeight();
                double ballW = ball.getTranslateX() + ball.getWidth();
                double batW = bat.getTranslateX() + bat.getWidth();
                double batH = bat.getTranslateY() + bat.getHeight();

                if (ballH > bat.getTranslateY() &&
                ballH < batH &&
                ballW > bat.getTranslateX() &&
                ballW < batW) {
                    ball.setTranslateX(ball.getTranslateX());
                    ball.setTranslateY(ballH - bat.getHeight());
                }
            }
        };
        loop.start();
        
    }

    public void handleMouseMove(MouseEvent e) {
        bat.handleMouseMove(e);
    }

}

public class OPongApp extends Application {

    private static Scene scene;
    private static final int GAME_HEIGHT = 600;
    private static final int GAME_WIDTH = 400;
    private static final int BAT_WIDTH = GAME_WIDTH / 3;
    private static final int BAT_HEIGHT = GAME_HEIGHT / 25;
    private static final int BALL_SIZE = 50;

    private static final Color BAT_COLOR = Color.TEAL;
    private static final Color BALL_COLOR = Color.BLUE;

    @Override
    public void start(Stage stage) throws IOException {

        Random r = new Random();
        Stage primaryStage = new Stage();

        Point2D batStart = new Point2D(0, GAME_HEIGHT - BAT_HEIGHT);
        Point2D ballStart = new Point2D(r.nextInt(GAME_WIDTH), 0);

        Bat bat = new Bat(batStart, BAT_COLOR,
                 BAT_WIDTH, BAT_HEIGHT);

        Ball ball = new Ball(ballStart, BALL_COLOR,
                BALL_SIZE, BALL_SIZE);
        Bounds topBounds = new Bounds(new Point2D(0,0), GAME_WIDTH, 0);
        Bounds rightBounds = new Bounds(new Point2D(0, 0), 0, GAME_HEIGHT);
        rightBounds.setTranslateX(GAME_WIDTH);
        Bounds leftBounds = new Bounds(new Point2D(0,0), 0, GAME_HEIGHT);
        ScoreDisplay scoreDisplay = new ScoreDisplay(GAME_WIDTH, GAME_HEIGHT);
        GameTimer gameTimer = new GameTimer();


        Pong root = new Pong(GAME_WIDTH, GAME_HEIGHT, bat, ball, scoreDisplay, gameTimer, topBounds, leftBounds, rightBounds);
        scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        primaryStage.setScene(scene);

        scene.setOnMouseMoved(e -> root.handleMouseMove(e));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) {
                gameTimer.show();
            }
        });

        primaryStage.setTitle("PONG!");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}