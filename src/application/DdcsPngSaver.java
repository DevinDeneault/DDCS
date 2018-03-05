package application;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import javafx.embed.swing.SwingFXUtils;
import javafx.stage.FileChooser;

public class DdcsPngSaver {

	DdcsPngSaver(String title) {
		saver.setTitle(title);
		setExtensionPNG();
	}

	private DdcsBridge bridgeClass = DdcsBridge.getInstance();
	private DdcsImage image = DdcsImage.getInstance();
	private DdcsPalette palette = DdcsPalette.getInstance();

	private FileChooser saver = new FileChooser();
	private String savedFile = null;		//the full directory to the previously saved file
	private String fileMatcher = ".*err";


	public void saveImage() {
		try {

			File previousDirectory = null;

			if(savedFile != null) {															                //make sure there was a previously saved file that can be used
				previousDirectory = new File(savedFile.substring(0, savedFile.lastIndexOf("\\") + 1));	//cut off the file name, leaving just the directory
			}

			saver.setInitialDirectory(previousDirectory);

			File file = saver.showSaveDialog(null);											//open the file saver and set the file name/directory to be saved

			if (file == null || !file.getPath().matches(fileMatcher)) {						                //make sure something is defined
				return;
			} else {
			    try {
			    	if(palette.size() < 257) {													            //if the number of colors used is less than 257
			    		saveImageAsIndexed(file, palette.loadedPaletteArray());				                //save as a PNG with a indexed palette
			    	} else {
			    		ImageIO.write(SwingFXUtils.fromFXImage(image.processedImage(), null), "PNG", file);	//save as a normal PNG (PNGs can only have up to 256 colors in a palette index)
			    	}
			    } catch (IOException ignored) {

			    }
			}

			savedFile = file.toString();

        } catch(Exception e) { bridgeClass.handleError(classID, "05", e); }
	}



	private void setExtensionPNG() {
		try {

			saver.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("png", "*.png"));
			fileMatcher = ".*png";

        } catch(Exception e) { bridgeClass.handleError(classID, "04", e); }
	}




	private void saveImageAsIndexed(File fileLocation, int[][] palette) {	//main method
		try {

			BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image.processedImage(), null);			//convert the image into the old style BufferedImage, only way I could find to do this whole process is with this
			IndexColorModel colorModel = makeColorModel(palette);							//make the color palette information
			BufferedImage indexedImage = convertImage(bufferedImage, colorModel);			//make a new image using the indexed palette
			saveImage(indexedImage, fileLocation);											//save the image

		} catch(Exception e) { bridgeClass.handleError(classID, "03", e); }
	}

	private void saveImage(BufferedImage image, File location) {						//save the image
		try {
			
			

			ImageIO.write(image, "png", location);										//save the file at the location as a png

		} catch(Exception e) { bridgeClass.handleError(classID, "02", e); }
	}

	private IndexColorModel makeColorModel(int[][] palette) {	//make the color palette for the image to use
		try {

			int size = palette.length;							//get the length of the palette matrix

			byte[] reds = new byte[size];						//create an array to hold the red values
			byte[] greens = new byte[size];						//create an array to hold the green values
			byte[] blues = new byte[size];						//create an array to hold the blue values

			for(int index = 0; index < size; index++) { 		//iterate through all the colors
			    reds[index] = (byte) palette[index][0]; 		//convert the red number into a byte value and store it in the red array
			    greens[index] = (byte) palette[index][1];		//convert the green number into a byte value and store it in the green array
			    blues[index] = (byte) palette[index][2];		//convert the blue number into a byte value and store it in the blue array
			}

			return new IndexColorModel(8, size, reds, greens, blues);

        } catch(Exception e) { bridgeClass.handleError(classID, "01", e); } return null;
	}

	private BufferedImage convertImage(BufferedImage original, IndexColorModel color_model) {	//turn the existing image into an image using an indexed palette
	    try {

	    	BufferedImage newImage = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_BYTE_INDEXED, color_model);	//make the new image object, defined as indexed, using the palette we previously made

			for (int x = 0; x < original.getWidth(); x++) {										//iterate through the image rows
			    for (int y = 0; y < original.getHeight(); y++) {								//iterate through the pixels
				    newImage.setRGB(x, y, original.getRGB(x, y));								//copy the pixel from the old image into the new image; as long as the RGB value is in the index it will work, if it is not, then it will find the closest estimate
			    }
			}

			return newImage;

		} catch(Exception e) { bridgeClass.handleError(classID, "00", e); } return null;
	}

    private final String classID = "05";	//used as a reference when displaying errors
}