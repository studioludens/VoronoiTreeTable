package megamu.mesh;
import java.awt.*;
import java.awt.geom.*;


public class Attractor {

	public Point2D pos;
	public float strength;
	public boolean active;
	public TreeNode closestNode;
	
	/**
	 * closest distance to a node
	 *
	 */
	public double closestDistance;
	
	//TreeNode closestNode;
	
	
	
	public Attractor(Point2D.Double pos, float strength){
		this.pos = pos;
		this.strength = strength;
		
		active = true;
		closestDistance = Double.POSITIVE_INFINITY;
	}
	/*
	public Attractor(Point2D.Double pos){
		Attractor( pos, 1.0f);
	}*/
	
	public double distanceSq( Point2D.Double point ){
		return pos.distanceSq( point );
	}
	
	public static Attractor createIn(Polygon p){
		// create an attractor point 
		
		// using the dart-throwing algorithm
		
		Rectangle bounds = p.getBounds();
		
		// generate a point within the boundaries of the polygon
		double xpos = bounds.getX() + ( Math.random() * bounds.getWidth() );
		double ypos = bounds.getY() + ( Math.random() * bounds.getHeight() );
		
		Point2D.Double point = new Point2D.Double( xpos, ypos );
		
		
		while( !p.contains(point) ){
			
			// generate a point within the boundaries of the polygon
			xpos = bounds.getX() + ( Math.random() * bounds.getWidth() );
			ypos = bounds.getY() + ( Math.random() * bounds.getHeight() );
			
			point = new Point2D.Double( xpos, ypos );
			
		}
		
		return new Attractor( point, 10);
	}
}
