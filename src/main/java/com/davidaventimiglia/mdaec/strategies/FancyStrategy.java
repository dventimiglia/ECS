package com.davidaventimiglia.mdaec.model;

import java.util.*;

/**
 * ElevatorControlSytem Strategy that tries to allocate route requests
 * to the nearest elevator that's going in the same direction as the
 * route.  This is the "Going my way, Sailor?" strategy.
 */
public class FancyStrategy extends AbstractStrategy {

    /**
     * Helper class to be used by some strategies that want to try to
     * pick the nearest elevator.
     */
    class Candidate<T extends Candidate> implements Comparable<T> {
	int d;
	AbstractElevator e;

	public Candidate (int d, AbstractElevator e) {
	    this.d = d;
	    this.e = e;}

	@Override
	public int compareTo (T c) {
	    if (c.d < this.d) return -1;
	    if (c.d > this.d) return +1;
	    return 0;}}

    public void calculate (AbstractElevatorControlSystem ecs) {
	for (AbstractElevatorControlSystem.Route route : ecs.routes)
	    if (!route.picked) {
		SortedSet<Candidate> candidates = new TreeSet<>();
		for (int j = 0; j<ecs.ns; j++) {
		    AbstractElevator e = ecs.elevators[j];
		    if (route.start()>=e.position() &&
			e.direction()>=0 &&
			route.direction()>0) {
			candidates.add(new Candidate(Math.abs(route.start()-e.position()), e));}
		    if (route.start()<=e.position() &&
			e.direction()<=0 &&
			route.direction()<0) {
			candidates.add(new Candidate(Math.abs(route.start()-e.position()), e));}}

		if (candidates.isEmpty())
		    for (int j = 0; j<ecs.ns; j++)
			if (ecs.elevators[j].direction()==0)
			    candidates.add(new Candidate(Math
							 .abs(route.start()-ecs.elevators[j].position()),
							 ecs.elevators[j]));

		candidates.first().e.pickup(route);}


	for (AbstractElevatorControlSystem.Route route : ecs.routes)
	    if (route.picked)
		ecs.routes.remove(route);}}
		
