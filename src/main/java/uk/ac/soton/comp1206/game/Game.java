package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.*;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.scene.SettingsScene;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to manipulate the game state
 * and to handle actions made by the player should take place inside this class.
 */
public class Game {

    private static final Logger logger = LogManager.getLogger(Game.class);

    /**
     * Number of rows
     */
    protected final int rows;

    /**
     * Number of columns
     */
    protected final int cols;

    /**
     * The grid model linked to the game
     */
    protected final Grid grid;

    protected Random random = new Random();
    protected boolean gameStarted = false;
    protected GamePiece currentPiece;
    protected GamePiece nextPiece;
    protected ScheduledExecutorService timer;
    protected ScheduledFuture loop;
    protected ArrayList<Pair<String,Integer>> scores = new ArrayList<>();

    protected IntegerProperty score = new SimpleIntegerProperty(0);
    protected IntegerProperty level = new SimpleIntegerProperty(0);
    protected IntegerProperty lives = new SimpleIntegerProperty(0);
    protected IntegerProperty multiplier = new SimpleIntegerProperty(0);
    protected StringProperty name = new SimpleStringProperty();

    protected LineClearedListener lineClearedListener = null;
    protected NextPieceListener nextPieceListener = null;
    protected GameLoopListener gameLoopListener = null;
    protected GameOverListener gameOverListener = null;

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     * @param cols number of columns
     * @param rows number of rows
     */
    public Game(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create a new grid model to represent the game state
        this.grid = new Grid(cols,rows);
        //Creating a timer
        timer = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Start the game
     */
    public void start() {
        logger.info("Starting game");
        initialiseGame();
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if(gameLoopListener != null){
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    /**
     * Initialise a new game and set up anything that needs to be done at the start
     */
    public void initialiseGame() {
        logger.info("Initialising game");
        this.score.set(0);
        this.level.set(0);
        this.lives.set(3);
        this.multiplier.set(1);
        this.nextPiece = spawnPiece();
        nextPiece();
        gameStarted = true;
    }

    /**
     * Handle what should happen when a particular block is clicked
     * @param gameBlock the block that was clicked
     */
    public boolean blockClicked(GameBlock gameBlock) {
        //Get the position of this block
        int x = gameBlock.getX();
        int y = gameBlock.getY();

        //If to check whether there is currentPiece
        if(this.currentPiece == null){
            return false;
        }
        //Boolean to get true if a piece is played
        boolean playPiece = this.grid.playPieceCentered(this.currentPiece, gameBlock.getX(), gameBlock.getY());
        //If to check whether a piece is played
        if(!playPiece){
            return false;
        }
        //Doing all the processes after a piece is played (clearing the lines, calculating the score, getting the next piece)
        afterPiece();
        nextPiece();
        return true;
    }

    /*Method telling the program what to do after placing a piece.
    *In it there are two HashSets to keep the coordinates of the blocks that are to be cleared.
    *(1) Checks if there is a line(column) and saves its' coordinates into the HashSets.
    *(2) Checks if there is a line(row) and saves its' coordinates into the HashSets.
    *(3) If no lines are to be cleared, the multiplier resets to 1.
    *(4) Increasing the score and calculating the level
    *(5) For loop that goes through every coordinate of the HashSets and clears the lines.
    *(6) Listener to handle the cleared line and fade the clearance
    * */
    public void afterPiece(){
        HashSet<IntegerProperty> toBeCleared = new HashSet<>();
        HashSet<GameBlockCoordinate> emptyLine = new HashSet<>();
        int lines = 0;
        //(1)
        for(int x = 0; x < this.cols; x++){
            int straight = this.cols;
            for(int y = 0; y < this.rows && this.grid.get(x,y) != 0; y++){
                straight = straight - 1;
            }
            if(straight == 0){
                lines = lines + 1;
                for(int y = 0; y < this.rows; y++){
                    toBeCleared.add(this.grid.getGridProperty(x, y));
                    emptyLine.add(new GameBlockCoordinate(x, y));
                }
            }
        }
        //(2)
        for(int y = 0; y < this.rows; y++){
            int straight = this.rows;
            for(int x = 0; x < this.cols && this.grid.get(x, y) != 0; x++){
                straight = straight - 1;
            }
            if(straight == 0){
                lines = lines + 1;
                for(int x = 0; x < this.cols; x++){
                    toBeCleared.add(this.grid.getGridProperty(x, y));
                    emptyLine.add(new GameBlockCoordinate(x, y));
                }
            }
        }
        //(3)
        if(lines == 0){
            if(multiplier.get() > 1){
                multiplier.set(1);
            }
            return;
        }
        //(4)
        scored(lines, toBeCleared.size(), multiplier.get());
        multiplier.set(multiplier.add(1).get());
        level.set(Math.floorDiv(score.get(), 1000));
        //(5)
        for(IntegerProperty block : toBeCleared){
            block.set(0);
        }
        //(6)
        if(this.lineClearedListener != null){
            this.lineClearedListener.lineCleared(emptyLine);
        }
    }

    //Method to calculate the score after a piece is played
    public void scored(int lines, int blocks, int multiplier){
        score.set(score.add(lines * blocks * 10 * multiplier).get());
    }

    /**
     * Get the grid model inside this game representing the game state of the board
     * @return game grid model
     */
    public Grid getGrid() {
        return grid;
    }

    /**
     * Get the number of columns in this game
     * @return number of columns
     */
    public int getCols() {
        return cols;
    }

    /**
     * Get the number of rows in this game
     * @return number of rows
     */
    public int getRows() {
        return rows;
    }

    //Method to spawn random piece from the 15 available
    public GamePiece spawnPiece(){
        return GamePiece.createPiece(random.nextInt(15));
    }

    //Method to spawn a line used for testing
    public GamePiece spawnLine(){
        return GamePiece.createPiece(3);
    }

    //Method to get the next piece when the current piece is placed
    public GamePiece nextPiece(){
        this.currentPiece = this.nextPiece;
        this.nextPiece = spawnPiece();
        if(this.nextPieceListener != null){
            this.nextPieceListener.nextPiece(this.currentPiece);
        }
        return this.currentPiece;
    }

    //Method to rotate the current piece by a number of times
    public void rotateCurrentPiece(int times){
        logger.info("Piece rotated");
        currentPiece.rotate(times);
    }

    //Method to swap the current and next piece
    public void swapCurrentPiece(){
        logger.info("Pieces swapped");
        GamePiece temporary = currentPiece;
        currentPiece = nextPiece;
        nextPiece = temporary;
    }

    //Method to calculate the time each turn
    public int getTimerDelay(){
        int min = 2500;
        int currentTime = 12000 - 500 * this.getLevel();
        if(currentTime > min){
            return currentTime;
            //return min;
        } else {return min;}
    }

    /*Method to handle the game loop
    *(1) Remove a life when the time ends and if there are no more life it is Game Over!
    *(2) Set the multiplier to 1 after the time runs out
    *(3) Gets the next piece after the time runs out
    *(4) Listener to listen when the timer runs out and restart it afterwards
    * */
    public void gameLoop(){
        //(1)
        if(this.getLives() > 0){
            logger.info("Life lost");
            lives.set(lives.get()-1);
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("lifelose.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("nanana.mp3");
            }
        } else {
            gameOver();
        }
        //(2)
        if(this.getMultiplier() > 1){
            logger.info("Multiplier set to 1");
            multiplier.set(1);
        }
        //(3)
        nextPiece();
        //(4)
        if(gameLoopListener != null){
            gameLoopListener.gameLoop(getTimerDelay());
        }
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
    }

    //Method to restart the timer after a block is placed
    public void restartLoop(){
        loop.cancel(false);
        logger.info("Timer restarted");
        loop = timer.schedule(this::gameLoop, getTimerDelay(), TimeUnit.MILLISECONDS);
        if(gameLoopListener != null){
            gameLoopListener.gameLoop(getTimerDelay());
        }
    }

    //Method to listen when the game ends
    public void gameOver(){
        logger.info("Game over");
        if(gameOverListener != null){
            Platform.runLater(()-> gameOverListener.gameOver());
        }
    }

    //Method to stop the timer
    public void stopTimer(){
        this.timer.shutdownNow();
    }

    //Getters for different things
    public GamePiece getCurrentPiece(){return currentPiece;}
    public GamePiece getNextPiece(){return nextPiece;}
    public ArrayList<Pair<String,Integer>> getScores(){return this.scores;}
    public int getScore(){return scoreProperty().get();}
    public int getLevel(){return levelProperty().get();}
    public int getLives(){return livesProperty().get();}
    public int getMultiplier(){return multiplierProperty().get();}

    public IntegerProperty scoreProperty(){return score;}
    public IntegerProperty levelProperty(){return level;}
    public IntegerProperty livesProperty(){return lives;}
    public IntegerProperty multiplierProperty(){return multiplier;}
    public StringProperty nameProperty(){return name;}

    //Setters for the listeners
    public void setOnLineCleared(LineClearedListener listener){lineClearedListener = listener;}
    public void setOnNextPiece(NextPieceListener listener){nextPieceListener = listener;}
    public void setOnGameLoop(GameLoopListener listener){gameLoopListener = listener;}
    public void setOnGameOver(GameOverListener listener){gameOverListener = listener;}
}
