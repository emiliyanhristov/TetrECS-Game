package uk.ac.soton.comp1206.game;

import javafx.application.Platform;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.network.Communicator;

import java.util.ArrayDeque;
import java.util.Queue;

//MultiplayerGame class created for the multiplayer game to handle the communicator messages
public class MultiplayerGame extends Game{

    private static final Logger logger = LogManager.getLogger(MultiplayerGame.class);

    private Communicator communicator;

    private ArrayDeque<GamePiece> queue = new ArrayDeque<>();

    /**
     * Create a new game with the specified rows and columns. Creates a corresponding grid model.
     *
     * @param cols number of columns
     * @param rows number of rows
     */
    public MultiplayerGame(int cols, int rows, Communicator communicator) {
        super(cols, rows);
        //Adding a communicator to the game and letting it listen for messages
        this.communicator = communicator;
        communicator.addListener(message -> Platform.runLater(() -> {
            handleIncomingMessages(message);
        }));
    }

    //Initialising the game with 0 score, 3 lives, level 0, multiplayer set to 1 and 3 generated Pieces
    public void initialiseGame(){
        logger.info("Initialising");
        score.set(0);
        lives.set(3);
        level.set(0);
        multiplier.set(1);
        for(int i = 0; i < 3; i++) {
            communicator.send("PIECE");
        }
    }

    //Method to update the scores of the players by constantly going through them and comparing them
    private void updateScore(String score){
        this.scores.clear();
        String[] scores = score.split("\\R");
        for(String individualScore : scores){
            String[] parts = individualScore.split(":");
            String name = parts[0];
            int result = Integer.parseInt(parts[1]);
            this.scores.add(new Pair<String,Integer>(name, result));
        }
        this.scores.sort((score1, score2) -> score2.getValue().compareTo(score1.getValue()));
    }

    /*Method that tells the program what to do with the incoming messages
    *(1) Adding the next piece received by the communicator into the queue with pieces to be spawned
    *(2) Updating the scores received by the communicator
    * */
    private void handleIncomingMessages(String message){
        String[] parts = message.split(" ", 2);
        String command = parts[0];
        //(1)
        if(command.equals("PIECE") && parts.length > 1){
            GamePiece gamePiece = GamePiece.createPiece(Integer.parseInt(parts[1]));
            queue.add(gamePiece);
            if(queue.size() > 2 && !gameStarted){
                nextPiece = spawnPiece();
                nextPiece();
                gameStarted = true;
            }
        //(2)
        } else if(command.equals("SCORES") && parts.length > 1){
            updateScore(parts[1]);
        }
    }

    //Method that overwrites the spawn piece method from the game and sends the spawned piece to the communicator
    public GamePiece spawnPiece(){
        logger.info("Piece Spawned");
        communicator.send("PIECE");
        return queue.pop();
    }

    //Method to send the coordinates of the clicked block and the shape into the communicator
    public boolean blockClicked(GameBlock gameBlock){
        boolean clicked = super.blockClicked(gameBlock);
        communicator.send("BOARD " + updatedBoard());
        return clicked;
    }

    //Method to update the Board by with message that shows the coordinates of the placed shape
    public String updatedBoard(){
        StringBuilder sb = new StringBuilder();
        for(int coordX = 0; coordX < this.cols; coordX++){
            for(int coordY = 0; coordY < this.rows; coordY++){
                sb.append("" + grid.get(coordX,coordY) + " ");
            }
        }
        return sb.toString().trim();
    }
}
