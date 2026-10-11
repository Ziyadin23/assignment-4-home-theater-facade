package theater;

import java.util.Objects;
import theater.subsystem.Amplifier;
import theater.subsystem.DvdPlayer;
import theater.subsystem.Projector;
import theater.subsystem.Screen;
import theater.subsystem.TheaterLights;

/** Coordinates a movie session while keeping device details out of the public API. */
public final class HomeTheaterFacade {
    private static final int MOVIE_BRIGHTNESS = 15;
    private static final int MOVIE_VOLUME = 5;
    private final DvdPlayer player;
    private final Amplifier amplifier;
    private final Projector projector;
    private final Screen screen;
    private final TheaterLights lights;
    private String currentMovie;
    private int previousBrightness;

    public HomeTheaterFacade() {
        this(new DvdPlayer(), new Amplifier(), new Projector(),
                new Screen(), new TheaterLights());
    }

    // Package access allows integration tests without exposing devices to the client.
    HomeTheaterFacade(DvdPlayer player, Amplifier amplifier, Projector projector,
            Screen screen, TheaterLights lights) {
        this.player = Objects.requireNonNull(player);
        this.amplifier = Objects.requireNonNull(amplifier);
        this.projector = Objects.requireNonNull(projector);
        this.screen = Objects.requireNonNull(screen);
        this.lights = Objects.requireNonNull(lights);
    }

    public String watchMovie(String title) {
        if (currentMovie != null) {
            throw new IllegalStateException("End the current movie before starting another.");
        }
        player.requireAvailable(title);
        previousBrightness = lights.getBrightness();
        try {
            lights.dimTo(MOVIE_BRIGHTNESS);
            screen.lower();
            player.powerOn();
            player.load(title);
            player.play();
            amplifier.powerOn();
            amplifier.setVolume(MOVIE_VOLUME);
            amplifier.playFrom(player);
            projector.powerOn();
            projector.project(player, screen);
            currentMovie = title;
            return "Now watching: " + title;
        } catch (RuntimeException failure) {
            restoreRoom();
            throw new IllegalStateException("Could not start movie: " + title, failure);
        }
    }

    public String endMovie() {
        if (currentMovie == null) {
            return "No movie session is active.";
        }
        restoreRoom();
        return "Movie ended. Devices off, screen raised, previous lighting restored.";
    }

    private void restoreRoom() {
        // Remove audio/video consumers before stopping their common source.
        projector.powerOff();
        amplifier.powerOff();
        player.powerOff();
        screen.raise();
        lights.dimTo(previousBrightness);
        currentMovie = null;
    }
}
