package com.ludens.treetable;

import java.awt.geom.Line2D;
import java.util.ArrayList;

import megamu.mesh.Attractor;
import megamu.mesh.Edge;
import megamu.mesh.Outline;
import processing.core.PGraphics;

public class Drawer {
	
	PGraphics pg;
	
	public static float scaling = 1;

	public static void drawAttractorList( PGraphics pg, ArrayList<Attractor> attractors ){
		
		if( attractors == null ) return;
		pg.strokeWeight(scaling);
		pg.stroke( 100, 100, 0);
		
		
		 for( int i = 0; i < attractors.size(); i++){
			 Attractor a = attractors.get(i);
			 pg.ellipse( (float)a.pos.getX(), (float)a.pos.getY(), 1, 1);
		 }
		
	}
	
	public static void drawOutlineList( PGraphics pg, ArrayList<Outline> outlines, boolean doFill){
		
		if( outlines == null ){
			//println("[ERROR] No outlines to draw!");
			return;
		}
		
		
		pg.strokeWeight( 0.4f * scaling );
		pg.stroke(250, 100, 100);
		//pg.stroke(0);
		
		if( doFill) pg.fill(200, 50,50,10);
		else pg.noFill();
		
		for( int i = 0; i < outlines.size(); i++){
			 Outline o = outlines.get(i);
			 o.draw(pg);
			 
		 }
	}
	
	/*
	public void drawOutlineListInset( PGraphics pg, ArrayList<Outline> outlines, double inset, double radius ){
		pg.strokeWeight( 0.4f );
		pg.stroke(200);
		
		//fill(200, 50,50,10);
		for( int i = 0; i < outlines.size(); i++){
			 Outline o = outlines.get(i);
			 //o.draw(g);
			 
			 ArrayList<Outline> insetOutlines = o.getRoundedInset(inset, radius);
			 
			 if( insetOutlines != null){
				 for( int j = 0; j < insetOutlines.size(); j++){
					 Outline insetOutline = insetOutlines.get(i);
					 insetOutline.draw(pg);
				 }
			 }
				 
			 
		 }
	}*/
	
	public static void drawEdgeList( PGraphics pg, ArrayList<Edge> edges ){
		if( edges == null) return;
		
		pg.strokeWeight(.9f * scaling);
		pg.stroke(200);
		
		for( int i = 0; i < edges.size(); i++ ){
			Edge e = edges.get(i);
			Line2D.Double l = e.getLine();
			pg.line( (float)l.getX1(), (float)l.getY1(), (float)l.getX2(), (float)l.getY2() );
			
		}
	}
	  
	public static void drawPoints( PGraphics pg, double[][] points ){
		
		if( points == null ) return;
		
		pg.stroke( 0, 100, 255);
		pg.strokeWeight(.5f * scaling);
		
		for( int i = 0; i < points.length; i++ ){
			pg.ellipse( (float)points[i][0], (float)points[i][1], 2*scaling, 2*scaling);
			
		}
	}
	
	public static void drawInts( PGraphics pg, int[][] points ){
		
		pg.stroke( 255, 0, 255);
		
		for( int i = 0; i < points.length; i++ ){
			pg.ellipse( (float)points[i][0], (float)points[i][1], 2*scaling, 2*scaling);
			
		}
	}
}
