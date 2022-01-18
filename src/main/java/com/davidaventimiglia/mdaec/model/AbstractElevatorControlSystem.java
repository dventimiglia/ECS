package com.davidaventimiglia.mdaec.model;

import com.davidaventimiglia.mdaec.renderers.*;
import java.util.*;

/**
 * Data type for the ElevatorControlSystem
 */
public abstract class AbstractElevatorControlSystem {
    int ns, nf;
    AbstractElevator[] elevators;
    AbstractRenderer renderer;
    AbstractStrategy strategy;
    SortedSet<Route> routes;
    boolean[][] calls;

    /**
     * Data type for a route (request), which records where a person
     * wants to pick up an elevator and what floor the person wants to
     * go to.
     */
    public class Route<T extends Route> implements Comparable<T> {
	int start;
	int goal;
	boolean picked;

	public Route (int start, int goal) {
	    this.start = start;
	    this.goal = goal;}

	/**
	 * Get the route direction.
	 */
	public int direction () {
	    return Integer.signum(goal-start);}

	/**
	 * Determine if the route has been picked up yet by an elevator 
	 * (more accurately, assigned by the ElevatorControlSytem).
	 */
	public boolean picked () {
	    return picked;}

	/**
	 * Get the route start floor.
	 */
	public int start () {
	    return start;}

	/**
	 * Get the route goal floor.
	 */
	public int goal () {
	    return goal;}

	/**
	 * Routes are ordered by their start floor.
	 */
	@Override
	public int compareTo (T r) {
	    if (r.start < this.start) return -1;
	    if (r.start > this.start) return +1;
	    return 0;}

	@Override
	public String toString () {
	    return String.format("%s->%s", start, goal);}

	@Override
	public boolean equals (Object o) {
	    if (!(o instanceof Route)) return false;
	    if (((Route)o).start != start) return false;
	    if (((Route)o).goal != goal) return false;
	    return true;}

	@Override
	public int hashCode () {
	    return (new int[]{start, goal}).hashCode();}}

    public AbstractElevatorControlSystem (int nshafts, int nfloors) {
	this(nshafts,
	     nfloors,
	     new AbstractRenderer(){},
	     new AbstractStrategy(){});}

    public AbstractElevatorControlSystem (int nshafts, int nfloors,
					  AbstractRenderer renderer) {
	this(nshafts,
	     nfloors,
	     renderer,
	     new AbstractStrategy(){});}

    public AbstractElevatorControlSystem (int nshafts, int nfloors,
					  AbstractStrategy strategy) {
	this(nshafts,
	     nfloors,
	     new AbstractRenderer(){},
	     strategy);}

    public AbstractElevatorControlSystem (int nshafts, int nfloors,
					  AbstractRenderer renderer,
					  AbstractStrategy strategy) {
	ns = nshafts;
	nf = nfloors;
	calls = new boolean[nfloors][2];
	elevators = new AbstractElevator[nshafts];
	for (int j=0; j<ns; j++) elevators[j] = new AbstractElevator(this, j){};
	this.renderer = renderer;
	this.strategy = strategy;
	routes = new TreeSet<>();}

    @Override
    public String toString () {
	return renderer.render(this);}

    /**
     * Iterable over the elevators and their statuses.  Each status is
     * an int[] of [Elevator ID (shaft), Floor Number (position), Goal
     * Floor Number]
     */
    public Iterable<int[]> status () {
	return new Iterable<int[]>() {
	    @Override
	    public Iterator<int[]> iterator () {
		return new Iterator<int[]>() {
		    int shaft = 0;
		    @Override
		    public boolean hasNext () {
			return shaft<ns;}
		    @Override
		    public int[] next () {
			if (!hasNext()) throw new NoSuchElementException();
			int[] v = new int[]{shaft,
					    elevators[shaft].position(),
					    elevators[shaft].goal()};
			shaft++;
			return v;}};}};}

    /**
     * This is a method from the Mesosphere-proposed interface.  I
     * didn't have much use for it, so it's unimplemented in a
     * somewhat hostile way (it throws).
     */
    public void update (int id, int floor, int goal) {
	throw new UnsupportedOperationException();}
	
    /**
     * (Debug) function to inspect the state of a particular elevator.
     */
    public String inspect (int shaft) {
	return ""+elevators[shaft];}

    /**
     * Alias to the 'call' method
     */
    public void pickup (int start, int goal) {
	call(start, goal);}

    /**
     * Call an elevator, creating a route request from the start floor
     * to the goal floor.
     */
    public void call (int start, int goal) {
	if (start==goal) return; // ignore routes to same floor
	if (start<goal) calls[start][0] = true;
	if (start>goal) calls[start][1] = true;
	routes.add(new Route(start, goal));} // add route to global sorted set

    /**
     * Get the set of calls (a call corresponds to a person waiting on
     * a floor).  I.e., it's the set of start values from all the
     * Routes.
     */
    public boolean[][] calls () {
	return calls;}

    /**
     * Get the set of routes (a Route includes both the start floor
     * and the goal floor).
     */
    public SortedSet<Route> routes () {
	return routes;}

    /**
     * Advance the ElevatorControlSystem one step.
     */
    public void step () {
	for (AbstractElevator e : elevators) e.step(); // advance elevators
	strategy.calculate(this); // allocate routes from sorted set to elevators
	return;}

    /**
     * Get the number of floors.
     */
    public int floors () {
	return nf;}

    /**
     * Get the set of elevators (as an array).
     */
    public AbstractElevator[] elevators () {
	return elevators;}

    /**
     * Get the number of elevator shafts.
     */
    public int shafts () {
	return ns;}}
