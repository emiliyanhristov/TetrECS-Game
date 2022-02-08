package uk.ac.soton.comp1206.scene;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.HashSet;
import java.util.Random;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the game.
 */
public class ChallengeScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    protected Random random = new Random();
    protected Game game;
    protected GameBoard board;
    protected PieceBoard currentPiece;
    protected PieceBoard nextPiece;
    protected StackPane timer;
    protected Rectangle timerBar;
    protected int coordX = 0;
    protected int coordY = 0;
    protected boolean chatPermission = false;

    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty highscore = new SimpleIntegerProperty(0);

    /**
     * Create a new Single Player challenge scene
     * @param gameWindow the Game Window
     */
    public ChallengeScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
    }

    /**
     * Build the Challenge window
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        mainPane.setCenter(board);

        var topBar = new GridPane();
        topBar.setPadding(new Insets(8, 8, 8, 8));
        mainPane.setTop(topBar);

        var scoreBox = new VBox();
        scoreBox.setAlignment(Pos.CENTER);
        var scoreName = new Text("Score");
        scoreName.getStyleClass().add("heading");
        var scoreResult = new Text("0");
        scoreResult.getStyleClass().add("score");
        scoreResult.textProperty().bind(game.scoreProperty().asString());
        scoreBox.getChildren().add(scoreName);
        scoreBox.getChildren().add(scoreResult);
        topBar.add(scoreBox, 0, 0);

        var gameMode = new Text("Challenge Mode");
        HBox.setHgrow(gameMode, Priority.ALWAYS);
        gameMode.setTextAlignment(TextAlignment.CENTER);
        gameMode.getStyleClass().add("title");
        topBar.add(gameMode, 1, 0);
        GridPane.setHgrow(gameMode, Priority.ALWAYS);
        GridPane.setHalignment(gameMode, HPos.CENTER);

        var livesBox = new VBox();
        livesBox.setAlignment(Pos.CENTER);
        var livesName = new Text("Lives");
        livesName.getStyleClass().add("heading");
        var livesResult = new Text("0");
        livesResult.getStyleClass().add("lives");
        livesResult.textProperty().bind(game.livesProperty().asString());
        livesBox.getChildren().add(livesName);
        livesBox.getChildren().add(livesResult);
        topBar.add(livesBox, 2, 0);

        var sideBar = new VBox();
        sideBar.setAlignment(Pos.CENTER);
        sideBar.setPadding(new Insets(5, 5, 5, 5));
        mainPane.setRight(sideBar);

        var highscoreName = new Text("High Score");
        highscoreName.getStyleClass().add("heading");
        var highscoreResult = new Text("0");
        highscoreResult.getStyleClass().add("hiscore");
        highscoreResult.textProperty().bind(highscore.asString());
        sideBar.getChildren().add(highscoreName);
        sideBar.getChildren().add(highscoreResult);

        var levelName = new Text("Level");
        levelName.getStyleClass().add("heading");
        var levelResult = new Text("0");
        levelResult.getStyleClass().add("level");
        levelResult.textProperty().bind(game.levelProperty().asString());
        sideBar.getChildren().add(levelName);
        sideBar.getChildren().add(levelResult);

        var multiplierName = new Text("Multiplier");
        multiplierName.getStyleClass().add("heading");
        var multiplierResult = new Text("1");
        multiplierResult.getStyleClass().add("multiplier");
        multiplierResult.textProperty().bind(game.multiplierProperty().asString());
        sideBar.getChildren().add(multiplierName);
        sideBar.getChildren().add(multiplierResult);

        var pieceName = new Text("Incoming");
        pieceName.getStyleClass().add("heading");
        sideBar.getChildren().add(pieceName);

        currentPiece = new PieceBoard(3, 3, gameWindow.getWidth() / 7, gameWindow.getHeight() / 7);
        currentPiece.colorCentre();
        currentPiece.setOnMouseClicked((e) -> {
            if(e.getButton() == MouseButton.PRIMARY){this.rotate();}
        });
        nextPiece  = new PieceBoard(3, 3, gameWindow.getWidth() / 10, gameWindow.getHeight() / 10);
        nextPiece.setPadding(new Insets(15, 0, 0, 0));
        nextPiece.setOnMouseClicked((e) -> {this.swap();});
        sideBar.getChildren().add(currentPiece);
        sideBar.getChildren().add(nextPiece);

        board.setOnRightClick(this::rotate);
        board.setOnBlockClick(this::blockClicked);

        timer = new StackPane();
        mainPane.setBottom(timer);
        timerBar = new Rectangle();
        timerBar.setHeight(15);
        timer.getChildren().add(timerBar);
    }

    //Method to change the pieces in the Piece Boards
    protected void nextPiece(GamePiece piece){
        currentPiece.setPiece(piece);
        nextPiece.setPiece(game.getNextPiece());
    }

    //Method to rotate the pieces in the Piece Boards
    protected void rotate(){
        logger.info("Block rotated");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playAudio("rotate.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playAudio("wooh.mp3");
        }
        game.rotateCurrentPiece(1);
        currentPiece.setPiece(game.getCurrentPiece());
    }

    //Method to rotate the pieces by number of times in the Piece Boards
    protected void rotate(int times){
        logger.info("Block rotated");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playAudio("rotate.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playAudio("wooh.mp3");
        }
        game.rotateCurrentPiece(times);
        currentPiece.setPiece(game.getCurrentPiece());
    }

    //Method to swap the pieces in the Piece Boards
    protected void swap(){
        logger.info("Block swapped");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playAudio("pling.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playAudio("luul.mp3");
        }
        game.swapCurrentPiece();
        currentPiece.setPiece(game.getCurrentPiece());
        nextPiece.setPiece(game.getNextPiece());
    }

    //Method to handle whe fade animation on the cleared lines
    protected void fadeLine(HashSet<GameBlockCoordinate> set){
        logger.info("Line cleared");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playAudio("clear.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playAudio("ubivai.mp3");
        }
        for(GameBlockCoordinate block : set){
            this.board.fadeOut(this.board.getBlock(block.getX(), block.getY()));
        }
    }

    //Method to project the timer in the Scene by using Timeline with Key Values and Frames
    protected void timer(int time){
        Timeline timeline = new Timeline();

        KeyValue green = new KeyValue(timerBar.fillProperty(), Color.GREEN);
        KeyValue red = new KeyValue(timerBar.fillProperty(), Color.RED);
        KeyValue start = new KeyValue(timerBar.widthProperty(), timer.getWidth());
        KeyValue end = new KeyValue(timerBar.widthProperty(), 0);

        KeyFrame start_green = new KeyFrame(new Duration(0), green);
        KeyFrame start_loop = new KeyFrame(new Duration(0), start);
        KeyFrame turn_red = new KeyFrame(new Duration(time / 2), red);
        KeyFrame end_loop = new KeyFrame(new Duration(time), end);

        timeline.getKeyFrames().add(start_green);
        timeline.getKeyFrames().add(start_loop);
        timeline.getKeyFrames().add(turn_red);
        timeline.getKeyFrames().add(end_loop);

        timeline.play();
    }

    protected void chatMessage(){}

    /**
     * Handle when a block is clicked
     * @param gameBlock the Game Block that was clocked
     */
    protected void blockClicked(GameBlock gameBlock) {
        if(game.blockClicked(gameBlock)){
            logger.info("Block placed");
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("place.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("shtrak.mp3");
            }
            game.restartLoop();
        } else {
            logger.info("Wrong block placement");
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("fail.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("haha.mp3");
            }
        }
    }

    /**
     * Setup the game object and model
     */
    public void setupGame() {
        logger.info("Starting a new challenge");

        //Start new game
        game = new Game(5, 5);
    }

    //Method to handle the events that can be done with the help of the Keyboard
    protected void keyEvents(KeyEvent key) {
        if(chatPermission){return;}
        if (key.getCode() == KeyCode.W || key.getCode() == KeyCode.UP) {
            if(coordY > 0){coordY--;}
        } else if(key.getCode() == KeyCode.S || key.getCode() == KeyCode.DOWN){
            if(coordY < game.getRows()-1){coordY++;}
        } else if(key.getCode() == KeyCode.A || key.getCode() == KeyCode.LEFT){
            if(coordX > 0){coordX--;}
        } else if(key.getCode() == KeyCode.D || key.getCode() == KeyCode.RIGHT){
            if(coordX < game.getCols()-1){coordX++;}
        } else if(key.getCode() == KeyCode.ENTER || key.getCode() == KeyCode.X){
            blockClicked(board.getBlock(coordX, coordY));
        } else if(key.getCode() == KeyCode.SPACE || key.getCode() == KeyCode.R){
            swap();
        } else if(key.getCode() == KeyCode.CLOSE_BRACKET || key.getCode() == KeyCode.E || key.getCode() == KeyCode.C){
            rotate();
        } else if(key.getCode() == KeyCode.OPEN_BRACKET || key.getCode() == KeyCode.Q || key.getCode() == KeyCode.Z){
            rotate(3);
        } else if(key.getCode() == KeyCode.ESCAPE){
            end();
        } else if(key.getCode() == KeyCode.T){
            chatMessage();
        }
        this.board.hover(this.board.getBlock(coordX, coordY));
    }

    //Method to update the High Score projected in the scene with the help of a timeline
    protected void getHighScore(ObservableValue<? extends Number> observable, Number oldHighScore, Number newHighScore) {
        logger.info("High Score updated");
        if (newHighScore.intValue() > this.highscore.get()) {
            this.highscore.set(newHighScore.intValue());
        }

        Timeline timeline = new Timeline();

        KeyValue oldScore = new KeyValue(score, oldHighScore);
        KeyValue newScore = new KeyValue(score, newHighScore);

        KeyFrame oldScoreFrame = new KeyFrame(new Duration(0), oldScore);
        KeyFrame newScoreFrame = new KeyFrame(new Duration(100), newScore);

        timeline.getKeyFrames().add(oldScoreFrame);
        timeline.getKeyFrames().add(newScoreFrame);

        timeline.play();
    }

    //Method that ends the game when using ESCAPE
    public void end(){
        logger.info("Ending game");
        game.stopTimer();
        gameWindow.startMenu();
    }
    /**
     * Initialise the scene and start the game
     */
    @Override
    public void initialise() {
        logger.info("Initialising Challenge");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playBackgroundMusic("game_start.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playBackgroundMusic(random.nextInt(11));
        }
        game.setOnNextPiece(this::nextPiece);
        game.setOnLineCleared(this::fadeLine);
        game.setOnGameLoop(this::timer);
        game.scoreProperty().addListener(this::getHighScore);
        scene.setOnKeyPressed(this::keyEvents);
        highscore.set((ScoresScene.loadScores().get(0).getValue()).intValue());
        game.start();
        game.setOnGameOver(() -> {
            game.stopTimer();
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("rotate.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("purvi1.mp3");
            }
            gameWindow.startScores(game);
        });
    }

}
