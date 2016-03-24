package com.ludens.treetable;

import java.awt.geom.Point2D;
import java.util.ArrayList;

import megamu.mesh.Attractor;

public class AttractorList {

	private ArrayList<Attractor> attractors;
	
	public AttractorList( ArrayList<Attractor> attractors ){
		this.attractors = attractors;
		
	}
	
	public Attractor closestAttractorTo( Point2D.Double point ){
		
		double largeNumber = 1000000000000d;
		
		Point2D.Double farPoint = new Point2D.Double(largeNumber, largeNumber);
		
		Attractor closestAttractor = new Attractor( farPoint, 1);
		double closestDistance = 1E30;
		
		for( int i = 0; i < attractors.size(); i++){
			Attractor a = attractors.get(i);
			if( a.distanceSq(point) < closestDistance){
				closestDistance = a.distanceSq(point);
				closestAttractor = a;
			}
		}
		
		return closestAttractor;
	}
	
}
