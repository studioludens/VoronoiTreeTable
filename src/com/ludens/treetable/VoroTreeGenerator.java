package com.ludens.treetable;
import java.util.ArrayList;

import processing.core.PGraphics;

import megamu.mesh.*;


public class VoroTreeGenerator {

	
	public PGraphics g;
	public ArrayList<Edge> edges;
	
	public ArrayList<EdgeNode> startNodes;
	
	public double startThickness;
	
	public VoroTreeGenerator( PGraphics g, double startThickness){
		this.g = g;
		this.startThickness = startThickness;
	}
	
	/**
	 * start drawing from an arbitrary number of edge nodes
	 * @param node
	 */
	public void start(ArrayList<EdgeNode> nodes){
		
		startNodes = nodes;
		
		for( int j = 0; j < nodes.size(); j++){
			ArrayList<Edge> edges = nodes.get(j).neighbourEdges;
			nodes.get(j).active = true;
			nodes.get(j).strength = startThickness;
			
			for(int i = 0; i < edges.size(); i++){
				Edge edge = edges.get(i);
				edge.thickness = startThickness;
				
				if( !edge.active){
					edge.active = true;
					edge.startNode.active = true;
					edge.endNode.active = true;
				}
				
			}
		}
		
		
		int activeEdges = edges.size();
		boolean changing = true;
		while(changing){
			int nowActiveEdges = step();
			if( nowActiveEdges != activeEdges){
				activeEdges = nowActiveEdges;
			} else {
				changing = false;
			}
			
			//System.out.println("Number of inactive edges:" + activeEdges);
		}
		
	}
	
	public int step(){
		
		int activeEdges = edges.size();
		
		// loop through all edges
		for(int i = 0; i < edges.size(); i++){
			Edge edge = edges.get(i);
			
			// is it active?
			if( edge.active ) calculateEdge(edge);
			else
				activeEdges--;
			
		}
		
		return activeEdges;
	}
	
	public void calculateEdge(Edge edge){
		
		// get all the neighbours
		
		/*
		if( edge.startNode == null ){
			System.out.println("Start node not found!");
			g.ellipse((float)edge.start.getX(), (float)edge.start.getY(), 10, 10);
			return;
		}
		
		if( edge.endNode == null ){
			System.out.println("End node not found!");
			g.ellipse((float)edge.end.getX(), (float)edge.end.getY(), 10, 10);
			
			return;
		}*/
		
			
		for( int i = 0; i < edge.startNeighbours.size(); i++){
			Edge thisEdge = edge.startNeighbours.get(i);
			
			// there is a small chance we don't draw this line
			if(!thisEdge.active){
				if( ( !thisEdge.endNode.active) 
						|| ( !thisEdge.startNode.active)){
					// if the edge is not active
					thisEdge.thickness = edge.thickness * .9;
					// step this edge
					//step(thisEdge);
					thisEdge.active = true;
					thisEdge.startNode.active = true;
					thisEdge.endNode.active = true;
				}
				
			}
		}
	
	
		for( int i = 0; i < edge.endNeighbours.size(); i++){
			Edge thisEdge = edge.endNeighbours.get(i);
			
			if(!thisEdge.active){
				// if the edge is not active
				if( ( !thisEdge.endNode.active) 
						|| ( !thisEdge.startNode.active)){
					
					// if the edge is not active
					thisEdge.thickness = edge.thickness * .9;
					// step this edge
					//step(thisEdge);
					thisEdge.active = true;
					thisEdge.startNode.active = true;
					thisEdge.endNode.active = true;
				}
				
				
				
			}
		}
		

		// see if they're active
		
		// if not, make them active
		// but only if they have no neighbouring active nodes
		
	}
	
	
	public void calculateEdgeThickness(){
		// start from the start nodes
		for( int j = 0; j < startNodes.size(); j++){
			ArrayList<Edge> edges = startNodes.get(j).neighbourEdges;
			
			for(int i = 0; i < edges.size(); i++){
				Edge edge = edges.get(i);
				edge.calculateThickness();
				
			}
		}
	}
	
	
}
