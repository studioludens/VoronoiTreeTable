package megamu.mesh;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import processing.core.PGraphics;
import processing.core.PShape;

import com.seisw.util.geom.*;
import com.vividsolutions.jts.geom.*;


import megamu.mesh.MPolygon;


public class Outline extends Polygon2D {

	
	public int color = 0xCCCCCC;
	
	public void addPoint(double x, double y){
		super.addPoint((float)x, (float)y);
	}
	
	
	public void draw(PGraphics g){
		//g.fill(255);
		//g.noStroke();
		g.beginShape();
		for(int i = 0; i < this.npoints; i++){
			g.vertex((float)xpoints[i], (float)ypoints[i]);
		}
		// close the loop
		g.vertex((float)xpoints[0], (float)ypoints[0]);
		g.endShape();
	}
	/**
	 * closest distance from a point to a point on this outline
	 * @param point
	 * @return
	 */
	public double distanceTo(Point2D.Double point ){
		// calculate the closest distance
		
		double closestDistance = 1E20D;
		
		// calculate closest distance to inside
		for( int j = 0; j < npoints; j++){
			
			double x2,y2;
			
			if(j == 0){
				x2 = xpoints[npoints-1];
				y2 = ypoints[npoints-1];
			} else {
				x2 = xpoints[j-1];
				y2 = ypoints[j-1];
			}
			
			double x1 = xpoints[j];
			double y1 = ypoints[j];
			
			Line2D.Double l = new Line2D.Double(x1,y1,x2,y2);

			double distance = l.ptSegDistSq(point);
			
			closestDistance = Math.min(closestDistance, distance);
		}
		
		return Math.sqrt(closestDistance);
	}
	
	/**
	 * get the closest point on the polygon from the given point
	 * @param point
	 * @return
	 */
	public Point2D.Double closestPoint( Point2D point, double endPointSnap ){
		
		double closestDistance = 1E20D;
		Line2D closestSegment = new Line2D.Double(0,0,0,0);
		// calculate closest distance to inside
		for( int j = 0; j < npoints; j++){
			
			double x2,y2;
			
			if(j == 0){
				x2 = xpoints[npoints-1];
				y2 = ypoints[npoints-1];
			} else {
				x2 = xpoints[j-1];
				y2 = ypoints[j-1];
			}
			
			double x1 = xpoints[j];
			double y1 = ypoints[j];
			
			Line2D l = new Line2D.Double(x1,y1,x2,y2);

			double distance = l.ptSegDistSq(point);
			
			if( distance < closestDistance ){
				closestSegment = l;
			}
			
			closestDistance = Math.min(closestDistance, distance);
		}
		
		// we have the closest segment, now return a point on that segment
		Point2D.Double closestPoint = (Point2D.Double) closestPoint( closestSegment, point);
		// check if it's close enough to one of the end points to snap to it
		if( closestPoint.distanceSq(closestSegment.getP1()) < endPointSnap*endPointSnap )
			closestPoint = (Point2D.Double) closestSegment.getP1();
		if( closestPoint.distanceSq(closestSegment.getP2()) < endPointSnap*endPointSnap )
			closestPoint = (Point2D.Double) closestSegment.getP2();
		
		return closestPoint;
		
	}
	
	/**
	 * return a GPJC compatible polygon
	 * @return
	 */
	public Poly getPoly(){
		
		PolyDefault poly = new PolyDefault();
		for(int i = 0; i < npoints; i++){
			poly.add(xpoints[i], ypoints[i]);
		}
		
		return poly;
		
		
	}
	
	public Geometry getJTSRing(){
		
		Coordinate[] points = new Coordinate[npoints+1];
		
		for(int i = 0; i < npoints; i++){
			points[i] = new Coordinate(xpoints[i], ypoints[i]);
		}
		
		// add the first point as last point
		points[npoints] = new Coordinate(xpoints[0], ypoints[0]);
		
		Geometry g = new GeometryFactory().createLinearRing(points);
		return g;
	}
	
	public Geometry getJTSPolygon(){
		Geometry poly = new GeometryFactory().createPolygon((LinearRing)getJTSRing(),null);
		
		return poly;
	}
	
	/**
	 * get an offset curve
	 * @param distance
	 * @return
	 */
	public Outline getOffset(double distance){
		if( distance < 0){
			return Outline.fromGeometry(this.getJTSRing().buffer(distance), true);
		} else if( distance > 0){
			return Outline.fromGeometry(this.getJTSRing().buffer(distance), false);
		} else {
			return this;
		}
		
	}
	
	public ArrayList<Outline> getRoundedInset( double distance, double radius ){
		return getRoundedInset( distance, radius, 8);
	}
	
	public ArrayList<Outline> getRoundedInset( double distance, double radius, int segments){
		Geometry g = this.getJTSRing();
		Geometry bufferedG = g.buffer(distance+radius);
		// get the inside polygon
		
		ArrayList<Outline> list = new ArrayList();
		
		if( bufferedG instanceof Polygon){
			if(((Polygon)bufferedG).getNumInteriorRing() < 1) return null;
			
			
			if( ((Polygon)bufferedG).getNumInteriorRing() > 1){
				//System.out.println("Polygon with " + ((Polygon)bufferedG).getNumInteriorRing() + " insides");
				
				for(int i = 0; i < ((Polygon)bufferedG).getNumInteriorRing(); i++){
					Geometry inset = ((Polygon)bufferedG).getInteriorRingN(i);
					// do an offset
					Geometry roundedG = inset.buffer(radius, segments);
					
					
					list.add(Outline.fromGeometry(roundedG, false));
					
				}
				return list;
				
			} else {
				Geometry inset = ((Polygon)bufferedG).getInteriorRingN(0);
				// do an offset
				Geometry roundedG = inset.buffer(radius, segments);
				
				list.add(Outline.fromGeometry(roundedG, false));
				return list;
			}
		} else if( bufferedG instanceof GeometryCollection){
			// we have a number of polygons
			System.out.println("Geometry Collection with " + ((GeometryCollection)bufferedG).getNumGeometries() + " geometries");
			
			return null;
			
		} else {
			return null;
		}
		
	}
	
	public String getSVGString(){
		
		String polyData = "";
		for(int i = 0; i < npoints; i++){
			polyData  += xpoints[i] + "," + ypoints[i] + " ";
		}
		
		String output = "<polygon points=\"" + polyData + "\"/>";
		return output;
		
	}
	
	/**
	 * STATIC FUNCTIONS
	 */
	
	public static String getSVGStringFromList( ArrayList<Outline> outlines){
		String output = "";
		
		for( int i = 0; i < outlines.size(); i++){
			output += outlines.get(i).getSVGString() + "\n";
		}
		
		return output;
	}
	
	
	public static ArrayList<Outline> listFromPoly(Poly poly){
		// create an outline for each polygon
		ArrayList<Outline> outlines = new ArrayList();
		
		for(int j = 0; j < poly.getNumInnerPoly(); j++){
			Outline o = new Outline();
			o.color = 0xFF0000;
			
			Poly thisPoly = poly.getInnerPoly(j);
			
			for(int i = 0; i < thisPoly.getNumPoints(); i++){
				o.addPoint(thisPoly.getX(i), thisPoly.getY(i));
			}
			
			outlines.add(o);
		}
		
		return outlines;
	}
	
	public static Outline fromPoly(Poly poly){
		Outline o = new Outline();
		
		/*
		if( poly.getNumInnerPoly() > 1){
			System.out.println("Poly children: " + poly.getNumInnerPoly());
		}*/
		
		
		
		for(int i = 0; i < poly.getNumPoints(); i++){
			o.addPoint(poly.getX(i), poly.getY(i));
		}
		
		return o;
	}
	
	/**
	 * returns a simple list of outlines
	 * @param g
	 * @return
	 */
	public static ArrayList<Outline> listFromGeometry( Geometry g ) {
		
		ArrayList<Outline> outlines = new ArrayList();
		for( int i = 0; i < g.getNumGeometries(); i++){
			Geometry thisG = g.getGeometryN(i);
			if( thisG instanceof Polygon){
				
				Polygon p = (Polygon)thisG;
				// get outside ring
				outlines.addAll(Outline.listFromGeometry(p.getExteriorRing()));
				// get inside rings
				for( int j = 0; j < p.getNumInteriorRing(); j++){
					outlines.addAll(Outline.listFromGeometry(p.getInteriorRingN(j)));
				}
				
			} else if( thisG instanceof MultiPolygon){
				
				MultiPolygon mpg = (MultiPolygon)thisG;
				for( int j = 0; j < mpg.getNumGeometries(); j++){
					Geometry thisGG = mpg.getGeometryN(j);
					outlines.addAll(Outline.listFromGeometry(thisGG));
				}
				
			} else if( thisG instanceof LinearRing){
				// get the coordinates
				
				// build an outline
				Outline o = new Outline();
				
				Coordinate[] coords = ((LinearRing)thisG).getCoordinates();
				
				for(int j = 0; j < coords.length;j++){
					o.addPoint(coords[j].x, coords[j].y);
				}
				
				outlines.add(o);
			}
		}
		
		return outlines;
	}
	
	/**
	 * convert a list of outlines to geometry
	 * @param g
	 * @return
	 */
	public static Geometry listToGeometry( ArrayList<Outline> outlines ) {
		
		Geometry[] geometries = new Geometry[outlines.size()];
		
		for( int i = 0 ; i < outlines.size(); i++){
			geometries[i] = outlines.get(i).getJTSPolygon();
		}
		
		return new GeometryFactory().createGeometryCollection(geometries);
	}
	
	public static Outline fromGeometry(Geometry g, Boolean inside){
		
		
		Outline o = new Outline();
		
		Coordinate[] coords;
		
		if( g instanceof Polygon){
			if(inside){
				// return the first inside polygon
				coords = ((Polygon)g).getInteriorRingN(0).getCoordinates();
			} else {
				// return the shell
				coords = ((Polygon)g).getExteriorRing().getCoordinates();
			}
		} else {
			return null;
		}
		
		
		for(int i = 0; i < coords.length;i++){
			o.addPoint(coords[i].x, coords[i].y);
		}
		
		return o;
	}

	public static Outline fromShape( PShape s ){
		Outline o = new Outline();
		
		// return an empty outline if the shape cannot be found
		if( s == null ) return o;
		
		for(int i = 0; i < s.getVertexCount(); i++){
			o.addPoint((double)s.getVertex(i)[0],(double)s.getVertex(i)[1]);
		}
		
		return o;
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
    protected Point2D closestPoint(Line2D line, Point2D p3) {
    	
    	Point2D p1 = line.getP1();
    	Point2D p2 = line.getP2();

		final double xDelta = p2.getX() - p1.getX();
		final double yDelta = p2.getY() - p1.getY();
	
		if ((xDelta == 0) && (yDelta == 0)) {
		    //throw new IllegalArgumentException("p1 and p2 cannot be the same point");
			return p1;
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
