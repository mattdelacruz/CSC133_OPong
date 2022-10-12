package com.opongapp;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Parent;
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
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
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

abstract class PhysicsObject {

    Point2D startPos;
    Group root, transform;
    Translate position;
    Rotate rotation;

    public PhysicsObject(Group parent, Point2D p) {

            root = new Group();
            transform = new Group();
            update(p, 0);
            root.getChildren().add(transform);
            parent.getChildren().add(root);
            position = new Translate(p.getX(), p.getY());

    }

    public void update(Point2D pos, double degree) {

        position = new Translate(pos.getX(), pos.getY());
        rotation = new Rotate(Math.toDegrees(degree));
        transform.getTransforms().clear();
        transform.getTransforms().addAll(position, rotation);
        
    }

    public Translate getPosition() { return position; }
}

class Bounds extends PhysicsObject {

    Rectangle bound;

    public Bounds(Group parent, Point2D p, double w, double h) {
        super(parent, p);

        startPos = p;
        bound = new Rectangle(w, h);
        transform.getChildren().add(bound);
    }
}

class Bat extends PhysicsObject {

    Rectangle bat;
    Point2D startPos;
    Color color;
    double width, height; 

    public Bat(Group parent, Point2D p, Color c, double w, double h) {
        super(parent, p);
        
        startPos = p;
        width = w;
        height = h;
        color = c;
        bat = new Rectangle(width, height, color);
        transform.getChildren().add(bat);

    }

    public void handleMouseMove(MouseEvent e) {
        update(new Point2D(e.getX() - (bat.getWidth() / 2), 
                startPos.getY()), 0);
    }

    public double getWidth() { return bat.getWidth();}

    public double getHeight() { return bat.getHeight(); }

    public void setWidth(double w) { bat = new Rectangle(w, height, color); }

    public void setColor(Color c) { bat = new Rectangle(width, height, c); }
}

class Ball extends PhysicsObject {

    Rectangle ball;
    Point2D startPos;

    Ball(Group parent, Point2D p, Color c, double w, double h) {
        super(parent, p);

        startPos = p;
        ball = new Rectangle(w, h, c);
        transform.getChildren().add(ball);
    }

    double getSize() {return ball.getWidth(); }

}

class ScoreDisplay extends Pane {

    private static final int SCORE_FONT_SIZE = 25;
    private static final FontWeight SCORE_FONT_WEIGHT = FontWeight.BOLD;
    private static final Color SCORE_FONT_COLOR = Color.BLUE;

    ScoreDisplay(int displayW, int displayH) {  

        setTranslateX(displayW / 2 - SCORE_FONT_SIZE / 2);
        setTranslateY(displayH / 2 - SCORE_FONT_SIZE / 2);
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

class Pong {

    public static int UPDATE_TIME = 2500;
    public static int BASE_SPEED = 4;

    Bounds topBounds, leftBounds, rightBounds;
    Bat bat;
    Ball ball;
    ScoreDisplay scoreDisplay;
    GameTimer gameTimer;

    double gravity_x = 0;
    double gravity_y = BASE_SPEED;
    double batVelocity = 0;
    double angle = 0;
    int points = 0;

    Pong(int display_w, int display_h, Bat b, Ball ba,
            ScoreDisplay sd, GameTimer gt) {
        bat = b;
        ball = ba;
        scoreDisplay = sd;
        gameTimer = gt;

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
                double ft = (1 / frames) * 1000;

                if (now % UPDATE_TIME == 0) {

                    gameTimer.remove(0);
                    gameTimer.update(frames, "FPS (avg)", 0);
                    gameTimer.remove(1);
                    gameTimer.update(ft, "FT (ms avg)", 1);
                    gameTimer.remove(2);
                    gameTimer.update(elapsedTime, "GT (s)", 2);

                }

                if (now % (UPDATE_TIME / 2) == 0) {
                    lastBatPos = new Point2D(bat.getPosition().getX(), 
                            bat.getPosition().getY());
                    lastBallPos = new Point2D(ball.getPosition().getX(),
                           ball.getPosition().getY());
                    initTime = elapsedTime;
                }

                updateRotation();

                ball.update(new Point2D((ball.getPosition().getX() + gravity_x),
                        ball.getPosition().getY() + gravity_y), 0);

                stuckBall();
                batOutofBounds();
                

            }
            void updateRotation() {
                ball.update(new Point2D(ball.getPosition().getX(), 
                        ball.getPosition().getY()), rotation);
                
                if (bouncedBack) {
                    rotation += batVelocity;

                    if (batVelocity < 0) 
                        ball.update(new Point2D(ball.getPosition().getX(), 
                        ball.getPosition().getY()), rotation);
                    else
                        ball.update(new Point2D(ball.getPosition().getX(), 
                        ball.getPosition().getY()), -rotation);
                }
            }

            void stuckBall() {
                double ballH = ball.getPosition().getY() + ball.getSize();
                double ballW = ball.getPosition().getX() + ball.getSize();
                double batW = bat.getPosition().getX() + 
            }
        };

        
        loop.start();
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
        Group root = new Group();
        Group game = new Group();

        Point2D batStart = new Point2D(0, GAME_HEIGHT - BAT_HEIGHT);
        Point2D ballStart = new Point2D(r.nextInt(GAME_WIDTH), 0);

        Bat bat = new Bat(game, batStart, BAT_COLOR,
                 BAT_WIDTH, BAT_HEIGHT);

        Ball ball = new Ball(game, ballStart, BALL_COLOR,
                BALL_SIZE, BALL_SIZE);

        ScoreDisplay scoreDisplay = new ScoreDisplay(GAME_WIDTH, GAME_HEIGHT);
        GameTimer gameTimer = new GameTimer();

        game.getChildren().addAll(scoreDisplay, gameTimer);
        root.getChildren().add(game);

        scene = new Scene(root, GAME_WIDTH, GAME_HEIGHT);
        scene.setOnMouseMoved(e -> bat.handleMouseMove(e));
        scene.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.I) {
                gameTimer.show();
            }
        });

        Pong pong = new Pong(GAME_WIDTH, GAME_HEIGHT, bat, ball, scoreDisplay, gameTimer);
        pong.start();

        stage.setTitle("PONG!");
        stage.setResizable(false);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }

}