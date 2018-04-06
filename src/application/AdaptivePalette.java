package application;

import java.util.*;

public class AdaptivePalette {

    private Bridge bridgeClass = Bridge.getInstance();

    private long lastProcessedImage = -1;   //the id number of the previously processed image
    private int colorCount = 1;

    private int[][] calculatedPalette;

    private final ArrayList<int[][]> listColorArrays = new ArrayList<>();   //this list is the main container for all the color 'cubes'

    private IdedImage image;


    public int[][] getAdaptivePalette(int colors, IdedImage _image) {

        image = _image;

        if( lastProcessedImage == image.id() && colorCount == colors )    //if the user hasn't changed the base image or the number of colors they want, just return the existing result
            return calculatedPalette;
        else {
            bridgeClass.updateProgress(-1);     //set the progress bar to indeterminate while the adaptive palette is being generated

            lastProcessedImage = image.id();       //remember the last image we made an adaptive palette for
            colorCount = colors;

            calculatedPalette = calculateAdaptivePalette();

            bridgeClass.updateProgress(0);
            return calculatedPalette;
        }
    }

    public void displayOriginalColorCount() {
//        bridgeClass.updateProgressInfo("original # colors = " + origColorCount);//---------------------------------------------------------------------------------------------------------------------------------------
    }



    private int[][] calculateAdaptivePalette() {

        listColorArrays.clear();
        listColorArrays.add(image.colors());
        int[][] colorArray;

        if( listColorArrays.get(0).length < colorCount ) {              //if there are already fewer colors in the image than the number we want to create it will break things, so trim it down if needed

            int[][] arrayColorArrays = new int[listColorArrays.get(0).length][3];

            for( int i = 0; i < listColorArrays.get(0).length; i++ ) {  //convert the ArrayList into the kind of array we need
                arrayColorArrays[i][0] = listColorArrays.get(0)[i][0];
                arrayColorArrays[i][1] = listColorArrays.get(0)[i][1];
                arrayColorArrays[i][2] = listColorArrays.get(0)[i][2];
            }
            return arrayColorArrays;
        }

        while( listColorArrays.size() < colorCount ) {                  //run through this until you've reached the number of color 'cubes' you want (the number of colors in the adaptive palette)

            colorArray = listColorArrays.get(0);                        //grab the first sub-array from the list

            sortByLargestRange(colorArray);

            int[] subArrayLengths = getSubArrayLengths();               //determine the size of the arrays we are going to make

            int[][] subArray1 = new int[Objects.requireNonNull(subArrayLengths)[0]][3];    //create the new sub-arrays
            int[][] subArray2 = new int[subArrayLengths[1]][3];

            System.arraycopy(colorArray, 0, subArray1, 0, subArrayLengths[0]);              //copy first half of the sub-array to first new sub-array
            System.arraycopy(colorArray, subArrayLengths[0], subArray2, 0, subArrayLengths[1]);    //second half to second new sub-array

            listColorArrays.add(subArray1);                         //add the new sub-arrays into the list
            listColorArrays.add(subArray2);

            listColorArrays.remove(0);                        //remove the sub-array we just split into two separate arrays
        }

        int[][] adaptiveColorArray = new int[colorCount][3];        //make an array to put the adaptive color values in
        int count = 0;                                              //counter for convenience, since we are using a FOR EACH loop instead of standard FOR loop in the next step

        for( int[][] array : listColorArrays ) {                    //iterate through all the sub-arrays in the list (the color 'cubes')

            int red = 0;
            int green = 0;
            int blue = 0;

            for( int[] colors : array ) {           //iterate through the colors in the current color 'cube'
                red += colors[0];                   //add up all the color values in the 'cube'
                green += colors[1];
                blue += colors[2];
            }

            red = red / array.length;               //get the average color values
            green = green / array.length;
            blue = blue / array.length;

            adaptiveColorArray[count][0] = red;     //averaged color values are the RGB values for this color in the adaptive palette
            adaptiveColorArray[count][1] = green;
            adaptiveColorArray[count++][2] = blue;
        }

        return adaptiveColorArray;
    }



    //================================================================================================================================================
    private int[] getSubArrayLengths() {                //find the values to use for the lengths of the two new sub-arrays based on the current one

        int[][] colors = listColorArrays.get(0);        //get the current sub-array

        double half = colors.length / 2;
        double fractional = half % 1;                   //get the fractional part of the halves, just in case the length was an odd number
        int integral = (int) (half - fractional);       //get the whole number part of the halves

        if( half == integral )                          //if the halves are whole numbers (the original sub-array did not have an odd length)
            return new int[]{integral, integral};
        else                                            //if the sub-array length did not divide equally (if array length is odd, one sub-array must be 1 value longer)
            return new int[]{integral, (integral + 1)};
    }


    //================================================================================================================================================
    private void sortByLargestRange(int[][] colors) {               //determine which color range is the largest and will sort the array by that range

        int rMin, gMin, bMin, rMax, gMax, bMax, rDiff, gDiff, bDiff;//create variables for the min, max, and difference for all three colors

        Arrays.sort(colors, (o1, o2) -> {                           //because we are working with a multidimensional array we will have to use a custom comparator to sort it properly
            return Integer.compare(o2[0], o1[0]);                   //number here determines column; in this case it's the values from the [0] (red) index column
        });

        rMax = colors[0][0];                    //the highest red value
        rMin = colors[colors.length - 1][0];    //the lowest red value
        rDiff = rMax - rMin;                    //get the difference

        Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[1], o1[1]));    //sort by the [1] (green) index column
        gMax = colors[0][1];                    //same as above, but with the green values
        gMin = colors[colors.length - 1][1];
        gDiff = gMax - gMin;

        Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[2], o1[2]));    //sort by the [2] (blue) index column
        bMax = colors[0][2];                    //same as above, but with the blue values
        bMin = colors[colors.length - 1][2];
        bDiff = bMax - bMin;

        //in the event of two or all three of the ranges are equal, we just have to make a decision on which one should be used
        //  current priority order is red, green, blue
        if( (rDiff >= gDiff) && (rDiff >= bDiff) )                          //if the red range is greater than or equal to the other two ranges
            Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[0], o1[0])); //sort the array by the red values
        else if( gDiff >= bDiff )                                           //else if the green range is greater than or equal to the blue range
            Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[1], o1[1])); //sort the array by the green values
        else                                                                //else the blue range is larger than both other ranges
            Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[2], o1[2])); //sort the array by the blue values
    }


    //================================================================================================================================================
//    private int[][] createInitialArray() {              //populate the initial sub-array
//
//        Set<Integer> colorHashSet = new HashSet<>();    //a HashSet of all the colors values, duplicates automatically removed due to the nature of HashSets
//
//        Color color;
//
//        PixelReader reader = image.getPixelReader();
//
//        for( int row = 0; row < image.getHeight(); row++ ) {
//            for( int column = 0; column < image.getWidth(); column++ ) {
//                color = reader.getColor(column, row);
//
//                //color values are received as a value between 0 to 1, so convert them to 0 to 255
//                //  then collapse them into a single int
//                //  this is SIGNIFICANTLY better performing than a HashSet of int[]
//                colorHashSet.add((0xFF << 24) |
//                                 ((int) (255 * color.getRed()) << 16) |
//                                 ((int) (255 * color.getGreen()) << 8) |
//                                 ((int) (255 * color.getBlue())) );
//            }
//        }
//
//        int[][] colorArray = new int[colorHashSet.size()][3];
//
//        int index = 0;
//        for( int colorInt : colorHashSet ) {
//            colorArray[index][0] = (colorInt >> 16) & 0xFF;   //split the color values out of the integer value and set them as the RGB values for the color in the new array
//            colorArray[index][1] = (colorInt >> 8) & 0xFF;
//            colorArray[index++][2] = (colorInt) & 0xFF;
//        }
//
//        origColorCount = colorHashSet.size();
//
//        return colorArray;
//    }

}

