package com.ludens.treetable.threads;

import java.util.ArrayList;

import com.ludens.treetable.EdgeGenerator;
import com.vividsolutions.jts.geom.Geometry;

import megamu.mesh.Edge;
import megamu.mesh.Outline;

public class CalcOffsetThread extends CalcThread {

	private ArrayList<Edge> edges;
	private ArrayList<Outline> insides;
	private double offsetAmount; 
	private double radius;
	private int edgeShape;
	
	private EdgeGenerator eg;
	private Geometry offsetGeometry;
	//public ArrayList<Outline> offsetEdges;

	
	public CalcOffsetThread(
			ArrayList<Edge> edges, 
			ArrayList<Outline> insides, 
			double offsetAmount, 
			double radius, 
			int edgeShape){
		
		
		this.edges = edges;
		this.offsetAmount = offsetAmount;
		this.radius = radius;
		this.insides = insides;
		this.edgeShape = edgeShape;
		
		
		
		eg = new EdgeGenerator();
		running = false;
		
		threadName = "Offset";
		this.setPriority(MIN_PRIORITY);
		
	}
	public void start(){
		
		trace("Starting calculation of offset edges");
		
		running = true;
		
		super.start();
	}
	
	public void run(){
		
		if( edges != null ){
			// calculate the sub offsets
			offsetGeometry = eg.getOffsetEdgesWithInsides( edges, insides, offsetAmount, radius, edgeShape );
			
			dirty = true;
			trace("Finished!");
		} else {
			trace("Didn't run, edges don't exist!");
		}
		
		
		running = false;
	}
	
	public ArrayList<Outline> getOffsetEdges(){
		return Outline.listFromGeometry(offsetGeometry);
		//eg.getOffsetEdgesWithInsides( edges, insides, offsetAmount, radius )
	}
}
