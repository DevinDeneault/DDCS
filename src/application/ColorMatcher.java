package application;

import javafx.scene.paint.Color;

//a color matcher will take a palette object and a color, then find the closest match to that color within the palette
//  each type of color matcher does so using different methods

public interface ColorMatcher {

    Color getMatch(double[] currentColor);

}
