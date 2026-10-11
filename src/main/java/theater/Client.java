package theater;

/** The application depends only on the facade, never on a subsystem class. */
public final class Client {
    private Client() { }

    public static void main(String[] args) {
        HomeTheaterFacade theater = new HomeTheaterFacade();
        String movie = args.length == 0 ? "Interstellar" : args[0];
        try {
            System.out.println(theater.watchMovie(movie));
            System.out.println(theater.endMovie());
            System.out.println(theater.endMovie());
        } catch (IllegalArgumentException | IllegalStateException failure) {
            System.err.println("Movie session failed: " + failure.getMessage());
            System.exit(1);
        }
    }
}
