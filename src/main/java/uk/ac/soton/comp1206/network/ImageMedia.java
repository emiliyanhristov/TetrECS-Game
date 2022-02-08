package uk.ac.soton.comp1206.network;

import javafx.scene.image.Image;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.scene.MenuScene;

//ImageMedia class used to place images in the UI
public class ImageMedia {

    private static final Logger logger = LogManager.getLogger(ImageMedia.class);

    public static Image getImage(String file){
        try {
            Image image = new Image(ImageMedia.class.getResource("/images/" + file).toExternalForm());
            return image;
        } catch (Exception e){
            e.printStackTrace();
            logger.error("No image found");
            return null;
        }
    }
}
