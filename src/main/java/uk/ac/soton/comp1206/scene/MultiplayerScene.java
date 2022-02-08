package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.MultiplayerGame;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Leaderboard;

import java.util.ArrayList;
import java.util.Random;

public class MultiplayerScene extends ChallengeScene{

    private static final Logger logger = LogManager.getLogger(MultiplayerScene.class);

    private Communicator communicator;

    private Random random = new Random();
    private Text chatText;
    private TextField chat;
    private Leaderboard leaderboard;
    private StringProperty gameName = new SimpleStringProperty();
    private ObservableList<Pair<String,Integer>> competitionList;
    private ArrayList<Pair<String,Integer>> competitionArray = new ArrayList<>();
    private ListProperty<Pair<String,Integer>> scoreProperty;

    /**
     * Create a new Single Player challenge scene
     *
     * @param gameWindow the Game Window
     */
    public MultiplayerScene(GameWindow gameWindow) {
        super(gameWindow);
        this.communicator = gameWindow.getCommunicator();
    }

    //Initialising the Multiplayer Game and using communicator to update the game's features
    @Override
    public void initialise(){
        logger.info("Initialising the Multiplayer Challenge");
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
        game.start();
        communicator.addListener(message -> Platform.runLater(() -> {
            handleMessages(message);
        }));
        communicator.send("NICK");
        communicator.send("SCORES");
        game.scoreProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                MultiplayerScene.this.communicator.send("SCORE " + newValue);
            }
        });
        game.setOnGameOver(() -> {
            game.stopTimer();
            communicator.send("DIE");
            gameWindow.startScores(game);
        });
    }

    /*Method that handles the different messages received from the communicator
    *(1) If the command is SCORES the programme updates the scores in the game and if someone is dead it crosses out his name
    *(2) If the command is NICK the programme sets the participants names in the UI
    *(3) If the command is MSG the programme sends the in-game message
    * */
    public void handleMessages(String message){
        String[] parts = message.split(" ",2);
        String command = parts[0];
        //(1)
        if(command.equals("SCORES") && parts.length > 1){
            competitionArray.clear();
            String[] scoreLine = parts[1].split("\\R");
            for(String score : scoreLine){
                String[] scoreParts = score.split(":");
                String participantName = scoreParts[0];
                if(scoreParts[2].equals("DEAD")){
                    leaderboard.onPlayersDead(participantName);
                }
                competitionArray.add(new Pair<String,Integer>(participantName, Integer.parseInt(scoreParts[1])));
            }
            competitionArray.sort((score1, score2) -> score2.getValue().compareTo(score1.getValue()));
            competitionList.clear();
            competitionList.addAll(competitionArray);
        //(2)
        } else if(command.equals("NICK") && parts.length > 1){
            String participantName = parts[1];
            if(!participantName.contains(":")) {
                gameName.set(participantName);
                game.nameProperty().set(participantName);
            }
        //(3)
        } else if(command.equals("MSG")){
            String chatMessage = parts[1];
            String[] messageParts = chatMessage.split(":", 2);
            String participantChatName = messageParts[0];
            if(participantChatName.equals(gameName.get())){
                this.chatPermission = false;
            }
            logger.info("Message send");
            if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                Multimedia.playAudio("message.wav");
            } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                Multimedia.playAudio("vahaha.mp3");
            }
            this.chatText.setText("<" + participantChatName + "> " + messageParts[1]);
        }
    }

    //Method to turn on the chat box(text flow)
    protected void chatMessage(){
        chatPermission = true;
        Platform.runLater(() -> {
            chat.setVisible(true);
            chat.setEditable(true);
            chat.requestFocus();
        });
    }

    //Method that overwrites the challenge end method to send message "DIE" on leaving the game
    public void end(){
        super.end();
        communicator.send("DIE");
    }

    //Method to set up the game board and add communicator
    public void setupGame(){
        logger.info("Starting Multiplayer Game");
        this.game = new MultiplayerGame(5 , 5, communicator);
    }

    //Building the Multiplayer Scene, mostly like the Challenge Scene with few changes
    public void build(){
        logger.info("Building the Multiplayer Game");

        setupGame();

        root = new GamePane(gameWindow.getWidth(),gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        var topBar = new GridPane();
        topBar.setPadding(new Insets(8, 8, 8, 8));
        mainPane.setTop(topBar);

        var score = new VBox();
        score.setAlignment(Pos.CENTER);
        var scoreName = new Text("Score");
        scoreName.getStyleClass().add("heading");
        scoreName.textProperty().bind(gameName);
        var scoreResult = new Text("0");
        scoreResult.getStyleClass().add("score");
        scoreResult.textProperty().bind(this.score.asString());
        score.getChildren().add(scoreName);
        score.getChildren().add(scoreResult);
        topBar.add(score, 0, 0);

        var gameMode = new Text("Multiplayer Match");
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

        var versus = new Text("Versus");
        versus.getStyleClass().add("heading");
        competitionList = FXCollections.observableArrayList(competitionArray);
        scoreProperty = new SimpleListProperty(competitionList);
        leaderboard = new Leaderboard();
        leaderboard.getScoreProperty().bind(scoreProperty);
        leaderboard.getNameProperty().bind(gameName);
        sideBar.getChildren().add(versus);
        sideBar.getChildren().add(leaderboard);

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

        var gameVBox = new VBox();
        gameVBox.setAlignment(Pos.CENTER);
        BorderPane.setAlignment(gameVBox, Pos.CENTER);
        mainPane.setCenter(gameVBox);

        board = new GameBoard(game.getGrid(),gameWindow.getWidth()/2,gameWindow.getWidth()/2);
        gameVBox.getChildren().add(board);
        VBox.setVgrow(board, Priority.ALWAYS);

        var chatFlow = new TextFlow();
        chatFlow.getStyleClass().add("messages");
        chatFlow.setTextAlignment(TextAlignment.CENTER);
        gameVBox.getChildren().add(chatFlow);

        chatText = new Text("Press T to start the chat");
        chatFlow.getChildren().add(chatText);

        chat = new TextField();
        chat.getStyleClass().add("messageBox");
        chat.setVisible(false);
        chat.setEditable(false);
        chat.setOnKeyPressed(e -> {
            if(e.getCode().equals(KeyCode.ESCAPE)){
                chat.setVisible(false);
                chat.setEditable(false);
                communicator.send("MSG " + "");
            }
            if(!e.getCode().equals(KeyCode.ENTER)){
                return;
            }
            chat.setVisible(false);
            chat.setEditable(false);
            communicator.send("MSG " + chat.getText());
            chat.clear();
        });
        gameVBox.getChildren().add(chat);

        board.setOnRightClick(this::rotate);
        board.setOnBlockClick(this::blockClicked);

        timer = new StackPane();
        mainPane.setBottom(timer);
        timerBar = new Rectangle();
        timerBar.setHeight(15);
        timer.getChildren().add(timerBar);

    }
}
