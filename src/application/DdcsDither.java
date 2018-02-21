package application;

public class DdcsDither {

    //---------------------------------------------------singleton
    private static volatile DdcsDither instance = null;

    private DdcsDither() { }

    public static DdcsDither getInstance() {
        if (instance == null) {
            synchronized (DdcsDither.class) {
                if (instance == null) {
                    instance = new DdcsDither();
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
            case "Ordered [8x8]":
                gridSize = 8;
                ditherArray = DITHER_ORDERED_8x8;
                break;
            case "Ordered [8x8] [Dark]":
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

    //first two numbers are the relative coords of the pixel to the current working pixel, third is the fraction of the error it recieves
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
    private final int[][] DITHER_ORDERED_2x2 = {{1,3},{4,2}};
    private final int[][] DITHER_ORDERED_3x3 = {{3,7,4},{6,1,9},{2,8,5}};
    private final int[][] DITHER_ORDERED_4x4 = {{1,9,3,11},{13,5,15,7},{4,12,2,10},{16,8,14,6}};
    private final int[][] DITHER_ORDERED_8x8 = {{1,49,13,61,4,52,16,64},{33,17,45,29,36,20,48,32},{9,57,5,53,12,60,8,56},{41,25,37,21,44,28,40,24},{3,51,15,63,2,50,14,62},{35,19,47,31,34,18,46,30},{11,59,7,55,10,58,6,54},{43,27,39,23,42,26,38,22}};
    //same as 8x8, but with all it's values shifted down by 32; this is a fairly successful attempt to remove the washed-out look that ordered dithering can cause with this large of matrix
    private final int[][] DITHER_ORDERED_8x8_D = {{-31,17,-19,29,-28,20,-16,31},{1,-15,13,-4,3,-12,16,0},{-23,25,-27,21,-20,28,-24,24},{9,-7,5,-11,12,-4,8,-8},{-29,19,-17,31,-30,18,-18,30},{3,-13,15,-1,2,-14,14,-2},{-21,27,-25,23,-22,26,-26,22},{11,-5,7,-9,10,-6,6,-10}};



}
