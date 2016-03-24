package com.ludens.treetable.threads;

import java.util.ArrayList;

import com.ludens.treetable.EdgeGenerator;

import megamu.mesh.Attractor;
import megamu.mesh.Edge;
import megamu.mesh.Outline;

public class CalcVoronoiThread extends CalcThread {

	ArrayList<Attractor> attractors;
	ArrayList<Outline> insides;
	ArrayList<Outline> simpleInsides;
	Outline outside;
	EdgeGenerator eg;
	
	double cellThickness;
	double cellCornerRadius;
	
	double snapDistance;
	
	// generated
	public ArrayList<Edge> edges;
	public ArrayList<Outline> cells;
	public ArrayList<Outline> insetCells;
	
	public double[][] dualPoints;
	
	
	
	public boolean edgesFinished = false;
	
	
	public CalcVoronoiThread( ArrayList<Attractor> attractors, ArrayList<Outline> insides, ArrayList<Outline> simpleInsides, Outline outside, double cellThickness, double cellCornerRadius, double snapDistance){
		this.attractors = attractors;
		this.insides = insides;
		this.simpleInsides = simpleInsides;
		this.outside = outside;
		
		this.cellThickness = cellThickness;
		this.cellCornerRadius = cellCornerRadius;
		this.snapDistance = snapDistance;
		
		
		edgesFinished = false;
		running = false;
		edgesFinished = false;
		
		threadName = "Voronoi";
		this.setPriority(NORM_PRIORITY);
	}
	
	public void start(){
		trace("Starting!");
		running = true;
		edgesFinished = false;
		super.start();
	}
	
	public void run(){
		eg = new EdgeGenerator(attractors, outside);
		
		// TODO: implement multiple inside shapes
		//Outline inside = insides.get(0);
		//Outline simpleInside = simpleInsides.get(0);
		
		eg.generateAllSnap(simpleInsides, snapDistance);
		edges = eg.getEdgesInOut(insides, outside);
		cells = eg.getCellsInOut(insides, outside);
		dualPoints = eg.dualPoints;
		edgesFinished = true;
		
		calculateInsetCells();
		dirty = true;
		running = false;
		trace("Finished!");
	}
	
	/**
	 * helper function to calculate the inset cells
	 */
	public void calculateInsetCells( ){
		insetCells = new ArrayList();
		
		for(int i = 0; i < cells.size();i++){
			ArrayList<Outline> os = cells.get(i).getRoundedInset(cellThickness/2, cellCornerRadius);
			
			if( os!= null)
				insetCells.addAll(os);
			
		}
	}
}
