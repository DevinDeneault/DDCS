package application;

public class Palette {

    Palette(String _name, String _id, boolean _mapped, String _imageName, boolean _hidden, int[][] _colors) {
        name = _name;
        id = _id;
        mapped = _mapped;
        hidden = _hidden;
        if( _colors.length == 0) {
            colors = new int[][]{{0,0,0}};
            imageName = "unknown";
        } else {
            colors = _colors.clone();
            imageName = _imageName;
        }
        size = colors.length;
    }

    private String name;
    private String id;
    private boolean mapped;
    private boolean hidden;
    private int[][] colors;
    private int size;
    private String imageName;

    //-----------------------------

    public int get(int a, int b) {
        return colors[a][b];
    }
    public int[] get(int a) {
        return colors[a];
    }
    public int[][] get() {
        return colors;
    }

    public String name() { return name; }
    public String id() { return id; }
    public int size() { return size; }
    public String imageName() { return imageName; }
    public boolean hidden() { return hidden; }
    public boolean mapped() {
        if( PaletteUtils.matchOverride == 0 ) { return mapped;
        } else { return PaletteUtils.getMatchOverrideType(); }
    }

    //-----------------------------

    public void setIntensities(double iR, double iG, double iB) {
        PaletteUtils.intensityRed = iR;
        PaletteUtils.intensityGreen = iG;
        PaletteUtils.intensityBlue = iB;
    }
    public double getRedIntensity() { return PaletteUtils.intensityRed; }
    public double getGreenIntensity() { return PaletteUtils.intensityGreen; }
    public double getBlueIntensity() { return PaletteUtils.intensityBlue; }

    public void setMachOverride(int type) { PaletteUtils.matchOverride = type; }


    //---------------------------------------------------------------------------------------------

    private static class PaletteUtils {

        private static int matchOverride = 0;

        private static boolean getMatchOverrideType() {
            return matchOverride != 1;      //0 is no override, 1 is match, 2 is map
        }

        private static double intensityRed = 0.2989;
        private static double intensityGreen = 0.5870;
        private static double intensityBlue = 0.1140;
    }
}
