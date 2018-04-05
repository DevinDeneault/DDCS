package application;

import javafx.scene.paint.Color;

public abstract class DitherOrderedAbstract {

    DitherData dither;

    public void addThreshold(double[] currentColor, int column, int row) {      //adds the threshold value from the ordered dither matrix ("bayer matrix") to the pixel values

        int matrixSize = dither.gridSize();
        int threshold = dither.get((row % matrixSize), column % matrixSize);

        currentColor[0] += threshold;    //row % matrix size; 324 % 8 = 4; uses remainder to determine location on matrix, ensures uniform use of threshold matrix
        currentColor[1] += threshold;    //     this uniform use of the threshold matrix causes the cross-hatch pattern visible on final image
        currentColor[2] += threshold;

        currentColor[0] = Math.min(Math.max(currentColor[0], 0), 255);          //this will cap the value to the range of 0 to 255
        currentColor[1] = Math.min(Math.max(currentColor[1], 0), 255);
        currentColor[2] = Math.min(Math.max(currentColor[2], 0), 255);
    }

    public void defineCurrentPixel(double[] currentColor, Color imageColor) {   //gets RGB values for current pixel
        currentColor[0] = (int) (255 * imageColor.getRed());                    //get the color value (returns a 0 to 1 value, multiply by 255 to get proper value)
        currentColor[1] = (int) (255 * imageColor.getGreen());
        currentColor[2] = (int) (255 * imageColor.getBlue());
    }

}
