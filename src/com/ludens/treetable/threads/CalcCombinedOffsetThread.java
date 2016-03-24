package com.ludens.treetable.threads;

import java.util.ArrayList;

import megamu.mesh.Edge;
import megamu.mesh.Outline;

import com.ludens.treetable.EdgeGenerator;
import com.vividsolutions.jts.geom.Geometry;

public class CalcCombinedOffsetThread extends CalcThread {

	private ArrayList<Outline> insides;
	private ArrayList<Edge> largeEdges;
	private double largeThickness;
	private double largeCornerRadius; 
	private ArrayList<Edge> smallEdges; 
	private double smallThickness;
	private double smallCornerRadius;
	private int edgeShape;
	
	private EdgeGenerator eg;
	
	public ArrayList<Outline> combinedOffsetEdges;
	
	
	public CalcCombinedOffsetThread( 
			ArrayList<Outline> insides,
			ArrayList<Edge> largeEdges, 
			double largeThickness, 
			double largeCornerRadius, 
			ArrayList<Edge> smallEdges, 
			double smallThickness,
			double smallCornerRadius,
			int edgeShape){
		
		this.insides = insides;
		this.largeEdges = largeEdges;
		this.largeThickness = largeThickness;
		this.largeCornerRadius = largeCornerRadius;
		this.smallEdges = smallEdges;
		this.smallThickness = smallThickness;
		this.smallCornerRadius = smallCornerRadius;
		this.edgeShape = edgeShape;
		
		eg = new EdgeGenerator();
		running = false;
		
		threadName = "CombinedOffset";
		this.setPriority(MIN_PRIORITY);
	}
	public void start(){
		
		trace("Starting calculation of offset edges");
		
		running = true;
		
		super.start();
	}
	
	public void run(){
		
		if( largeEdges != null && smallEdges != null ){
			// calculate the combined offsets
			combinedOffsetEdges = eg.generateCombinedEdges(
					insides, 
					largeEdges, 
					largeThickness, 
					largeCornerRadius, 
					smallEdges, 
					smallThickness, 
					smallCornerRadius,
					edgeShape
				);
			dirty = true;
			trace("Finished!");
		} else {
			trace("Didn't run, small or large edges don't exist!");
		}
		
		
		
		running = false;
	}
	
	
}
