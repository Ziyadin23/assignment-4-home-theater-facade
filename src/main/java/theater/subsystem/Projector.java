package theater.subsystem;

/** Checks lamp readiness, live video input, and the receiving screen. */
public final class Projector {
    private boolean poweredOn;
    private boolean lampHealthy;
    private DvdPlayer source;
    private Screen screen;
    private double aspectRatio = 16.0 / 9.0;

    public Projector() { this(true); }

    public Projector(boolean lampHealthy) {
        this.lampHealthy = lampHealthy;
    }

    public void powerOn() {
        if (!lampHealthy) {
            throw new IllegalStateException("The projector lamp needs replacement.");
        }
        poweredOn = true;
    }

    public void setAspectRatio(double aspectRatio) {
        if (!Double.isFinite(aspectRatio) || aspectRatio <= 0) {
            throw new IllegalArgumentException("Aspect ratio must be positive and finite.");
        }
        this.aspectRatio = aspectRatio;
    }

    public void project(DvdPlayer source, Screen screen) {
        if (!poweredOn) {
            throw new IllegalStateException("The projector is powered off.");
        }
        if (source == null || !source.isPlaying()) {
            throw new IllegalStateException("The projector requires a playing DVD source.");
        }
        if (screen == null || !screen.isLowered()) {
            throw new IllegalStateException("Lower the screen before projecting.");
        }
        this.source = source;
        this.screen = screen;
    }

    public double getImageDiagonalInches() {
        if (!isProjecting()) {
            throw new IllegalStateException("An image must be projected before measuring it.");
        }
        return screen.diagonalInches(aspectRatio);
    }

    public void replaceLamp() {
        if (poweredOn) {
            throw new IllegalStateException("Power off the projector before replacing its lamp.");
        }
        lampHealthy = true;
    }

    public void powerOff() {
        source = null;
        screen = null;
        poweredOn = false;
    }

    public boolean isPoweredOn() { return poweredOn; }
    public boolean isProjecting() {
        return poweredOn && source != null && source.isPlaying()
                && screen != null && screen.isLowered();
    }
}
