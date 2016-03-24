package com.ludens.treetable.threads;

import java.util.ArrayList;

import com.ludens.treetable.EdgeGenerator;

import megamu.mesh.Edge;
import megamu.mesh.Outline;

public class CalcSubOffsetThread extends CalcThread {

	private ArrayList<Edge> edges;
	private double offsetAmount; 
	private double radius;
	
	private EdgeGenerator eg;
	
	public ArrayList<Outline> subOffsetEdges;
	
	public CalcSubOffsetThread(ArrayList<Edge> edges, double offsetAmount, double radius){
		
		
		this.edges = edges;
		this.offsetAmount = offsetAmount;
		this.radius = radius;
		
		
		eg = new EdgeGenerator();
		running = false;
		
		threadName = "SubOffset";
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
			subOffsetEdges = eg.getOffsetEdges( edges, offsetAmount, radius );
			dirty = true;
			trace("Finished!");
		} else {
			trace("Didn't run, edges don't exist!");
		}
		
		
		running = false;
	}
}
