package application;

public class DitherData {

    private DitherType type;    //what kind of dither this is; ordered, error diffusion, ...

    //the splitOrSize value represents one of two things
    //  the size of an ordered dither bayer matrix -> X by X
    //  or how many parts the error in an error diffusion dither will be split into
    private int splitOrSize;
    private int arraySize;
    private int[][] array;

    DitherData(DitherType _type, int _splitOrSize, int[][] _array) {
        type = _type;
        splitOrSize = _splitOrSize;
        array = _array.clone();
        arraySize = array.length;
    }

    public int get(int y, int x) { return array[y][x]; }

    public DitherType type() { return type; }
    public int split() { return splitOrSize; }
    public int gridSize() { return splitOrSize; }
    public int arraySize() { return arraySize; }

}
