package megamu.mesh;
import java.awt.*;
import java.util.*;
import java.awt.geom.*;
/**
 * representing a node in a tree
 * @author rulkens
 *
 */
public class TreeNode {

	public TreeNode parentNode;
	public ArrayList<TreeNode> children;
	public ArrayList<Attractor> attractors;
	
	public Attractor closestAttractor;
	
	// location of the node
	public Point2D.Double pos;
	
	// size of the node
	public double size;
	
	public double thickness;
	
	/**
	 * constructor
	 * 
	 */
	public TreeNode( TreeNode parentNode, Point2D.Double position, double size, double thickness){
		
		children = new ArrayList();
		attractors = new ArrayList();
		
		this.parentNode = parentNode;
		this.pos = position;
		this.size = size;
		this.thickness = thickness;
	}
	
}
