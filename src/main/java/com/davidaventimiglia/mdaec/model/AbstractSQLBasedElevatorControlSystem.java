package com.davidaventimiglia.mdaec.model;

import com.davidaventimiglia.mdaec.renderers.*;
import java.util.*;
import java.sql.*;

public abstract class AbstractSQLBasedElevatorControlSystem extends AbstractElevatorControlSystem {
    public abstract class AbstractSQLBasedElevator extends AbstractElevator {
	public AbstractSQLBasedElevator (AbstractElevatorControlSystem ecs, int shaft, int posiiton, int direction) {
	    super(ecs, shaft);
	    this.position = position;
	    this.direction = direction;}}
    
    Connection c;

    public AbstractSQLBasedElevatorControlSystem (int nshafts, int nfloors,
						  AbstractRenderer renderer) {
	super(nshafts, nfloors, renderer);
	try {
	    c = DriverManager.getConnection("jdbc:sqlite::memory:");
	    try (Statement s = c.createStatement()) {
		s.executeUpdate((new Scanner(AbstractSQLBasedElevatorControlSystem.class.getResourceAsStream("/model.sql")).useDelimiter("\\A")).next());
		s.executeUpdate("insert into building (name) values ('Test Building')");
		for (int i = 0; i<nfloors; i++) s.executeUpdate(String.format("insert into floor (building_id, floor_number) values (1, %s)", i));
		for (int i = 0; i<nshafts; i++) s.executeUpdate("insert into elevator (floor_id) values (1)");
	    }}
	catch (Exception e) {
	    e.printStackTrace(System.out);
	    throw new RuntimeException(e);}}

    public Connection getConnection () {
    	return c;}

    @Override
    public int shafts () {
	try (Statement s = c.createStatement();
	     ResultSet r = s.executeQuery("select count(*) from elevator")) {
	    while (r.next()) return r.getInt(1);}
	catch (Exception e) {throw new RuntimeException(e);}
	throw new IllegalStateException();}

    @Override
    public int floors () {
	try (Statement s = c.createStatement();
	     ResultSet r = s.executeQuery("select count(*) from floor")) {
	    while (r.next()) return r.getInt(1);}
	catch (Exception e) {throw new RuntimeException(e);}
	throw new IllegalStateException();}

    @Override
    public Iterable<int[]> status () {
	throw new UnsupportedOperationException();}

    @Override
    public String inspect (int shaft) {
	throw new UnsupportedOperationException();}

    @Override
    public void pickup (int start, int goal) {
	throw new UnsupportedOperationException();}

    @Override
    public void call (int start, int goal) {
	try (Statement s = c.createStatement()) {
	    s.executeUpdate(String.format("insert into route (start_floor_id, end_floor_id) select s.id as start_id, e.id as end_id from floor s, floor e where s.floor_number = %s and e.floor_number = %s", start, goal));}
	catch (Exception e) {throw new RuntimeException(e);}}

    @Override
    public boolean[][] calls () {
	try (Statement s = c.createStatement();
	     ResultSet r = s.executeQuery("select * from v_route_ext")) {
	    boolean[][] calls = new boolean[floors()][2];
	    while (r.next()) {
		if (r.getInt("direction") > 0) calls[r.getInt("start_floor")][0] = true;
		if (r.getInt("direction") < 0) calls[r.getInt("start_floor")][1] = true;}
	    return calls;}
	catch (Exception e) {throw new RuntimeException(e);}}

    @Override
    public SortedSet<Route> routes () {
	throw new UnsupportedOperationException();}

    @Override
    public void step () {
	try (Statement s = c.createStatement()) {
	    s.executeUpdate("insert into clock (id) values (null);");}
	catch (Exception e) {throw new RuntimeException(e);}}

    @Override
    public AbstractElevator[] elevators () {
	try (Statement s = c.createStatement();
	     ResultSet r = s.executeQuery("select * from v_elevator_ext")) {
	    List<AbstractElevator> elevators = new ArrayList<>();
	    while (r.next()) elevators.add(new AbstractSQLBasedElevator(this, r.getInt(1), r.getInt(4), r.getInt(3)){});
	    return elevators.toArray(new AbstractElevator[0]);}
	catch (Exception e) {throw new RuntimeException(e);}}}
