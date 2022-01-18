package com.davidaventimiglia.mdaec.model;

import java.util.*;

/**
 * Data type for an Elevator
 */
public abstract class AbstractElevator {
    int shaft;
    int position;
    int direction;
    SortedSet<Integer> mystops = new TreeSet<>();
    AbstractElevatorControlSystem ecs;

    public AbstractElevator (AbstractElevatorControlSystem ecs, int shaft) {
	this.shaft = shaft;
	this.ecs = ecs;}

    @Override
    public String toString () {
	StringBuffer sb = new StringBuffer();
	sb.append(String.format("shaft:  %s\n", shaft));
	sb.append(String.format("position:  %s\n", position));
	sb.append(String.format("direction:  %s\n", direction));
	sb.append(String.format("stops:  %s\n", mystops));
	return sb.toString();}

    /**
     * Pick up a route, adding the route start and goal floors to our
     * list of stops.
     */
    public void pickup (AbstractElevatorControlSystem.Route route) {
	route.picked = true;
	mystops.add(route.start);	// add route endpoints
	mystops.add(route.goal);}	// to my list of stops

    /**
     * Get my shaft number.
     */
    public int shaft () {
	return shaft;}

    /**
     * Get my current floor number.
     */
    public int position () {
	return position;}

    /**
     * Get my current direction.
     *
     * -1 = down
     *  0 = stationary
     * +1 = up
     */
    public int direction () {
	// direction SHOULD always be in [-1, 0, 1], but let's make sure.
	return Integer.signum(direction);}

    /**
     * Get my current goal floor (or the ground floor as a last resort).
     */
    public int goal () {
	return mystops.isEmpty() ? 0 : mystops.first();}

    /**
     * Advance my own state by one step.
     */
    public void step () {
	position+=direction;	// advance one floor in whatever direction we're going

	if (position==ecs.nf-1) { // but reverse at the top
	    position = ecs.nf-1;
	    direction = -1;}

	if (position<0) {	// and stop at the bottom
	    position = 0;
	    direction = 0;}

	mystops.remove(position); // clear stop from list of stops
	if (direction>=0)
	    ecs.calls[position][0] = false;
	else
	    ecs.calls[position][1] = false;

	if (direction>0)	// going up?
	    if (mystops.subSet(position, ecs.nf).isEmpty()) { // but no stops above us?
		direction=-1;	// then go back down
		return;}

	if (direction<0)	// going down?
	    if (mystops.isEmpty()) { // but no stops at all?
		direction=-1;	   // then head for the ground floor
		return;}

	if (direction<0)	   // going down?
	    if (!mystops.subSet(position, ecs.nf).isEmpty()) // stops above us?
		if (mystops.subSet(0, position).isEmpty()) { // but none below us
		    direction=1;			   // then head up
		    return;}

	if (direction==0)	// stationary?
	    if (!mystops.subSet(position, ecs.nf).isEmpty()) { // stops above us?
		direction = 1;	// then go up
		return;}

	if (direction==0)	// stationary?
	    if (!mystops.subSet(0, position).isEmpty()) { // stops below us?
		direction = -1;				  // then go down
		return;}}}


