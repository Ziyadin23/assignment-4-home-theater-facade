package theater;

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
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
        StringJoiner report = new StringJoiner(System.lineSeparator());
        report.add("Preparing movie: " + title);
        try {
            lights.dimTo(MOVIE_BRIGHTNESS);
            report.add("  [1] Lights dimmed: " + previousBrightness + "% -> "
                    + lights.getBrightness() + "%.");
            screen.lower();
            report.add("  [2] Projection screen lowered.");
            player.powerOn();
            player.load(title);
            player.play();
            report.add("  [3] DVD player powered on; disc loaded; playback started.");
            amplifier.powerOn();
            amplifier.setVolume(MOVIE_VOLUME);
            amplifier.playFrom(player);
            report.add("  [4] Amplifier powered on; DVD audio connected; volume "
                    + amplifier.getVolume() + "/10.");
            projector.powerOn();
            projector.project(player, screen);
            report.add("  [5] Projector powered on; DVD video connected to the screen.");
            currentMovie = title;
            report.add("");
            report.add("Now watching: " + currentMovie);
            report.add("  DVD playback: " + (player.isPlaying() ? "PLAYING" : "STOPPED"));
            report.add("  Audio output: " + (amplifier.isAudible() ? "ACTIVE" : "SILENT"));
            report.add("  Projection: " + (projector.isProjecting() ? "ACTIVE" : "INACTIVE"));
            report.add(String.format(Locale.ROOT, "  Image diagonal: %.1f inches.",
                    projector.getImageDiagonalInches()));
            report.add("  Screen position: " + (screen.isLowered() ? "LOWERED" : "RAISED"));
            report.add("  Room brightness: " + lights.getBrightness() + "%.");
            return report.toString();
        } catch (RuntimeException failure) {
            restoreRoom();
            throw new IllegalStateException("Could not start movie: " + title, failure);
        }
    }

    public String endMovie() {
        if (currentMovie == null) {
            return "No movie session is active.";
        }
        String endedMovie = currentMovie;
        restoreRoom();
        return String.join(System.lineSeparator(),
                "Ending movie: " + endedMovie,
                "  [1] Projector powered off.",
                "  [2] Amplifier powered off.",
                "  [3] DVD player powered off; disc unloaded.",
                "  [4] Projection screen raised.",
                "  [5] Lighting restored to " + lights.getBrightness() + "%.",
                "", "Room after shutdown:",
                "  DVD player: " + (player.isPoweredOn() ? "ON" : "OFF"),
                "  Amplifier: " + (amplifier.isPoweredOn() ? "ON" : "OFF"),
                "  Projector: " + (projector.isPoweredOn() ? "ON" : "OFF"),
                "  Screen position: " + (screen.isLowered() ? "LOWERED" : "RAISED"),
                "  Room brightness: " + lights.getBrightness() + "%.",
                "Movie ended. Devices off, screen raised, previous lighting restored.");
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
