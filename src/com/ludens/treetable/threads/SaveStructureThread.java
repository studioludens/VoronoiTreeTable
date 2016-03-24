package com.ludens.treetable.threads;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.ludens.treetable.Drawer;
import com.ludens.treetable.applet.VoronoiTreeTable;

import megamu.mesh.Attractor;
import megamu.mesh.Edge;
import megamu.mesh.Outline;
import processing.core.*;

public class SaveStructureThread extends CalcThread {

	private VoronoiTreeTable vtt;
	
	
	
	public SaveStructureThread( VoronoiTreeTable vtt) {
		// TODO Auto-generated constructor stub
		this.vtt = vtt;
		
		threadName = "PDFExport";
	}
	
	public void start(){
		
		trace("Starting export");
		
		running = true;
		super.start();
	}
	
	public void run(){
		trace("Saving....");
		
		String state = "";
		if( vtt.showAttractors ) state += "a";
    	
    	if( vtt.showSubAttractors ) state += "A";
		
		if ( vtt.showEdges ) state += "e";
		
		if ( vtt.showSubEdges ) state += "E";
		
		if( vtt.showCells ) state += "c";
		
		if( vtt.showDuals ) state += "d";
		
		if ( vtt.showSubCells ) state += "C";
		
		if( vtt.showOffsetEdges ) state += "o";
		
		if( vtt.showSubOffsetEdges) state += "O";
		
		if( vtt.showCombinedOffsetEdges) state += "q";
		
		PGraphics pg = vtt.createGraphics(vtt.width, vtt.height, vtt.PDF, "export-" + state + "-" + getDateTime() + ".pdf");
    	pg.beginDraw();
    	
    	
    	
    	if( vtt.showAttractors ) {
    		System.out.println("[PDF] Saving attractors...");
    		Drawer.drawAttractorList( pg, vtt.attractors );
    	}
    	
    	if( vtt.showSubAttractors ){
    		System.out.println("[PDF] Saving sub-attractors...");
    		Drawer.drawAttractorList( pg, vtt.subAttractors );
    	}
		
		if ( vtt.showEdges ){
			System.out.println("[PDF] Saving edges...");
			Drawer.drawEdgeList( pg, vtt.edges );
		}
		
		if ( vtt.showSubEdges ){
			System.out.println("[PDF] Saving sub-edges...");
			Drawer.drawEdgeList( pg, vtt.subEdges );
		}
		
		if( vtt.showCells ){
			System.out.println("[PDF] Saving cells...");
			Drawer.drawOutlineList( pg, vtt.insetCells, false );
		}
		
		if( vtt.showDuals ){
			System.out.println("[PDF] Saving dual points...");
			Drawer.drawPoints( pg, vtt.eg.v.dualPoints );
		}
		
		if ( vtt.showSubCells ){
			System.out.println("[PDF] Saving sub cells...");
			Drawer.drawOutlineList( pg, vtt.subInsetCells, false );
		}
		
		if( vtt.showOffsetEdges ){
			System.out.println("[PDF] Saving offset edges...");
			Drawer.drawOutlineList( pg, vtt.offsetEdges, false);
		}
		
		if( vtt.showSubOffsetEdges){
			System.out.println("[PDF] Saving sub offset edges...");
			Drawer.drawOutlineList( pg, vtt.subOffsetEdges, false);
		}
		
		if( vtt.showCombinedOffsetEdges){
			System.out.println("[PDF] Saving combined offset edges...");
			Drawer.drawOutlineList( pg, vtt.combinedOffsetEdges, false);
		}
		
		System.out.println("[PDF] Disposing...");
		
    	pg.dispose();
    	
    	System.out.println("[PDF] Ending draw...");
    	pg.endDraw();
    	
    	System.out.println("[PDF] Finished!");
    	System.out.println("[THREAD:PDFExport] Finished!");
    	running = false;
    	
	}
	
	private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HHmmss");
        Date date = new Date();
        return dateFormat.format(date);
    }

}
