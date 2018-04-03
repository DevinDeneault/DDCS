package application;

import javafx.scene.paint.Color;

public class ColorMatcherMap implements ColorMatcher {

    private Palette palette;

    ColorMatcherMap(Palette _palette) {
        palette = _palette;
    }

    @Override
    public Color getMatch(double[] currentColor) {
        double scale = (double) (palette.size() - 1) / 255;

        double intensity = palette.getRedIntensity() * currentColor[0] + palette.getGreenIntensity() * currentColor[1] + palette.getBlueIntensity() * currentColor[2];

        double scaledToPalette = scale * intensity;

        int paletteIndex = Math.max(0, (int) Math.round(scaledToPalette));

        int nRed = palette.get(paletteIndex, 0);
        int nGreen = palette.get(paletteIndex, 1);
        int nBlue = palette.get(paletteIndex, 2);

        return Color.rgb(nRed, nGreen, nBlue);	//return the new color
    }
}
