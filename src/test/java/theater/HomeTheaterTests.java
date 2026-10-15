package theater;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import theater.subsystem.Amplifier;
import theater.subsystem.DvdPlayer;
import theater.subsystem.Projector;
import theater.subsystem.Screen;
import theater.subsystem.TheaterLights;

/** Dependency-free integration and subsystem behavior tests; failures exit nonzero. */
public final class HomeTheaterTests {
    private static int passed;
    private HomeTheaterTests() { }

    public static void main(String[] args) {
        run("Complete movie session", HomeTheaterTests::completeMovieSession);
        run("Restore previous lighting", HomeTheaterTests::restorePreviousLighting);
        run("Repeated shutdown is harmless", HomeTheaterTests::repeatedShutdown);
        run("Reject invalid titles before changing state", HomeTheaterTests::invalidTitles);
        run("Reject overlapping sessions", HomeTheaterTests::overlappingSessions);
        run("Recover from a failed projector lamp", HomeTheaterTests::failedStartup);
        run("Restart after repairing the lamp", HomeTheaterTests::restartAfterRepair);
        run("Advanced clients bypass the facade", HomeTheaterTests::directSubsystemUse);
        run("Audio and video follow source state", HomeTheaterTests::signalTracksSource);
        run("Playback completion and replay", HomeTheaterTests::playbackCompletion);
        run("Reject invalid numeric settings", HomeTheaterTests::numericValidation);
        run("Reject unsafe device ordering", HomeTheaterTests::unsafeOrder);
        run("Public facade API hides subsystem types", HomeTheaterTests::publicApiBoundary);
        run("Validate and protect the movie catalogue", HomeTheaterTests::catalogueValidation);
        System.out.println("Passed " + passed + " tests.");
    }

    private static void completeMovieSession() {
        Fixture f = new Fixture(true);
        String playback = f.facade.watchMovie("Interstellar");
        check(playback.contains("Now watching: Interstellar"));
        check(playback.contains("Audio output: ACTIVE") && playback.contains("Projection: ACTIVE"));
        check(f.player.isPlaying() && f.amplifier.isAudible() && f.projector.isProjecting());
        check(f.screen.isLowered() && f.lights.getBrightness() == 15);
        check(f.amplifier.getVolume() == 5);
        String shutdown = f.facade.endMovie();
        check(shutdown.contains("DVD player: OFF") && shutdown.contains("Projector: OFF"));
        f.requireIdle(100);
    }

    private static void restorePreviousLighting() {
        Fixture f = new Fixture(true);
        f.lights.dimTo(65);
        String playback = f.facade.watchMovie("Inception");
        check(playback.contains("Lights dimmed: 65% -> 15%."));
        String shutdown = f.facade.endMovie();
        check(shutdown.contains("Lighting restored to 65%."));
        f.requireIdle(65);
    }

    private static void repeatedShutdown() {
        Fixture f = new Fixture(true);
        check(f.facade.endMovie().equals("No movie session is active."));
        f.lights.dimTo(80);
        f.facade.watchMovie("The Matrix");
        f.facade.endMovie();
        check(f.facade.endMovie().equals("No movie session is active."));
        f.requireIdle(80);
    }

    private static void invalidTitles() {
        Fixture f = new Fixture(true);
        f.lights.dimTo(70);
        for (String title : new String[] {null, "", "   ", "Unknown Movie"}) {
            expect(IllegalArgumentException.class, () -> f.facade.watchMovie(title));
            f.requireIdle(70);
        }
    }

    private static void overlappingSessions() {
        Fixture f = new Fixture(true);
        f.facade.watchMovie("Interstellar");
        expect(IllegalStateException.class, () -> f.facade.watchMovie("Inception"));
        check("Interstellar".equals(f.player.getLoadedMovie()));
        check(f.player.isPlaying() && f.amplifier.isAudible() && f.projector.isProjecting());
        f.facade.endMovie();
        f.facade.watchMovie("Inception");
        check("Inception".equals(f.player.getLoadedMovie()));
    }

    private static void failedStartup() {
        Fixture f = new Fixture(false);
        f.lights.dimTo(42);
        IllegalStateException error = expect(IllegalStateException.class,
                () -> f.facade.watchMovie("Interstellar"));
        check(error.getMessage().equals("Could not start movie: Interstellar"));
        check(error.getCause() instanceof IllegalStateException);
        check(error.getCause().getMessage().contains("lamp"));
        f.requireIdle(42);
        check(f.facade.endMovie().equals("No movie session is active."));
    }

    private static void restartAfterRepair() {
        Fixture f = new Fixture(false);
        expect(IllegalStateException.class, () -> f.facade.watchMovie("The Matrix"));
        f.projector.replaceLamp();
        f.facade.watchMovie("The Matrix");
        check(f.projector.isProjecting() && f.amplifier.isAudible());
        f.facade.endMovie();
        f.requireIdle(100);
    }

    private static void directSubsystemUse() {
        DvdPlayer player = new DvdPlayer(Map.of("Short Film", 20));
        Screen screen = new Screen(254.0);
        Projector projector = new Projector();
        player.powerOn();
        player.load("Short Film");
        player.play();
        screen.lower();
        projector.setAspectRatio(4.0 / 3.0);
        projector.powerOn();
        projector.project(player, screen);
        check(Math.abs(projector.getImageDiagonalInches() - 125.0) < 0.000001);
    }

    private static void signalTracksSource() {
        Fixture f = new Fixture(true);
        f.facade.watchMovie("Inception");
        f.player.stop();
        check(!f.amplifier.isAudible() && !f.projector.isProjecting());
        f.facade.endMovie();
        f.requireIdle(100);
    }

    private static void playbackCompletion() {
        DvdPlayer player = new DvdPlayer(Map.of("Short Film", 20));
        player.powerOn();
        player.load("Short Film");
        player.play();
        player.advanceMinutes(5);
        check(player.getElapsedMinutes() == 5 && player.isPlaying());
        player.advanceMinutes(Integer.MAX_VALUE);
        check(player.getElapsedMinutes() == 20 && !player.isPlaying());
        player.play();
        check(player.getElapsedMinutes() == 0 && player.isPlaying());
    }

    private static void numericValidation() {
        Fixture f = new Fixture(true);
        f.amplifier.powerOn();
        expect(IllegalArgumentException.class, () -> f.amplifier.setVolume(-1));
        expect(IllegalArgumentException.class, () -> f.amplifier.setVolume(11));
        expect(IllegalArgumentException.class, () -> f.lights.dimTo(-1));
        expect(IllegalArgumentException.class, () -> f.lights.dimTo(101));
        expect(IllegalArgumentException.class, () -> new Screen(0));
        expect(IllegalArgumentException.class, () -> new Screen(Double.NaN));
        expect(IllegalArgumentException.class, () -> f.projector.setAspectRatio(0));
        expect(IllegalArgumentException.class, () -> f.projector.setAspectRatio(Double.POSITIVE_INFINITY));
        expect(IllegalArgumentException.class, () -> f.screen.diagonalInches(Double.NaN));
        f.player.powerOn();
        f.player.load("Inception");
        f.player.play();
        expect(IllegalArgumentException.class, () -> f.player.advanceMinutes(0));
        check(f.player.getElapsedMinutes() == 0);
    }

    private static void unsafeOrder() {
        Fixture f = new Fixture(true);
        expect(IllegalStateException.class, () -> f.player.load("Inception"));
        expect(IllegalStateException.class, () -> f.amplifier.setVolume(5));
        f.player.powerOn();
        expect(IllegalStateException.class, f.player::play);
        f.player.load("Inception");
        f.projector.powerOn();
        expect(IllegalStateException.class, () -> f.projector.project(f.player, f.screen));
        f.player.play();
        expect(IllegalStateException.class, () -> f.player.load("The Matrix"));
        expect(IllegalStateException.class, () -> f.projector.project(f.player, f.screen));
        expect(IllegalStateException.class, () -> f.amplifier.playFrom(f.player));
        expect(IllegalStateException.class, f.projector::replaceLamp);
    }

    private static void publicApiBoundary() {
        for (var method : HomeTheaterFacade.class.getDeclaredMethods()) {
            if (Modifier.isPublic(method.getModifiers())) {
                check(method.getReturnType() == String.class);
                for (var parameter : method.getParameterTypes()) {
                    check(parameter == String.class);
                }
            }
        }
        for (var constructor : HomeTheaterFacade.class.getConstructors()) {
            check(constructor.getParameterCount() == 0);
        }
    }

    private static void catalogueValidation() {
        Map<String, Integer> catalogue = new HashMap<>();
        catalogue.put("Short Film", 20);
        DvdPlayer player = new DvdPlayer(catalogue);
        catalogue.clear();
        player.requireAvailable("Short Film");
        expect(IllegalArgumentException.class, () -> new DvdPlayer(Map.of()));
        expect(IllegalArgumentException.class, () -> new DvdPlayer(Map.of("Broken Film", 0)));
        expect(IllegalArgumentException.class, () -> new DvdPlayer(Map.of(" ", 10)));
    }

    private static void run(String name, Runnable test) {
        try {
            test.run();
            passed++;
            System.out.println("PASS " + name);
        } catch (RuntimeException | AssertionError failure) {
            throw new AssertionError("FAIL " + name, failure);
        }
    }

    private static void check(boolean condition) {
        if (!condition) { throw new AssertionError("Expected condition to be true."); }
    }

    private static <T extends RuntimeException> T expect(Class<T> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException failure) {
            if (type.isInstance(failure)) { return type.cast(failure); }
            throw new AssertionError("Expected " + type.getSimpleName(), failure);
        }
        throw new AssertionError("Expected " + type.getSimpleName() + " to be thrown.");
    }

    private static final class Fixture {
        final DvdPlayer player = new DvdPlayer();
        final Amplifier amplifier = new Amplifier();
        final Projector projector;
        final Screen screen = new Screen();
        final TheaterLights lights = new TheaterLights();
        final HomeTheaterFacade facade;

        Fixture(boolean lampHealthy) {
            projector = new Projector(lampHealthy);
            facade = new HomeTheaterFacade(player, amplifier, projector, screen, lights);
        }

        void requireIdle(int expectedBrightness) {
            check(!player.isPoweredOn() && !player.isPlaying() && player.getLoadedMovie() == null);
            check(!amplifier.isPoweredOn() && !amplifier.isAudible());
            check(!projector.isPoweredOn() && !projector.isProjecting());
            check(!screen.isLowered() && lights.getBrightness() == expectedBrightness);
        }
    }
}
