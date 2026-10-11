package theater.subsystem;

/** Models screen position and calculates the usable image diagonal. */
public final class Screen {
    private final double widthCentimeters;
    private boolean lowered;

    public Screen() { this(240.0); }

    public Screen(double widthCentimeters) {
        if (!Double.isFinite(widthCentimeters) || widthCentimeters <= 0) {
            throw new IllegalArgumentException("Screen width must be positive and finite.");
        }
        this.widthCentimeters = widthCentimeters;
    }

    public void lower() { lowered = true; }
    public void raise() { lowered = false; }
    public boolean isLowered() { return lowered; }

    public double diagonalInches(double aspectRatio) {
        if (!Double.isFinite(aspectRatio) || aspectRatio <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive and finite.");
        }
        double heightCentimeters = widthCentimeters / aspectRatio;
        return Math.hypot(widthCentimeters, heightCentimeters) / 2.54;
    }
}
