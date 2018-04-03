package application;

import javafx.scene.image.Image;

public interface ImageProcessor {

    Image processImage(ColorMatcher matcher, Image image);

}
