package application;

import javafx.scene.paint.Color;

import java.util.Arrays;

public class ColorMatcherExhaustive implements ColorMatcher {

    private Palette palette;

    ColorMatcherExhaustive(Palette _palette) {
        palette = _palette;
    }

    @Override
    public Color getMatch(double[] currentColor) {
        int[] distances = new int[palette.size()];					//array of distances between current color and palette colors (they are being treated as coords in 3 dimensional space)
        int index;																//this value will be used to keep track of the index of the smallest distance in the list

        for (int i = 0; i < palette.size(); i++) {
            int rDistance = (int) currentColor[0] - palette.get(i, 0);
            int gDistance = (int) currentColor[1] - palette.get(i, 1);
            int bDistance = (int) currentColor[2] - palette.get(i, 2);
            int distance = (rDistance)*(rDistance) + (gDistance)*(gDistance) + (bDistance)*(bDistance);	//find the distance between the two points (returns 'squared distance' to avoid make costly square rooot calculations)
            distances[i] = distance;																	//add the newly found distance to the list of distances
        }

        index = indexOfSmallestInt(distances);

        int nRed = palette.get(index, 0);
        int nGreen = palette.get(index, 1);
        int nBlue = palette.get(index, 2);

        return Color.rgb(nRed, nGreen, nBlue);	//return the new color
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
}
