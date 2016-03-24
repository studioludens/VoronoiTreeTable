package com.ludens.treetable.applet;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import megamu.mesh.Outline;

import com.ludens.treetable.Drawer;
import com.vividsolutions.jts.geom.*;

import controlP5.ControlP5;
import controlP5.Slider;
import processing.core.PApplet;

public class EdgeTest extends PApplet {

	Point2D firstPoint = new Point2D.Double(20, 20);
	Point2D lastPoint = new Point2D.Double(300, 300);
	
	double startThickness = 100;
	double endThickness = 10;
	int steps = 100;
	
	boolean shiftPressed = false;
	
	double lineLength = 40;
	
	/**
	 * interface elements
	 */
	ControlP5 controlP5;
	Slider thicknessSlider;
	Slider lengthSlider;
	
	public void setup(){
		size(1200,800);
		smooth();
		
		controlP5 = new ControlP5(this);
		//controlP5.addSlider("length", 10, 400);
		controlP5.addSlider("thickness", 10, 100);
		controlP5.addSlider("steps", 4, 100);
	}
	
	public void length( float value ){
		lineLength = value;
	}
	
	public void thickness( float value ){
		startThickness = value;
	}
	
	public void steps( float value ){
		steps = (int)value;
	}
	
	
	public void draw(){
		background(50);
		
		stroke(255);
		fill(255,100);
		
		Line2D l = new Line2D.Double(firstPoint, lastPoint);

		
		Outline o = getParabola(l, startThickness, steps );
		o.draw(g);
		
		
	}
	
	public Outline getParabola( Line2D line, double thickness, double steps ){
		
		Outline o = new Outline();
		
		double lineLengthX = (line.getX2() - line.getX1())*(line.getX2() - line.getX1());
		double lineLengthY = (line.getY2() - line.getY1())*(line.getY2() - line.getY1());
		
		double lineLength = Math.sqrt(lineLengthX + lineLengthY);
		
		double angle = Math.atan2(line.getY2()-line.getY1(), line.getX2()-line.getX1())-Math.PI/2;
		
		//double endY = startY + lineLength;
		double stepSize = thickness / steps;
		
		double thSq = .25 * ( thickness * thickness );
		
		double a = lineLength / thSq;
		double pStartX = ( thickness * .5 );

		
		for( int i = 0; i <= steps; i++){
			// calculate the point
			double x = (-thickness*.5)+(i*stepSize);
			double y = a * x * x;
			
			double cX = (x + pStartX + thickness*.5);
			
			double pX = line.getX1() + x * Math.cos(angle) - y * Math.sin(angle);
			double pY = line.getY1() + x * Math.sin(angle) + y * Math.cos(angle);
			
			o.addPoint(pX, pY);
			
		}
		
		
		//line( (float)line.getX1(), (float)line.getY1(), (float)line.getX2(), (float)line.getY2());
		
		return o;
	}
	
	public void drawLine(){
		
		// get angle
		double angle = Math.atan2(lastPoint.getY()-firstPoint.getY(), lastPoint.getX()-firstPoint.getX());
		
		// get two points close to the start
		
		line( (float)firstPoint.getX(), (float)firstPoint.getY(), (float)lastPoint.getX(), (float)lastPoint.getY());
	}
	
	
	public void mousePressed(){
		if( mouseButton == LEFT ){
			firstPoint = new Point2D.Double((double)mouseX, (double)mouseY);
		} else {
			lastPoint = new Point2D.Double((double)mouseX, (double)mouseY);
		}
	}
	
	public void mouseMoved(){
		
	}
	
	public void keyPressed(){
		
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
