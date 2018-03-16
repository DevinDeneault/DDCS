package application;

import java.util.Arrays;

import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeySizeException;

public class DdcsImageProcessor {

	private DdcsBridge bridgeClass = DdcsBridge.getInstance();
	private DdcsImage image = DdcsImage.getInstance();
	private DdcsDither dither = DdcsDither.getInstance();

	private KDTree kdTree;                          //KDTree for searching for closest matching colors

	private WritableImage imageNew;		            //the variable of the new blank, writable, image

    private PixelReader pixelReader;	            //object that reads pixel data
    private PixelWriter pixelWriter;	            //object that writes pixel data

    private double[] currentColor = new double[3];	//the current pixel's color values
    private double[] newColor = new double[3];		//the new pixel's color values

    private int[][][] errorMatrix;                  //the matrix that will be used to store the error data for error-diffusion dithering

    private double intensityRed = 0.2989;
    private double intensityGreen = 0.5870;
    private double intensityBlue = 0.1140;


    private DdcsPalette palette;

	public void processImage(DdcsPalette _palette) {
		try {

			palette = _palette;

			pixelReader = image.image().getPixelReader();
			imageNew = new WritableImage(image.width(), image.height());	//make a new, blank writable image
			pixelWriter = imageNew.getPixelWriter();						//make the pixel writer for the new writable image

			bridgeClass.updateProgressInfo("processing image . . .");

            convertPaletteAndDither();

        } catch(Exception e) { bridgeClass.handleError(classID, "07", e); }
	}






	private Color treeNodeToColor() {	//take the 'Object' (in this case just an integer) returned by the KDTree search and convert it into a palette color value
        Object node = null;
        try {
            node = kdTree.nearest(currentColor);
        } catch (KeySizeException e) {
            e.printStackTrace();
        }

        int index = Integer.parseInt(node.toString());

        int red = palette.get(index, 0);
        int green = palette.get(index, 1);
        int blue = palette.get(index, 2);

		newColor[0] = red;
		newColor[1] = green;
		newColor[2] = blue;

		return Color.rgb(red, green, blue);
	}

	private double[] arrayIntToDouble(int[] input) {	//convert an array of integers to an array of doubles

		double[] output = new double[3];
	    for(int i=0; i < 3; i++) {
	    	output[i] = input[i];
	    }
	    return output;
	}





    interface colorMatcher {
        Color matchColor();
    }

    private colorMatcher[] colorMatchers = new colorMatcher[] {
            this::treeNodeToColor,
            this::findNearestExhaustiveSearch,
            this::mapColor
    };

	private Color mapColor() {
        double scale = (double) (palette.size() - 1) / 255;

        double intensity = intensityRed * currentColor[0] + intensityGreen * currentColor[1] + intensityBlue * currentColor[2];

        double scaledToPalette = scale * intensity;

        int paletteIndex = Math.max(0, (int) Math.round(scaledToPalette));

        int nRed = palette.get(paletteIndex, 0);
        int nGreen = palette.get(paletteIndex, 1);
        int nBlue = palette.get(paletteIndex, 2);

        newColor[0] = nRed;						//define the new color's RGB values
        newColor[1] = nGreen;
        newColor[2] = nBlue;

        return Color.rgb(nRed, nGreen, nBlue);	//return the new color
    }




















	private void convertPaletteAndDither() {
		try {

			boolean useKdTree;
			if(palette.size() < 31) {	//if there are few colors the KDTree performance is equal to or worse than the exhaustive search and -at very low numbers- can produce strange results and errors, so use exhaustive search
				useKdTree = false;
			} else {						//if there aren't to few colors, use the KDTree, it is exponentially faster the bigger the palette is
				useKdTree = true;

				kdTree = new KDTree(3);													//create a new KDTree

                for(int index = 0; index < palette.size(); index++) {

                    kdTree.insert(arrayIntToDouble(palette.get(index)), index);				//send all the palette values to the KDTree, the tree will return the index value when searched
                }
			}


            int matcher;

            if (palette.mapped()) {
                matcher = 2;
            } else if (useKdTree) {
                matcher = 0;
            } else {
                matcher = 1;
            }




			int imageHeight = image.height();
			int imageWidth = image.width();



			bridgeClass.updateProgress(imageHeight);

			switch (dither.type()) {
				case "ordered":
					for (int row = 0; row < imageHeight; row++) {
						for (int column = 0; column < imageWidth; column++) {
							defineCurrentPixel(column, row);                                    //grab the current pixel
							addThreshold(column, row);                                            //add threshold from the ordered dither matrix ("bayer matrix") to the current pixel values
                            pixelWriter.setColor(column, row, colorMatchers[matcher].matchColor());
						}
						bridgeClass.updateProgress(1);
					}
					break;
				case "none":
					for (int row = 0; row < imageHeight; row++) {
						for (int column = 0; column < imageWidth; column++) {
							defineCurrentPixel(column, row);
                            pixelWriter.setColor(column, row, colorMatchers[matcher].matchColor());
						}
						bridgeClass.updateProgress(1);
					}
					break;
				default:                                                                        //any other dither is assumed to be error-diffusion dither
					errorMatrix = new int[imageHeight][imageWidth][3];                        //create the error matrix; a matrix of equivalent size to the image to hold the error values while the image is being processed


					for (int row = 0; row < imageHeight; row++) {
						for (int column = 0; column < imageWidth; column++) {
							defineCurrentPixelWithError(column, row);                            //grab the current pixel, modified by the error value associated with that pixel position
                            pixelWriter.setColor(column, row, colorMatchers[matcher].matchColor());
							spreadError(column, row);                                            //find the error between the old and new colors and spread those in the defined areas in the error matrix
						}
						bridgeClass.updateProgress(1);
					}
					break;
			}

			bridgeClass.updateProgress(0);

			image.setProcessedImage(imageNew);  //----------------------------------------------------- actual image update, may not be best for readability to have it here, this is a result of some refactoring elsewhere, may change in the future

        } catch(Exception e) { bridgeClass.handleError(classID, "06", e); }
	}




	private void defineCurrentPixel(int column, int row) {		//gets RGB values for current pixel
		try {

			Color color = pixelReader.getColor(column, row);

			currentColor[0] = (int) (255 * color.getRed());		//get the color value (returns a 0 to 1 value, multiply by 255 to get proper value)
			currentColor[1] = (int) (255 * color.getGreen());
			currentColor[2] = (int) (255 * color.getBlue());

        } catch(Exception e) { bridgeClass.handleError(classID, "05", e); }
	}

	private void defineCurrentPixelWithError(int column, int row) {	//defines RGB values for current pixel, modified by the corresponding error value from the error matrix
		try {

			Color color = pixelReader.getColor(column, row);

			currentColor[0] = (int) ((255 * color.getRed()) + errorMatrix[row][column][0]);
			currentColor[1] = (int) ((255 * color.getGreen()) + errorMatrix[row][column][1]);
			currentColor[2] = (int) ((255 * color.getBlue()) + errorMatrix[row][column][2]);
			currentColor[0] = Math.min(Math.max(currentColor[0], 0), 255);			//this will cap the value to the range of 0 to 255
			currentColor[1] = Math.min(Math.max(currentColor[1], 0), 255);
			currentColor[2] = Math.min(Math.max(currentColor[2], 0), 255);

        } catch(Exception e) { bridgeClass.handleError(classID, "04", e); }
	}

	private void addThreshold(int column, int row) {	//adds the threshold value from the ordered dither matrix ("bayer matrix") to the pixel values
		try {

			int matrixSize = dither.gridSize();
			int threshold = dither.get((row % matrixSize), column % matrixSize);

			currentColor[0] += threshold;	//row % matrix size; 324 % 8 = 4; uses remainder to determine location on matrix, ensures uniform use of threshold matrix
			currentColor[1] += threshold;	//		this uniform use of the threshold matrix causes the cross-hatch patter visible on final image
			currentColor[2] += threshold; 	//

            currentColor[0] = Math.min(Math.max(currentColor[0], 0), 255);			//this will cap the value to the range of 0 to 255
            currentColor[1] = Math.min(Math.max(currentColor[1], 0), 255);
            currentColor[2] = Math.min(Math.max(currentColor[2], 0), 255);


        } catch(Exception e) { bridgeClass.handleError(classID, "03", e); }
	}




	private Color findNearestExhaustiveSearch() {	//finds palette color that is closest to current color; this is an exhaustive search, very inefficient unless there are few matches to test
		try {

            int[] distances = new int[palette.size()];					//array of distances between current color and palette colors (they are being treated as coords in 3 dimensional space)
            //int[] distances_sorted = new int[palette.getPaletteSize()];				//this will be used as a copy of the previous array, but sorted
			int index;																//this value will be used to keep track of the index of the smallest distance in the list

            for (int i = 0; i < palette.size(); i++) {
                int rDistance = (int) (currentColor[0] - palette.get(i, 0));
                int gDistance = (int) (currentColor[1] - palette.get(i, 1));
                int bDistance = (int) (currentColor[2] - palette.get(i, 2));
		     	int distance = (rDistance)*(rDistance) + (gDistance)*(gDistance) + (bDistance)*(bDistance);	//find the distance between the two points (returns 'squared distance' to avoid make costly square rooot calculations)
                distances[i] = distance;																	//add the newly found distance to the list of distances
                //distances_sorted[i] = distance;																//add the newly found distance to the list of distances that will be sorted later
			}

            //Arrays.sort(distances_sorted);								//sort the second list of distances, this means the first list element will be the smallest value in the list
            //index = indexOfIntArray(distances, distances_sorted[0]);	//find the index from the first list that corresponds with the first element from the sorted list; this is the index of the nearest color in the palette array
			index = indexOfSmallestInt(distances);

			int nRed = palette.get(index, 0);
            int nGreen = palette.get(index, 1);
            int nBlue = palette.get(index, 2);

			newColor[0] = nRed;						//define the new color's RGB values
			newColor[1] = nGreen;
			newColor[2] = nBlue;

			return Color.rgb(nRed, nGreen, nBlue);	//return the new color

        } catch(Exception e) { bridgeClass.handleError(classID, "02", e); } return null;
	}



	private void spreadError(int column, int row) {			//find and spread the error onto the error matrix for use in error-diffusion dithering
		try {

			int redError = (int) (currentColor[0] - newColor[0]);							//red error is the current value minus the new value
			int greenError = (int) (currentColor[1] - newColor[1]);
			int blueError = (int) (currentColor[2] - newColor[2]);
			int numerator;									//each error-diffusion method splits the error into fractions to spread it around the current pixel; this is the numerator in the fraction (i.e. 2/X or 1/X)
			int denominator = dither.split();			//this is the denominator in the fraction (i.e. X/48 or X/16)
			int column_new;
			int row_new;

			for (int i = 0; i < dither.arraySize(); i++) {	//goes through the list of locations to spread the error to

				numerator = dither.get(i, 2);				//defines how much of the error is going to be placed at the location (i.e. 4/X or 5/X)
			    column_new = column + (dither.get(i, 1));	//defines X/Y of locations; values from the dither array are coordinates relative to the current pixel
			    row_new = row + (dither.get(i, 0));

			    //attempt at speed optimization, instead of using conditionals, just let it error out when the pixel it's looking for is outside of the image bounds and continue (no noticeable difference, but much easier to read the code)
			    //it's either this or a 4 argument conditional for every single point
			    try {
			    	errorMatrix[row_new][column_new][0] += getErrorPortion(redError, numerator, denominator);
			    	errorMatrix[row_new][column_new][1] += getErrorPortion(greenError, numerator, denominator);
			    	errorMatrix[row_new][column_new][2] += getErrorPortion(blueError, numerator, denominator);
			    } catch (Exception err) {
			    	//pixel out of bounds; do nothing
			    }
			}

        } catch(Exception e) { bridgeClass.handleError(classID, "01", e); }
	}


	private int getErrorPortion(int error, int numerator, int denominator) {				//takes the error and the fraction of the error to be used and returns the resulting fraction of the error
		try {
			double errorPortion;															//the actual value; fractional portion of the error
			errorPortion = ((double) numerator / (double) denominator) * (double) error;
			return (int) Math.floor(errorPortion + 0.5d);									//return the value, rounded to the nearest whole number
        } catch(Exception e) { bridgeClass.handleError(classID, "00", e); } return 0;
	}



	private int indexOfSmallestInt(int[] array) {
		int[] sortArray = array.clone();
		Arrays.sort(sortArray);
		int smallestValue = sortArray[0];

		for (int i = 0; i < array.length; i++) {
			if (smallestValue == array[i]) { return i; }
		}
		return -1;
	}



    public void setColorIntensityValues(double iR, double iG, double iB) {
        intensityRed = iR;
        intensityGreen = iG;
        intensityBlue = iB;
    }



	private final String classID = "07";	//used as a reference when displaying errors
}
