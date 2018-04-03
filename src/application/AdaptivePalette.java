package application;

import java.util.*;

import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class AdaptivePalette {

	private Bridge bridgeClass = Bridge.getInstance();
//	private DdcsImage image = DdcsImage.getInstance();

    private long lastProcessedImage = -1;                                            //the number of the previously processed image
	private int colorCount = 1;

	private int[][] calculatedPalette;

    private int origColorCount = 0;                                                 //the number of colors the original image has

    private final ArrayList<int[][]> listColorArrays = new ArrayList<>();           //this list is the main container for all the color 'cubes'

	private IdedImage image = null;


    public int[][] getAdaptivePalette(int colors, IdedImage _image) {
    	try {

    	    image = _image;

        	if(lastProcessedImage == image.getId() && colorCount == colors) {	//if the user hasn't changed the base image or the number of colors they want, just return the existing result
        		return calculatedPalette;
        	} else {


    			bridgeClass.updateProgressInfo("generating adaptive palette . . .");
    			bridgeClass.updateProgress(-1);

                lastProcessedImage = image.getId();
        		colorCount = colors;

        		calculatedPalette = findAdaptivePalette();


                bridgeClass.updateProgress(0);
        		return calculatedPalette;
        	}

        } catch(Exception e) { bridgeClass.handleError(classID, "05", e); } return null;
    }

    public void displayOriginalColorCount() {
    	bridgeClass.updateProgressInfo("original # colors = " + origColorCount);
    }






    private int[][] findAdaptivePalette() {
        try {

            listColorArrays.clear();                                        //make sure the list is empty
            listColorArrays.add(createInitialArray());                      //populate the list with a single array of all the colors of the image
            int[][] colorArray;                                             //this is an array to hold the values of the first sub-array of the list so we can work with it easier

            if (listColorArrays.get(0).length < colorCount) {               //if there are already fewer colors in the image than the number we want to create it will break things, so trim it down if needed

                int[][] arrayColorArrays = new int[listColorArrays.get(0).length][3];

                for (int i = 0; i < listColorArrays.get(0).length; i++) {   //convert the ArrayList into the kind of array we need
                    arrayColorArrays[i][0] = listColorArrays.get(0)[i][0];
                    arrayColorArrays[i][1] = listColorArrays.get(0)[i][1];
                    arrayColorArrays[i][2] = listColorArrays.get(0)[i][2];
                }
                return arrayColorArrays;
            }

            while (listColorArrays.size() < colorCount) {                   //run through this until you've reached the number of color 'cubes' you want (the number of colors in the adaptive palette)

                colorArray = listColorArrays.get(0);                        //grab the first sub-array from the list

                sortByLargestRange(colorArray);                             //function that sorts the array by the sub-value with the largest range

                int[] subArrayLengths = getSubarrayLengths();               //determine the size of the arrays we are going to make

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

            for (int[][] array : listColorArrays) {                     //iterate through all the sub-arrays in the list (the color 'cubes')

                int red = 0;
                int green = 0;
                int blue = 0;

                for (int[] colors : array) {            //iterate through the colors in the current color 'cube'
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

        } catch (Exception e) { bridgeClass.handleError(classID, "04", e); }
        return null;
    }



	//================================================================================================================================================
    private int[] getSubarrayLengths() {					//find the values to use for the lengths of the two new sub-arrays based on the current one
    	try {

    		int[][] colors = listColorArrays.get(0);		//get the current sub-array

        	double half = colors.length / 2;
        	double fractional = half % 1;					//get the fractional part of the halves, just in case the length was an odd number
        	int integral = (int) (half - fractional);   	//get the whole number part of the halves

        	if(half == integral) {							//if the halves are whole numbers (the original sub-array did not have an odd length)
        		return new int[]{integral, integral};
        	} else {										//if the sub-array length did not divide equally (if array length is odd, one sub-array must be 1 value longer)
        		return new int[]{integral, (integral + 1)};
        	}

        } catch(Exception e) { bridgeClass.handleError(classID, "03", e); } return null;
    }


    //================================================================================================================================================
	private void sortByLargestRange(int[][] colors) {						//this method will determine which color range is the largest and will sort the array by that range
		try {

			int rMin, gMin, bMin, rMax, gMax, bMax, rDiff, gDiff, bDiff;	//create variables for the min, max, and difference for all three colors


            Arrays.sort(colors, (o1, o2) -> {                           //because we are working with a multidimensional array we will have to use a custom comparator to sort it properly
                return Integer.compare(o2[0], o1[0]);					//number here determines column; in this case it's the values from the [0] (red) index column
            });

	    	rMax = colors[0][0];					//the highest red value
	    	rMin = colors[colors.length - 1][0];	//the lowest red value
	    	rDiff = rMax - rMin;					//get difference between the two red values

	    	Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[1], o1[1]));	//sort by the [1] (green) index column
	    	gMax = colors[0][1];					//same as above, but with the green values
	    	gMin = colors[colors.length - 1][1];
	    	gDiff = gMax - gMin;

	    	Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[2], o1[2]));	//sort by the [2] (blue) index column
	    	bMax = colors[0][2];					//same as above, but with the blue values
	    	bMin = colors[colors.length - 1][2];
	    	bDiff = bMax - bMin;

	    	if(	    	((rDiff >= gDiff)   &&  (rDiff >= bDiff))	) {	 //if the red range is greater than or equal to the other two ranges
	    		Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[0], o1[0]));  //sort the array by the red values
	    	} else if(  ((gDiff >= rDiff)   &&  (gDiff > bDiff)) |
	    				((gDiff > rDiff)    &&  (gDiff >= bDiff))	) { //if the green range is greater than both other ranges OR greater than one and equal to the other
	    		Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[1], o1[1]));  //sort the array by the green values
	    	} else if(  ((bDiff >= rDiff)	&&  (bDiff > gDiff)) |
	    				((bDiff > rDiff)	&&  (bDiff >= gDiff))	) { //if the blue range is greater than both other ranges OR greater than one and equal to the other
	    		Arrays.sort(colors, (o1, o2) -> Integer.compare(o2[2], o1[2]));  //sort the array by the blue values
	    	} else {
	    		//if something went wrong, shouldn't happen, but who knows
	    	}

        } catch(Exception e) { bridgeClass.handleError(classID, "02", e); }
    }


    //================================================================================================================================================
    private int[][] createInitialArray() {								//this method will populate the initial sub-array
    	try {

        	int[][] colors;												//array for the color values
        	Color color;												//color object to get the values

            PixelReader reader = image.getPixelReader();        //pixel reader to get the color values from the image

        	colors = new int[(int) image.getHeight() * (int) image.getWidth()][3];		//create the array with the appropriate dimensions

        	int count = 0;												//counter for convenience

        	for (int row = 0; row < image.getHeight(); row++) {				//iterate through the rows of the image
        		for (int column = 0; column < image.getWidth(); column++) {	//iterate through the columns of the image

        			color = reader.getColor(column, row);				//get the color value of the current pixel

        			colors[count][0] = (int) (255 * color.getRed());    //turn the color values into a proper 0-255 numbers and store them in the new array
        			colors[count][1] = (int) (255 * color.getGreen());
        			colors[count][2] = (int) (255 * color.getBlue());

        			count++;
        		}
        	}

        	return removeDuplicateColors(colors);						//return the initial array after remove duplicate entries

    	} catch(Exception e) { bridgeClass.handleError(classID, "01", e); } return null;
    }


	//================================================================================================================================================
    private int[][] removeDuplicateColors(int[][] originalArray) {			//this method removes duplicate entries from the color array
    	try {

    		Integer[] simplifiedArray = new Integer[originalArray.length];	//create a second array the same size as the original
        	int count = 0;

        	for(int[] color : originalArray) {
        		int intColor = (0xFF << 24) | (color[0] << 16) | (color[1] << 8) | (color[2]);	//combined the colors into a single integer value
        		simplifiedArray[count] = intColor;												//add that integer value into the new array
        		count++;
        	}

        	Set<Integer> tmpSet = new HashSet<>(Arrays.asList(simplifiedArray));	//converting the array into a set will auto-magically remove all duplicate entries

        	Integer[] cleanedArray = tmpSet.toArray(new Integer[tmpSet.size()]);	//create a new array and store all the color values in it, now without duplicates

        	int[][] newArray = new int[cleanedArray.length][3];						//create a new array to get those color values in a proper RGB format
        	count = 0;

        	for(Integer color : cleanedArray) {				//iterate through the array of integer values without duplicates

        		newArray[count][0] = (color >> 16) & 0xFF;	//split the color values out of the integer value and set them as the RGB values for the color in the new array
        		newArray[count][1] = (color >> 8) & 0xFF;
    	    	newArray[count][2] = (color) & 0xFF;
    	    	count++;
        	}

        	origColorCount = newArray.length;

        	return newArray;

        } catch(Exception e) { bridgeClass.handleError(classID, "00", e); } return null;
    }

    private final String classID = "06";	//used as a reference when displaying errors
}

