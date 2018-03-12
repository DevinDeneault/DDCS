package application;

import java.util.HashMap;
import java.util.Map;

public class DdcsPaletteManager {

    //---------------------------------------------------singleton
    private static volatile DdcsPaletteManager instance = null;

    private DdcsPaletteManager() { }

    public static DdcsPaletteManager getInstance() {
        if (instance == null) {
            synchronized (DdcsPaletteManager.class) {
                if (instance == null) {
                    instance = new DdcsPaletteManager();
                }
            }
        }
        return instance;
    }
    //---------------------------------------------------

    private final int[][] PALETTE_NULL = {{0,0,0}};

    private Map<String, int[][]> paletteMap = new HashMap<>();	//this HashMap will hold all the palette array data
    private String paletteName = "- None -";	//name of the current palette selected by the user
    private int paletteSize = 1;

    private int matchOverride = 0;
    private boolean sortPalette = false;

    private double intensityRed = 0.2989;
    private double intensityGreen = 0.5870;
    private double intensityBlue = 0.1140;

    private int[][] paletteArray = PALETTE_NULL;

    public String paletteType() {
        if(matchOverride == 1) {
            return "mapped";
        } else if (matchOverride == 2) {
            return "searched";
        } else {
            if (paletteName.contains("Gradient") || paletteName.contains("Greyscale")) {
                return "mapped";
            } else {
                return "searched";
            }
        }
    }

    public void setMatchOverride(String type) {
        switch (type) {
            case "map": matchOverride = 1; break;
            case "search": matchOverride = 2; break;
            default: matchOverride = 0; break;
        }
    }

    public void setSortPaletteFlag(boolean sort) {
        sortPalette = sort;
    }



    public int get(int a, int b) { return paletteArray[a][b]; }
    public int[] get(int a) { return paletteArray[a]; }

    public int[][] loadedPaletteArray() { return paletteArray; }
    public int[][] selectedPaletteArray() {
        if(sortPalette) {
            int[][] tmpArray = paletteMap.get(paletteName).clone(); //deep copy so we don't end up sorting the base palette array
            sortPaletteByIntensity(tmpArray, 0, paletteArray.length - 1);
            return tmpArray;
        } else {
            return paletteMap.get(paletteName);
        }
//        return paletteMap.get(paletteName);
    }

    public void setSelectedPalette(String name) {
        paletteName = name;
        paletteSize = paletteMap.get(name).length;
    }
    public void loadSelectedPalette() {
        if(sortPalette) {
            paletteArray = paletteMap.get(paletteName).clone(); //deep copy so we don't end up sorting the base palette array
            sortPaletteByIntensity(paletteArray, 0, paletteArray.length - 1);
        } else {
            paletteArray = paletteMap.get(paletteName);
        }
    }
    public String selectedPalette() { return paletteName; }

    public int size() { return paletteSize; }

    public void addPaletteData(String name) { paletteMap.put(name, PALETTE_NULL); }
    public void addPaletteData(String name, int[][] colors) { paletteMap.put(name, colors); }
    public void addPaletteData(int adaptivePaletteSize, int[][] colors) {
        paletteSize = adaptivePaletteSize;
        paletteMap.put("Adaptive Palette", colors);
    }



    public void setColorIntensityValues(double iR, double iG, double iB) {
        intensityRed = iR;
        intensityGreen = iG;
        intensityBlue = iB;
    }



    private void sortPaletteByIntensity(int[][] array, int lowerIndex, int upperIndex) {   //quicksort

        int a = lowerIndex;
        int b = upperIndex;
        double pivot = calculateIntensity(array[lowerIndex + ( upperIndex - lowerIndex ) / 2 ]);
        while (a <= b) {
            while (calculateIntensity(array[a]) < pivot) { a++; }
            while (calculateIntensity(array[b]) > pivot) { b--; }
            if (a <= b) { swap(array, a++, b--); }
        }
        //call method recursively
        if (lowerIndex < b) sortPaletteByIntensity(array, lowerIndex, b);
        if (a < upperIndex) sortPaletteByIntensity(array, a, upperIndex);
    }

    private void swap(int[][] array, int a, int b) {
        int[] temp = array[a];
        array[a] = array[b];
        array[b] = temp;
    }

    private double calculateIntensity(int[] color) {
        return intensityRed * color[0] + intensityGreen * color[1] + intensityBlue * color[2];
    }
}