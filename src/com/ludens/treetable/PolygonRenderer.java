package com.ludens.treetable;
import java.awt.Polygon;

import megamu.mesh.Polygon2D;

import processing.core.PGraphics;


/**
 * renders a polygon on the screen
 * 
 * @author rulkens
 *
 */
public class PolygonRenderer {

	/** render a polygon on a graphics object
	 * 
	 * @param g the graphics object
	 * @param p the polygon
	 */
	public void render( PGraphics g, Polygon2D p){
		
		g.stroke(0);
		
		g.beginShape();
		
		for( int i = 0; i < p.npoints; i++){
			g.vertex(p.xpoints[i], p.ypoints[i]);
		}
		
		g.endShape(g.CLOSE);
	}
}
