package application;

abstract class DitherOrderedAbstract {


    DitherOrderedAbstract() {
        //dither information OR object
    }


    private double[] currentColor = new double[3];	//the current pixel's color values

    private void addThreshold(int column, int row) {	//adds the threshold value from the ordered dither matrix ("bayer matrix") to the pixel values

        int matrixSize = dither.gridSize();
        int threshold = dither.get((row % matrixSize), column % matrixSize);

        currentColor[0] += threshold;	//row % matrix size; 324 % 8 = 4; uses remainder to determine location on matrix, ensures uniform use of threshold matrix
        currentColor[1] += threshold;	//		this uniform use of the threshold matrix causes the cross-hatch patter visible on final image
        currentColor[2] += threshold; 	//

        currentColor[0] = Math.min(Math.max(currentColor[0], 0), 255);			//this will cap the value to the range of 0 to 255
        currentColor[1] = Math.min(Math.max(currentColor[1], 0), 255);
        currentColor[2] = Math.min(Math.max(currentColor[2], 0), 255);
    }

}
