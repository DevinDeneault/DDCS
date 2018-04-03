package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

public class ImageProcessorOrdered extends DitherOrderedAbstract implements ImageProcessor {

    //knolls will need a palette-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private Bridge bridgeClass = Bridge.getInstance();

    private ColorMatcher matcher;
    private Image image;
    ImageProcessorOrdered(ColorMatcher _matcher, Image _image) {
        matcher = _matcher;
        image = _image;
    }

    @Override
    public Image processImage() {
        //Palette palette = _palette;
        WritableImage imageNew;
        PixelReader pixelReader;

        PixelWriter pixelWriter;

        double[] currentColor = new double[3];

        pixelReader = image.getPixelReader();
        imageNew = new WritableImage((int) image.getWidth(), (int) image.getHeight());	//make a new, blank writable image
        pixelWriter = imageNew.getPixelWriter();						                //make the pixel writer for the new writable image

        for (int row = 0; row < image.getHeight(); row++) {
            for (int column = 0; column < image.getWidth(); column++) {
                defineCurrentPixel(currentColor, pixelReader.getColor(column, row));
                addThreshold(currentColor, column, row);                                            //add threshold from the ordered dither matrix ("bayer matrix") to the current pixel values
                pixelWriter.setColor(column, row, matcher.getMatch(currentColor));
            }
            bridgeClass.updateProgress(1);
        }

        return imageNew;
    }
}
