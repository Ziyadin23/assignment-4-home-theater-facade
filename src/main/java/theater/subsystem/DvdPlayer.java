package theater.subsystem;

import java.util.Map;

/** Owns the movie catalogue, disc selection, and playback position. */
public final class DvdPlayer {
    private final Map<String, Integer> catalogue;
    private boolean poweredOn;
    private boolean playing;
    private String loadedMovie;
    private int elapsedMinutes;

    public DvdPlayer() {
        this(Map.of("Interstellar", 169, "Inception", 148, "The Matrix", 136));
    }

    public DvdPlayer(Map<String, Integer> catalogue) {
        if (catalogue == null || catalogue.isEmpty()) {
            throw new IllegalArgumentException("The movie catalogue must not be empty.");
        }
        catalogue.forEach((title, duration) -> {
            if (title == null || title.isBlank() || duration == null || duration <= 0) {
                throw new IllegalArgumentException("Each movie needs a title and positive duration.");
            }
        });
        this.catalogue = Map.copyOf(catalogue);
    }

    public void requireAvailable(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("A movie title is required.");
        }
        if (!catalogue.containsKey(title)) {
            throw new IllegalArgumentException("Movie is not in the catalogue: " + title);
        }
    }

    public void powerOn() {
        poweredOn = true;
    }

    public void load(String title) {
        requirePoweredOn();
        requireAvailable(title);
        if (playing) {
            throw new IllegalStateException("Stop playback before changing the disc.");
        }
        loadedMovie = title;
        elapsedMinutes = 0;
    }

    public void play() {
        requirePoweredOn();
        if (loadedMovie == null) {
            throw new IllegalStateException("Load a movie before starting playback.");
        }
        if (elapsedMinutes == catalogue.get(loadedMovie)) {
            elapsedMinutes = 0;
        }
        playing = true;
    }

    public void advanceMinutes(int minutes) {
        if (!playing) {
            throw new IllegalStateException("Playback must be active before advancing time.");
        }
        if (minutes <= 0) {
            throw new IllegalArgumentException("Elapsed time must be positive.");
        }
        int duration = catalogue.get(loadedMovie);
        elapsedMinutes += Math.min(minutes, duration - elapsedMinutes);
        if (elapsedMinutes == duration) {
            playing = false;
        }
    }

    public void stop() {
        playing = false;
    }

    public void powerOff() {
        stop();
        loadedMovie = null;
        elapsedMinutes = 0;
        poweredOn = false;
    }

    public boolean isPoweredOn() { return poweredOn; }
    public boolean isPlaying() { return poweredOn && playing; }
    public String getLoadedMovie() { return loadedMovie; }
    public int getElapsedMinutes() { return elapsedMinutes; }

    private void requirePoweredOn() {
        if (!poweredOn) {
            throw new IllegalStateException("The DVD player is powered off.");
        }
    }
}
