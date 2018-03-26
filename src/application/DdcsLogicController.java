package application;

import java.util.*;

import javafx.scene.image.Image;

public class DdcsLogicController {

	private DdcsBridge bridgeClass = DdcsBridge.getInstance();
	private DdcsImage image = DdcsImage.getInstance();
	private DdcsDither dither = DdcsDither.getInstance();

	private DdcsFileManager fileManager = new DdcsFileManager();					//class that will be managing all the file operations (opening, validating, etc.)
	private DdcsImageProcessor imageProcessor = new DdcsImageProcessor();			//class that will handle processing the image
	private DdcsAdaptivePalette adaptivePaletteCalc = new DdcsAdaptivePalette();	//class that will calculate the adaptive palette

	public Image getNewImage() {	//get and send off a selected image from a FileChooser; also remember it so it can be be used later
		fileManager.loadBaseImage();
		return image.image();
	}

	public Image processImage() {	//process the image according to the currently selected instructions

        workingPalette = selectedPalette;

		imageProcessor.processImage(workingPalette);

		//setting the loading progress to say complete or, if you used an adaptive palette, the number of colors in the original image
		//doing this here out of convenience, might be appropriate to move it to the document controller in the future
		if(selectedPalette.id().equals("adaptive")) {
			adaptivePaletteCalc.displayOriginalColorCount();
		} else {
	        bridgeClass.updateProgressInfo("complete !");
		}

		return image.processedImage();
	}

	public void saveImage() { fileManager.saveImage(workingPalette); }



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

	public void updateSelectedPalette(int index) { selectedPalette = paletteList.get(index); }

	public void updateSelectedDither(String name) { dither.setDitherName(name); }

	public int getPaletteSize() { return selectedPalette.size(); }

	public void generateAdaptivePalette(int colorCount) {

        int index = getPaletteIndex("adaptive");

        paletteList.remove(index);

        paletteList.add(index, new DdcsPalette(
                "Adaptive Palette",
                "adaptive",
                false,
                "adaptive",
                false,
                adaptivePaletteCalc.getAdaptivePalette(colorCount)));

        updateSelectedPalette(index);
    }

    public void updateColorListDisplay() {
	    bridgeClass.updateColorList(selectedPalette.get());
    }

    public void saveUserColorList(String colorsString) {
	    fileManager.savePalette(colorsString);
    }

	public void validateUserColorList(String colorsString) {

        int[][] colorArray = validateColors(colorsString);

        if (colorArray.length == 0) { colorArray = new int[][]{{0,0,0}}; }

        int index = getPaletteIndex("user");

        paletteList.remove(index);

        paletteList.add(index, new DdcsPalette(
                "- User defined palette -",
                "user",
                false,
                "user",
                false,
                colorArray));

        updateSelectedPalette(index);
    }



    public void sortPalette(boolean sort) {
        paletteList.get(0).setSortOverride(sort);   //static value, setting it in one carries to all
    }
    public void matchingStyleOverride(int type) {
	    paletteList.get(0).setMachOverride(type);   //static value, setting it in one carries to all
    }


    public void setColorIntensityValues(double iR, double iG, double iB) {
        paletteList.get(0).setIntensities(iR, iG, iB);  //static values, setting it in one carries to all
        imageProcessor.setColorIntensityValues(iR, iG, iB);
    }




    public void showPaletteViewer(String colorsString) {

        int[][] colorArray = validateColors(colorsString);
        bridgeClass.setColors(colorArray);
	    bridgeClass.showStage();
    }




    private int[][] validateColors(String colorsString) {
        String[] colorStringArray = colorsString.split("\n");
        ArrayList<int[]> colorList = new ArrayList<>();

        for (String color: colorStringArray) {
            DdcsFileManager.colorStringToArray(colorList, color);
        }

        return colorList.toArray(new int[colorList.size()][3]);
    }






    private int getPaletteIndex(String idOrName) {
	    int index = 0;
	    while( !idOrName.equals(paletteList.get(index).id()) && !idOrName.equals(paletteList.get(index).name()) ) { index++; }
	    return index;
    }




	public Image getPaletteImage(int index) {	//take the name of the palette currently selected and return the preview image
		try {
			return new Image(this.getClass().getResourceAsStream("/palette_images/" + paletteList.get(getPaletteIndex(visiblePalettes[index])).imageName() + ".png"));
        } catch(Exception e) { bridgeClass.handleError(classID, "01", e); }
        return new Image(this.getClass().getResourceAsStream("/palette_images/error.png"));
	}

	public String[] toggleExtraPalettes(boolean showAll) {
        ArrayList<String> paletteNames = new ArrayList<>();

        for(DdcsPalette palette : paletteList) {
            if( (!palette.hidden()) || (palette.hidden() && showAll) ) { paletteNames.add(palette.name()); }
        }

        visiblePalettes = new String[paletteNames.size()];
        visiblePalettes = paletteNames.toArray(visiblePalettes);

        return visiblePalettes;
    }


    public String[] loadPalettes() {	//load all the palette data from the text files into the HashMap

        String[] internalPaletteList = fileManager.getBuiltInPaletteList();
        Map<String, String> metaData;
        String paletteFile;
        ArrayList<String> paletteNames = new ArrayList<>();

        for(String palette : internalPaletteList) {

            paletteFile = "/palette_txt/" + palette + ".txt";

            metaData = fileManager.getMetaData(paletteFile, false);

            paletteList.add(new DdcsPalette(
                    metaData.get("name"),
                    metaData.get("id"),
                    metaData.get("mapped").equals("true"),
                    metaData.get("image"),
                    metaData.get("hidden").equals("true"),
                    fileManager.validatePalette(paletteFile, false)));

            if( metaData.get("hidden").equals("false") ) {
                paletteNames.add(metaData.get("name"));
            }
        }

        visiblePalettes = new String[paletteNames.size()];
        visiblePalettes = paletteNames.toArray(visiblePalettes);

        return visiblePalettes;
    }



    private DdcsPalette selectedPalette;
	private DdcsPalette workingPalette;

	private ArrayList<DdcsPalette> paletteList = new ArrayList<>();
	private String[] visiblePalettes;

    private final String classID = "02";	//used as a reference when displaying errors
}
