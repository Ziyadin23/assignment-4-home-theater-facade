# Home Theater Facade

Assignment 4 for ShP-2216 Software Design Patterns, Astana IT University.
Option B implements a stateful home theater simulation in Java 17. The devices
do real validation, playback-state management, signal routing, and screen geometry
calculations; the application does not control physical hardware or decode video.

## Run and test

Install a **JDK 17**, not only a JRE. No Maven, Gradle, or external Java libraries
are required. If several JDKs are installed, set `JAVA_HOME` to the JDK 17 folder.

On Linux or macOS, from the repository root:

```bash
./scripts/run.sh
./scripts/run.sh "Inception"
./scripts/test.sh
```

The default demonstration prints numbered setup and shutdown steps, followed by
actual device states. The image diagonal is calculated by the projector and screen.
The client still uses only the facade; its two operations return these text reports.
The full verified transcript is also saved in `docs/demo-output.txt`.

```text
=== HOME THEATER FACADE DEMO ===

--- Start movie ---
Preparing movie: Interstellar
  [1] Lights dimmed: 100% -> 15%.
  [2] Projection screen lowered.
  [3] DVD player powered on; disc loaded; playback started.
  [4] Amplifier powered on; DVD audio connected; volume 5/10.
  [5] Projector powered on; DVD video connected to the screen.

Now watching: Interstellar
  DVD playback: PLAYING
  Audio output: ACTIVE
  Projection: ACTIVE
  Image diagonal: 108.4 inches.
  Screen position: LOWERED
  Room brightness: 15%.

--- End movie ---
Ending movie: Interstellar
  [1] Projector powered off.
  [2] Amplifier powered off.
  [3] DVD player powered off; disc unloaded.
  [4] Projection screen raised.
  [5] Lighting restored to 100%.

Room after shutdown:
  DVD player: OFF
  Amplifier: OFF
  Projector: OFF
  Screen position: RAISED
  Room brightness: 100%.
Movie ended. Devices off, screen raised, previous lighting restored.

--- End movie again ---
No movie session is active.
```

An unknown title is rejected before any device is changed. This command exits 1:

```bash
./scripts/run.sh "Unknown Movie"
```

In **IntelliJ IDEA**, open this folder, choose JDK 17 as the project SDK, mark
`src/main/java` as Sources Root and `src/test/java` as Test Sources Root, then run
`theater.Client` or `theater.HomeTheaterTests` using their main methods.

On Windows PowerShell with JDK 17 on PATH:

```powershell
New-Item -ItemType Directory -Force build/classes, build/test-classes | Out-Null
Get-ChildItem src/main/java -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName + '"' } | Set-Content build/main-sources.txt
javac --release 17 -Xlint:all -Werror -d build/classes '@build/main-sources.txt'
java -cp build/classes theater.Client
Get-ChildItem src/test/java -Recurse -Filter *.java | ForEach-Object { '"' + $_.FullName + '"' } | Set-Content build/test-sources.txt
javac --release 17 -Xlint:all -Werror -cp build/classes -d build/test-classes '@build/test-sources.txt'
java -cp 'build/classes;build/test-classes' theater.HomeTheaterTests
```

## Folder structure

```text
src/main/java/theater/
  Client.java                     Facade-only application
  HomeTheaterFacade.java           Two public session operations
  subsystem/
    DvdPlayer.java                 Catalogue and playback position
    Amplifier.java                 Audio routing and volume
    Projector.java                 Lamp and video signal checks
    Screen.java                    Position and image geometry
    TheaterLights.java             Brightness validation
src/test/java/theater/
  HomeTheaterTests.java            14 behavior tests
scripts/                          Build, demonstration, and test scripts
docs/                             UML, report, test results, and defense guide
```

## Design

The public facade has a no-argument constructor and only two operations:

```java
HomeTheaterFacade theater = new HomeTheaterFacade();
System.out.println(theater.watchMovie("Interstellar"));
System.out.println(theater.endMovie());
```

`watchMovie` validates the title, saves the old brightness, prepares the room,
starts the DVD source, connects audio, and starts projection. It rejects a second
session until the first ends. A startup failure powers off the devices, raises the
screen, restores the old brightness, and retains the original exception as a cause.

`endMovie` shuts down consumers before the DVD source, restores lighting, and can
be repeated safely. The main client neither imports nor instantiates a device.
All five public subsystem classes work independently, with no reference to the
facade. Integration tests use a package-private constructor to inspect owned
devices and simulate a broken lamp without expanding the public client API.

![UML class diagram](docs/uml.png)

The diagram shows selected members. Solid arrows are navigable associations;
dashed arrows are dependencies. The editable UML source is `docs/uml.puml`.

## Clean Code

1. Meaningful names: `watchMovie`, `endMovie`, and `requireAvailable` state intent.
2. Single responsibility: the facade coordinates; each device owns its own rules.
3. Encapsulation: only strings cross the facade's public API; no device getters.
4. Focused classes: playback, sound, projection, geometry, and lighting are separate.
5. Error handling: validate first, restore the room on startup failure, retain causes.
6. Named constants and shared cleanup: brightness and volume policies have names;
   normal shutdown and failure recovery share `restoreRoom`.

The report includes an annotated code excerpt and justification for each principle.

## Verification and defense

Verified on Eclipse Temurin **JDK 17.0.20.1** with `--release 17 -Xlint:all -Werror`.
All **14 tests passed**. The [Word report](docs/Assignment_4_Facade_Pattern_Report.docx)
includes the required introduction, UML, annotated Clean Code excerpts, conclusion,
and repository link. Full output is in `docs/test-results.txt`. The tests cover
coordination, invalid requests, recovery, geometry, device ordering, the public
API boundary, and independent subsystem use.

The [defense guide in Word](docs/Assignment_4_Facade_Defense_Guide.docx) and
[online study guide](docs/defense-guide.md) explain Facade and key terms, the UML
diagram, setup and recovery, Clean Code principles, advantages and disadvantages,
related patterns, and practice questions with answers.

The program is synchronous and intended for one caller. Device power-off methods
cannot fail in this simulation. Physical hardware would require device adapters,
independent cleanup attempts, and a policy for reporting cleanup failures.

## Sources

- Assignment 4 Facade Pattern Instructions, supplied course document.
- [Facade explanation](https://refactoring.guru/design-patterns/facade).
- [Java 17 IllegalStateException](https://docs.oracle.com/en/java/javase/17/docs/api/java.base/java/lang/IllegalStateException.html).
