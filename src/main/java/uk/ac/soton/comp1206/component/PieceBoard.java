package uk.ac.soton.comp1206.component;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.game.Grid;

//Creating the PieceBoard used to initialise the current and next piece in the Game
public class PieceBoard extends GameBoard {

    private static final Logger logger = LogManager.getLogger(GameBoard.class);

    final Grid grid;
    GameBlock[][] gameBlocks;
    private double newH;
    private double newW;

    public PieceBoard(int cols, int rows, double width, double height){
        super(cols, rows, width, height);
        this.grid = new Grid(cols, rows);
        play();
    }

    public void setPiece(GamePiece piece){
        this.grid.clean();
        this.grid.playPiece(piece, 0, 0);
    }

    public void play(){
        //Create a new GameBlock UI component
        gameBlocks = new GameBlock[this.cols][this.rows];
        for(int coordY = 0; coordY < this.rows; coordY++){
            for(int coordX = 0; coordX < this.cols; coordX++){
                newW = this.width / this.cols;
                newH = this.height / this.rows;
                GameBlock block = new GameBlock(this, coordX, coordY, newW, newH);
                //Add to our block directly
                gameBlocks[coordX][coordY] = block;
                //Add to the Grid
                add(block, coordX, coordY);
                //Link the GameBlock component to the corresponding value in the Grid
                block.bind(grid.getGridProperty(coordX, coordY));
            }
        }
    }

    //Coloring the centre of the current block Piece Board
    public void colorCentre(){
        this.gameBlocks[1][1].setCentre(true);
    }

}
