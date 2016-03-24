package megamu.mesh;

import java.awt.geom.Point2D;
import java.util.ArrayList;

public class EdgeNode {

	public ArrayList<EdgeNode> neighbours;
	public ArrayList<Edge> neighbourEdges;
	
	public Point2D.Double pos;
	public boolean active = false;
	public double strength = 1;
	
	/**
	 * initialize with a position
	 * @param pos
	 */
	public EdgeNode( Point2D.Double pos){
		neighbours = new ArrayList();
		neighbourEdges = new ArrayList();
		this.pos = pos;
	}
	
}
