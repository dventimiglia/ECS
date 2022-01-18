# README #

Elevator Control System simulation.

## Build Instructions ##

This project uses [Maven](https://maven.apache.org/).  To build it,
type this command from the command line, while in the directory
containing this README file:

    mvn compile

This will compile the classes into the target/classes sub-directory.

## Operating Instructions ##

To run this program, type this command from the command line, while in
the directory containing this README file:

    java -cp target/classes/ com.davidaventimiglia.mdaec.ECS <shafts> <floors>

* <shafts> is a positive integer representing the number of shafts.
* <floors> is a position integer representing the number of floors.

This will start a command loop with the "ecs>" prompt.  Type the
"help" command to get a descriptive list of the available commands.

    call=Call an elevator.
    routes=Display pending route requests.
    help=Print help for commands.
    display=Display the elevator positions.
    inspect=Inspect an elevator.
    quit=Quit the application.
    step=Advance the simulation by one step. (default)
    status=Get status of each elevator in turn.

## Discussion ##

I had to make some decisions about how to interpret this assignment
and how to solve it.  One decision was not to treat it as a problem of
"concurrent" or "distributed" programming.  Partly, that was to make
life easier for myself, but partly that was because it's plausible to
me the circumstances don't warrant it.  I don't know anything about
elevator control systems, but in my experience buildings don't usually
have a huge number of elevators, so it doesn't seem like there would
be much of a performance gain in distributing the computation across
multiple unites.  Moreover, it doesn't seem like the actual elevators
are where one would place the intelligence, putting that rather into a
central Elevator Control System.  I suppose one benefit of putting it
into the elevators and making them more independent would be in the
case of failure.  If elevators are autonomous agents making bids with
each other over who will fulfill a pickup request, then there's no
"single point of failure" in a central Elevator Control System.
That's worth considering, but it's not the direction I chose.

Anyway, given that decision, it seemed to obviate the necessity of the
`update` API call in the Mesosphere-proposed interface.  That call
would seem to satisfy an update request *from* an elevator *to* the
elevator control system, regarding its id, its current floor, and its
goal floor.  But, if all the intelligence is in the central elevator
control system, and if it has references to all the elevators and can
easily get their state just with method calls, this `update` call
would seem to be unnecessary.  Anyway, that's how I treated it,
leaving it basically unimplemented (it throws an exception if called).

Another decision I made had to do with some details of Java program,
and it was combination of Java classes, abstract classes, and
interfaces would I use.  Ideally, the cleanest and most orthodox
solution would be to have Java interfaces for all the types, then
maybe abstract classes (partially) implementing them, and then
concrete implementations filling out the rest of the details.  But,
that seemed like a lot of ceremony for what *should* be a short
project, so I elected just to go with the model/api being defined with
abstract classes, and then a few concrete implementations here and
there (like the FancyRenderer and the FancyStrategy).

Another decision I made was to make a functioning---albeit
crude---command interpreter.  Doing this for a small project is
debatable, but I anticipated having to run my simulation in an
exploratory fashion in order to understand its behavior and
iteratively improve upon it.

Another decision I made was not to provide any tests.  Writing code
without tests is a high-wire act with no net and a bit of a "no no"
these days, but this project was simply taking too long as it is, so
tests were among the first thing to get cut.  Instead, I just
"verified" the program behavior by running it and issuing commands.
NOTE:  I do not recommend this practice in general.

Another decision I made was to add *some* degree of extensibility into
the system.  This came in in several ways.  First, there's the
aforementioned command interpreter, with new commands being relatively
easy to add.  Second, rendering of the Elevator Control System state
(and all the Elevators) is handled through *ahem* "pluggable"
components (the renderers).  Third, that pattern also is applied to
the control system logic.  NOTE:  having said that, I really only
implemented one Renderer and one Strategy.

Finally, another decision I made regarded *what* logic I would put
into my (one) Strategy implementation.  What I *tried* to achieve was
the following:

  * Elevators shouldn't just reverse course willy-nilly to handle
    requests.  That might take some passengers out of their way en
    route to their destination!
  * Instead, elevators should "smoothly" rove up and down, fulfilling
    whatever requests they can along the way.
  * If an elevator is going up but there are no more pickup requests
    whose starts are above it, then it should reverse course and go
    down.
  * An elevator going up should only pickup requests that start at its
    current floor and go up.
  * An elevator going down should only pickup requests that start at
    its current floor and go down.
  * If multiple elevators *could* satisfy a request (they're going up
    and are below the request or are going down and are above the
    request) then the "nearest" elevator picks up the request.
  * People make call an elevator (a pickup request) by specifying a
    start floor and a goal floor.  Together, these comprise a "route"
    which goes into a SortedSet, ordered by the start floor.
  * When an elevator picks up a route, both the start and goal floor
    are added to its list of "stops."  The start/goal distinction
    doesn't much matter to an elevator, since it has to stop on the
    floor and open/close its doors in the same way for each, in any
    case.
  * If the above strategy fails to pick up a route request, then fall
    back to having the first available stationary elevator pick up the
    request.  This isn't ideal, but it *should* work since absent any
    stops, all elevators eventually head for the ground floor and stop
    there.  If they do that, then they're available to pick up this
    passenger.  We don't want to strand anybody!
  * NOTE:  Above, when I say that an elevator "picks up" a route
    request, bear in mind that what's really happening is the
    ElevatorControlSystem is *assigning* the route to the elevator.
    Again, individual elevators have no intelligence within them.

