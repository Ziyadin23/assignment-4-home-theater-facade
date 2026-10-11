package theater.subsystem;

/** Owns the room brightness and enforces its percentage range. */
public final class TheaterLights {
    private int brightness = 100;

    public void dimTo(int brightness) {
        if (brightness < 0 || brightness > 100) {
            throw new IllegalArgumentException("Brightness must be between 0 and 100.");
        }
        this.brightness = brightness;
    }

    public int getBrightness() { return brightness; }
}
