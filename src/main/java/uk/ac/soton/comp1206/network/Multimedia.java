package uk.ac.soton.comp1206.network;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.SettingsScene;

import java.util.Random;

//Class that handles the audio in the game
public class Multimedia {

    private static final Logger logger = LogManager.getLogger(Multimedia.class);

    public static MediaPlayer audioPlayer;
    public static MediaPlayer musicPlayer;
    private static String file;
    private static boolean audioEnabled = true;
    private static boolean started = false;
    private static Random random = new Random();

    //Method to play audio effects for the game
    public static void playAudio(String file){
        if(!audioEnabled){
            return;
        }
        String toPlay = Multimedia.class.getResource("/sounds/" + file).toExternalForm();
        logger.info("Playing " + toPlay);
        try{
            Media play = new Media(toPlay);
            audioPlayer = new MediaPlayer(play);
            audioPlayer.setVolume(SettingsScene.getAudioVolume()/100);
            audioPlayer.play();
        } catch (Exception e){
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    //Method to play background music for the game, looping in until the background music changes
    public static void playBackgroundMusic(String file){
        if(!audioEnabled){
            return;
        }
        logger.info("Playing " + file);
        if(musicPlayer != null){musicPlayer.stop();}
        try{
            String toPlay = Multimedia.class.getResource("/music/" + file).toExternalForm();
            Media play = new Media(toPlay);
            musicPlayer = new MediaPlayer(play);
            musicPlayer.setVolume(SettingsScene.getMusicVolume()/100);
            musicPlayer.setCycleCount(-1);
            musicPlayer.play();
            started = false;
        } catch (Exception e){
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }

    //Method to play the different songs from the boro soundtrack
    public static void playBackgroundMusic(int number){
        if(!audioEnabled || started){
            return;
        }
        if(musicPlayer != null){musicPlayer.stop();}
        switch(number){
            case 0 -> {
                file = "AreMa.mp3";
            }
            case 1 -> {
                file = "CherniOchila.mp3";
            }
            case 2 -> {
                file = "DaNePitat.mp3";
            }
            case 3 -> {
                file = "Freestyle.mp3";
            }
            case 4 -> {
                file = "KatoHorata.mp3";
            }
            case 5 -> {
                file = "koStana.mp3";
            }
            case 6 -> {
                file = "KuchaMarka.mp3";
            }
            case 7 -> {
                file = "Maiko.mp3";
            }
            case 8 -> {
                file = "NqmaKakDaSpra.mp3";
            }
            case 9 -> {
                file = "SoDis.mp3";
            }
            case 10 -> {
                file = "Ubivai.mp3";
            }
        }
        try{
            String toPlay = Multimedia.class.getResource("/music/" + file).toExternalForm();
            Media play = new Media(toPlay);
            musicPlayer = new MediaPlayer(play);
            musicPlayer.setVolume(SettingsScene.getMusicVolume()/100);
            musicPlayer.setOnEndOfMedia(() -> {
                logger.info("Starting next song");
                started = false;
                playBackgroundMusic(random.nextInt(11));
            });
            musicPlayer.play();
            started = true;
        } catch (Exception e){
            audioEnabled = false;
            e.printStackTrace();
            logger.error("Unable to play audio file, disabling audio");
        }
    }
}
