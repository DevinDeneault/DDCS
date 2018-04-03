package application;

abstract class DitherErrorDiffAbstract {


    DitherErrorDiffAbstract() {
        //dither information OR object
    }


    private double[] currentColor = new double[3];	//the current pixel's color values
    private double[] newColor = new double[3];		//the new pixel's color values

    private int[][][] errorMatrix;                  //the matrix that will be used to store the error data for error-diffusion dithering

    private void spreadError(int column, int row) {			        //find and spread the error onto the error matrix for use in error-diffusion dithering
        int redError = (int) (currentColor[0] - newColor[0]);		//red error is the current value minus the new value
        int greenError = (int) (currentColor[1] - newColor[1]);
        int blueError = (int) (currentColor[2] - newColor[2]);
        int numerator;									            //each error-diffusion method splits the error into fractions to spread it around the current pixel; this is the numerator in the fraction (i.e. 2/X or 1/X)
        int denominator = dither.split();			                //this is the denominator in the fraction (i.e. X/48 or X/16)
        int column_new;
        int row_new;

        for (int i = 0; i < dither.arraySize(); i++) {	            //goes through the list of locations to spread the error to

            numerator = dither.get(i, 2);				        //defines how much of the error is going to be placed at the location (i.e. 4/X or 5/X)
            column_new = column + (dither.get(i, 1));	        //defines X/Y of locations; values from the dither array are coordinates relative to the current pixel
            row_new = row + (dither.get(i, 0));

            if( column_new < image.width() && column_new > 0 && row_new < image.height() ) {
                errorMatrix[row_new][column_new][0] += getErrorPortion(redError, numerator, denominator);
                errorMatrix[row_new][column_new][1] += getErrorPortion(greenError, numerator, denominator);
                errorMatrix[row_new][column_new][2] += getErrorPortion(blueError, numerator, denominator);
            }
        }
    }


    private int getErrorPortion(int error, int numerator, int denominator) {				//takes the error and the fraction of the error to be used and returns the resulting fraction of the error
        double errorPortion;															//the actual value; fractional portion of the error
        errorPortion = ((double) numerator / (double) denominator) * (double) error;
        return (int) Math.floor(errorPortion + 0.5d);									//return the value, rounded to the nearest whole number
    }
}
