package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

public class ImageProcessorErrorDiff extends DitherErrorDiffAbstract implements ImageProcessor {

    private Bridge bridgeClass = Bridge.getInstance();

    private ColorMatcher matcher;
    private Image image;
    ImageProcessorErrorDiff(ColorMatcher _matcher, DitherData _dither, Image _image) {
        matcher = _matcher;
        dither = _dither;
        image = _image;
    }

    @Override
    public Image processImage() {
        imageHeight = (int) image.getHeight();
        imageWidth = (int) image.getWidth();
        errorMatrix = new int[imageHeight][imageWidth][3];
        WritableImage imageNew;
        PixelReader pixelReader;

        PixelWriter pixelWriter;

        double[] currentColor = new double[3];
        double[] newColor = new double[3];

        Color newColorRGB;

        pixelReader = image.getPixelReader();
        imageNew = new WritableImage((int) image.getWidth(), (int) image.getHeight());
        pixelWriter = imageNew.getPixelWriter();

        for( int row = 0; row < image.getHeight(); row++ ) {
            for( int column = 0; column < image.getWidth(); column++ ) {
                defineCurrentPixelWithError(currentColor, column, row, pixelReader.getColor(column, row));

                newColorRGB = matcher.getMatch(currentColor);
                rgbToArray(newColor, newColorRGB);

                pixelWriter.setColor(column, row, newColorRGB);
                spreadError(currentColor, newColor, column, row);
            }
            bridgeClass.updateProgress(1);
        }

        return imageNew;
    }

    private void rgbToArray(double[] array, Color color) {  //gets RGB values for current pixel
        array[0] = (int) (255 * color.getRed());		    //get the color value (returns a 0 to 1 value, multiply by 255 to get proper value)
        array[1] = (int) (255 * color.getGreen());
        array[2] = (int) (255 * color.getBlue());
    }

}
