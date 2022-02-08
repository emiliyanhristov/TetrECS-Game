package uk.ac.soton.comp1206.scene;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.network.ImageMedia;
import uk.ac.soton.comp1206.network.Multimedia;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;

import java.io.*;
import java.util.Random;

public class SettingsScene extends BaseScene{

    private static final Logger logger = LogManager.getLogger(SettingsScene.class);

    private static double musicVolume = 50;
    private static double audioVolume = 50;

    private Slider musicSlider;
    private Slider audioSlider;

    private static boolean defaultStarted;
    private static boolean boroStarted;

    public static Text soundtrackName = new Text("DEFAULT");
    private Random random = new Random();

    public SettingsScene(GameWindow gameWindow){
        super(gameWindow);
    }

    //Initialising the Settings Scene
    @Override
    public void initialise() {
        logger.info("Initialising Settings Scene");
        scene.setOnKeyPressed(e -> {
            if(e.getCode() == KeyCode.ESCAPE){
                if(SettingsScene.getSoundtrack().equals("DEFAULT")) {
                    Multimedia.playAudio("rotate.wav");
                } else if(SettingsScene.getSoundtrack().equals("BORO PURVI")){
                    Multimedia.playAudio("purvi1.mp3");
                }
                writeSettings();
                gameWindow.startMenu();
            }
        });
    }

    public static double getMusicVolume(){
        return musicVolume;
    }

    public static double getAudioVolume(){
        return audioVolume;
    }

    public static String getSoundtrack(){
        return soundtrackName.getText();
    }

    //Method to load the settings and the soundtrack from a file
    public static void loadSettings(){
        File file = new File("settings.txt");
        if(!file.exists()){
            writeSettings();
        }
        try {
            Reader reader = new FileReader(file);
            BufferedReader br = new BufferedReader(reader);
            try {
                String line = br.readLine();
                String[] parts = line.split(" ", 3);
                musicVolume = Double.valueOf(parts[0]);
                audioVolume = Double.valueOf(parts[1]);
                soundtrackName.setText(parts[2]);
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    //Method to write/save the settings and soundtrack into a file
    public static void writeSettings(){
        File file = new File("settings.txt");
        try {
            PrintWriter pw = new PrintWriter(file);
            pw.write("");
            pw.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            Writer writer = new FileWriter(file);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(musicVolume + " ");
            bw.write(audioVolume +" ");
            bw.write(getSoundtrack());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Building the Settings Scene
    @Override
    public void build() {
        logger.info("Building the Settings Scene");

        root = new GamePane(this.gameWindow.getWidth(), this.gameWindow.getHeight());

        var settings = new StackPane();
        settings.setMaxWidth(gameWindow.getWidth());
        settings.setMaxHeight(gameWindow.getHeight());
        settings.getStyleClass().add("menu-background");
        root.getChildren().add(settings);

        var pane = new BorderPane();
        settings.getChildren().add(pane);

        var vBox = new VBox();
        vBox.setSpacing(20);
        vBox.setAlignment(Pos.TOP_CENTER);
        BorderPane.setAlignment(vBox, Pos.CENTER);
        pane.setCenter(vBox);

        var title = new Text("Settings and Options");
        title.getStyleClass().add("title");
        vBox.getChildren().add(title);

        var settingsGrid = new GridPane();
        settingsGrid.setAlignment(Pos.CENTER);
        settingsGrid.setHgap(80);
        settingsGrid.setVgap(15);
        vBox.getChildren().add(settingsGrid);

        var volumeGrid = new GridPane();
        volumeGrid.setAlignment(Pos.CENTER);
        volumeGrid.setHgap(80);
        volumeGrid.setVgap(15);
        vBox.getChildren().add(volumeGrid);

        var musicControl = new Text("Music Volume");
        musicControl.getStyleClass().add("heading");
        musicControl.setTextAlignment(TextAlignment.CENTER);
        settingsGrid.add(musicControl, 0, 0);

        musicSlider = new Slider(0, 100, musicVolume);
        musicSlider.setPrefSize(300, 20);
        musicSlider.setShowTickLabels(true);
        musicSlider.setShowTickMarks(true);
        musicSlider.setMinorTickCount(5);
        musicSlider.setMajorTickUnit(25);
        musicSlider.setOrientation(Orientation.HORIZONTAL);
        settingsGrid.add(musicSlider, 0, 1);

        var hBox = new HBox();
        hBox.setSpacing(60);
        volumeGrid.add(hBox, 0, 0);

        var musicText = new Text("Current Music Volume is: " + musicVolume);
        musicText.getStyleClass().add("heading");
        hBox.getChildren().add(musicText);

        musicSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                musicVolume = (int) musicSlider.getValue();
                Multimedia.musicPlayer.setVolume(musicVolume/100);
                musicText.setText("Current Music Volume is: " + musicVolume);
            }
        });

        var audioControl = new Text("Audio Volume");
        audioControl.getStyleClass().add("heading");
        audioControl.setTextAlignment(TextAlignment.CENTER);
        settingsGrid.add(audioControl, 1, 0);

        audioSlider = new Slider(0, 100, audioVolume);
        audioSlider.setPrefSize(300,20);
        audioSlider.setShowTickLabels(true);
        audioSlider.setShowTickMarks(true);
        audioSlider.setMinorTickCount(5);
        audioSlider.setMajorTickUnit(25);
        audioSlider.setOrientation(Orientation.HORIZONTAL);
        settingsGrid.add(audioSlider, 1, 1);

        var audioText = new Text("Current Audio Volume is: " + audioVolume);
        audioText.getStyleClass().add("heading");
        hBox.getChildren().add(audioText);

        audioSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                audioVolume = (int) audioSlider.getValue();
                Multimedia.audioPlayer.setVolume(audioVolume/100);
                audioText.setText("Current Music Volume is: " + audioVolume);
            }
        });

        var soundtrackHBox = new HBox();
        soundtrackHBox.setAlignment(Pos.CENTER);
        vBox.getChildren().add(soundtrackHBox);

        var soundtrack = new Text("Current Soundtrack is: ");
        soundtrack.getStyleClass().add("heading");
        soundtrackHBox.getChildren().add(soundtrack);

        soundtrackName.getStyleClass().add("heading");
        soundtrackHBox.getChildren().add(soundtrackName);

        var soundtrackGrid = new GridPane();
        soundtrackGrid.setAlignment(Pos.CENTER);
        soundtrackGrid.setHgap(80);
        soundtrackGrid.setVgap(15);
        vBox.getChildren().add(soundtrackGrid);

        ImageView defaultImage = new ImageView(ImageMedia.getImage("TetrECS.png"));
        defaultImage.setFitHeight(270);
        defaultImage.setFitWidth(200);
        defaultImage.setPreserveRatio(true);
        soundtrackGrid.add(defaultImage, 0, 0);

        ImageView boroImage = new ImageView(ImageMedia.getImage("Boro.jpg"));
        boroImage.setFitHeight(270);
        boroImage.setFitWidth(200);
        boroImage.setPreserveRatio(true);
        soundtrackGrid.add(boroImage, 1, 0);

        var defaultText = new Text("DEFAULT");
        defaultText.getStyleClass().add("heading");
        defaultText.setTextAlignment(TextAlignment.CENTER);
        soundtrackGrid.add(defaultText, 0, 1);

        var boroText = new Text("BORO PURVI");
        boroText.getStyleClass().add("heading");
        boroText.setTextAlignment(TextAlignment.CENTER);
        soundtrackGrid.add(boroText, 1, 1);

        defaultImage.setOnMouseClicked(e -> {
            if(!soundtrackName.equals("DEFAULT") && !defaultStarted) {
                soundtrackName.setText("DEFAULT");
                Multimedia.playBackgroundMusic("menu.mp3");
                defaultStarted = true;
                boroStarted = false;
            }
        });

        defaultText.setOnMouseClicked(e -> {
            if(!soundtrackName.equals("DEFAULT") && !defaultStarted) {
                soundtrackName.setText("DEFAULT");
                Multimedia.playBackgroundMusic("menu.mp3");
                defaultStarted = true;
                boroStarted = false;
            }
        });

        boroImage.setOnMouseClicked(e -> {
            if(!soundtrackName.equals("BORO PURVI") && !boroStarted) {
                soundtrackName.setText("BORO PURVI");
                Multimedia.playBackgroundMusic(random.nextInt(11));
                boroStarted = true;
                defaultStarted = false;
            }
        });

        boroText.setOnMouseClicked(e -> {
            if(!soundtrackName.equals("BORO PURVI") && !boroStarted) {
                soundtrackName.setText("BORO PURVI");
                logger.info("Played");
                Multimedia.playBackgroundMusic(random.nextInt(11));
                boroStarted = true;
                defaultStarted = false;
            }
        });
    }
}
