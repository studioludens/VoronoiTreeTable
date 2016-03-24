package com.ludens.treetable.applet;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;

import javax.swing.*;

import org.gicentre.utils.move.ZoomPan;

import com.ludens.treetable.*;
import com.ludens.treetable.threads.*;
import com.seisw.util.geom.Poly;
import com.vividsolutions.jts.geom.*;

import megamu.mesh.*;
import processing.core.PApplet;
import processing.core.PFont;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;
import controlP5.*;

public class VoronoiTreeTableSub extends PApplet {

	//String defaultFileName = "test1.svg";
	
	String defaultFileName = "110826_FSC_testC2.svg";
	
	int edgeShape = 4;
	
	//Voronoi v;
	public EdgeGenerator eg;
	ArrayList<EdgeGenerator> subEdgeGenerators;
	
	public ArrayList<Attractor> attractors;
	public ArrayList<Attractor> subAttractors;
	
	public ArrayList<Edge> edges;
	public ArrayList<Edge> subEdges;
	public ArrayList<Outline> cells;
	public ArrayList<Outline> insetCells;
	public ArrayList<Outline> subCells;
	public ArrayList<Outline> subInsetCells;
	
	public ArrayList<Outline> offsetEdges;
	public ArrayList<Outline> subOffsetEdges;
	public ArrayList<Outline> combinedOffsetEdges;
	
	public double[][] dualPoints;
	
	/**
	 * HELP TEXT
	 */
	PFont helpFont;
	public String HELP_TEXT = "A - show large attractors\na - show small attractors\nC - show large cells\nc - show small cells\nE - show large edges\ne - show small edges\ni - show insides\nO - show large offset edges\no - show small offset edges\nQ - calculate final outline\nq - show final outline\nr - reset\np - print\nh - show help";
	
	/**
	 * dirty variables
	 * - to indicate when something has changed and needs to be re-calculated
	 */
	
	public boolean _edgesDirty = false;
	public boolean _subEdgesDirty = false;
	public boolean _cellsDirty = false;
	public boolean _subCellsDirty = false;
	public boolean _offsetEdgesDirty = false;
	
	/**
	 * global variables
	 */
	public int attractorDistance = 120;
	public int subAttractorDistance = 50;
	public double edgeDistance = 30;
	public double snapDistance = 50;
	
	public double cellCornerRadius = 12;
	public double cellThickness = 12;
	
	public double subEdgeDistance = 10;
	public double subCellCornerRadius = 3;
	public double subCellThickness = 2;
	
	public boolean showAttractors = true;
	public boolean showSubAttractors = false;
	public boolean showDuals = false;
	public boolean showEdges = true;
	public boolean showCells = true;
	public boolean showSubCells = false;
	public boolean showSubEdges = false;
	
	public boolean showOffsetEdges = false;
	public boolean showSubOffsetEdges = false;
	public boolean showCombinedOffsetEdges = false;
	
	public boolean showHelp = true;
	public boolean showInsides = true;
	
	public int roundedCornerSegments = 8;
	public int subRoundedCornerSegments = 8;
	
	/** 
	 * mouse navigation
	 */
	public float m_scaling = 1;
	public float m_translateX = 0;
	public float m_translateY = 0;
	public float m_width = 0;
	public float m_height = 0;
	public float m_trScX = 0; // translate after scale
	public float m_tr_ScY = 0;
	
	
	Outline outside;
	//Outline inside;
	
	ArrayList<Outline> insides;
	ArrayList<Outline> simpleInsides;
	
	PShape tableShape;
	
	/**
	 * Threading - for a more responsive UI
	 */
	
	CalcVoronoiThread threadVo;
	CalcSubVoronoiThread threadSubVo;
	CalcCombinedOffsetThread threadCoO;
	CalcSubOffsetThread threadSubO;
	CalcOffsetThread threadO;
	SavePDFThread threadPDF;
	
	
	/** interface elements
	 * 
	 */
	ControlP5 controlP5;
	Slider spacingSlider;
	Slider subSpacingSlider;
	Slider cellThicknessSlider;
	Slider subCellThicknessSlider;
	Slider cellCornerRadiusSlider;
	Slider subCellCornerRadiusSlider;
	
	Slider sizeSlider;
	Slider snapSlider;
	Slider edgeSlider;
	
	Button resetButton;
	Button saveButton;
	Button loadButton;
	
	ZoomPan zoomer;
	
	public void setup(){
		
		size( 1400, 800 );
		smooth();
		
		// initialize the default shape
		initShapes(defaultFileName);
		
		// initialize the interface
		initInterface();
		
		// reset the structure and calculate everything
		reset();
		initCombinedOffsetEdges();
		
		// mouse wheel listener
		addMouseWheelListener(new java.awt.event.MouseWheelListener() { 
		    public void mouseWheelMoved(java.awt.event.MouseWheelEvent evt) { 
		      mouseWheel(evt.getWheelRotation());
		  }}); 
		
		zoomer = new ZoomPan(this);
	}
	
	/*
	 * load svg data
	 */
	public void loadExternalFile(){
		println("Loading external SVG...");
		
		SwingUtilities.invokeLater(new Runnable() {
			   public void run() {
			     try {
			       JFileChooser fc = new JFileChooser();
			        int returnVal = fc.showOpenDialog(null);
			        if (returnVal == JFileChooser.APPROVE_OPTION) {
			          File file = fc.getSelectedFile();
			          String name = file.getName().toLowerCase();
			          if (name.endsWith(".svg")) {
			            initShapes(file.getAbsolutePath());
			            reset();
			            
			          } else{
			        	  System.out.println("Not a valid SVG file!");
			          }
			        }
			     } catch (Exception e) {
			       e.printStackTrace();
			     }
			   }
			 });
	}
	
	public void initShapes(String fileName){
		
		println( "Initializing shapes...");
		
		insides = new ArrayList();
		simpleInsides = new ArrayList();
		
		PShape file = loadShape(fileName);
		outside = Outline.fromShape(file.findChild("outside"));
		
		
		boolean hasElement = true;
		int i = 1;
		// get all inside elements
		while( hasElement == true){
			PShape s = file.findChild("inside" + i);
			PShape sLines = file.findChild("simpleinside" + i);
			//Geometry g = getLineGeometry( sLines );
			if( s != null && sLines != null ){
				insides.add(Outline.fromShape(s));
				simpleInsides.add(Outline.fromShape(sLines));
				// add it to the list of inside shapes
			} else {
				hasElement = false;
			}
			i++;
		}
		
		println("Found " + i + " inside elements");
		
		
		//inside = insides.get(0);
		tableShape = file.findChild("table");
	}
	
	
	
	public void initInterface(){
		
		float controlWidth = 100;
		float controlHeight = 20;
		
		controlP5 = new ControlP5(this);
		// interface elements
		// interface elements
		
		edgeSlider = controlP5.addSlider("edge distance",1,100);
		edgeSlider.setPosition(10, 10);
		edgeSlider.setValue((float)edgeDistance);
		edgeSlider.setId(7);
		
		snapSlider = controlP5.addSlider("snap distance",1,100);
		snapSlider.setPosition(10, 25);
		snapSlider.setValue((float)snapDistance);
		snapSlider.setId(11);
		
		
		spacingSlider = controlP5.addSlider("spacing",3,300);
		spacingSlider.setPosition(10, 60);
		spacingSlider.setValue(attractorDistance);
		spacingSlider.setId(1);
		
		cellThicknessSlider = controlP5.addSlider("cell thickness",0.1f,40);
		cellThicknessSlider.setPosition(10, 75);
		cellThicknessSlider.setValue((float)cellThickness);
		cellThicknessSlider.setId(3);
		
		cellCornerRadiusSlider = controlP5.addSlider("cell radius",0.1f,90);
		cellCornerRadiusSlider.setValue((float)cellCornerRadius);
		cellCornerRadiusSlider.setPosition(10, 90);
		cellCornerRadiusSlider.setId(5);
		
		
		subSpacingSlider = controlP5.addSlider("sub spacing",3,200);
		subSpacingSlider.setValue(subAttractorDistance);
		subSpacingSlider.setPosition(10, 110);
		subSpacingSlider.setId(2);
		

		subCellThicknessSlider = controlP5.addSlider("sub cell thickness",0.1f,13);
		subCellThicknessSlider.setValue((float)subCellThickness);
		subCellThicknessSlider.setPosition(10, 125);
		subCellThicknessSlider.setId(4);
		

		subCellCornerRadiusSlider = controlP5.addSlider("sub cell radius",0.1f,40);
		subCellCornerRadiusSlider.setValue((float)subCellCornerRadius);
		subCellCornerRadiusSlider.setPosition(10, 140);
		subCellCornerRadiusSlider.setId(6);
		
		resetButton = controlP5.addButton("reset");
		resetButton.setPosition(10, 160);
		resetButton.setSize(50, 20);
		resetButton.setId(8);
		
		loadButton = controlP5.addButton("load");
		loadButton.setPosition(65, 160);
		loadButton.setSize(50, 20);
		loadButton.setId(9);
		
		saveButton = controlP5.addButton("save");
		saveButton.setPosition(120, 160);
		saveButton.setSize(50, 20);
		saveButton.setId(10);
		
		// help font
		helpFont = loadFont("Monospaced-12.vlw");
		textFont(helpFont);
		
	}
	
	public void reset(){
		
		attractors = null;
		edges = null;
		subEdges = null;
		cells = null;
		subCells = null;
		subInsetCells = null;
		offsetEdges = null;
		subOffsetEdges = null;
		combinedOffsetEdges = null;
		
		initAttractors();
		initEdgeGenerator();
		initVoronoi();
		
		
		//initSubAttractors();
		//initSubVoronoi();
		//initOffsetEdges();
		//initSubOffsetEdges();
	}
	
	public void stopAllThreads(){
		
		if( threadVo != null){
			threadVo.quit();
			threadVo = null;
		}
			
		
		if( threadSubVo != null){
			threadSubVo.quit();
			threadSubVo = null;
		}
		
		if( threadSubO != null){
			threadSubO.quit();
			threadSubO = null;
		}
		
		if( threadCoO != null ){
			threadCoO.quit();
			threadCoO = null;
		}
		
	}
	
	public void initEdgeGenerator(){
		eg = new EdgeGenerator();
	}
	
	public void initAttractors(){
		
		attractors = AttractorGenerator.generateInOut(outside, insides, attractorDistance, edgeDistance);
		
	}
	
	public void initVoronoi(){
		
		if( threadVo != null)
			threadVo.quit();
		
		if( threadSubVo != null && threadSubVo.running) threadSubVo.quit();
		if( threadCoO != null && threadCoO.running) threadCoO.quit();
		if( threadO != null && threadO.running) threadO.quit();
		
		threadVo = new CalcVoronoiThread( attractors, insides, simpleInsides, outside, cellThickness, cellCornerRadius, snapDistance);
		threadVo.start();
		
	}
	
	
	
	public void initSubVoronoi( ){
		if( threadSubVo != null)
			threadSubVo.quit();
		
		threadSubVo = new CalcSubVoronoiThread( cells, subAttractorDistance, subCellThickness, subCellCornerRadius, subEdgeDistance );
		threadSubVo.start();
	}
	
	public void initOffsetEdges(){
		if( threadO != null)
			threadO.quit();
		
		threadO = new CalcOffsetThread( edges, insides, cellThickness/2, cellCornerRadius, edgeShape );
		threadO.start();
		
		//offsetEdges = eg.getOffsetEdges( edges, cellThickness/2, cellCornerRadius );
	}
	
	public void initSubOffsetEdges(){
		
		if( threadSubO != null)
			threadSubO.quit();
		
		threadSubO = new CalcSubOffsetThread( subEdges, subCellThickness/2, subCellCornerRadius );
		threadSubO.start();
	}
	
	public void initCombinedOffsetEdges(){
		
		if( threadCoO != null ){
			threadCoO.quit();
		}
		
		threadCoO = new CalcCombinedOffsetThread(insides, edges, cellThickness, cellCornerRadius, subEdges, subCellThickness, subCellCornerRadius, edgeShape);
		threadCoO.start();
	}
	
	
	public void draw(){
		
		background( 0 );
		
		// draw the outline shape
		
		
		PolygonRenderer pr = new PolygonRenderer();
		
		//pr.render(g, outside);
		//pr.render(g, inside);
		updateThreadData();
		drawThreadStates();
		
		
		pushMatrix();
		//zoomer.transform();
		//zoomer.setMouseMask(SHIFT);
		
		
		//translate( m_translateX, m_translateY);
		scale( m_scaling );
		
		
		
		shape(tableShape);
		
		// compensate for scaling
		Drawer.scaling = 1/m_scaling;
	
		if( showAttractors 	) Drawer.drawAttractorList( g, attractors );
		
		if( showSubAttractors ) Drawer.drawAttractorList( g, subAttractors );
		
		if( showEdges ) Drawer.drawEdgeList( g, edges );
		
		if( showSubEdges ) Drawer.drawEdgeList( g, subEdges );
		
		if( showCells ) Drawer.drawOutlineList( g, insetCells, true );
		
		if( showDuals ) Drawer.drawPoints( g, dualPoints );
		
		if( showSubCells ) Drawer.drawOutlineList( g, subInsetCells, true );
		
		if( showOffsetEdges ) Drawer.drawOutlineList( g, offsetEdges, false);
		
		if( showSubOffsetEdges) Drawer.drawOutlineList( g, subOffsetEdges, false);
		
		if( showCombinedOffsetEdges ) Drawer.drawOutlineList( g, combinedOffsetEdges, false);
		
		if( showInsides )	Drawer.drawOutlineList( g, insides, true );
		
		// closest attractor to mouse
		noFill();
		stroke( 0, 255, 0);
		strokeWeight( 3/m_scaling );
		
		if( closestAttractor != null)
			g.ellipse( (float)closestAttractor.pos.getX(), (float)closestAttractor.pos.getY(), 4/m_scaling, 4/m_scaling);
		
		
		popMatrix();
		
		
		// help text
		if( showHelp ){
			fill(150);
			text(HELP_TEXT, 10, height-200);
		}
		
	}
	
	/** draw an indicator for a specific boolean
	 *
	 * @param x
	 * @param y
	 * @param active
	 */
	public void drawIndicator( float x, float y, boolean active){
		
		noStroke();
		if( active ){
			fill( 255, 0, 0);
		} else {
			fill( 0, 255, 0);
		}
		
		ellipse( x, y, 10, 10);
	}
	
	public void drawThreadStates(){
		
		// Voronoi thread
		
		if( threadVo != null){
			drawIndicator( 18, 195, threadVo.running);
			text("Voronoi", 30, 197);
		}

		// sub-voronoi thread
		if( threadSubVo != null){
			drawIndicator( 18, 217, threadSubVo.running);
			text("Sub Voronoi", 30, 220);
		}
		
		
		// offset edges
		if( threadO != null){
			drawIndicator( 18, 239, threadO.running);
			text("Offset edges", 30, 242);
		}
		
		/*
		// sub offset edges
		if( threadSubO != null){
			drawIndicator( 18, 280, threadSubO.running);
			text("Sub offset edges", 30, 280);
		}
		*/
			
		// combined offset edges
		if( threadCoO != null){
			drawIndicator( 18, 261, threadCoO.running);
			text("Final", 30, 264);
		}
		
		// PDF export thread
		if( threadPDF != null){
			drawIndicator( 18, 283, threadPDF.running);
			text("Export", 30, 286);
		}
	}
	
	public void updateThreadData(){
		// check the offset thread
		if( threadCoO != null && !threadCoO.running && threadCoO.dirty ){
			combinedOffsetEdges = threadCoO.combinedOffsetEdges;
			threadCoO.dirty = false;
		}
			
		
		if( threadSubO != null && !threadSubO.running && threadSubO.dirty ){
			subOffsetEdges = threadSubO.subOffsetEdges;
			threadSubO.dirty = false;
		}
		
		if( threadO != null && !threadO.running && threadO.dirty ){
			offsetEdges = threadO.getOffsetEdges();
			threadO.dirty = false;
		}
			
		// intermediate step to show edges earlier
		if( threadVo != null && threadVo.edgesFinished){
			edges = threadVo.edges;
			cells = threadVo.cells;
			
		}
		
		// voronoi
		if( threadVo != null && !threadVo.running && threadVo.dirty){
			insetCells = threadVo.insetCells;
			dualPoints = threadVo.dualPoints;
			threadVo.dirty = false;
			initSubVoronoi();
			initOffsetEdges();
			if( threadSubO != null) threadSubO.quit();
			if( threadCoO != null) threadCoO.quit();
		}

		// sub voronoi
		if( threadSubVo != null && !threadSubVo.running && threadSubVo.dirty ){
			subEdges = threadSubVo.subEdges;
			subCells = threadSubVo.subCells;
			subInsetCells = threadSubVo.subInsetCells;
			threadSubVo.dirty = false;
			//initSubOffsetEdges();
			//initCombinedOffsetEdges();
		}
		
		
			
	}

	
	/**
	 * EVENT HANDLERS
	 */
	public void controlEvent( ControlEvent e){
		//println( "Control Event: " + e.controller().id());
		
		float value = e.value();
		switch( e.controller().id() ){
		case 1:
			attractorDistance = (int)value;
			reset();
			break;
		case 2:
			subAttractorDistance = (int)value;
			initSubVoronoi();
			break;
		case 3:
			cellThickness = (double)value;
			initVoronoi();
			//initOffsetEdges();
			
			break;
		case 4:
			subCellThickness = (double)value;
			//calculateInsetSubCells();
			initSubVoronoi();
			
			//initSubOffsetEdges();
			break;
		case 5:
			cellCornerRadius = (double)value;
			initVoronoi();
			//initOffsetEdges();
			break;
		case 6:
			subCellCornerRadius = (double)value;
			//calculateInsetSubCells();
			initSubVoronoi();
			//initSubOffsetEdges();
			break;
		case 7:
			edgeDistance = (double)value;
			reset();
			break;
		case 8:
			// reset button
			reset();
			break;
		case 9:
			// load button
			loadExternalFile();
			break;
		case 10:
			printPDF();
			break;
			
		case 11:
			// snap distance
			snapDistance = (double)value;
			initVoronoi();
			break;
			
		
		}
		//initVoronoi();
		//attractorDistance = (int)e.value();
		//doInit();
	}
	
	
			
	public void keyPressed(){
		
		
		switch( key ){
		case 'a':
			// SHOW MAIN ATTRACTORS
			showAttractors = !showAttractors;
			break;
		case 'A':
			// SHOW SUB ATTRACTORS
			showSubAttractors = !showSubAttractors;
			break;
		case 'r':
			// RESET
			reset();
			
			draw();
			break;
		case 'c':
			showCells = !showCells;
			break;
		case 'C':
			showSubCells = !showSubCells;
			break;
		case 'e':
			showEdges = !showEdges;
			break;
		case 'E':
			showSubEdges = !showSubEdges;
			break;
		case 'o':
			showOffsetEdges = !showOffsetEdges;
			break;
		case 'O':
			showSubOffsetEdges = !showSubOffsetEdges;
			break;
		case 'q':
			showCombinedOffsetEdges = !showCombinedOffsetEdges;
			break;
		case 'Q':
			initCombinedOffsetEdges();
			break;
		case 'd':
			showDuals = !showDuals;
			break;
		case 'h':
			showHelp = !showHelp;
			break;
		case 'i':
			showInsides = !showInsides;
			break;
		case 'p':
			printPDF();
			break;
		case 's':
			stopAllThreads();
			break;
		}
		
		if( keyCode == 8 ) {
			// Backspace key, remove closest attractor
			attractors.remove( closestAttractor );
			initVoronoi();
		}
		
	}
	
	/**
	 * MOUSE EDITING
	 */
	
	private Attractor closestAttractor;
	
	public void mouseMoved(){
		AttractorList al = new AttractorList(attractors);
		
		PVector mousePosition = zoomer.getMouseCoord();
		double mx =(double)(mousePosition.x);    // Equivalent to mouseX
		double my =(double)(mousePosition.y);    // Equivalent to mouseY
		  
		closestAttractor = al.closestAttractorTo(new Point2D.Double(scaledMouseX(), scaledMouseY()));
		
		
	}
	
	public double scaledMouseX(){
		return mouseX / m_scaling;
	}
	
	public double scaledMouseY(){
		return mouseY / m_scaling;
	}
	
	public void mouseDragged(){
		
		
		
		if( mouseButton == LEFT && outside.contains(scaledMouseX(), scaledMouseY())){
			closestAttractor.pos.setLocation(scaledMouseX(), scaledMouseY());
			initVoronoi();
		}
		
		
		
	}
	
	public void mouseReleased(){
		if( mouseButton == RIGHT){
			// add attractor
			attractors.add(new Attractor( new Point2D.Double( scaledMouseX(), scaledMouseY()),1));
			initVoronoi();
		}
	}
	
	void mouseWheel(int delta) {
		float scalingAmount = .1f;
		
		
		float scalingDelta = 1;
		
		if( delta < 0){ 	// down
			scalingDelta = (1 - (scalingAmount * (float)abs(delta)));
		} else { 			// up
			scalingDelta = (1 + (scalingAmount * (float)abs(delta)));
		}
		
		m_scaling *= scalingDelta;
		
		
		
		//mouseTranslateX 
		//  println(delta); 
	}
	
	
	/**
	 * PDF EXPORT FUNCTIONALITY
	 * 
	 */
	public void printPDF(){
		
		if( threadPDF != null ){
			threadPDF.quit();
		}
		
		
		//threadPDF = new SavePDFThread(this);
		//threadPDF.start();
		
    	
    }
	
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main(new String[] { "--bgcolor=#000000", "com.ludens.treetable.applet.VoronoiTreeTable" });
	}

}
