package application;

import java.util.*;

import javafx.scene.image.Image;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeyDuplicateException;
import net.sf.javaml.core.kdtree.KeySizeException;

public class LogicController {

	private Bridge bridgeClass = Bridge.getInstance();
	private DitherDataFactory ditherFactory = DitherDataFactory.getInstance();

	private FileManager fileManager = new FileManager();					//class that will be managing all the file operations (opening, validating, etc.)
	private AdaptivePalette adaptivePaletteCalc = new AdaptivePalette();	//class that will calculate the adaptive palette

    private IdedImage nullImage = new IdedImage(this.getClass().getResourceAsStream("/images/null.png"));

    private IdedImage image = nullImage;

    private String selectedDither;

	public Image getNewImage() {	//get and send off a selected image from a FileChooser; also remember it so it can be be used later
		image = fileManager.loadBaseImage();
        return image;
	}

	public Image processImage() {	//process the image according to the currently selected instructions

        workingPalette = selectedPalette;

//		imageProcessor.processImage(workingPalette);//---------------------------------------------------------------------------------------------------------------------------------------------------------------------
        KDTree kdTree;
        boolean useKdTree;
        useKdTree = workingPalette.size() >= 31;

        ColorMatcher matcher;
        ImageProcessor imageProcessor;

        if (workingPalette.mapped()) {
            matcher = new ColorMatcherMap(workingPalette);
        } else if (useKdTree) {
            kdTree = new KDTree(3);
            try {
                for(int index = 0; index < workingPalette.size(); index++) {
                    kdTree.insert(arrayIntToDouble(workingPalette.get(index)), index);
                }
            } catch (KeySizeException | KeyDuplicateException e) { e.printStackTrace(); }

            matcher = new ColorMatcherKdTree(workingPalette, kdTree);
        } else {
            matcher = new ColorMatcherExhaustive(workingPalette);
        }

        DitherData dither = ditherFactory.getDitherData(selectedDither);

        switch (dither.type()) {
            case "ordered":
                imageProcessor = new ImageProcessorOrdered(matcher, dither, image);
                break;
            case "none":
                imageProcessor = new ImageProcessorNone(matcher, image);
                break;
            default:
                imageProcessor = new ImageProcessorErrorDiff(matcher, dither, image);
                break;
        }

//        bridgeClass.updateProgressInfo("processing image . . .");
        bridgeClass.updateProgress((int) image.getHeight());

//        image.setProcessedImage(imageProcessor.processImage());

        //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

		//setting the loading progress to say complete or, if you used an adaptive palette, the number of colors in the original image
		//doing this here out of convenience, might be appropriate to move it to the document controller in the future
		if(selectedPalette.id().equals("adaptive")) {
			adaptivePaletteCalc.displayOriginalColorCount();
		} else {
//	        bridgeClass.updateProgressInfo("complete !");
		}

//		return image.processedImage();
        return imageProcessor.processImage();
	}

	public void saveImage() { fileManager.saveImage(workingPalette, image); }



	public Image getNullImage() { return nullImage; }

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

	public void updateSelectedDither(String name) { selectedDither = name; }

	public int getPaletteSize() { return selectedPalette.size(); }

	public void generateAdaptivePalette(int colorCount) {

        int index = getPaletteIndex("adaptive");

        paletteList.remove(index);

        paletteList.add(index, new Palette(
                "Adaptive Palette",
                "adaptive",
                false,
                "adaptive",
                false,
                adaptivePaletteCalc.getAdaptivePalette(colorCount, image)));

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

        paletteList.add(index, new Palette(
                "- User defined palette -",
                "user",
                false,
                "user",
                false,
                colorArray));

        updateSelectedPalette(index);
    }



//    public void sortPalette(boolean sort) {
//        paletteList.get(0).setSortOverride(sort);   //static value, setting it in one carries to all
//    }
    public void matchingStyleOverride(int type) {
	    paletteList.get(0).setMachOverride(type);   //static value, setting it in one carries to all
    }


    public void setColorIntensityValues(double iR, double iG, double iB) {
        paletteList.get(0).setIntensities(iR, iG, iB);  //static values, setting it in one carries to all
//        imageProcessor.setColorIntensityValues(iR, iG, iB);
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
            FileManager.colorStringToArray(colorList, color);
        }

        return colorList.toArray(new int[colorList.size()][3]);
    }

    public String getHelpText() {
	    return fileManager.loadHelpText();
    }





    private int getPaletteIndex(String idOrName) {
	    int index = 0;
	    while( !idOrName.equals(paletteList.get(index).id()) && !idOrName.equals(paletteList.get(index).name()) ) { index++; }
	    return index;
    }


	public Image getPaletteImage(int index) {	//take the name of the palette currently selected and return the preview image
        return new Image(this.getClass().getResourceAsStream("/palette_images/" + paletteList.get(getPaletteIndex(visiblePalettes[index])).imageName() + ".png"));
	}

	public String[] toggleExtraPalettes(boolean showAll) {
        ArrayList<String> paletteNames = new ArrayList<>();

        for(Palette palette : paletteList) {
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

            paletteList.add(new Palette(
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



    private Palette selectedPalette;
	private Palette workingPalette;

	private ArrayList<Palette> paletteList = new ArrayList<>();
	private String[] visiblePalettes;





    private double[] arrayIntToDouble(int[] input) {	//convert an array of integers to an array of doubles

        double[] output = new double[3];
        for(int i=0; i < 3; i++) {
            output[i] = input[i];
        }
        return output;
    }





    private class PaletteFactory {

    }

}
