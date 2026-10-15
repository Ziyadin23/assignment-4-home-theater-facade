# Home Theater Facade Defense Guide

## Walkthrough

1. State the problem: playing a movie requires five devices and an ordered sequence.
2. Show `Client.java`. It creates only `HomeTheaterFacade` and calls two methods.
3. Show the UML diagram. Point out Client -> Facade, five device associations,
   Amplifier -> DvdPlayer, and Projector -> DvdPlayer/Screen. No device points back
   to the facade.
4. Open `watchMovie`. Explain validation before mutation, saved lighting, source
   readiness before routing, and the final session flag.
5. Open `restoreRoom`. Explain disconnecting audio and video before stopping the
   source, then raising the screen and restoring the actual previous brightness.
6. Open the device classes. Demonstrate the catalogue, bounded playback position,
   volume range, lamp validation, and screen diagonal calculation.
7. Open `failedStartup` in the test class. The failed lamp is reached after the
   player and amplifier have started; the test checks cleanup and the saved cause.
8. Open `directSubsystemUse`. It builds devices without any facade and computes
   a 125-inch diagonal for a 254 cm screen at a 4:3 aspect ratio.

## Live demonstration

```bash
./scripts/run.sh
./scripts/run.sh "The Matrix"
./scripts/run.sh "Unknown Movie"
./scripts/test.sh
```

In the default demo, explain each numbered setup step, the active playback state,
the calculated image diagonal, and the shutdown steps. The final state confirms
that the devices are off and the previous lighting has been restored. The client
prints text returned by the facade and never accesses the device classes.

Explain that the invalid-movie command intentionally returns a nonzero exit code.
In IntelliJ, run `Client` with a movie title as one quoted program argument, then
run `HomeTheaterTests`. Set a breakpoint inside the failed-startup test to inspect
the device state before the exception and after cleanup.

## Questions and answers

**Why is this a Facade?**
It gives a common client two session operations over five independently useful
device classes. It hides construction, ordering, shared-source routing, policy
defaults, and failure cleanup. The devices do not know that the facade exists.

**Why is the subsystem non-trivial?**
The player validates a catalogue and advances playback; amplifier and projector
depend on that player's live state; projector also depends on the screen position
and lamp state. Geometry and numeric constraints are computed or enforced rather
than represented by print statements.

**What is the correct start order?**
Validate the title, save and dim lighting, lower the screen, power/load/play the
DVD, power/configure/connect the amplifier, then power/connect the projector.

**Why does the client never create the devices?**
The facade's default constructor creates them. The client depends only on the
facade; a package-private constructor is a test seam, not a public client API.

**Can an advanced client bypass it?**
Yes. Public subsystem constructors and methods remain available. An advanced
client can choose its own catalogue, brightness, volume, screen, and aspect ratio
and is responsible for the resulting device order. The bypass test verifies this.

**Is this Adapter or Mediator?**
An Adapter translates an incompatible interface. This facade simplifies a group
of existing interfaces. A Mediator becomes part of component communication;
here the devices directly inspect the DVD source and screen without the facade.

**Which Clean Code principles are demonstrated?**
Meaningful names, single responsibility, encapsulation, focused classes, proper
error handling, and named constants with shared cleanup. Point to the six
annotated excerpts in the report.

**What happens if startup fails?**
The room returns to idle, previous lighting is restored, and an IllegalStateException
retains the subsystem exception as its cause. Invalid titles are rejected earlier
without mutating any device. A broken lamp can be repaired before retrying.

**Does endMovie restore all possible prior state?**
It restores the saved lighting and returns facade-owned devices to idle. The
facade is a session controller for its own devices, not a snapshot system for
arbitrary state changes made externally. Advanced direct clients own separate
device configurations.

**Why no second facade?**
The assignment makes it optional. All present operations belong to one movie
session; a separate music or maintenance workflow could justify another facade.

**Does this play actual video or operate hardware?**
No. It is an executable stateful simulation for the design-pattern assignment.
Its constraints and dependencies are real program behavior, but no physical
device or media-decoding integration is claimed.

**What would change for real hardware?**
Add device interfaces and hardware implementations, make every cleanup attempt
independently, preserve cleanup failures, and define concurrency and timeouts.
The simple public movie-session API could remain the same.

## Submission steps

- Read the source and report and practice the demonstration and answers.
- Upload the report and repository link to Moodle by the course Week 6 Sunday
  23:59 deadline. Confirm the calendar date in Moodle rather than guessing it.
- Attend the last practice class in Week 7 for the defense.
