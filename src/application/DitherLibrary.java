package application;

public class DitherLibrary {

    //---------------------------------------------------singleton
    private static volatile DitherLibrary instance = null;

    private DitherLibrary() { }

    public static DitherLibrary getInstance() {
        if (instance == null) {
            synchronized (DitherLibrary.class) {
                if (instance == null) {
                    instance = new DitherLibrary();
                }
            }
        }
        return instance;
    }
    //---------------------------------------------------

    private String ditherName = "- None -"; //name of the current dither selected by the user
    private int[][] ditherArray;
    private int split = 1;
    private int gridSize = 2;
    private int ditherArraySize = 0;


    public void setDitherName(String name) {
        ditherName = name;

        setDitherArray();
        ditherArraySize = ditherArray.length;
    }

    public String type() {	//determine is the dither is ordered, error-diffusion, or none
        if(ditherName.contains("Ordered")) {
            return "ordered";
        } else if(ditherName.contains("None")) {
            return "none";
        } else {
            return "diffusion";
        }
    }

    public int split() { return split; }
    public int gridSize() { return gridSize; }
    public int arraySize() { return ditherArraySize; }

    public int get(int y, int x) { return ditherArray[y][x]; }

    private void setDitherArray() {
        switch(ditherName) {
            case "- None -":
                split = 1;
                ditherArray = DITHER_NULL;
                break;
            case "Floyd-Steinberg":
                split = 16;
                ditherArray = DITHER_FS;
                break;
            case "Jarvis, Judice, & Ninke":
                split = 48;
                ditherArray = DITHER_JJN;
                break;
            case "Stucki":
                split = 42;
                ditherArray = DITHER_ST;
                break;
            case "Atkinson":
                split = 8;
                ditherArray = DITHER_AT;
                break;
            case "Burkes":
                split = 32;
                ditherArray = DITHER_BU;
                break;
            case "Sierra":
                split = 32;
                ditherArray = DITHER_SI;
                break;
            case "Two-Row Sierra":
                split = 16;
                ditherArray = DITHER_SI_TR;
                break;
            case  "Sierra Lite":
                split = 4;
                ditherArray = DITHER_SI_L;
                break;
            case "Ordered [2x2]":
                gridSize = 2;
                ditherArray = DITHER_ORDERED_2x2;
                break;
            case "Ordered [3x3]":
                gridSize = 3;
                ditherArray = DITHER_ORDERED_3x3;
                break;
            case "Ordered [4x4]":
                gridSize = 4;
                ditherArray = DITHER_ORDERED_4x4;
                break;
            case "Ordered [4x4] [Negative]":
                gridSize = 4;
                ditherArray = DITHER_ORDERED_4x4_N;
                break;
            case "Ordered [8x8]":
                gridSize = 8;
                ditherArray = DITHER_ORDERED_8x8;
                break;
            case "Ordered [8x8] [Darkened]":
                gridSize = 8;
                ditherArray = DITHER_ORDERED_8x8_D;
                break;
            default:
                ditherArray = DITHER_NULL;
        }
    }



    //DITHER DATA
    //null dither
    private final int[][] DITHER_NULL = {{0,0,0}};

    //first two numbers are the relative coords of the pixel to the current working pixel, third is the fraction of the error it receives
    //-------------------------------------------------------------------------------------------------------
    //Floyd-Steinberg
    private final int[][] DITHER_FS = {{0,1,7}, {1,-1,3}, {1,0,5}, {1,1,1}};
    //Jarvis, Judice, and Ninke
    private final int[][] DITHER_JJN = {{0,1,7},{0,2,5},{1,-2,3},{1,-1,5},{1,0,7},{1,1,5},{1,2,3},{2,-2,1},{2,-1,3},{2,0,5},{2,1,3},{2,2,1}};
    //Stucki
    private final int[][] DITHER_ST = {{0,1,8},{0,2,4},{1,-2,2},{1,-1,4},{1,0,8},{1,1,4},{1,2,2},{2,-2,1},{2,-1,2},{2,0,4},{2,1,2},{2,2,1}};
    //Atkinson
    private final int[][] DITHER_AT = {{0,1,1},{0,2,1},{1,-1,1},{1,0,1},{1,1,1},{2,0,1}};
    //Burkes
    private final int[][] DITHER_BU = {{0,1,8},{0,2,4},{1,-2,2},{1,-1,4},{1,0,8},{1,1,4},{1,2,2}};
    //Sierra
    private final int[][] DITHER_SI = {{0,1,5},{0,2,3},{1,-2,2},{1,-1,4},{1,0,5},{1,1,4},{1,2,2},{2,-1,2},{2,0,3},{2,1,2}};
    //Two-row Sierra
    private final int[][] DITHER_SI_TR = {{0,1,4},{0,2,3},{1,-2,1},{1,-1,2},{1,0,3},{1,1,2},{1,2,1}};
    //Sierra Lite
    private final int[][] DITHER_SI_L = {{0,1,2},{1,-1,1},{1,0,1}};

    //bayer matrices for ordered dithering
    //-------------------------------------------------------------------------------------------------------
    private final int[][] DITHER_ORDERED_2x2 = {{0,2},{3,1}};
    private final int[][] DITHER_ORDERED_3x3 = {{0,7,3},{6,5,2},{4,1,8}};
    private final int[][] DITHER_ORDERED_4x4 = {{0,8,2,10},{12,4,14,6},{3,11,1,9},{15,7,13,5}};
    //experiment with negating the modifiers
    private final int[][] DITHER_ORDERED_4x4_N = {{0,-8,-2,-10},{-12,-4,-14,-6},{-3,-11,-1,-9},{-15,-7,-13,-5}};
    private final int[][] DITHER_ORDERED_8x8 = {{0,48,12,60,3,51,15,63},{32,16,44,28,35,19,47,31},{8,56,4,52,11,59,7,55},{40,24,36,20,43,27,39,23},{2,50,14,62,1,49,13,61},{34,18,46,30,33,17,45,29},{10,58,6,54,9,57,5,53},{42,36,38,22,41,25,37,21}};
    //same as 8x8, but with all it's values shifted down by 32; this is a fairly successful attempt to remove the washed-out look that ordered dithering can cause with this large of matrix
    private final int[][] DITHER_ORDERED_8x8_D = {{-32,16,-18,28,-29,19,-17,30},{0,-16,12,-5,2,-13,15,-1},{-24,24,-28,20,-21,27,-25,23},{8,-8,4,-12,11,-5,8,-9},{-30,18,-18,30,-31,17,-19,29},{2,-14,14,-2,1,-15,13,-3},{-22,26,-26,22,-23,25,-27,21},{10,-6,6,-10,9,-7,5,-11}};

}
