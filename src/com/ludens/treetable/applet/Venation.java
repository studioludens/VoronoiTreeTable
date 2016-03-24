package com.ludens.treetable.applet;
import processing.core.*; 
import controlP5.*;
import processing.pdf.*;


import processing.xml.*; 
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import com.ludens.treetable.AttractorGenerator;
import com.ludens.treetable.EdgeGenerator;
import com.ludens.treetable.OutlineLib;
import com.ludens.treetable.PolygonRenderer;
import com.ludens.treetable.VoroTreeGenerator;

import megamu.mesh.*;


public class Venation extends PApplet {
	
	public double branchScale = .7;
	public double branchInitThickness = 10;
	public double attractorKillDistance = 20;
	public int attractorDistance = 50;
	public double newNodeDistance = 10;
	
	public int subAttractorDistance = 18;
	public double snapDistance = 10;
	public double trimDistance = 6;
	
	public int subSubAttractorDistance = 7;
	
	public boolean showAttractors = true;
	
	public boolean isoLineEnabled = false;
	
	private String state = "";
	

	Outline outline;
	
	// background image
	PImage backgroundImage;
	
	ArrayList<Edge> edges;
	
	
	/**
	 * renderers
	 */
	PolygonRenderer pr;
	
	EdgeGenerator eg;
	
	/** 
	 * start position of the tree
	 */
	Point2D.Double startPosition;
	Point2D.Double startPosition2;
	Point2D.Double startPosition3;
	
	ArrayList<Attractor> attractorList;
	ArrayList<Attractor> subAttractors;
	ArrayList<EdgeGenerator> subEdgeGenerators;
	
	ArrayList<Attractor> subSubAttractors;
	ArrayList<EdgeGenerator> subSubEdgeGenerators;
	
	EdgeGenerator subEdgeGenerator;
	
	// last amount of active attractors
	int __lastActiveAttractors = 0;

	/**
	 * INTERFACE ELEMENTS
	 * 
	 */
	
	ControlP5 controlP5;
	Slider spacingSlider;
	Slider subSpacingSlider;
	Slider scalingSlider;
	Slider sizeSlider;
	Slider snapSlider;
	Slider trimSlider;

	/** ISOLINE stuff
	 * 
	 */
	
	PGraphics isoGraphics;
	
	double isoThreshold = 1;
	

	public void setup(){ 
		size(1350,800); 
		
		// interface elements
		controlP5 = new ControlP5(this);
		spacingSlider = controlP5.addSlider("spacing",3,100);
		spacingSlider.setValue(attractorDistance);
		spacingSlider.setId(1);
		
		spacingSlider = controlP5.addSlider("sub spacing",3,100);
		spacingSlider.setValue(subAttractorDistance);
		spacingSlider.setId(2);
		
		
		scalingSlider = controlP5.addSlider("scaling",0.1f,1.0f);
		scalingSlider.setValue((float)branchScale);
		scalingSlider.setId(3);
		
		scalingSlider = controlP5.addSlider("thickness",1,20);
		scalingSlider.setValue((float)branchInitThickness);
		scalingSlider.setId(4);
		
		snapSlider = controlP5.addSlider("snap",1,20);
		snapSlider.setValue((float)snapDistance);
		snapSlider.setId(5);
		
		snapSlider = controlP5.addSlider("trim",1,20);
		snapSlider.setValue((float)trimDistance);
		snapSlider.setId(6);

		smooth();
		
		// set the start position
		startPosition = new Point2D.Double(width/2, height/2);
		startPosition2 = new Point2D.Double(width/3, height/3);
		startPosition3 = new Point2D.Double(width/2, height/1.7);
		
		//initOneRootNode();
		
		doInit();
		
		this.frameRate(6);
		//this.noLoop();
		
		backgroundImage = loadImage("../data/outline_table_1.png");
		
		
	} 
	
	public void controlEvent( ControlEvent e){
		println( "Control Event: " + e.controller().id());
		
		float value = e.value();
		switch( e.controller().id() ){
		case 1:
			attractorDistance = (int)value;
			break;
		case 2:
			subAttractorDistance = (int)value;
			break;
		case 3:
			branchScale = (double)value;
			break;
		case 4:
			branchInitThickness = (double)value;
			break;
		case 5:
			snapDistance = (double)value;
			break;
		case 6:
			trimDistance = (double)value;
			break;
		
		}
		
		//attractorDistance = (int)e.value();
		//doInit();
	}
	
	
	
	public void doInit() {
		
		/**
		 * initialize the rest
		 */
		
		outline = new Outline();
		pr = new PolygonRenderer();
		//r = new TreeRenderer();
		
		isoGraphics = createGraphics( width, height, P2D );
		
		// setup the outline of the shape
		setupOutline();
		
		// setup the attractors
		setupAttractors();
		
		eg = new EdgeGenerator(attractorList, outline);
		eg.generateAll();
		
		setupSubAttractors();
		
		// setup tree generator
		
		/*
		tg = new TreeGenerator( rootNodes, attractorList, outline);
		tg.branchScale = branchScale;
		tg.attractorKillDistance = attractorKillDistance;
		tg.newNodeDistance = newNodeDistance;
		*/
		
		// do one round of calculation
		calculate();
		
	}

	public void setupOutline() {
		
		outline = OutlineLib.getTable1();
	}
	
	
	
	public void setupAttractors() {
		// generate a lot of attractors
		attractorList = new ArrayList();
		
		/*
		for( int i = 0; i < NUM_ATTRACTORS; i++){
			attractorList.add(Attractor.createIn(outline));
			
		}*/
		attractorList = AttractorGenerator.generate(outline, attractorDistance);
		
		__lastActiveAttractors = attractorList.size()+1;
	}
	
	public void setupSubAttractors(){
		
		
		
		ArrayList<Attractor> allSubAttractors = AttractorGenerator.generate(outline, subAttractorDistance);
		subAttractors = new ArrayList();
		
		println( "Total # of sub attractors:   " + allSubAttractors.size());
		
		// remove all subattractors that are closer to a line than a certain point
		
		
		
		for( int i = 0; i < allSubAttractors.size(); i++){
			Attractor a = allSubAttractors.get(i);
			
			boolean attractorActive = true;
			
			for( int j = 0; j < eg.edges.size(); j++){
				Edge e = eg.edges.get(j);
				//if( e.active ){
					// calculate distance
					Line2D.Double line = e.getLine();
					double distance = line.ptSegDistSq(a.pos);
					if( distance < (trimDistance * trimDistance)){
						attractorActive = false;
					}
				//}
			}
			
			if( attractorActive) subAttractors.add(a);
			
		}
		
		println( "Total # of sub attractors 2: " + subAttractors.size());
		
		/*
		subAttractors.addAll(eg.getAttractorsFromNodes());
		*/
		//subAttractors.addAll(attractorList);
		
		
		subEdgeGenerator = new EdgeGenerator(subAttractors, outline);
		subEdgeGenerator.generateAll();
		
		
		
		
		subSubEdgeGenerators = new ArrayList();
		
		// generate points for each region
		for(int i = 0; i < subEdgeGenerator.regions.size(); i++){
			Outline region = subEdgeGenerator.regions.get(i);
			
			subSubAttractors = AttractorGenerator.generate(region, subSubAttractorDistance);

			EdgeGenerator subEg = new EdgeGenerator(subSubAttractors, region);

			subEg.generateAll();
			subSubEdgeGenerators.add(subEg);
			
		}
		
	}
	 
	public void draw(){ 
		
		background(255);
		image(backgroundImage, 0, 0);
		
		//tg.generate();
		

		strokeWeight(1);
		
		fill( 255 );
		//pr.render(this.g, outline);
		
		
		// render the tree
		//r.render(this.g, rootNodes);
		
		strokeWeight(1);
		
		noStroke();
		
		if( showAttractors){
			drawAttractors();
			drawAttractorList( subAttractors );
			drawAttractorList( subSubAttractors );
		}
			
		//println("Generated everything");
		
		stroke(0);
		drawSubVoronoi();
		if( !isoLineEnabled ) drawVoronoi();
		
		if( isoLineEnabled ) drawIsoLines();
		
		
	} 
	
	public void calculate(){
		
		eg.generateAll();
		
		calculateTree();
		
		if( isoLineEnabled ) calculateIsoLines();
	}
	
	
	public void calculateTree(){
		// starting nodes
		ArrayList<EdgeNode> startNodes = new ArrayList();
		
		/*
		startNodes.add(eg.getClosestNodeTo(startPosition3));
		startNodes.add(eg.getClosestNodeTo(startPosition));
		startNodes.add(eg.getClosestNodeTo(startPosition2));
		*/
		
		VoroTreeGenerator tg = new VoroTreeGenerator(g, branchInitThickness);
		
		tg.edges = eg.edges;
		
		
		
		tg.start(startNodes);
		tg.step();
		tg.step();
		tg.step();
		//tg.calculateEdgeThickness();
				
	}
	
	public void drawAttractorList( ArrayList<Attractor> attractors ){
		
		strokeWeight(1);
		stroke( 0, 255, 0);
		
		
		 for( int i = 0; i < attractors.size(); i++){
			 Attractor a = attractors.get(i);
			 ellipse( (float)a.pos.getX(), (float)a.pos.getY(), 1, 1);
		 }
		
	}
	
	public void drawVoronoi(){
		
		
		stroke(0);
		eg.drawEdges(g);
		
	}
	
	public void drawSubVoronoi(){
		//stroke(100, 30, 255);
		stroke( 100 );
		strokeWeight(1f);
		
		
		/*
		for( int i = 0; i < subEdgeGenerators.size(); i++){
			EdgeGenerator eg = subEdgeGenerators.get(i);
			eg.drawEdges(g, true);
		}
		
		eg.drawEdges(g, true);
		*/
		subEdgeGenerator.snapEdges(eg.edges, snapDistance);
		subEdgeGenerator.drawEdges(g, true);
		
		//stroke( 255, 100, 3);
		stroke( 200 );
		strokeWeight(.4f);
		// draw smallest structure
		for( int i = 0; i < subSubEdgeGenerators.size(); i++){
			EdgeGenerator eg = subSubEdgeGenerators.get(i);
			eg.drawEdges(g, true);
		}
	}
	
	/**
	 * draw the attractors on the screen
	 * 
	 */
	public void drawAttractors(){
		Iterator i = attractorList.iterator();
		
		while( i.hasNext() ){
			
			Attractor a = (Attractor) i.next();
			
			if( a.active) fill(0, 255, 255);
			else			fill( 255, 0,0);
			
			// draw a circle on the place of the attractor
			ellipse( (float) a.pos.getX(), (float) a.pos.getY(), 2, 2);
		}
	}
	
    public void calculateIsoLines(){
		
		// loop through all pixels
		isoGraphics.beginDraw();
		isoGraphics.loadPixels();
		
		ArrayList<Edge> lines = eg.edges;
		
		for (int y = 0; y < isoGraphics.height; y++) {
			for (int x = 0; x < isoGraphics.width; x++) {
				
				double m = 0;
				for (int i = 0; i < lines.size(); i++) {
					
					Edge e = lines.get(i);
					
					if( e.active )
						m += e.thickness / e.getActiveLine().ptSegDistSq(x, y);
				}
				
				// threshold
				if( m > isoThreshold ){
					isoGraphics.pixels[x+y*isoGraphics.width] = color(0);
				} else {
					isoGraphics.pixels[x+y*isoGraphics.width] = color(0, 0);
				}
				
			}
		}
		
		isoGraphics.updatePixels();
		isoGraphics.endDraw();
    }
    
    public void drawIsoLines(){
		image( isoGraphics, 0, 0 );
	}
    
    public void printPDF(){
    	beginRecord(PDF, "item.pdf");
    	
    	// draw edges
    	for( int i = 0; i < eg.edges.size(); i++ ){
			// draw only if it's active
    		Edge e = eg.edges.get(i);
    		Line2D.Double l = e.getLine();
    		line( (float)l.getX1(), (float)l.getY1(), (float)l.getX2(), (float)l.getY2() );
		}
    	
    	//
    		
    	endRecord();
    }
    
    
	
	/** keyboard stuff
	 * 
	 */
	boolean shiftKeyPressed = false;
	boolean altKeyPressed = false;
	
	public void keyPressed(){
		//println("Updating...");
		//draw();
		if( key == 'a'){
			showAttractors = !showAttractors;
		} else if( key == ' '){
			// space pressed, generate new diagram
			doInit();
			draw();
			
		} else if( key == 'i'){
			isoLineEnabled = !isoLineEnabled;
			
		} else if( key == 'p'){
			printPDF();
			
		} 
		
		
		if (key == CODED && keyCode == SHIFT){
			shiftKeyPressed = true;
		} 
		
		if (key == CODED && keyCode == ALT){
			altKeyPressed = true;
		} 
		
	}
	
	public void keyReleased(){
		if (key == CODED && keyCode == SHIFT){
			shiftKeyPressed = false;
		} 
		
		if (key == CODED && keyCode == ALT){
			altKeyPressed = false;
		} 
	}
	
	public void mousePressed(){
		
		// find the closest voronoi node to start the tree
		
		if( shiftKeyPressed ){
			startPosition2 = new Point2D.Double((double)mouseX, (double)mouseY);
		} else if( altKeyPressed ){
			startPosition3 = new Point2D.Double((double)mouseX, (double)mouseY);
		} else {

			startPosition = new Point2D.Double((double)mouseX, (double)mouseY);
		}
		
		// calculate isolines
		
		// calculate 
		calculate();
		
		draw();
	}
	
	
	static public void main(String args[]) {
	        PApplet.main(new String[] { "--bgcolor=#ECE9D8", "VenationTest" });
	}

}
