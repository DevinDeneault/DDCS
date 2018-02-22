package application;

import java.util.HashMap;
import java.util.Map;

public class DdcsPalette {

    //---------------------------------------------------singleton
    private static volatile DdcsPalette instance = null;

    private DdcsPalette() { }

    public static DdcsPalette getInstance() {
        if (instance == null) {
            synchronized (DdcsPalette.class) {
                if (instance == null) {
                    instance = new DdcsPalette();
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

    private int[][] paletteArray = PALETTE_NULL;

    public String paletteType() {
        if(paletteName.contains("Gradient") || paletteName.contains("Greyscale")) {
            return "mapped";
        } else {
            return "searched";
        }
    }

    public int get(int a, int b) { return paletteArray[a][b]; }
    public int[] get(int a) { return paletteArray[a]; }

    public int[][] paletteArray() { return paletteArray; }

    public void setSelectedPalette(String name) {
        paletteName = name;
        paletteSize = paletteMap.get(name).length;
        paletteArray = paletteMap.get(name);
    }
    public String selectedPalette() { return paletteName; }

    public int size() { return paletteSize; }

    public void addPaletteData(String name) { paletteMap.put(name, PALETTE_NULL); }
    public void addPaletteData(String name, int[][] colors) { paletteMap.put(name, colors); }
    public void addPaletteData(int adaptivePaletteSize, int[][] colors) { paletteSize = adaptivePaletteSize; paletteMap.put("Adaptive Palette", colors); }
}
