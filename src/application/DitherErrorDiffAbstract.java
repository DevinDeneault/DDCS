package application;

import javafx.scene.paint.Color;

abstract class DitherErrorDiffAbstract {

    DitherData dither;
    int imageHeight;
    int imageWidth;
    int[][][] errorMatrix;  //the matrix that will be used to store the error data for error-diffusion dithering

    void spreadError(double[] currentColor, double[] newColor, int column, int row) {   //find and spread the error onto the error matrix for use in error-diffusion dithering
        int redError = (int) (currentColor[0] - newColor[0]);		//red error is the current value minus the new value
        int greenError = (int) (currentColor[1] - newColor[1]);
        int blueError = (int) (currentColor[2] - newColor[2]);
        int numerator;									            //each error-diffusion method splits the error into fractions to spread it around the current pixel; this is the numerator in the fraction (i.e. 2/X or 1/X)
        int denominator = dither.split();			                //this is the denominator in the fraction (i.e. X/48 or X/16)
        int column_new;
        int row_new;

        for( int i = 0; i < dither.arraySize(); i++ ) {	            //goes through the list of locations to spread the error to

            numerator = dither.get(i, 2);				        //defines how much of the error is going to be placed at the location (i.e. 4/X or 5/X)
            column_new = column + (dither.get(i, 1));	        //defines X/Y of locations; values from the dither array are coordinates relative to the current pixel
            row_new = row + (dither.get(i, 0));

            if( column_new < imageWidth && column_new > 0 && row_new < imageHeight ) {
                errorMatrix[row_new][column_new][0] += getErrorPortion(redError, numerator, denominator);
                errorMatrix[row_new][column_new][1] += getErrorPortion(greenError, numerator, denominator);
                errorMatrix[row_new][column_new][2] += getErrorPortion(blueError, numerator, denominator);
            }
        }
    }


    private int getErrorPortion(int error, int numerator, int denominator) {			//takes the error and the fraction of the error to be used and returns the resulting fraction of the error
        double errorPortion;															//the actual value; fractional portion of the error
        errorPortion = ((double) numerator / (double) denominator) * (double) error;
        return (int) Math.floor(errorPortion + 0.5d);									//return the value, rounded to the nearest whole number
    }

    void defineCurrentPixelWithError(double[] currentColor, int column, int row, Color imageColor) {	//defines RGB values for current pixel, modified by the corresponding error value from the error matrix

        currentColor[0] = (int) ((255 * imageColor.getRed()) + errorMatrix[row][column][0]);
        currentColor[1] = (int) ((255 * imageColor.getGreen()) + errorMatrix[row][column][1]);
        currentColor[2] = (int) ((255 * imageColor.getBlue()) + errorMatrix[row][column][2]);
        currentColor[0] = Math.min(Math.max(currentColor[0], 0), 255);			//cap the value to the range of 0 to 255
        currentColor[1] = Math.min(Math.max(currentColor[1], 0), 255);
        currentColor[2] = Math.min(Math.max(currentColor[2], 0), 255);

    }




}
