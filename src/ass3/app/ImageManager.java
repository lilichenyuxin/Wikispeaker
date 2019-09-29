package ass3.app;

import javafx.scene.image.Image;
import java.util.HashMap;

public class ImageManager {

    private HashMap<String, Image> images;

    public ImageManager() {
        images = new HashMap<String, Image>();
    }

    public void loadImage(String key, String relativePath, int width, int height) {

        Image img = new Image(getClass().getResourceAsStream(relativePath), width, height, true, true);
        images.put(key, img);

    }

    public Image getImage(String key) {

        return images.get(key);

    }

}