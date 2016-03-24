package com.ludens.treetable.threads;

import java.util.ArrayList;

import com.ludens.treetable.AttractorGenerator;
import com.ludens.treetable.EdgeGenerator;

import megamu.mesh.Attractor;
import megamu.mesh.Edge;
import megamu.mesh.Outline;

public class CalcSubVoronoiThread extends CalcThread {
	/*
	ArrayList<Attractor> attractors;
	ArrayList<Outline> insides;
	Outline outside;
	*/
	
	ArrayList<EdgeGenerator> subEdgeGenerators;
	ArrayList<Attractor> subAttractors;
	ArrayList<Outline> insetCells;
	
	
	double subCellThickness;
	double subCellCornerRadius;
	double subAttractorDistance;
	double subEdgeDistance;
	
	// generated
	public ArrayList<Edge> subEdges;
	public ArrayList<Outline> subCells;
	public ArrayList<Outline> subInsetCells;
	
	
	public boolean edgesFinished = false;
	
	public CalcSubVoronoiThread( ArrayList<Outline> insetCells, double subAttractorDistance,  double subCellThickness, double subCellCornerRadius, double subEdgeDistance){
		
		this.insetCells = insetCells;
		
		
		this.subCellThickness = subCellThickness;
		this.subCellCornerRadius = subCellCornerRadius;
		this.subAttractorDistance = subAttractorDistance;
		this.subEdgeDistance = subEdgeDistance;
		
		running = false;
		edgesFinished = false;
		
		threadName = "SubVoronoi";
	}
	
	public void start(){
		trace("Starting!");
		running = true;
		edgesFinished = false;
		super.start();
	}
	
	public void run(){
		
		initSubAttractors();
		
		if( subEdgeGenerators == null ){
			trace("Sub Edge Generators don't exist!");
			return;
		}
		
		
		
		subCells = new ArrayList();
		subEdges = new ArrayList();
		
		
		// generate a edge generator for each cell
		for(int i = 0; i < subEdgeGenerators.size(); i++){
			//Outline insetCell = insetCells.get(i);
			EdgeGenerator eg = subEdgeGenerators.get(i);
			eg.generateAll();
			subCells.addAll( eg.regions );
			subEdges.addAll(eg.edges );

		}
		
		edgesFinished = true;
		
		//calculateInsetSubCells( );
		dirty = true;
		
		running = false;
		trace("Finished!");
	}
	
	public void initSubAttractors( ){
		
		if( insetCells == null){
			trace( "No insets calculated yet!");
			return;
		}
		
		subEdgeGenerators = new ArrayList();
		subAttractors = new ArrayList();
		
		for(int i = 0; i < insetCells.size(); i++){
			Outline insetCell = insetCells.get(i);
			
			ArrayList<Attractor> subAttr = new ArrayList();
			
			// do a loop to pretty much make sure we have some points in this 
			int iter = 0;
			int maxIterations = 10;
			while( subAttr.size() == 0 && iter < maxIterations){
				subAttr = AttractorGenerator.generateInOut(insetCell, new ArrayList<Outline>(), (int)subAttractorDistance, subEdgeDistance);
				iter++;
			}
			subAttractors.addAll(subAttr);
			
			if( subAttr.size() > 0){
				EdgeGenerator eg = new EdgeGenerator(subAttr, insetCell);
				subEdgeGenerators.add(eg);
			} else {
				trace("Cell without Attractors! " + i);
			}
		}
		
		//
		trace( "# of sub edge generators: " + subEdgeGenerators.size());
		trace( "# of sub attractors     : " + subAttractors.size());
	}
	
	/**
	 * helper function to calculate the inset cells
	 */
	public void calculateInsetSubCells( ){
		
		subInsetCells = new ArrayList();
		
		for(int i = 0; i < subCells.size();i++){
			ArrayList<Outline> os = subCells.get(i).getRoundedInset(subCellThickness/2, subCellCornerRadius);
			if( os!= null)
				subInsetCells.addAll(os);
		}
	}
}
