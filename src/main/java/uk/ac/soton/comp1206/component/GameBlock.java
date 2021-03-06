package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.effect.BlendMode;
import javafx.scene.paint.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Visual User Interface component representing a single block in the grid.
 *
 * Extends Canvas and is responsible for drawing itself.
 *
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 *
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

    private static final Logger logger = LogManager.getLogger(GameBlock.class);

    /**
     * The set of colours for different pieces
     */
    public static final Color[] COLOURS = {
            Color.TRANSPARENT,
            Color.DEEPPINK,
            Color.RED,
            Color.ORANGE,
            Color.YELLOW,
            Color.YELLOWGREEN,
            Color.LIME,
            Color.GREEN,
            Color.DARKGREEN,
            Color.DARKTURQUOISE,
            Color.DEEPSKYBLUE,
            Color.AQUA,
            Color.AQUAMARINE,
            Color.BLUE,
            Color.MEDIUMPURPLE,
            Color.PURPLE
    };

    private final GameBoard gameBoard;
    private GameBlockTimer myTimer;

    private final double width;
    private final double height;
    private boolean centre = false;
    private boolean hoveredBlock = false;

    /**
     * The column this block exists as in the grid
     */
    private final int x;

    /**
     * The row this block exists as in the grid
     */
    private final int y;

    /**
     * The value of this block (0 = empty, otherwise specifies the colour to render as)
     */
    private final IntegerProperty value = new SimpleIntegerProperty(0);

    /**
     * Create a new single Game Block
     * @param gameBoard the board this block belongs to
     * @param x the column the block exists in
     * @param y the row the block exists in
     * @param width the width of the canvas to render
     * @param height the height of the canvas to render
     */
    public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
        this.gameBoard = gameBoard;
        this.width = width;
        this.height = height;
        this.x = x;
        this.y = y;

        //A canvas needs a fixed width and height
        setWidth(width);
        setHeight(height);

        //Do an initial paint
        paint();

        //When the value property is updated, call the internal updateValue method
        value.addListener(this::updateValue);
    }

    /**
     * When the value of this block is updated,
     * @param observable what was updated
     * @param oldValue the old value
     * @param newValue the new value
     */
    private void updateValue(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
        paint();
    }

    /**
     * Handle painting of the block canvas
     */
    public void paint() {
        //If the block is empty, paint as empty
        if(value.get() == 0) {
            paintEmpty();
        } else {
            //If the block is not empty, paint with the colour represented by the value
            paintColor(COLOURS[value.get()]);
        }
        //Painting the centre circle
        if(this.centre){
            centreCircle();
        }
        //Hoovering over the block
        if(this.hoveredBlock){
            hoverBlock();
        }
    }

    //Method to create a white Centre Circle
    private void centreCircle(){
        var gc = getGraphicsContext2D();

        gc.setFill(Color.color(1, 1, 1, 0.7));
        gc.fillOval(width / 4, height / 4, width / 2, height / 2 );
    }

    /**
     * Paint this canvas empty
     */
    private void paintEmpty() {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Fill
        Stop[] stops = {new Stop(0.0D, Color.color(0.0D, 0.0D, 0.0D, 0.4D)), new Stop(1.0D, Color.color(0.0D, 0.0D, 0.0D, 0.8D))};
        gc.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.REFLECT, stops[0], stops[1]));
        gc.fillRect(0,0, width, height);

        //Border
        gc.setStroke(Color.color(1, 1, 1, 0.5D));
        gc.strokeRect(0,0,width,height);
    }

    /**
     * Paint this canvas with the given colour
     * @param colour the colour to paint
     */
    private void paintColor(Paint colour) {
        var gc = getGraphicsContext2D();

        //Clear
        gc.clearRect(0,0,width,height);

        //Colour fill
        gc.setFill(colour);
        gc.fillRect(0,0, width, height);

        //Filling the blocks borders
        gc.setFill(Color.color(1, 1, 1, 0.1D));
        gc.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);
        gc.setFill(Color.color(1, 1, 1, 0.3D));
        gc.fillRect(0, 0, width, 3);
        gc.setFill(Color.color(1, 1, 1, 0.3D));
        gc.fillRect(0, 0, 3, height);
        gc.setFill(Color.color(0, 0, 0, 0.3D));
        gc.fillRect(width - 3, 0, width, height);
        gc.setFill(Color.color(0, 0, 0, 0.3D));
        gc.fillRect(0, height - 3, width, height);

        //Border
        gc.setStroke(Color.color(0, 0,0, 0.5D));
        gc.strokeRect(0,0,width,height);
    }

    //Method to decide on which block the Centre Circle should be painted
    public void setCentre(boolean centre){
        this.centre = centre;
        paint();
    }

    //Method to allow hovering
    public void setHovering(boolean hoveredBlock){
        this.hoveredBlock = hoveredBlock;
        paint();
    }

    //Method to set how a hovered block should look like
    public void hoverBlock(){
        var gc = getGraphicsContext2D();

        //Creating the hoovering block
        gc.setFill(Color.color(1, 1, 1, 0.5D));
        gc.fillRect(0, 0, width, height);
    }

    //Class to create a timer for the fading animation after a line is deleted
    private class GameBlockTimer extends AnimationTimer{

        @Override
        public void handle(long a){
            fadeOut();
        }

        double opacity = 1;

        //Method to fade the line by removing 0.05 opacity until it's gone
        private void fadeOut(){
            GameBlock.this.paintEmpty();
            opacity = opacity - 0.05;
            if(opacity <= 0){
                stop();
                return;
            }
            var gc = GameBlock.this.getGraphicsContext2D();
            gc.setFill(Color.color(0,1,0, this.opacity));
            gc.fillRect(0,0, GameBlock.this.width, GameBlock.this.height);
        }
    }

    //Method to start the timer
    public void fade(){
        myTimer = new GameBlockTimer();
        myTimer.start();
    }

    /**
     * Get the column of this block
     * @return column number
     */
    public int getX() {
        return x;
    }

    /**
     * Get the row of this block
     * @return row number
     */
    public int getY() {
        return y;
    }

    /**
     * Get the current value held by this block, representing it's colour
     * @return value
     */
    public int getValue() {
        return this.value.get();
    }

    /**
     * Bind the value of this block to another property. Used to link the visual block to a corresponding block in the Grid.
     * @param input property to bind the value to
     */
    public void bind(ObservableValue<? extends Number> input) {
        value.bind(input);
    }

}
