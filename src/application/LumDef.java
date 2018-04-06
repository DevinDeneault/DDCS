package application;

//default luminance values

public enum LumDef {
    R(0.299),
    G(0.587),
    B(0.114);

    private final double value;
    
    LumDef(double value) {
        this.value = value;
    }

    public double val() {
        return value;
    }
}