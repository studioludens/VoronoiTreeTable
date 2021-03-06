package megamu.mesh;
import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;

import com.vividsolutions.jts.geom.*;


public class Edge {

	public Point2D.Double start;
	public Point2D.Double end;
	
	public ArrayList<Edge> startNeighbours;
	public ArrayList<Edge> endNeighbours;
	
	public EdgeNode startNode;
	public EdgeNode endNode;
	
	public double thickness;
	public boolean active = false;
	
	private boolean _thicknessCalculated = false;
	
	private static double PARABOLA_STEPS = 40;
	
	
	public Edge(){
	
		startNeighbours = new ArrayList();
		endNeighbours = new ArrayList();
	}
	
	public Edge( Point2D.Double start, Point2D.Double end){
		this.start = start;
		this.end = end;
		
		startNeighbours = new ArrayList();
		endNeighbours = new ArrayList();
	}
	
	public Line2D.Double getLine(){
		if( startNeighbours.size() == 0){
			return new Line2D.Double(start, end);
		} else {
			return new Line2D.Double(end, start);
		}
		
	}
	
	/**
	 * get the part of the line that's active
	 * 
	 * return only half of the line if it's an end line
	 * @return
	 */
	public Line2D.Double getActiveLine(){
		
		double startX = start.getX();
		double startY = start.getY();
		double endX = end.getX();
		double endY = end.getY();
		
		
		if( !endActive() ){
				// start point is halfway across the length of the edge
				endX = (startX + endX ) / 2;
				endY = (startY + endY ) / 2;
		}
			
		if( !startActive() ){
				// start point is halfway across the length of the edge
				startX = (startX + endX ) / 2;
				startY = (startY + endY ) / 2;
		}
		
		
		return new Line2D.Double(new Point2D.Double(startX, startY), new Point2D.Double(endX, endY));
	}
	
	public Geometry getJTSGeometry(){
		Coordinate start = new Coordinate( this.start.getX(), this.start.getY());
		Coordinate end = new Coordinate( this.end.getX(), this.end.getY());
		Coordinate[] coordinates = new Coordinate[2];
		coordinates[0] = start;
		coordinates[1] = end;
		Geometry g = new GeometryFactory().createLineString(coordinates);
		return g;
	}
	
	
	public Geometry getOffsetGeometry( double offset, boolean calculateCorner, int edgeShape ){
		// check if one of the points is an end point
		
		if( calculateCorner ){
			if( startNeighbours.size() == 0 || endNeighbours.size() == 0){
				// the line has been cut, generate a parabola shape
				Outline o = getParabola( offset*2, PARABOLA_STEPS, edgeShape);
				
				return o.getJTSPolygon();
			} else {
				return getJTSGeometry().buffer(offset);
			}
		} else {
			return getJTSGeometry().buffer(offset);
		}
		
		
	}
	
	public Outline getParabola( double thickness, double steps, int power ){
		
		
		
		Line2D line = getLine();
		
		Outline o = new Outline();
		
		double lineLengthX = (line.getX2() - line.getX1())*(line.getX2() - line.getX1());
		double lineLengthY = (line.getY2() - line.getY1())*(line.getY2() - line.getY1());
		
		double lineLength = Math.sqrt(lineLengthX + lineLengthY);
		
		double angle = Math.atan2(line.getY2()-line.getY1(), line.getX2()-line.getX1())-Math.PI/2;
		
		//double endY = startY + lineLength;
		double stepSize = thickness / steps;
		
		double thSq = (1/Math.abs(Math.pow(2, power))) * ( Math.abs(Math.pow(thickness, power)) );
		
		double a = lineLength / thSq;
		//double pStartX = ( thickness * .5 );

		
		for( int i = 0; i <= steps; i++){
			// calculate the point
			double x = (-thickness*.5)+(i*stepSize);
			double y = a * Math.abs(Math.pow(x, power));
			
			//double cX = (x + pStartX + thickness*.5);
			
			double pX = line.getX1() + x * Math.cos(angle) - y * Math.sin(angle);
			double pY = line.getY1() + x * Math.sin(angle) + y * Math.cos(angle);
			
			o.addPoint(pX, pY);
			
		}
		
		
		//line( (float)line.getX1(), (float)line.getY1(), (float)line.getX2(), (float)line.getY2());
		
		return o;
	}
	
	
	/**
	 * is this edge on the edge of the shape?
	 */
	boolean isEndEdge(){
		if( startNeighbours.size() == 0 || endNeighbours.size() == 0 ) return true;
		else
			return false;
	}
	
	/**
	 * is the start point active?
	 * 
	 * if one of the start edges is active, it returns true
	 * @return
	 */
	public boolean startActive(){
		
		if( startNeighbours.size() == 0) return false;
		
		for(int i = 0; i < startNeighbours.size(); i++){
			if( startNeighbours.get(i).active ) return true;
		}
		
		return false;
	}
	
	/**
	 * is the end point active?
	 * 
	 * if one of the start edges is active, it returns true
	 * @return
	 */
	public boolean endActive(){
		if( endNeighbours.size() == 0) return false;
		
		for(int i = 0; i < endNeighbours.size(); i++){
			if( endNeighbours.get(i).active ) return true;
		}
		
		return false;
	}
	
	/**
	 * calculate the thickness of the edge
	 * adds the thickness of each of it's children
	 */
	public double calculateThickness(){
		
		_thicknessCalculated = true;
		
		if( !active ) return 0;
		
		double calculatedThickness = .1;
		
		for(int i = 0; i < endNeighbours.size(); i++){
			Edge e = endNeighbours.get(i);
			if( !e._thicknessCalculated && e.active ){
				calculatedThickness += e.calculateThickness();
			}
		}
		
		for(int i = 0; i < startNeighbours.size(); i++){
			Edge e = startNeighbours.get(i);
			if( !e._thicknessCalculated && e.active ){
				calculatedThickness += e.calculateThickness();
			}
		}
		
		thickness = calculatedThickness;
		
		return calculatedThickness;
	}
		
		
	
	/**
	 * returns the location of intersection with a polygon, if it exists
	 * @param path
	 * @return
	 */
	public Point2D.Double intersectPolygon( Polygon2D path ){
		
		// get last point
		double[] prevPoint = new double[2];
		prevPoint[0] = path.xpoints[path.npoints-1];
		prevPoint[1] = path.ypoints[path.npoints-1];
		
		for( int i = 0; i < path.npoints; i++){
			Line2D.Double line = new Line2D.Double(prevPoint[0], prevPoint[1], path.xpoints[i], path.ypoints[i]);
			// get intersection point
			Point2D.Double point = this.intersectLine(line);
			if( point != null) return point;
			
			prevPoint[0] = path.xpoints[i];
			prevPoint[1] = path.ypoints[i];
		}
		
		
		
		return null;
	}
	
	public Point2D.Double intersectLine( Line2D.Double line ){
		
		Line2D.Double L1 = this.getLine();
		
		double d =
	            (line.getY2() - line.getY1()) * (L1.getX2() - L1.getX1())
	            -
	            (line.getX2() - line.getX1()) * (L1.getY2() - L1.getY1());

	         //n_a and n_b are calculated as seperate values for readability
	         double n_a =
	            (line.getX2() - line.getX1()) * (L1.getY1() - line.getY1())
	            -
	            (line.getY2() - line.getY1()) * (L1.getX1() - line.getX1());

	         double n_b =
	            (L1.getX2() - L1.getX1()) * (L1.getY1() - line.getY1())
	            -
	            (L1.getY2() - L1.getY1()) * (L1.getX1() - line.getX1());

	         // Make sure there is not a division by zero - this also indicates that
	         // the lines are parallel.  
	         // If n_a and n_b were both equal to zero the lines would be on top of each 
	         // other (coincidental).  This check is not done because it is not 
	         // necessary for this implementation (the parallel check accounts for this).
	         if (d == 0)
	            return null;

	         // Calculate the intermediate fractional point that the lines potentially intersect.
	         double ua = n_a / d;
	         double ub = n_b / d;

	         // The fractional point will be between 0 and 1 inclusive if the lines
	         // intersect.  If the fractional calculation is larger than 1 or smaller
	         // than 0 the lines would need to be longer to intersect.
	         if (ua >= 0d && ua <= 1d && ub >= 0d && ub <= 1d)
	         {
	            double x = L1.getX1() + (ua * (L1.getX2() - L1.getX1()));
	            double y = L1.getY1() + (ua * (L1.getY2() - L1.getY1()));
	            return new Point2D.Double(x, y);
	         }
	         return null;
	
	}
	
	
}
