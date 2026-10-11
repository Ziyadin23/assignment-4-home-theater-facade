package theater.subsystem;

/** Routes a playing DVD source to speakers and validates the volume. */
public final class Amplifier {
    private boolean poweredOn;
    private int volume;
    private DvdPlayer source;

    public void powerOn() { poweredOn = true; }

    public void setVolume(int volume) {
        if (!poweredOn) {
            throw new IllegalStateException("Power on the amplifier before setting volume.");
        }
        if (volume < 0 || volume > 10) {
            throw new IllegalArgumentException("Volume must be between 0 and 10.");
        }
        this.volume = volume;
    }

    public void playFrom(DvdPlayer source) {
        if (!poweredOn) {
            throw new IllegalStateException("The amplifier is powered off.");
        }
        if (source == null || !source.isPlaying()) {
            throw new IllegalStateException("The amplifier requires a playing DVD source.");
        }
        this.source = source;
    }

    public void powerOff() {
        source = null;
        volume = 0;
        poweredOn = false;
    }

    public boolean isPoweredOn() { return poweredOn; }
    public int getVolume() { return volume; }
    public boolean isAudible() {
        return poweredOn && volume > 0 && source != null && source.isPlaying();
    }
}
