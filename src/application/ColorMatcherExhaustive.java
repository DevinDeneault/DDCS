package application;

import javafx.scene.paint.Color;

public class ColorMatcherExhaustive implements ColorMatcher {

    private Palette palette;

    ColorMatcherExhaustive(Palette _palette) {
        palette = _palette;
    }

    //this color matcher will take a color and measures its euclidean distance in 3D space to all the colors in the palette
    //  then returns the closest one

    @Override
    public Color getMatch(double[] currentColor) {
        int[] distances = new int[palette.size()];
        int index;

        for( int i = 0; i < palette.size(); i++ ) {
            int rDistance = (int) currentColor[0] - palette.get(i, 0);
            int gDistance = (int) currentColor[1] - palette.get(i, 1);
            int bDistance = (int) currentColor[2] - palette.get(i, 2);
            int distance = (rDistance)*(rDistance) + (gDistance)*(gDistance) + (bDistance)*(bDistance);	//find the distance between the two points (returns 'squared distance' to avoid making costly square root calculations)
            distances[i] = distance;
        }

        index = indexOfSmallestInt(distances);

        int nRed = palette.get(index, 0);
        int nGreen = palette.get(index, 1);
        int nBlue = palette.get(index, 2);

        return Color.rgb(nRed, nGreen, nBlue);
    }

    private int indexOfSmallestInt(int[] array) {   //find the index of the smallest element in the array without changing the order of the elements
        int currentSmallest = 0;

        for( int i = 1; i < array.length; i++ )
            if( array[i] <= array[currentSmallest] )
                currentSmallest = i;

        return currentSmallest;
    }

}
