package com.ludens.treetable.applet;


import java.util.ArrayList;

import megamu.mesh.Outline;
import processing.core.PApplet;

import com.ludens.treetable.OutlineLib;
import com.vividsolutions.jts.geom.*;

import controlP5.ControlEvent;
import controlP5.ControlP5;
import controlP5.Slider;


public class JTSTest extends PApplet {

	Outline o1 = OutlineLib.getOutline1();
	
	double insetDistance = 5;
	double cornerRadius = 5;
	
	ControlP5 controlP5;
	Slider insetSlider;
	Slider radiusSlider;
	
	public void setup(){
		size(1200,800);
		smooth();
		initInterface();
	}
	
	public void initInterface(){
		// interface elements
		// interface elements
		controlP5 = new ControlP5(this);
		insetSlider = controlP5.addSlider("inset",0,300);
		insetSlider.setValue((float)insetDistance);
		insetSlider.setId(1);
		
		radiusSlider = controlP5.addSlider("radius",0.1f,200);
		radiusSlider.setValue((float)cornerRadius);
		radiusSlider.setId(2);
		
		
	}
	
	public void draw(){
		background(50);
		
		stroke(0);
		fill(255,100);
		o1.draw(g);
		
		Geometry gg = new GeometryFactory().createPoint(new Coordinate(width/2,height/2));
		
		Geometry circle = gg.buffer(insetDistance);
		ArrayList<Outline> co = Outline.listFromGeometry(circle);
		
		if( co != null ){
			for(int i = 0; i < co.size(); i++){
				co.get(i).draw(g);
			}
		}
		
		
		/*
		ArrayList<Outline> o2 = o1.getRoundedInset(insetDistance, cornerRadius);
		if( o2 != null ){
			for(int i = 0; i < o2.size(); i++){
				o2.get(i).draw(g);
			}
		}*/
		
		
	}
	
	/**
	 * EVENT HANDLERS
	 */
	public void controlEvent( ControlEvent e){
		//println( "Control Event: " + e.controller().id());
		
		float value = e.value();
		switch( e.controller().id() ){
		case 1:
			insetDistance = (double)value;
			break;
		case 2:
			cornerRadius = (double)value;
			break;
		
		}
		//initVoronoi();
		//attractorDistance = (int)e.value();
		//doInit();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
