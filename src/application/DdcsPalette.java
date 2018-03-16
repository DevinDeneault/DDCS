package application;

public class DdcsPalette {

    DdcsPalette(String _name, String _id, boolean _mapped, String _imageName, boolean _hidden, int[][] _colors) {
        name = _name;
        id = _id;
        mapped = _mapped;
        hidden = _hidden;
        if( _colors.length == 0) {
            colors = new int[][]{{0,0,0}};
            colorsSorted = new int[][]{{0,0,0}};
            imageName = "unknown";
        } else {
            colors = _colors.clone();
            colorsSorted = PaletteUtils.sortPaletteByIntensity(_colors);
            imageName = _imageName;
        }
        size = colors.length;
    }

    private String name;
    private String id;
    private boolean mapped;
    private boolean hidden;
    private int[][] colors;
    private int[][] colorsSorted;
    private int size;
    private String imageName;

    //-----------------------------

    public int get(int a, int b) {
        if(PaletteUtils.sortedOverride) return colorsSorted[a][b];
        return colors[a][b];
    }
    public int[] get(int a) {
        if(PaletteUtils.sortedOverride) return colorsSorted[a];
        return colors[a];
    }
    public int[][] get() {
        if(PaletteUtils.sortedOverride) return colorsSorted;
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

    public void setMachOverride(int type) { PaletteUtils.matchOverride = type; }

    public void setSortOverride(boolean override) {PaletteUtils.sortedOverride = override; }

    //---------------------------------------------------------------------------------------------

    private static class PaletteUtils {

        private static boolean sortedOverride = false;

        //-----------------------------

        private static int matchOverride = 0;

        private static boolean getMatchOverrideType() {
            return matchOverride != 1;      //0 is no override, 1 is match, 2 is map
        }

        //-----------------------------

        private static int[][] sortPaletteByIntensity(int[][] colors) {
            quickSort(colors, 0, colors.length - 1);
            return colors.clone();
        }

        private static void quickSort(int[][] array, int lowerIndex, int upperIndex) {
            int a = lowerIndex;
            int b = upperIndex;
            double pivot = calculateIntensity(array[lowerIndex + ( upperIndex - lowerIndex ) / 2 ]);
            while (a <= b) {
                while (calculateIntensity(array[a]) < pivot) { a++; }
                while (calculateIntensity(array[b]) > pivot) { b--; }
                if (a <= b) { swap(array, a++, b--); }
            }
            //call method recursively
            if (lowerIndex < b) quickSort(array, lowerIndex, b);
            if (a < upperIndex) quickSort(array, a, upperIndex);
        }

        private static void swap(int[][] array, int a, int b) {
            int[] temp = array[a];
            array[a] = array[b];
            array[b] = temp;
        }

        //-----------------------------

        private static double intensityRed = 0.2989;
        private static double intensityGreen = 0.5870;
        private static double intensityBlue = 0.1140;

        private static double calculateIntensity(int[] color) {
            return intensityRed * color[0] + intensityGreen * color[1] + intensityBlue * color[2];
        }
    }
}
