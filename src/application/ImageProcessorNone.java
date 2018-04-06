package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

//an image processor that uses no dithering

public class ImageProcessorNone implements ImageProcessor {

    private Bridge bridgeClass = Bridge.getInstance();

    private ColorMatcher matcher;
    private Image image;
    ImageProcessorNone(ColorMatcher _matcher, Image _image) {
        matcher = _matcher;
        image = _image;
    }

    @Override
    public Image processImage() {
        WritableImage imageNew;
        PixelReader pixelReader;

        PixelWriter pixelWriter;

        double[] currentColor = new double[3];

        pixelReader = image.getPixelReader();
        imageNew = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        pixelWriter = imageNew.getPixelWriter();

        for( int row = 0; row < image.getHeight(); row++ ) {
            for( int column = 0; column < image.getWidth(); column++ ) {
                defineCurrentPixel(currentColor, pixelReader.getColor(column, row));
                pixelWriter.setColor(column, row, matcher.getMatch(currentColor));
            }
            bridgeClass.updateProgress(1);
        }

        return imageNew;
    }

    private void defineCurrentPixel(double[] currentColor, Color imageColor) {  //gets RGB values for current pixel
        currentColor[0] = (int) (255 * imageColor.getRed());                    //get the color value (returns a 0 to 1 value, multiply by 255 to get proper value)
        currentColor[1] = (int) (255 * imageColor.getGreen());
        currentColor[2] = (int) (255 * imageColor.getBlue());
    }

}
