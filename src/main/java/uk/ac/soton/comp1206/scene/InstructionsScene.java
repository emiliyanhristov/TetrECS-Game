package uk.ac.soton.comp1206.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.network.ImageMedia;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

public class InstructionsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(InstructionsScene.class);


    public InstructionsScene(GameWindow gameWindow){
        super(gameWindow);
    }

    //Initialising the Instruction scene
    @Override
    public void initialise(){
        logger.info("Initialising Instructions Scene");
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                    Multimedia.playAudio("rotate.wav");
                } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                    Multimedia.playAudio("purvi1.mp3");
                }
                gameWindow.startMenu();
            }
        });
    }

    //Building the Instruction scene
    @Override
    public void build(){
        logger.info("Building the Instruction Scene");

        root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());

        var instructions = new StackPane();
        instructions.setMaxWidth(gameWindow.getWidth());
        instructions.setMaxHeight(gameWindow.getHeight());
        instructions.getStyleClass().add("menu-background");
        root.getChildren().add(instructions);

        var pane = new BorderPane();
        instructions.getChildren().add(pane);

        var vBox = new VBox();
        vBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setAlignment(vBox, Pos.CENTER);
        pane.setCenter(vBox);

        var heading = new Text("Instructions");
        heading.getStyleClass().add("heading");
        var description = new Text("TetrECS is a fast-paced gravity-free block placement game, where you must survive by clearing rows through careful placement of the upcoming blocks before the time runs out. Lose all 3 lives and you're destroyed!");
        var descriptionFlow = new TextFlow(description);
        description.getStyleClass().add("instructions");
        description.setTextAlignment(TextAlignment.CENTER);
        descriptionFlow.setTextAlignment(TextAlignment.CENTER);
        vBox.getChildren().add(heading);
        vBox.getChildren().add(descriptionFlow);

        ImageView image = new ImageView(ImageMedia.getImage("Instructions.png"));
        image.setFitHeight(320);
        image.setPreserveRatio(true);
        vBox.getChildren().add(image);

        var pieces = new Text("Game Pieces");
        pieces.getStyleClass().add("heading");
        vBox.getChildren().add(pieces);

        var grid = new GridPane();
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(0,180,0,180));
        grid.setHgap(10);
        grid.setVgap(10);
        for(int y = 0; y < 3; y++){
            for(int x = 0; x < 5; x++){
                GamePiece piece = GamePiece.createPiece((y * 5) + x);
                PieceBoard pieceBoard = new PieceBoard(3, 3, 55, 55);
                pieceBoard.setPiece(piece);
                grid.add(pieceBoard, x, y);
            }
        }
        vBox.getChildren().add(grid);
    }
}
