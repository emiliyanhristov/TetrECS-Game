package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer values arranged in a 2D
 * arrow, with rows and columns.
 *
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display of the contents of
 * the grid.
 *
 * The Grid contains functions related to modifying the model, for example, placing a piece inside the grid.
 *
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

    /**
     * The number of columns in this grid
     */
    private final int cols;

    /**
     * The number of rows in this grid
     */
    private final int rows;

    /**
     * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
     */
    private final SimpleIntegerProperty[][] grid;

    /**
     * Create a new Grid with the specified number of columns and rows and initialise them
     * @param cols number of columns
     * @param rows number of rows
     */
    public Grid(int cols, int rows) {
        this.cols = cols;
        this.rows = rows;

        //Create the grid itself
        grid = new SimpleIntegerProperty[cols][rows];

        //Add a SimpleIntegerProperty to every block in the grid
        for(var y = 0; y < rows; y++) {
            for(var x = 0; x < cols; x++) {
                grid[x][y] = new SimpleIntegerProperty(0);
            }
        }
    }

    /**
     * Get the Integer property contained inside the grid at a given row and column index. Can be used for binding.
     * @param x column
     * @param y row
     * @return the IntegerProperty at the given x and y in this grid
     */
    public IntegerProperty getGridProperty(int x, int y) {
        return grid[x][y];
    }

    /**
     * Update the value at the given x and y index within the grid
     * @param x column
     * @param y row
     * @param value the new value
     */
    public void set(int x, int y, int value) {
        grid[x][y].set(value);
    }

    /**
     * Get the value represented at the given x and y index within the grid
     * @param x column
     * @param y row
     * @return the value
     */
    public int get(int x, int y) {
        try {
            //Get the value held in the property at the x and y index provided
            return grid[x][y].get();
        } catch (ArrayIndexOutOfBoundsException e) {
            //No such index
            return -1;
        }
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

    /*Method to check whether a piece can be played by checking
    * if there is a piece already placed on the place we want to put the new piece
    * */
    public boolean canPlacePiece(GamePiece piece, int coordX, int coordY){
        int[][] blocks = piece.getBlocks();
        for(int blockX = 0; blockX < blocks.length; blockX++){
            for(int blockY = 0; blockY < blocks[blockX].length; blockY++){
                if(blocks[blockX][blockY] == 0) continue;
                int current = get(coordX + blockX, coordY + blockY);
                if(current != 0){return false;}
            }
        }
        return true;
    }
    /*Method to place the piece on the place with given coordinates,
    * it firstly checks whether a piece can be placed and then places it.
    * */
    public boolean playPiece(GamePiece piece, int coordX, int coordY){
        int[][] blocks = piece.getBlocks();
        if(!canPlacePiece(piece, coordX, coordY)){return false;}
        for(int blockX = 0; blockX < blocks.length; blockX++){
            for(int blockY = 0; blockY < blocks[blockX].length; blockY++){
                int value = blocks[blockX][blockY];
                if(value == 0) continue;
                grid[coordX+blockX][coordY+blockY].set(value);
            }
        }
        return true;
    }
    /*Method to place the piece(center it) where the it should be played*/
    public boolean playPieceCentered(GamePiece piece, int coordX, int coordY){
        coordX = coordX - 1;
        coordY = coordY - 1;
        return playPiece(piece, coordX, coordY);
    }
    /*Method to clear the Grid*/
    public void clean(){
        for(int coordX = 0; coordX < this.cols; coordX++){
            for(int coordY = 0; coordY < this.rows; coordY++){
                this.grid[coordX][coordY].set(0);
            }
        }
    }

}
