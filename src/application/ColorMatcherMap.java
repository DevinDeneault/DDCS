package application;

import javafx.scene.paint.Color;

public class ColorMatcherMap implements ColorMatcher {

    private Palette palette;
    private double intensityRed;
    private double intensityGreen;
    private double intensityBlue;

    ColorMatcherMap(Palette _palette, double iR, double iG, double iB) {
        palette = _palette;
        intensityRed = iR;
        intensityGreen = iG;
        intensityBlue = iB;
    }

    @Override
    public Color getMatch(int[] currentColor) {
        double scale = (double) (palette.size() - 1) / 255;

        double intensity = intensityRed * currentColor[0] + intensityGreen * currentColor[1] + intensityBlue * currentColor[2];

        double scaledToPalette = scale * intensity;

        int paletteIndex = Math.max(0, (int) Math.round(scaledToPalette));

        int nRed = palette.get(paletteIndex, 0);
        int nGreen = palette.get(paletteIndex, 1);
        int nBlue = palette.get(paletteIndex, 2);

        return Color.rgb(nRed, nGreen, nBlue);	//return the new color
    }
}
