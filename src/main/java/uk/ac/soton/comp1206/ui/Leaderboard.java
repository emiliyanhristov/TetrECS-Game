package uk.ac.soton.comp1206.ui;

import javafx.collections.ListChangeListener;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;

import java.util.ArrayList;

//Leaderboard class to represent the online scores
public class Leaderboard extends ScoreList {

    private static final Logger logger = LogManager.getLogger(Leaderboard.class);

    private ArrayList<String> otherScores = new ArrayList<>();

    public Leaderboard() {
        getStyleClass().add("leaderboard");
        setAlignment(Pos.CENTER);
        scores.addListener((ListChangeListener<? super Pair<String, Integer>>) e -> updateDeath());
    }

    //Method to update the player's name and score on death
    public void updateDeath(){
        for(Pair<String,Integer> score : scores) {
            if (otherScores.contains(score.getKey())) {
                ScoreList.name.getStyleClass().add("deadscore");
            }
        }
    }

    //Method to add the player into an array list with death players
    public void onPlayersDead(String player){
        otherScores.add(player);
    }
}
