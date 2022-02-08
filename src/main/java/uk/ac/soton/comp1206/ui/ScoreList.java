package uk.ac.soton.comp1206.ui;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.Transition;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import java.util.ArrayList;

//Class which creates the score list
public class ScoreList extends VBox {

    private static final Logger logger = LogManager.getLogger(ScoreList.class);

    public final SimpleListProperty<Pair<String,Integer>> scores = new SimpleListProperty();

    private ArrayList<HBox> scoreSpaces = new ArrayList<>();
    private StringProperty nameProperty = new SimpleStringProperty();
    private int scoresNumber;
    protected static Text name;

    //Creating the Score List and listening for changes in the list
    public ScoreList(){
        getStyleClass().add("scorelist");
        setAlignment(Pos.CENTER);
        scores.addListener((ListChangeListener<? super Pair<String, Integer>>) e -> update());
        nameProperty.addListener(e -> update());
    }

    //Adding the scores to the list
    public void update(){
        logger.info("Score updated");

        scoreSpaces.clear();
        getChildren().clear();
        scoresNumber = 0;

        for(Pair<String,Integer> score : scores){
            scoresNumber++;

            if(scoresNumber > 10){break;}

            var scoreBox = new HBox();
            scoreBox.getStyleClass().add("scoreitem");
            scoreBox.setAlignment(Pos.CENTER);

            name = new Text(score.getKey() + ":");
            name.getStyleClass().add("scorer");

            if(score.getKey().equals(nameProperty.get())){
                name.getStyleClass().add("myscore");
            }

            name.setFill(GameBlock.COLOURS[scoresNumber]);
            name.setTextAlignment(TextAlignment.CENTER);

            var result = new Text(score.getValue().toString());
            result.getStyleClass().add("points");
            result.setFill(GameBlock.COLOURS[scoresNumber]);
            result.setTextAlignment(TextAlignment.CENTER);
            scoreBox.getChildren().addAll(name, result);

            getChildren().add(scoreBox);
            scoreSpaces.add(scoreBox);
            reveal();
        }
    }

    //Method to reveal the scores with fade animation
    public void reveal(){
        ArrayList<Transition> transitions = new ArrayList<>();
        for(HBox score : scoreSpaces){
            FadeTransition fade = new FadeTransition(new Duration(200), score);
            fade.setFromValue(0);
            fade.setToValue(1);
            transitions.add(fade);
        }
        SequentialTransition seqTransition = new SequentialTransition(transitions.toArray(effect -> new Animation[effect]));
        seqTransition.play();
    }

    public ListProperty<Pair<String,Integer>> getScoreProperty(){return scores;}
    public StringProperty getNameProperty(){return nameProperty;}

}
