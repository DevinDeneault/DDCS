package application;

import javafx.scene.image.Image;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeyDuplicateException;
import net.sf.javaml.core.kdtree.KeySizeException;
import java.util.ArrayList;
import java.util.Map;

//this is the main logic class, the document controller only calls to this class -
//  and this class manages all the other classes needed

public class LogicController {

    private Bridge bridgeClass = Bridge.getInstance();
    private DitherDataFactory ditherFactory = new DitherDataFactory();

    private FileManager fileManager = new FileManager();                    //class that will be managing all the file operations (opening, validating, etc.)
    private AdaptivePalette adaptivePaletteCalc = new AdaptivePalette();    //class that will calculate the adaptive palette

    private IdedImage nullImage = new IdedImage(this.getClass().getResourceAsStream("/images/null.png"));

    private IdedImage image = nullImage;
    private Image processedImage = nullImage;

    private String selectedDither;

    private Palette selectedPalette;
    private Palette workingPalette;

    private ArrayList<Palette> paletteList = new ArrayList<>();
    private String[] visiblePalettes;


    public IdedImage getNewImage() {    //get and send off a selected image from a FileChooser; also remember it so it can be be used later
        image = fileManager.loadBaseImage();
        return image;
    }



    public Image processImage() {   //process the image according to the currently selected options

        workingPalette = selectedPalette;

        //set the progress bar to a meaningful number
        //  in this case each completed row of the image is one unit of progress
        //  so the number of rows of the image is equal to the 100% mark
        bridgeClass.updateProgress((int) image.getHeight());

        processedImage = imageProcessorBuilder().processImage();

        return processedImage;
    }

    public void saveImage() { fileManager.saveImage(workingPalette, processedImage); }

    public Image getNullImage() { return nullImage; }

    public void loadUserPalette() {     //load and validate the user defined palette

        int[][] userPalette = fileManager.loadUserPalette();
        bridgeClass.updateColorDisplay(userPalette, "user");  //paletteList.get(getPaletteIndex("user")).imageName()
    }

    public void updateSelectedPalette(int index, boolean fromVisible) {

        if( fromVisible )   //is the index referring to the list of visible palettes or the list of all palettes?
            selectedPalette = paletteList.get(getPaletteIndex(visiblePalettes[index]));
        else
            selectedPalette = paletteList.get(index);
    }

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

        updateSelectedPalette(index, false);
    }

    public void updateColorListDisplay() {
        bridgeClass.updateColorDisplay(selectedPalette.get(), selectedPalette.imageName());
    }

    public void saveUserColorList(String colorsString) {
        fileManager.savePalette(colorsString);
    }

    public void updateUserPalette(String colorsString) {

        int[][] colorArray = validateColors(colorsString);

        if( colorArray.length == 0 ) { colorArray = new int[][]{{0,0,0}}; }

        int index = getPaletteIndex("user");

        paletteList.remove(index);

        paletteList.add(index, new Palette(
                "- User defined palette -",
                "user",
                false,
                "user",
                false,
                colorArray));

        updateSelectedPalette(index, false);
    }


    public void matchingStyleOverride(int type) {
        paletteList.get(0).setMachOverride(type);   //static value, setting it in one carries to all
    }


    public void setColorIntensityValues(double iR, double iG, double iB) {
        paletteList.get(0).setIntensities(iR, iG, iB);  //static values, setting it in one carries to all
    }

    public void showPaletteViewer(String colorsString) {
        int[][] colorArray = validateColors(colorsString);
        bridgeClass.setColors(colorArray);
        bridgeClass.showStage();
    }


    private int[][] validateColors(String colorsString) {
        String[] colorStringArray = colorsString.split("\n");
        ArrayList<int[]> colorList = new ArrayList<>();

        for( String color: colorStringArray )
            FileManager.colorStringToArray(colorList, color);

        return colorList.toArray(new int[colorList.size()][3]);
    }

    public String getHelpText() { return fileManager.loadHelpText(); }




    private int getPaletteIndex(String idOrName) {      //gets the index location of one of the palette objects in the palette map, this can  be done through either the id or the name
        int index = 0;
        while( !idOrName.equals(paletteList.get(index).id()) && !idOrName.equals(paletteList.get(index).name()) ) { index++; }
        return index;
    }

    //there are a number of more specialized/gimmicky/experimental palettes built in, but they aren't shown to prevent overloading the user with options
    //  this will toggle between showing and hiding them
    public String[] toggleExtraPalettes(boolean showAll) {
        ArrayList<String> paletteNames = new ArrayList<>();

        for( Palette palette : paletteList )
            if( !palette.hidden() || (palette.hidden() && showAll) ) { paletteNames.add(palette.name()); }

        visiblePalettes = new String[paletteNames.size()];
        visiblePalettes = paletteNames.toArray(visiblePalettes);

        return visiblePalettes;
    }


    public String[] loadPalettes() {    //load all the palette data from the text files into the HashMap

        String[] internalPaletteList = fileManager.getBuiltInPaletteList();
        Map<String, String> metaData;
        String paletteFile;
        ArrayList<String> paletteNames = new ArrayList<>();

        for( String palette : internalPaletteList ) {

            paletteFile = "/palette_txt/" + palette + ".txt";

            metaData = fileManager.getMetaData(paletteFile);

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


    private ImageProcessor imageProcessorBuilder() {    //build the imageProcessor object based on all the options selected and defined by the user
        KDTree kdTree;
        boolean useKdTree;
        useKdTree = workingPalette.size() >= 31;

        ColorMatcher matcher;
        ImageProcessor imageProcessor;

        if( workingPalette.mapped() )
            matcher = new ColorMatcherMap(workingPalette);
        else if( useKdTree ) {
            kdTree = new KDTree(3);
            try {
                for( int index = 0; index < workingPalette.size(); index++ )
                    kdTree.insert(arrayIntToDouble(workingPalette.get(index)), index);
            } catch (KeySizeException | KeyDuplicateException e) { e.printStackTrace(); }

            matcher = new ColorMatcherKdTree(workingPalette, kdTree);
        } else
            matcher = new ColorMatcherExhaustive(workingPalette);

        DitherData dither = ditherFactory.getDitherData(selectedDither);

        switch( dither.type() ) {
            case ORDERED:
                imageProcessor = new ImageProcessorOrdered(matcher, dither, image);
                break;
            case ERROR_DIFFUSION:
                imageProcessor = new ImageProcessorErrorDiff(matcher, dither, image);
                break;
            default:
                imageProcessor = new ImageProcessorNone(matcher, image);
                break;
        }

        return imageProcessor;
    }


    private double[] arrayIntToDouble(int[] input) {    //convert an array of integers to an array of doubles

        double[] output = new double[3];
        for( int i=0; i < 3; i++ )
            output[i] = input[i];
        return output;
    }

}
