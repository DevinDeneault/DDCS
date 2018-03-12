package application;

public class DdcsPalette {

    DdcsPalette(String _name, String _id, boolean _mapped, int[][] _colors) {
        name = _name;
        id = _id;
        mapped = _mapped;
        colors = _colors.clone();
        size = _colors.length;
    }

    private String name;
    private String id;
    private boolean mapped;
    private int[][] colors;
    private int size;

    public int get(int a, int b) { return colors[a][b]; }
    public int[] get(int a) { return colors[a]; }
    public int[][] get() { return colors; }
    public String name() { return name; }
    public String id() { return id; }
    public boolean mapped() { return mapped; }
    public int size() { return size; }
}
