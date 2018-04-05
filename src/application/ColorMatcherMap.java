package application;

import javafx.scene.paint.Color;

public class ColorMatcherMap implements ColorMatcher {

    private Palette palette;

    ColorMatcherMap(Palette _palette) {
        palette = _palette;
    }

    //this color matcher will take a color and determine it's perceived luminance via the HSP model
    //  it then scales that luminance value to the size of the palette
    //  the color is matched to the color in the palette that occupies that scaled luminance position
    //  NOTE:   this assumes the palette is already sorted from darkest to lightest (lowest to highest luminance)
    //          if it is not then the results wild be wildly incorrect, but can produce some fun and interesting results
    //          a good example is the built in 's2p' and 'b2p2b2p' color palettes that take advantage of this matching scheme

    @Override
    public Color getMatch(double[] currentColor) {
        double scale = (double) (palette.size() - 1) / 255;

        double intensity =  Math.sqrt(
                                palette.getRedIntensity() * (currentColor[0] * currentColor[0]) +
                                palette.getGreenIntensity() * (currentColor[1] * currentColor[1]) +
                                palette.getBlueIntensity() * (currentColor[2] * currentColor[2]));

        double scaledToPalette = scale * intensity;

        int paletteIndex = Math.max(0, (int) Math.round(scaledToPalette));

        int nRed = palette.get(paletteIndex, 0);
        int nGreen = palette.get(paletteIndex, 1);
        int nBlue = palette.get(paletteIndex, 2);

        return Color.rgb(nRed, nGreen, nBlue);
    }
}
