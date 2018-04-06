package application;

import javafx.scene.image.Image;

//image processors take an image and convert it to a new palette of colors using dithering if needed

public interface ImageProcessor {

    Image processImage();

}
