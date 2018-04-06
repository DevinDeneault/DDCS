package application;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

//a file chooser to save an image
//  only saves as PNG by design
//      lossy formats such as jpg defeats the purpose of the program in general
//      other loss-less formats such as BMP are much larger than PNG, especially if the PNG can be saved with an included palette
//  if a PNG has 256 colors or less you can have a built in palette in the image
//      in that case the pixel is just an index value instead of a full RGB value

public class PngSaver {

    PngSaver(String title) {
        saver.setTitle(title);
        setExtensionPNG();
    }

    private FileChooser saver = new FileChooser();
    private String savedFile = null;        //the full directory to the previously saved file
    private String fileMatcher = ".*err";   //a regex-ready string used to verify a file has the proper extension - initial value should never be seen

    public void saveImage(Palette palette, IdedImage image) {

        File previousDirectory = null;

        if(savedFile != null)     //check if there is a previously saved file that can be used
            previousDirectory = new File(savedFile.substring(0, savedFile.lastIndexOf("\\") + 1));

        saver.setInitialDirectory(previousDirectory);

        File file = saver.showSaveDialog(null);            //open the file saver and set the file name/directory to be saved

        if (file == null || !file.getPath().matches(fileMatcher))       //make sure something is defined
            return;
        else {
            try {
                if(palette.size() < 257)
                    saveImageAsIndexed(file, palette.get(), image);                                     //save as a PNG with a indexed palette
                else
                    ImageIO.write(SwingFXUtils.fromFXImage(image, null), "PNG", file); //save as a normal PNG (PNGs can only have up to 256 colors in a palette index)
            } catch (IOException e) { e.printStackTrace(); }
        }

        savedFile = file.toString();
    }


    private void setExtensionPNG() {
        saver.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
        fileMatcher = ".*png";
    }

    private void saveImageAsIndexed(File fileLocation, int[][] palette, IdedImage image) {  //main method
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);          //convert the image into the old style BufferedImage, only way I could find to do this whole process is with this
        IndexColorModel colorModel = makeColorModel(palette);                               //make the color palette information
        BufferedImage indexedImage = convertImage(bufferedImage, colorModel);               //make a new image using the indexed palette
        saveImage(indexedImage, fileLocation);                                              //save the image
    }

    private void saveImage(BufferedImage image, File location) {            //save the image
        try {
            ImageIO.write(image, "png", location);              //save the file at the location as a png
        } catch (IOException e) { e.printStackTrace(); }
    }

    private IndexColorModel makeColorModel(int[][] palette) {   //make the color palette for the image to use

        int size = palette.length;                              //get the length of the palette matrix

        byte[] reds = new byte[size];                           //create an array to hold the red values
        byte[] greens = new byte[size];                         //create an array to hold the green values
        byte[] blues = new byte[size];                          //create an array to hold the blue values

        for(int index = 0; index < size; index++) {             //iterate through all the colors
            reds[index] = (byte) palette[index][0];             //convert the red number into a byte value and store it in the red array
	        greens[index] = (byte) palette[index][1];           //convert the green number into a byte value and store it in the green array
            blues[index] = (byte) palette[index][2];            //convert the blue number into a byte value and store it in the blue array
        }

        return new IndexColorModel(8, size, reds, greens, blues);

    }

    private BufferedImage convertImage(BufferedImage original, IndexColorModel color_model) {   //turn the existing image into an image using an indexed palette

        BufferedImage newImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, color_model);    //make the new image object, defined as indexed, using the palette we previously made

        for (int x = 0; x < original.getWidth(); x++) {
            for (int y = 0; y < original.getHeight(); y++) {
                newImage.setRGB(x, y, original.getRGB(x, y));
            }
        }

        return newImage;
    }

}