package com.davidaventimiglia.mdaec.renderers;

import com.davidaventimiglia.mdaec.model.*;
import java.util.*;

/**
 * Make a LITTLE more effort than just printing raw values to the
 * screen.
 */
public class FancyRenderer extends AbstractRenderer {
    public static final String ELEVATOR = "\u2588";
    public static final String BLACK_SQUARE = "\u25A0";
    public static final String UP = "\u25B2";
    public static final String DOWN = "\u25BC";
    public static final String LIGHT_SHADE = "\u2591";
    public static final String MEDIUM_SHADE = "\u2592";
    public static final String DARK_SHADE = "\u2593";

    @Override
    public String render (AbstractElevatorControlSystem ecs) {
	StringBuffer sb = new StringBuffer();
	sb.append("\n");
	sb.append("    ");
	for (int j = 0; j<ecs.shafts(); j++)
	    sb.append(ecs.elevators()[j].direction()<0 ?
		      DOWN :
		      ecs.elevators()[j].direction()==0 ?
		      " " :
		      UP).append("  ");
	sb.append("\n");
	for (int i = ecs.floors()-1; i>=0; i--) {
	    sb.append(String.format("%s:  ", i));
	    for (int j = 0; j<ecs.shafts(); j++)
		sb.append(String
			  .format("%s  ",
				  ecs.elevators()[j].position()==i ?
				  DARK_SHADE :
				  LIGHT_SHADE));
	    sb.append(ecs.calls()[i][0] ? UP : " ");
	    sb.append(ecs.calls()[i][1] ? DOWN : " ");
	    sb.append("\n");}
	return sb.toString();}}

