package application;

import java.util.ArrayList;
import java.util.List;

import javafx.scene.image.Image;

public class DdcsLogicController {

	private DdcsBridge bridgeClass = DdcsBridge.getInstance();
	private DdcsImage image = DdcsImage.getInstance();
	private DdcsDither dither = DdcsDither.getInstance();
    private DdcsPalette palette = DdcsPalette.getInstance();

	private DdcsFileManager fileManager = new DdcsFileManager();					//class that will be managing all the file operations (opening, validating, etc.)
	private DdcsImageProcessor imageProcessor = new DdcsImageProcessor();			//class that will handle processing the image
	private DdcsAdaptivePalette adaptivePaletteCalc = new DdcsAdaptivePalette();	//class that will calculate the adaptive palette


	public Image getNewImage() {	//get and send off a selected image from a FileChooser; also remember it so it can be be used later
		fileManager.loadBaseImage();
		return image.image();
	}

	public Image processImage() {	//process the image according to the currently selected instructions

		imageProcessor.processImage();

		//setting the loading progress to say complete or, if you used an adaptive palette, the number of colors in the original image
		//doing this here out of convenience, might be appropriate to move it to the document controller in the future
		if(palette.selectedPalette().equals("Adaptive Palette")) {
			adaptivePaletteCalc.displayOriginalColorCount();
		} else {
	        bridgeClass.updateProgressInfo("complete !");
		}

		return image.processedImage();
	}

	public void saveImage() { fileManager.saveImage(); }



	public Image getNullImage() { return image.nullImage(); }

	public void loadUserPalette() {						//load and validate the user defined palette, also get it's size

        int[][] userPalette = fileManager.loadUserPalette();

        if (userPalette.length > 1) {
            bridgeClass.updateColorList(userPalette);
        } else {
            if (!(userPalette[0][0] == 0 && userPalette[0][1] == 0 && userPalette[0][2] == 0)) {
                bridgeClass.updateColorList(userPalette);
            }
        }
	}

	public void updateSelectedPalette(String name) {
	    palette.setSelectedPalette(name);
	}

	public void updateSelectedDither(String name) { dither.setDitherName(name); }

	public int getPaletteSize() { return palette.size(); }

	public void generateAdaptivePalette(int colorCount) {
        palette.addPaletteData(colorCount, adaptivePaletteCalc.getAdaptivePalette(colorCount));
        updateSelectedPalette("Adaptive Palette");  //call to update the palette array with the new data
    }

    public void updateColorListDisplay() {
	    bridgeClass.updateColorList(palette.paletteArray());
    }

    public void saveUserColorList(String colorsString) {
	    fileManager.savePalette(colorsString);
    }

	public void validateUserColorList(String colorsString) {
	    String[] colorStringArray = colorsString.split("\n");
        ArrayList<int[]> colorList = new ArrayList<>();
        int[][] colorArray;

        for (String color: colorStringArray) {
            DdcsFileManager.colorStringToArray(colorList, color);
        }

        colorArray = colorList.toArray(new int[colorList.size()][3]);

        if (colorArray.length == 0) {
            palette.addPaletteData("- User defined palette -");
        } else {
            palette.addPaletteData("- User defined palette -", colorArray);
        }

        updateSelectedPalette("- User defined palette -");

    }






	public Image getPaletteImage(String name) {	//take the name of the palette currently selected and return the preview image
		try {
			return new Image(this.getClass().getResourceAsStream("/palette_images/" + name + ".png"));
        } catch(Exception e) { bridgeClass.handleError(classID, "01", e); } return getNullImage();
	}

    public void loadPalettes(List<String> palettes) {	//load all the palette data from the text files into the HashMap
        for (String subPalette : palettes) {
            if (subPalette.equals("- None -") || subPalette.equals("Adaptive Palette") || subPalette.equals("- User defined palette -")) {	//these palettes are defined elsewhere, so just put in the place holder null palette (the 'none' palette will remain null of course)
                palette.addPaletteData(subPalette);
            } else {
                palette.addPaletteData(subPalette, fileManager.validatePalette("/palette_txt/" + subPalette + ".txt", "internal"));
            }
        }
    }

    private final String classID = "02";	//used as a reference when displaying errors
}
