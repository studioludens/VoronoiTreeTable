package com.ludens.treetable;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

import com.seisw.util.geom.Poly;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

import processing.core.PGraphics;

import megamu.mesh.*;


public class EdgeGenerator {

	public ArrayList<Edge> edges;
	public ArrayList<Attractor> attractors;
	public Outline outline;
	
	//public ArrayList<EdgeNode> nodes;
	
	public ArrayList<Outline> regions;
	
	public Voronoi v;
	public double[][] dualPoints;
	
	public double snapDistance;
	
	public EdgeGenerator(){
		
	}
	
	public EdgeGenerator( ArrayList<Attractor> attractors, Outline outline){
		this.attractors = attractors;
		this.outline = outline;
		
	}
	
	public double[][] getPointsFromAttractors( ){
		
		double[][] points = new double[attractors.size()][2];
		
		for( int i = 0; i < attractors.size(); i++){
			Attractor a = attractors.get(i);
			
			points[i][0] = (double)a.pos.getX();
			points[i][1] = (double)a.pos.getY();
			
		}
		
		return points;
	}
	
	public void generateAll(){
		
		
		v = new Voronoi(getPointsFromAttractors());
		v.generateAll();		
		// get all the edges
		//Edge[] edges = v.getEdges();
		v.getClippedEdges(outline);
		//v.getClippedEdgeNodes(outline);
		//v.getClippedRegions(outline);
		//v.linkNodesToEdges();
		
		
		
		edges = v.clippedEdges;
		//nodes = v.clippedNodes;
		regions = v.getClippedRegions(outline);
		dualPoints = v.dualPoints;
		
	}
	
	public void generateAllSnap( ArrayList<Outline> insides, double snapDistance ){
		v = new Voronoi(getPointsFromAttractors());
		v.generateDualPoints();
		
		// snap points to all inside polygons
		for( int i = 0; i < insides.size(); i++){
			v.dualPoints = snapPointsToPolygon(v.dualPoints, insides.get(i), snapDistance);
		}
		
		v.generateEdges();
		v.generateRegions();
		
		v.getClippedEdges(outline);
		//v.getClippedEdgeNodes(outline);
		//v.linkNodesToEdges();
		
		
		
		edges = v.clippedEdges;
		//nodes = v.clippedNodes;
		regions = v.getClippedRegions(outline);
		dualPoints = v.dualPoints;
	}
	
	
	
	public ArrayList<Edge> getEdgesInOut(ArrayList<Outline> insides, Outline outside){
		
		ArrayList<Edge> clippedEdges = getClippedEdges(edges, outside, true);
		
		 
		//clippedEdges = getClippedEdges(clippedEdges, inside, false);
		
		return clippedEdges;
		
	}
	
	public ArrayList<Edge> getClippedEdges( ArrayList<Edge> edges, Outline outline, Boolean inside ){
		
		ArrayList<Edge> clippedEdges = new ArrayList();
		
		//ArrayList<EdgeNode> clippedNodes = getClippedEdgeNodes(outline);
		
		// draw the edges
		for(int i=0; i<edges.size(); i++)
		{
			Edge e = edges.get(i);
			// check if an edge point is outside of the outline
			double startX = e.start.getX();
			double startY = e.start.getY();
			double endX = e.end.getX();
			double endY = e.end.getY();
			
			boolean startInside = outline.contains(e.start);
			boolean endInside = outline.contains(e.end);
			
			if( inside ){
				if( startInside || endInside){
					// if one of either points are inside, calculate possible intersections
					
					Point2D.Double intersection = e.intersectPolygon(outline);
					if( intersection != null){
						// check which part of the edge is inside of the polygon
						
						if( startInside && !endInside ){
							// point 1 is inside
							
							endX = intersection.getX();
							endY = intersection.getY();
							
							// remove end neighbours
							e.endNeighbours = new ArrayList();
							
							e.end.x = endX;
							e.end.y = endY;

						} else if( endInside && !startInside){
							
							startX = intersection.getX();
							startY = intersection.getY();
							
							// remove start neighbours
							e.startNeighbours = new ArrayList();
							
							e.start.x = startX;
							e.start.y = startY;
						}

					}
					// add the edge to the list
					clippedEdges.add( e );
				}
			} else {
				// OUTSIDE
				if( !startInside || !endInside){
					// if one of either points are inside, calculate possible intersections
					
					Point2D.Double intersection = e.intersectPolygon(outline);
					if( intersection != null){
						// check which part of the edge is inside of the polygon
						
						if( startInside && !endInside ){
							// point 1 is inside
							
							startX = intersection.getX();
							startY = intersection.getY();
							
							// remove start neighbours
							e.startNeighbours = new ArrayList();
							
							e.start.x = startX;
							e.start.y = startY;

						} else if( endInside && !startInside){
							endX = intersection.getX();
							endY = intersection.getY();
							
							// remove end neighbours
							e.endNeighbours = new ArrayList();
							
							e.end.x = endX;
							e.end.y = endY;
							
						}

					}
					// add the edge to the list
					clippedEdges.add( e );
				}
			}
			
			
		}
		
		return clippedEdges;
	}
	
	/** generates a list of outlines of all the individual cells
	 * 
	 * @param inside
	 * @param outside
	 * @return
	 */
	public ArrayList<Outline> getCellsInOut(ArrayList<Outline> insides, Outline outside){
		
		ArrayList<Outline> clippedRegions = new ArrayList();
		
		Poly clipPoly = outside.getPoly();//outside.getPoly().xor(inside.getPoly());
		
		for( int i = 0; i < regions.size(); i++){
			// get a Poly object from the outline
			Outline region = regions.get(i);
			// clip the region
			Poly regionPoly = region.getPoly();
			// 
			Poly clippedPoly = regionPoly.intersection(clipPoly);
			
			for( int j = 0; j < insides.size(); j++){
				clippedPoly = insides.get(j).getPoly().difference(clippedPoly);
			}
			
			//Outline clippedRegion = Outline.fromGeometry(inside.getJTSGeometry().difference(region.getJTSGeometry().intersection(outside.getJTSGeometry())),false);
			
			if( !clippedPoly.isEmpty() ){
				// check if we have a complex polygon
				if( clippedPoly.getNumInnerPoly() > 1){
					//System.out.println("Poly children: " + clippedPoly.getNumInnerPoly());
					clippedRegions.addAll(Outline.listFromPoly(clippedPoly));
				} else{
					clippedRegions.add(Outline.fromPoly(clippedPoly));
				}
				
			} else {
				System.out.println("Poly removed: " + i);
			}
			//clippedRegions.add(clippedRegion);
				
			
			
		}
		
		return clippedRegions;
		
	}
	
	
	
	
	public ArrayList<Outline> getOffsetEdges( ArrayList<Edge> edges, double offsetAmount, double radius ){
		
		// get all offsets
		Geometry[] geometries = new Geometry[edges.size()];
		
		for(int i = 0; i < edges.size(); i++){
			geometries[i] = edges.get(i).getOffsetGeometry(offsetAmount, true, 0);
		}
		
		Geometry allEdges = new GeometryFactory().createGeometryCollection(geometries).buffer(radius).buffer(-radius);
		
		ArrayList<Outline> offsetEdges = Outline.listFromGeometry(allEdges);
		System.out.println("[EG] Total # of offset edges: " + offsetEdges.size());
		
		return offsetEdges;

	}
	
	public Geometry getOffsetEdgesWithInsides( ArrayList<Edge> edges, ArrayList<Outline> insides, double offsetAmount, double radius, int edgeShape ){
		// loop through the edges
		//LineString[] lineStrings = new LineString[edges.size()];
		
		// get all insides as one geometry
		Geometry[] insideGeometries = new Geometry[insides.size()];
		
		for( int i = 0; i < insides.size(); i++){
			insideGeometries[i] = insides.get(i).getJTSPolygon();
		}
		
		
		Geometry insideCollection = new GeometryFactory().createGeometryCollection(insideGeometries);
		
		// get all edges as one geometry
		
		Geometry[] edgeGeometries = new Geometry[edges.size()];
		
		for(int i = 0; i < edges.size(); i++){
			edgeGeometries[i] = edges.get(i).getOffsetGeometry(offsetAmount, true, edgeShape);
		}
		
		Geometry edgeCollection = new GeometryFactory().createGeometryCollection(edgeGeometries);
		
		System.out.println("[EG:OffsetWithInsides] generating combined edges");
		Geometry combinedEdges = edgeCollection.buffer(0);
		
		// free memory
		edgeCollection = null;
		
		
		System.out.println("[EG:OffsetWithInsides] combined edges points: " + combinedEdges.getNumPoints());
		
		System.out.println("[EG:OffsetWithInsides] generating collection");
		
		Geometry[] bothGeometries = new Geometry[2];
		bothGeometries[0] = combinedEdges;
		bothGeometries[1] = insideCollection;
		
		Geometry collection = new GeometryFactory().createGeometryCollection(bothGeometries);
		
		
		
		System.out.println("[EG:OffsetWithInsides] generating inset buffer");
		Geometry inset = collection.buffer(radius, 8);
		
		collection = null;
		bothGeometries = null;
		
		System.out.println("[EG:OffsetWithInsides] generating outset buffer");
		Geometry allEdges = inset.buffer(-radius, 8);
		
		System.out.println("[EG:OffsetWithInsides] combined geometry points: " + allEdges.getNumPoints());
		
		
		return allEdges;
		//ArrayList<Outline> offsetEdges = Outline.listFromGeometry(allEdges);
		//System.out.println("[EG] Total # of offset edges: " + offsetEdges.size());
		
		//return offsetEdges;

	}
	
	/*
	public Geometry getOffsetEdgesWithInsides( ArrayList<Edge> edges, Geometry insides, double offsetAmount, double radius ){
		// loop through the edges
		LineString[] lineStrings = new LineString[edges.size()];
		
		// get all offsets
		Geometry[] geometries = new Geometry[edges.size()+1];
		
		for(int i = 0; i < edges.size(); i++){
			geometries[i] = edges.get(i).getOffsetGeometry(offsetAmount, true);
		}
		// add the insides
		geometries[edges.size()] = insides;
		
		Geometry allEdges = new GeometryFactory().createGeometryCollection(geometries).buffer(radius).buffer(-radius);
		
		return allEdges;
		//ArrayList<Outline> offsetEdges = Outline.listFromGeometry(allEdges);
		//System.out.println("[EG] Total # of offset edges: " + offsetEdges.size());
		
		//return offsetEdges;

	}*/
	
	
	
	/**
	 * FINAL GEOMETRY GENERATION ROUTINES
	 */
	
	
	/**
	 * get the edge offset geometry when creating an offset for all edges in the list
	 * - usually a polygon with some inside polygons
	 * @param edges
	 * @param offsetAmount
	 * @return
	 */
	public Geometry getEdgeOffsetGeometry( ArrayList<Edge> edges, double offsetAmount, double cornerRadius, boolean calculateCorner ){
		
		/*
		LineString[] lineStrings = new LineString[edges.size()];
		
		for(int i = 0; i < edges.size(); i++){
			lineStrings[i] = (LineString)edges.get(i).getJTSGeometry();
		}
		
		Geometry allEdges = new GeometryFactory().createMultiLineString(lineStrings);
		*/
		
		// get all offsets
		Geometry[] geometries = new Geometry[edges.size()];
		
		for(int i = 0; i < edges.size(); i++){
			geometries[i] = edges.get(i).getOffsetGeometry(offsetAmount, calculateCorner, 1);
		}
		
		Geometry allEdges = new GeometryFactory().createGeometryCollection(geometries);
		
		
		if( calculateCorner ){
			Geometry offset = allEdges.buffer(cornerRadius).buffer(-cornerRadius);
			//Geometry offset2 = offset.buffer(-cornerRadius);
			return offset;
		} else {
			Geometry offset = allEdges.buffer(0);
			return offset;
		}
		
		
	}
	
	/**
	 * generate a structure of combined edges
	 * - first, create an outline for both the large and the small edges
	 * - then, union the insides with the large edges
	 * - then, create corners for the large edges
	 * - then, union the two geometries
	 * - then, create corners for the small edges
	 * @param insides
	 * @param largeEdges
	 * @param largeThickness
	 * @param largeCornerRadius
	 * @param smallEdges
	 * @param smallThickness
	 * @param smallCornerRadius
	 * @return
	 */
	public ArrayList<Outline> generateCombinedEdges(
			ArrayList<Outline> insides,
			ArrayList<Edge> largeEdges, 
			double largeThickness, 
			double largeCornerRadius, 
			ArrayList<Edge> smallEdges, 
			double smallThickness,
			double smallCornerRadius,
			int edgeShape){
		
		
		System.out.println("[EG:CombinedEdges] Generating geometry for large edges...");
		// create an outline for the large edges + corner
		Geometry gLarge = getOffsetEdgesWithInsides( largeEdges, insides, largeThickness/2, largeCornerRadius, edgeShape );
		
		System.out.println("[EG:CombinedEdges] Generating geometry for small edges...");
		// create an outline for the small edges
		Geometry gSmall = getEdgeOffsetGeometry( smallEdges, smallThickness/2, 0, false);
		
		System.out.println("[EG:CombinedEdges] Generating combined geometry...");
		// generate a union of the two geometries
		
		Geometry[] allGeometries = new Geometry[3];
		allGeometries[0] = gLarge;
		allGeometries[1] = gSmall;
		allGeometries[2] = Outline.listToGeometry(insides);
		Geometry gBoth = new GeometryFactory().createGeometryCollection(allGeometries);
		
		System.out.println("[EG:CombinedEdges] Generating small corners...");
		// generate corners for the two geometries
		Geometry gFinal = gBoth.buffer(smallCornerRadius/2).buffer(-smallCornerRadius/2);
		
		System.out.println("[EG:CombinedEdges] Generating outlines...");
		
		System.out.println("[EG:CombinedEdges] Total # of points: " + gFinal.getNumPoints());
		
		ArrayList<Outline> outlines = Outline.listFromGeometry(gFinal);
		
		System.out.println("[EG:CombinedEdges] Generation DONE!");
		
		return outlines;
		
	}
	
	
	public void drawEdges(PGraphics g){
		for( int i = 0; i < edges.size(); i++ ){
			// draw only if it's active
			if( edges.get(i).active)
				
				drawEdge(g, edges.get(i), false);
			
		}
	}
	
	public void drawEdges( PGraphics g, boolean drawAll){
		if( drawAll){
			for( int i = 0; i < edges.size(); i++ ){
				// draw only if it's active
					
					drawEdge(g, edges.get(i), drawAll);
				
			}
		} else {
			drawEdges( g );
		}
	}
	
	/**
	 * draw the regions
	 * @param g
	 */
	public void drawRegions(PGraphics g){
		for( int i = 0; i < regions.size(); i++ ){
			// draw only if it's active
			Outline region = regions.get(i);
			region.draw(g);
			
		}
	}
	
	public void drawEdge(PGraphics g, Edge e, boolean drawAll){
		
		
		if( drawAll ){
			//g.strokeWeight( 1 );
		} else {
			g.strokeWeight( (float)e.thickness );
		}
		
		Line2D.Double l = e.getLine();
		
		// draw the line
		if( ! drawAll ){
			l = e.getActiveLine();
		}
		
		
		g.line( (float)l.getX1(), (float)l.getY1(), (float)l.getX2(), (float)l.getY2() );
		
	}
	
	
	
	/*
	public void drawNodes(PGraphics g){
		for( int i = 0; i < nodes.size(); i++){
			EdgeNode node = nodes.get(i);
			g.ellipse((float)node.pos.getX(), (float)node.pos.getY(), 3, 3);
		}
		
	}*/
	/*
	public ArrayList<Attractor> getAttractorsFromNodes(){
		
		ArrayList<Attractor> attractors = new ArrayList();
		
		for( int i = 0; i < nodes.size(); i++){
			EdgeNode node = nodes.get(i);
			attractors.add(new Attractor( (Point2D.Double)node.pos.clone(), 10));
		}
		
		return attractors;
	}*/
	
	public Edge getClosestEdgeTo(Point2D.Double point){
		
		double largeNumber = 1000000000000d;
		
		Point2D.Double farPoint = new Point2D.Double(largeNumber, largeNumber);
		Point2D.Double farPoint2 = new Point2D.Double(largeNumber+10, largeNumber+10);
		
		Edge closestEdge = new Edge(farPoint, farPoint2);
		double closestDistance = 1.0E12;
		
		
		for( int i = 0; i < edges.size(); i++ ){

			Edge e = edges.get(i);
			Line2D.Double line = e.getLine();
			double distance = line.ptSegDistSq(point);

			
			if( distance < closestDistance){
				// line is closer than previous one
				closestEdge = e;
				closestDistance = distance;
			}
			
		}
		
		return closestEdge;
	}
	
	/*
	public EdgeNode getClosestNodeTo(Point2D.Double point){
		
		double largeNumber = 1000000000000d;
		
		Point2D.Double farPoint = new Point2D.Double(largeNumber, largeNumber);
		
		EdgeNode closestNode = new EdgeNode(farPoint);
		double closestDistance = 1.0E12;
		
		
		for( int i = 0; i < nodes.size(); i++ ){

			EdgeNode e = nodes.get(i);
			double distance = e.pos.distance(point);

			
			if( distance < closestDistance){
				// line is closer than previous one
				closestNode = e;
				closestDistance = distance;
			}
			
		}
		
		//System.out.println("loc: " + closestNode.pos.getX() + " " + closestNode.pos.getY());
		//System.out.println("closest distance:" + closestDistance);
		
		return closestNode;
	}*/
	
	/**
	 * snap points close to a polygon to the edges of that polygon
	 * @param points
	 * @param snapDistance
	 * @return
	 */
	
	public double[][] snapPointsToPolygon( double[][] points, Outline polygon, double snapDistance){
		
		double[][] newPoints = new double[points.length][2];
		
		// get a copy of the points array
		System.arraycopy(points, 0, newPoints, 0, points.length);
		
		for( int i = 0; i < points.length; i++){
			Point2D.Double p = new Point2D.Double(points[i][0], points[i][1]);
			if( polygon.distanceTo(p) < snapDistance){
				// closer than the snap distance
				Point2D snapP = polygon.closestPoint(p, snapDistance);
				// snap it!
				newPoints[i][0] = snapP.getX();
				newPoints[i][1] = snapP.getY();
			}
		}
		
		return newPoints;
	}

	public void snapEdges(ArrayList<Edge> largeEdges, double snapDistance) {
		
		double largeNumber = 1000000000000d;
		
		// draw adges adapted to the bigger structure
		
		// move edges close to the bigger structure to it
		for( int i = 0; i < edges.size(); i++ ){
			Edge e = edges.get(i);
			
			Point2D.Double start = e.start;
			Point2D.Double end = e.end;
			
			Edge closestStartEdge = new Edge();
			Edge closestEndEdge = new Edge();
			
			double closestStartDist = 1E12;
			double closestEndDist = 1E12;
			
			
			for(int j = 0; j < largeEdges.size(); j++){
				
				Edge le = largeEdges.get(j);
				Line2D line = le.getLine();

				// find the closest edge
				double startDist = line.ptSegDistSq(start);
				double endDist = line.ptSegDistSq(end);
				
				if( startDist < closestStartDist ){
					closestStartDist = startDist;
					closestStartEdge = le;
				}
				
				if( endDist < closestEndDist ){
					closestEndDist = endDist;
					closestEndEdge = le;
					
				}
			}
			
			// see if it is closer than the threshold
			if( closestStartDist < (snapDistance * snapDistance) && closestStartEdge.active){
				// snap it!
				// get the closest point on the line segment
				Point2D.Double newPoint = (Point2D.Double) closestPoint( closestStartEdge.getLine(), start);
				
				e.start = newPoint;
				
				// if the closest point is close enough to the start or end point, use that
				if( newPoint.distance(closestStartEdge.start) < snapDistance ){
					e.start = closestStartEdge.start;
				} 
				
				if( newPoint.distance(closestStartEdge.end) < snapDistance ){
					e.start = closestStartEdge.end;
				} 
				
				
			}
			
			if( closestEndDist < (snapDistance * snapDistance) && closestEndEdge.active){
				// snap it!
				// get the closest point on the line segment
				Point2D.Double newPoint = (Point2D.Double) closestPoint( closestEndEdge.getLine(), end);
				
				e.end = newPoint;
				
				// if the closest point is close enough to the start or end point, use that
				if( newPoint.distance(closestEndEdge.start) < snapDistance ){
					e.end = closestEndEdge.start;
				} 
				
				if( newPoint.distance(closestEndEdge.end) < snapDistance ){
					e.end = closestEndEdge.end;
				} 
			}
			
			
		}
		
	}
	
	/**
     * Returns the distance of p3 to the segment defined by p1,p2;
     * 
     * @param p1
     *                First point of the segment
     * @param p2
     *                Second point of the segment
     * @param p3
     *                Point to which we want to know the distance of the segment
     *                defined by p1,p2
     * @return The distance of p3 to the segment defined by p1,p2
     */
    public Point2D closestPoint(Line2D line, Point2D p3) {
    	
    	Point2D p1 = line.getP1();
    	Point2D p2 = line.getP2();

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();
	
		if ((xDelta == 0) && (yDelta == 0)) {
		    throw new IllegalArgumentException("p1 and p2 cannot be the same point");
		}
	
		final double u = ((p3.getX() - p1.getX()) * xDelta + (p3.getY() - p1.getY()) * yDelta) / (xDelta * xDelta + yDelta * yDelta);
	
		final Point2D closestPoint;
		if (u < 0) {
		    closestPoint = p1;
		} else if (u > 1) {
		    closestPoint = p2;
		} else {
		    closestPoint = new Point2D.Double(p1.getX() + u * xDelta, p1.getY() + u * yDelta);
		}

		return closestPoint;
    }
	
	
}
