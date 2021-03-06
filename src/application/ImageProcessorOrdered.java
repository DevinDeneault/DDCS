package application;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

//an image processor that uses ordered dithering

public class ImageProcessorOrdered extends DitherOrderedAbstract implements ImageProcessor {

    private Bridge bridgeClass = Bridge.getInstance();

    private ColorMatcher matcher;
    private Image image;
    ImageProcessorOrdered(ColorMatcher _matcher, DitherData _dither, Image _image) {
        matcher = _matcher;
        dither = _dither;
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
                addThreshold(currentColor, column, row);                        //add threshold from the ordered dither matrix ("bayer matrix") to the current pixel values
                pixelWriter.setColor(column, row, matcher.getMatch(currentColor));
            }
            bridgeClass.updateProgress(1);
        }

        return imageNew;
    }
}
