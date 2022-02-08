package uk.ac.soton.comp1206.scene;

import javafx.animation.RotateTransition;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.network.ImageMedia;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.util.Random;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

    private static final Logger logger = LogManager.getLogger(MenuScene.class);

    private Random random = new Random();

    /**
     * Create a new menu scene
     * @param gameWindow the Game Window this will be displayed in
     */
    public MenuScene(GameWindow gameWindow) {
        super(gameWindow);
        logger.info("Creating Menu Scene");
        SettingsScene.loadSettings();
    }

    /**
     * Build the menu layout
     */
    @Override
    public void build() {
        logger.info("Building " + this.getClass().getName());

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var menuPane = new StackPane();
        menuPane.setMaxWidth(gameWindow.getWidth());
        menuPane.setMaxHeight(gameWindow.getHeight());
        menuPane.getStyleClass().add("menu-background");
        root.getChildren().add(menuPane);

        var mainPane = new BorderPane();
        menuPane.getChildren().add(mainPane);

        ImageView titleImage = new ImageView(ImageMedia.getImage("TetrECS.png"));
        titleImage.setFitHeight(100);
        titleImage.setPreserveRatio(true);
        mainPane.setCenter(titleImage);

        RotateTransition effects = new RotateTransition(new Duration(10000.0D), titleImage);
        effects.setFromAngle(-10);
        effects.setToAngle(10);
        effects.setAutoReverse(true);
        effects.setCycleCount(-1);
        effects.play();

        var menu = new VBox(5);
        menu.setAlignment(Pos.CENTER);
        menu.getStyleClass().add("menu");

        var singlePlayer = new Text("Single Player");
        singlePlayer.setTextAlignment(TextAlignment.CENTER);
        singlePlayer.getStyleClass().add("title");
        singlePlayer.setOnMouseClicked((e) -> {
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("rotate.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("purvi1.mp3");
            }
            gameWindow.startChallenge();
        });
        menu.getChildren().add(singlePlayer);

        var multiPlayer = new Text("Multi Player");
        multiPlayer.setTextAlignment(TextAlignment.CENTER);
        multiPlayer.getStyleClass().add("title");
        multiPlayer.setOnMouseClicked(e -> {
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("rotate.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("purvi1.mp3");
            }
            gameWindow.startMultiplayerLobby();
        });
        menu.getChildren().add(multiPlayer);

        var instructions = new Text("How to Play");
        instructions.setTextAlignment(TextAlignment.CENTER);
        instructions.getStyleClass().add("title");
        instructions.setOnMouseClicked((e) -> {
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("rotate.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("purvi1.mp3");
            }
            gameWindow.startInstructions();
        });
        menu.getChildren().add(instructions);

        var settings = new Text("Settings and Options");
        settings.setTextAlignment(TextAlignment.CENTER);
        settings.getStyleClass().add("title");
        settings.setOnMouseClicked((e) -> {
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("rotate.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("purvi1.mp3");
            }
            gameWindow.startSettings();
        });
        menu.getChildren().add(settings);

        var exit = new Text("Exit");
        exit.setTextAlignment(TextAlignment.CENTER);
        exit.getStyleClass().add("title");
        exit.setOnMouseClicked((e) -> {
            App.getInstance().shutdown();
        });
        menu.getChildren().add(exit);

        mainPane.setBottom(menu);
    }

    /**
     * Initialise the menu
     */
    @Override
    public void initialise() {
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playBackgroundMusic("menu.mp3");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playBackgroundMusic(random.nextInt(11));
        }
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                App.getInstance().shutdown();
            }
        });
    }

    /**
     * Handle when the Start Game button is pressed
     * @param event event
     */
    private void startGame(ActionEvent event) {
        gameWindow.startChallenge();
    }

}
