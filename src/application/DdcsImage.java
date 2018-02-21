package application;

import javafx.scene.image.Image;

public class DdcsImage {

    //--------------------------------------------------------singleton
    private static volatile DdcsImage instance = null;

    private DdcsImage() { }

    public static DdcsImage getInstance() {
        if (instance == null) {
            synchronized (DdcsImage.class) {
                if (instance == null) {
                    instance = new DdcsImage();
                }
            }
        }
        return instance;
    }
    //--------------------------------------------------------

    private Image nullImage = new Image(this.getClass().getResourceAsStream("/images/null.png"));

    private Image workingImage = nullImage;     //the image currently being worked with
    private Image processedImage = nullImage;   //the image after being processed

    private int imageHeight = 0;
    private int imageWidth = 0;

    private int imageCount = 0;                 //this number will be used to keep track of how many images have been loaded


    public void setImage(Image image) {
        workingImage = image;

        imageHeight = (int) workingImage.getHeight();
        imageWidth = (int) workingImage.getWidth();

        imageCount++;
    }

    public void setProcessedImage(Image image) { processedImage = image; }

    public Image processedImage() { return processedImage; }

    public Image nullImage() { return nullImage; }

    public Image image() { return workingImage; }

    public int imageNumber() { return imageCount; }

    public int width() { return imageWidth; }

    public int height() { return imageHeight; }

}
