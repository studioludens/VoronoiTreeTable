package com.ludens.treetable.applet;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.*;

//import org.gicentre.utils.move.ZoomPan;

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

public class VoronoiTreeTable extends PApplet {

	//String defaultFileName = "test1.svg";
	
	String defaultFileName = "110826_FSC_testC2.svg";
	
	
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
	
	public int edgeShape = 4;
	
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
	Slider subEdgeSlider;
	
	Button resetButton;
	Button saveButton;
	Button loadButton;
	
	Bang offsetBang;
	Bang combinedBang;
	Bang loadBang;
	
	// view check boxes
	CheckBox viewCheck;
	
	//ZoomPan zoomer;
	
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
		
		//zoomer = new ZoomPan(this);
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
		
		subEdgeSlider = controlP5.addSlider("sub edge distance",0,50);
		subEdgeSlider.setPosition(10, 155);
		subEdgeSlider.setValue((float)subEdgeDistance);
		subEdgeSlider.setId(12);
		
		resetButton = controlP5.addButton("reset");
		resetButton.setPosition(10, 180);
		resetButton.setSize(50, 20);
		resetButton.setId(8);
		
		loadButton = controlP5.addButton("load");
		loadButton.setPosition(65, 180);
		loadButton.setSize(50, 20);
		loadButton.setId(9);
		
		saveButton = controlP5.addButton("save");
		saveButton.setPosition(120, 180);
		saveButton.setSize(50, 20);
		saveButton.setId(10);
	
		
		offsetBang = controlP5.addBang("laser file", 10, 360, 30, 10);
		offsetBang.setId(100);
	
		
		combinedBang = controlP5.addBang("ets file", 10, 390, 30, 10);
		combinedBang.setId(101);
		
		loadBang = controlP5.addBang("load settings", 10, 420, 30, 10);
		loadBang.setId(102);
		
		viewCheck = controlP5.addCheckBox("view", 10, height-200);
		viewCheck.setItemsPerRow(1);
		viewCheck.setSpacingRow(10);
		viewCheck.addItem("insides", 1);
		viewCheck.addItem("points", 2);
		viewCheck.addItem("cells", 3);
		viewCheck.addItem("edges", 4);
		viewCheck.addItem("sub edges", 5);
		viewCheck.addItem("laser file", 6);
		viewCheck.addItem("ets file", 7);
		
		initViewCheck = true;
		
		if( showInsides ) viewCheck.activate("insides");
		if( showAttractors ) viewCheck.activate("points");
		if( showCells ) viewCheck.activate("cells");
		if( showEdges ) viewCheck.activate("edges");
		if( showSubEdges ) viewCheck.activate("sub edges");
		if( showOffsetEdges ) viewCheck.activate("laser file");
		if( showCombinedOffsetEdges ) viewCheck.activate("ets file");
		
		initViewCheck = false;
		//viewCheck.setUpdate(true);
		//viewCheck.update();
		
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
		
		/*
		if( showHelp ){
			fill(150);
			text(HELP_TEXT, 10, height-200);
		}*/
		
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
			text("Laser file", 30, 242);
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
			text("Ets file", 30, 264);
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
			//threadO = null;
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
			//initOffsetEdges();
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
	
	
	// indicates if the view check boxes are being initialized (used to prevent updating)
	private boolean initViewCheck = false;
	
	public void controlEvent( ControlEvent e){
		if( e.isController() ) println( "Control Event: " + e.controller().id());
		
		if( e.isGroup() && e.name() == "view" && !initViewCheck ){
			// we have the view checkbox group
			println("Changing view...");
			
			float[] values = e.group().arrayValue();
			
			showInsides 			= ( values[0] == 1 );
			showAttractors 			= ( values[1] == 1 );
			showCells 				= ( values[2] == 1 );
			showEdges	 			= ( values[3] == 1 );
			showSubEdges 			= ( values[4] == 1 );
			showOffsetEdges 		= ( values[5] == 1 );
			showCombinedOffsetEdges = ( values[6] == 1 );
			
			//println(viewCheck.getItem(1));
			println("Values: " + e.group().arrayValue());
			return;
		} else if( e.isGroup() ){
			return;
		}
		
		float value = e.value();
		switch( e.controller().id() ){
		case 1:
			attractorDistance = (int)value;
			if(!isLoading) reset();
			break;
		case 2:
			subAttractorDistance = (int)value;
			if(!isLoading) initSubVoronoi();
			break;
		case 3:
			cellThickness = (double)value;
			if(!isLoading) initVoronoi();
			//initOffsetEdges();
			
			break;
		case 4:
			subCellThickness = (double)value;
			//calculateInsetSubCells();
			if(!isLoading) initSubVoronoi();
			
			//initSubOffsetEdges();
			break;
		case 5:
			cellCornerRadius = (double)value;
			if(!isLoading) initVoronoi();
			//initOffsetEdges();
			break;
		case 6:
			subCellCornerRadius = (double)value;
			//calculateInsetSubCells();
			if(!isLoading) initSubVoronoi();
			//initSubOffsetEdges();
			break;
		case 7:
			edgeDistance = (double)value;
			if(!isLoading) reset();
			break;
		case 8:
			// reset button
			if(!isLoading) reset();
			break;
		case 9:
			// load button
			loadExternalFile();
			break;
		case 10:
			printPDF( false );
			break;
			
		case 11:
			// snap distance
			snapDistance = (double)value;
			if(!isLoading) initVoronoi();
			break;
		case 12:
			// sub edge distance
			subEdgeDistance = (double)value;
			if(!isLoading) initSubVoronoi();
			break;
		case 100:
			// calcualte offset
			initOffsetEdges();
			break;
		case 101:
			initCombinedOffsetEdges();
			break;
		case 102:
			loadStructureFile();
			break;
		
		}
		//initVoronoi();
		//attractorDistance = (int)e.value();
		//doInit();
		if(!isLoading) saveStructureFile();
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
			printPDF( false );
			break;
		case 's':
			//stopAllThreads();
			saveStructureFile();
			break;
		case 'l':
			loadStructureFile();
			break;
		case 'x':
			printPDF( true );
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
		/*
		PVector mousePosition = zoomer.getMouseCoord();
		double mx =(double)(mousePosition.x);    // Equivalent to mouseX
		double my =(double)(mousePosition.y);    // Equivalent to mouseY
		  */
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
			saveStructureFile();
			initVoronoi();
		}
		
		
		
	}
	
	public void mouseReleased(){
		if( mouseButton == RIGHT){
			// add attractor
			attractors.add(new Attractor( new Point2D.Double( scaledMouseX(), scaledMouseY()),1));
			saveStructureFile();
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
	public void printPDF( boolean saveLargeStructure ){
		
		if( threadPDF != null ){
			threadPDF.quit();
		}
		
		
		threadPDF = new SavePDFThread(this);
		threadPDF.saveLargeStructure = saveLargeStructure;
		threadPDF.start();
		
    	
    }
	
	public void saveStructureFile(){
		// save a file with all the data for the coarse structure
		
		println("[VoroTreeTable] saving file...");
		
		String[] points = new String[attractors.size()];
		
		for( int i = 0; i < attractors.size(); i++){
			Attractor a = attractors.get(i);
			points[i] = a.pos.getX() + "," + a.pos.getY();
		}
		
		//println(points);
		saveStrings("attractors.txt", points);
		// save a copy, just for safety
		saveStrings("backup/attractors-" + getDateTime() + ".txt", points);
		
		// SAVE SETTINGS
		String[] settings = new String[8];
		settings[0] = Double.toString(attractorDistance);
		settings[1] = Double.toString(subAttractorDistance);
		settings[2] = Double.toString(edgeDistance);
		settings[3] = Double.toString(snapDistance);
		
		settings[4] = Double.toString(cellCornerRadius);
		settings[5] = Double.toString(cellThickness);
		settings[6] = Double.toString(subCellCornerRadius);
		settings[7] = Double.toString(subCellThickness);
		
		saveStrings("settings.txt", settings);
		saveStrings("backup/settings-" + getDateTime() + ".txt", settings);
	}
	
	private boolean isLoading = false;
	
	public void loadStructureFile(){
		
		println("[VoroTreeTable] loading file...");
		String[] points = loadStrings("attractors.txt");
		if( points != null ){
		
			attractors = new ArrayList();
			
			for(int i = 0; i < points.length; i++){
				
				String[] positions = points[i].split(",");
				
				Point2D.Double pos = new Point2D.Double( Double.parseDouble(positions[0]), Double.parseDouble(positions[1]));
				
				Attractor a = new Attractor(pos, 10);
				attractors.add(a);
			}
		
			println("Attractors loaded!");
		}
		
		// we have all the attractors now
		
		// LOAD SETTINGS
		String[] settings = loadStrings("settings.txt");
		if( settings != null ){
			isLoading = true;
		
			spacingSlider.setValue(				Float.parseFloat( settings[0] ) );
			subSpacingSlider.setValue(			Float.parseFloat( settings[1] ) );
			edgeSlider.setValue(				Float.parseFloat( settings[2] ) );
			snapSlider.setValue(				Float.parseFloat( settings[3] ) );
			
			cellCornerRadiusSlider.setValue(	Float.parseFloat( settings[4] ) );
			cellThicknessSlider.setValue(		Float.parseFloat( settings[5] ) );
			subCellCornerRadiusSlider.setValue(	Float.parseFloat( settings[6] ) );
			subCellThicknessSlider.setValue(	Float.parseFloat( settings[7] ) );
			
			println("Settings loaded!");
			isLoading = false;
		}
		
		// initialize the structure
		initEdgeGenerator();
		initVoronoi();
	}
	
	private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		PApplet.main(new String[] { "--bgcolor=#000000", "com.ludens.treetable.applet.VoronoiTreeTable" });
	}

}
