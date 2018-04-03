package application;

import javafx.scene.paint.Color;
import net.sf.javaml.core.kdtree.KDTree;
import net.sf.javaml.core.kdtree.KeySizeException;

public class ColorMatcherKdTree implements ColorMatcher {

    private Palette palette;
    private KDTree kdTree;

    ColorMatcherKdTree(Palette _palette, KDTree _kdTree) {
        palette = _palette;
        kdTree = _kdTree;
    }

    @Override
    public Color getMatch(double[] currentColor) {
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

        return Color.rgb(red, green, blue);
    }

//    private double[] arrayIntToDouble(int[] input) {	//convert an array of integers to an array of doubles
//
//        double[] output = new double[3];
//        for(int i=0; i < 3; i++) {
//            output[i] = input[i];
//        }
//        return output;
//    }
}
