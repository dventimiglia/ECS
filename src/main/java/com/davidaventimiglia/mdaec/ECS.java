package com.davidaventimiglia.mdaec;

import com.davidaventimiglia.mdaec.model.*;
import com.davidaventimiglia.mdaec.renderers.*;
import java.util.*;

/**
 * Application main class
 *
 * Bootstraps the application with the number of elevator shafts and
 * the number of floors taken as input arguments. Sets up the
 * interpreter and installs a set of UI commands. Starts the
 * ElevatorControlSystem and injects a Renderer and a Strategy.
 */
public class ECS {
    static Map<String, Command> commands = new HashMap<>();

    public static void main (String[] args) {
	int nshafts = 0, nfloors = 0;

	// Try to parse input args but bail if fail.
	try {
	    nshafts = Integer.parseInt(args[0]);
	    nfloors = Integer.parseInt(args[1]);}
	catch (Exception e) {
	    System.out.println("Bad arguments!");
	    System.exit(1);}

	// Start the ElevatorControlSystem.
	// AbstractElevatorControlSystem ecs =
	//     new AbstractElevatorControlSystem(nshafts,
	// 				      nfloors,
	// 				      new FancyRenderer(),
	// 				      new FancyStrategy()){};

	AbstractElevatorControlSystem ecs =
	    new AbstractSQLBasedElevatorControlSystem(nshafts,
						      nfloors,
						      new FancyRenderer()){};

	// Install commands.
	commands.put("quit", new Command() {
		@Override
		public String toString () {return "Quit the application.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    System.exit(0);}});

	commands.put("call", new Command() {
		@Override
		public String toString () {return "Call an elevator.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    try {
			ecs.call(Integer
				 .parseInt(System
					   .console()
					   .readLine("start floor [0-%s]? ",
						     ecs.floors()-1)),
				 Integer
				 .parseInt(System
					   .console()
					   .readLine("goal floor [0-%s]? ",
						     ecs.floors()-1)));}
		    catch (Exception e) {
			System.out.println("Error!");}}});

	commands.put("step", new Command() {
		@Override
		public String toString () {return "Advance the simulation by one step. (default)";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    ecs.step();
		    System.out.println(ecs);}});

	commands.put("", commands.get("step"));

	commands.put("status", new Command() {
		@Override
		public String toString () {return "Get status of each elevator in turn.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    System.out.println("[id, floor, direction]:\n");
		    for (int[] s : ecs.status())
			System.out.println(Arrays.toString(s));}});

	commands.put("inspect", new Command() {
		@Override
		public String toString () {return "Inspect an elevator.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    try {
			System.out.println(ecs.inspect(Integer
						       .parseInt(System
								 .console()
								 .readLine("shaft? [0-%s]? ",
									   ecs.shafts()-1))));}
		    catch (Exception e) {
			System.out.println("Error!");}}});

	commands.put("display", new Command() {
		@Override
		public String toString () {return "Display the elevator positions.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    System.out.println(ecs);}});

	commands.put("routes", new Command() {
		@Override
		public String toString () {return "Display pending route requests.";}
		@Override
		public void exec (AbstractElevatorControlSystem ecs) {
		    System.out.println(ecs.routes());}});

	commands.put("help", new Command() {
		@Override
		public String toString () {return "Print help for commands.";}
		@Override	
		public void exec (AbstractElevatorControlSystem ecs) {
		    for (Map.Entry<String, Command> e : commands.entrySet())
			if (!"".equals(e.getKey()))
			    System.out.println(e);}});

	// Print a greeting.
	System.out.println("Enter 'help' to get command info.");

	// Start the command loop.
	while (true) {
	    Command c = commands.get(System.console().readLine("ecs> ").trim());
	    if (c!=null) {c.exec(ecs); continue;}
	    System.out.println("Unrecognized command");}}

    /**
     * Simple command interface
     */
    public interface Command {
	void exec (AbstractElevatorControlSystem ecs);}}

