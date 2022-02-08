package uk.ac.soton.comp1206.scene;

import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableArray;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.network.Communicator;
import uk.ac.soton.comp1206.network.ImageMedia;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.ScoreList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScoresScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(ScoresScene.class);

    private SimpleListProperty<Pair<String,Integer>> localScore;
    private SimpleListProperty<Pair<String,Integer>> remoteScore;
    private StringProperty currentName = new SimpleStringProperty("");
    private BooleanProperty provideScore = new SimpleBooleanProperty(false);
    private ObservableList<Pair<String,Integer>> localScoresList;
    private ObservableList<Pair<String,Integer>> onlineScoresList;
    private ScoreList localScores;
    private ScoreList onlineScores;
    private ArrayList<Pair<String,Integer>> onlineScoresArray = new ArrayList<>();
    private boolean newScore = false;
    private boolean newOnlineScore = false;
    private boolean getScores = true;
    private VBox layout;
    private Text hs;
    private Communicator communicator;
    private Random random = new Random();

    public ScoresScene (GameWindow gameWindow, Game game){
        super(gameWindow);
        this.game = game;
        communicator = gameWindow.getCommunicator();
        logger.info("Starting Scores scene");
    }

    //Initialising the Scores Scene and listening for the online scores and new high scores
    @Override
    public void initialise(){
        logger.info("Initialising the Scores Scene");
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playAudio("explode.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playAudio("razdva.mp3");
        }
        if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
            Multimedia.playBackgroundMusic("end.wav");
        } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
            Multimedia.playBackgroundMusic(random.nextInt(11));
        }        if(!game.getScores().isEmpty()){
            currentName.set(game.nameProperty().getValue());
        }
        loadOnlineScores();
        communicator.addListener(message -> Platform.runLater(() -> {
            receiveOnlineMessage(message);
        }));
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                gameWindow.startMenu();
            }
        });
    }

    /*Method to tell the program what to do with a new high score
    *(1) Checking for a game score and if its 0, just projecting the high scores
    *(2) Checking if the current score is greater than the smallest local score and finding its place
    *(3) Checking if the current score is greater than the smallest remote score and finding its place
    *(4) If there is new high score, asking for the players name and adding it to the high scores
    *(5) If not, projecting the current high scores
    * */
    public void newHighScore(){
        //(1)
        if(!game.getScores().isEmpty()){
            logger.info("No new score");
            provideScore.set(true);
            localScores.reveal();
            onlineScores.reveal();
            return;
        }

        int scoreNumber = 0;
        int onlineScoreNumber = 0;
        int currentScore = game.getScore();
        int smallestScore = localScoresList.get(localScoresList.size() - 1).getValue();
        int smallestRemoteScore = onlineScoresList.get(onlineScoresList.size() - 1).getValue();

        //(2)
        if(currentScore > smallestScore){
            for(Pair<String,Integer> score : localScoresList){
                if(currentScore > score.getValue()){
                    newScore = true;
                    break;
                }
                scoreNumber++;
            }
        }

        //(3)
        if(currentScore > smallestRemoteScore){
            for(Pair<String,Integer> score : onlineScoresList){
                if(currentScore > score.getValue()){
                    newOnlineScore = true;
                    break;
                }
                onlineScoreNumber++;
            }
        }

        //(4)
        if(newScore || newOnlineScore){
            hs.setText("New High Score");
            hs.setTextAlignment(TextAlignment.CENTER);
            var nameBox = new TextField();
            nameBox.setPrefWidth(600);
            nameBox.setPromptText("Enter your name here");
            nameBox.requestFocus();
            layout.getChildren().add(2, nameBox);
            var button = new Button("Submit");
            button.setDefaultButton(true);
            layout.getChildren().add(3, button);
            int scoreNumber1 = scoreNumber;
            int onlineScoreNumber1 = onlineScoreNumber;
            button.setOnAction((e) -> {
                String name = nameBox.getText().replace(":", "");
                currentName.set(name);
                layout.getChildren().remove(2);
                layout.getChildren().remove(2);
                if(newScore){
                    localScoresList.add(scoreNumber1, new Pair<String,Integer>(name, currentScore));
                }
                if(newOnlineScore){
                    onlineScoresList.add(onlineScoreNumber1, new Pair<String,Integer>(name, currentScore));
                }
                writeOnlineScore(name, currentScore);
                writeScores(localScoresList);
                loadOnlineScores();
                newScore = false;
                newOnlineScore = false;
                if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                    Multimedia.playAudio("pling.wav");
                } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                    Multimedia.playAudio("buzuu.mp3");
                }
            });
        //(5)
        } else {
            logger.info("No new High Scores");
            provideScore.set(true);
            localScores.reveal();
            onlineScores.reveal();
        }
    }

    //Method to handle the remote HISCORES message
    private void receiveOnlineMessage(String message){
        String[] parts = message.split(" ",2);
        if(parts[0].equals("HISCORES")){
            if(parts.length <= 1){
                splitMessage("");
            } else {
                splitMessage(parts[1]);
            }
        }
    }

    //Method to get the remote scores into an array so the can be handled
    private void splitMessage(String message){
        onlineScoresArray.clear();
        String[] scoreText = message.split("\\R");
        for(String line : scoreText){
            String[] parts = line.split(":", 2);
            String name = parts[0];
            int score = Integer.parseInt(parts[1]);
            onlineScoresArray.add(new Pair<String,Integer>(name, score));
        }
        onlineScoresArray.sort((score1, score2) -> score2.getValue().compareTo(score1.getValue()));
        onlineScoresList.clear();
        onlineScoresList.addAll(onlineScoresArray);
        if(getScores) {
            newHighScore();
            getScores = false;
            return;
        }
        provideScore.set(true);
        localScores.reveal();
        onlineScores.reveal();
    }

    //Building the Scores Scene
    @Override
    public void build(){
        logger.info("Building the Scores Scene");

        root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

        var challengePane = new StackPane();
        challengePane.setMaxWidth(gameWindow.getWidth());
        challengePane.setMaxHeight(gameWindow.getHeight());
        challengePane.getStyleClass().add("menu-background");
        root.getChildren().add(challengePane);

        var mainPane = new BorderPane();
        challengePane.getChildren().add(mainPane);

        layout = new VBox();
        layout.setAlignment(Pos.TOP_CENTER);
        layout.setSpacing(15);
        mainPane.setCenter(layout);

        var image = new ImageView(ImageMedia.getImage("TetrECS.png"));
        image.setFitHeight(100);
        image.setPreserveRatio(true);
        layout.getChildren().add(image);

        var go = new Text("Game Over");
        go.getStyleClass().add("bigtitle");
        go.setTextAlignment(TextAlignment.CENTER);
        layout.getChildren().add(go);

        hs = new Text("High Scores");
        hs.getStyleClass().add("title");
        hs.setTextAlignment(TextAlignment.CENTER);
        layout.getChildren().add(hs);

        var gridPane = new GridPane();
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(150);
        gridPane.visibleProperty().bind(provideScore);
        layout.getChildren().add(gridPane);

        var local = new Text("Local Scores");
        local.getStyleClass().add("heading");
        local.setTextAlignment(TextAlignment.CENTER);
        gridPane.add(local, 0, 0);

        localScores = new ScoreList();
        gridPane.add(localScores, 0, 1);

        var online = new Text("Online Scores");
        online.getStyleClass().add("heading");
        online.setTextAlignment(TextAlignment.CENTER);
        gridPane.add(online, 1, 0);

        onlineScores = new ScoreList();
        gridPane.add(onlineScores, 1, 1);

        if(game.getScores().isEmpty()){
            localScoresList = FXCollections.observableArrayList(loadScores());
        } else {
            localScoresList = FXCollections.observableArrayList(game.getScores());
            local.setText("This game");
        }

        localScoresList.sort((score1, score2) -> (score2.getValue().compareTo(score1.getValue())));
        onlineScoresList = FXCollections.observableArrayList(onlineScoresArray);

        localScore = new SimpleListProperty(localScoresList);
        localScores.getScoreProperty().bind(localScore);
        localScores.getNameProperty().bind(currentName);

        remoteScore = new SimpleListProperty(onlineScoresList);
        onlineScores.getScoreProperty().bind(remoteScore);
        onlineScores.getNameProperty().bind(currentName);
    }

    //Method to ask for the remote scores
    private void loadOnlineScores(){
        communicator.send("HISCORES");
    }

    //Method to write the current score into the communicator
    private void writeOnlineScore(String name, Integer score){
        communicator.send("HISCORE " + name + ":" + score);
    }

    //Method to load the local scores from a file
    public static ArrayList<Pair<String,Integer>> loadScores() {
        ArrayList<Pair<String,Integer>> score = new ArrayList<>();
        File file = new File("results.txt");
        if(!file.exists()){
                ArrayList<Pair<String,Integer>> results = new ArrayList<>();
                for(int i = 10; i >= 1; i--){
                    results.add(new Pair("Emko", 200 - i * 10));
                }
                writeScores(results);
        }
        Reader reader = null;
        try {
            reader = new FileReader(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        BufferedReader bufferedReader = new BufferedReader(reader);
        String line = null;
        while(true){
            try {
                if (!((line = bufferedReader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            String[] parts = line.split(":");
            score.add(new Pair(parts[0], Integer.parseInt(parts[1])));
        }
        try {
            bufferedReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return score;
    }

    //Method to write scores into a file if they don't exist
    public static void writeScores(List<Pair<String,Integer>> scores){
        logger.info("Writing scores");
        scores.sort((score1, score2) -> (score2.getValue()).compareTo(score1.getValue()));
        try{
            int scoresNumber = 0;
            File file = new File("results.txt");
            Writer writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            for(Pair<String,Integer> score : scores){
                bw.write(score.getKey() + ":" + score.getValue() + "\n");
                scoresNumber ++;
                if(scoresNumber >= 10){
                    break;
                }
            }
            bw.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
