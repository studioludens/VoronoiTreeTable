package com.ludens.treetable.old;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;

import megamu.mesh.Attractor;
import megamu.mesh.Outline;
import megamu.mesh.TreeNode;


/**
 * generates a tree based on sources and a root node
 * @author rulkens
 *
 */
public class TreeGenerator {
	
	public double attractorKillDistance = 10;
	public double branchScale = .8;
	public double newNodeDistance = 10;

	private ArrayList<TreeNode> rootNodes;
	private Outline outline;
	private ArrayList<Attractor> attractorList;
	
	private ArrayList<TreeNode> nodeList;
	
	TreeGenerator( ArrayList<TreeNode> rootNodes, ArrayList<Attractor> attractorList, Outline outline ){
		this.rootNodes = rootNodes;
		this.attractorList = attractorList;
		this.outline = outline;
		
		nodeList = new ArrayList();
		
		nodeList.addAll( rootNodes );
		
	}
	
	public void generate(){
		// loop through all the nodes until we don't have any attractors left 
		
		//while( updateNodes() > 0);
		invalidateAttractors();
		clearAttractors();
		updateAttractors();
		calculateAttractors();
		
		
		
		//System.out.println("Number of tree nodes:" + nodeList.size());
	}
	
	private int updateNodes(){
		
		ArrayList<TreeNode> newChildNodes = new ArrayList();
		
		int activeAttractors = 0;
		
		// loop through the nodes
		Iterator<TreeNode> iNode = nodeList.iterator();
		
		while( iNode.hasNext()){
			TreeNode thisNode = iNode.next();
			
			Attractor closestAttractor = new Attractor(new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 1);
			double closestDistance = Point2D.distanceSq(thisNode.pos.getX(), thisNode.pos.getY(), closestAttractor.pos.getX(), closestAttractor.pos.getY());
			
			// find the closest attractor to each node
			Iterator<Attractor> i = attractorList.iterator();
			
			activeAttractors = 0;
			
			while( i.hasNext() ){
				Attractor attr = i.next();
				
				// don't do this when an attractor is not active
				if( !attr.active ) continue;
				
				// add one to the active attractors counter
				activeAttractors++;
				
				double distance = Point2D.distanceSq(attr.pos.getX(), attr.pos.getY(), thisNode.pos.getX(), thisNode.pos.getY());
				
				if( distance < closestDistance){
					// this node is closer than the last one
					closestAttractor = attr;
					closestDistance = distance;
				}
				
			}
			
			if( activeAttractors > 0){
				// we have found at least one active attractor
				thisNode.closestAttractor = closestAttractor;
				closestAttractor.active = false;
				
				// create a new child node for this attractor
				TreeNode newChildNode = new TreeNode(thisNode, (Point2D.Double)closestAttractor.pos.clone(), 3, thisNode.thickness/2);
				thisNode.children.add( newChildNode );
				newChildNodes.add(newChildNode);

			}
			
		}
		
		// add the new child nodes to the list of nodes
		nodeList.addAll(newChildNodes);
		
		//System.out.println( "Number of nodes: " + nodeList.size());
		return activeAttractors;
					
		
	}
	
	private void clearAttractors(){
		Iterator<TreeNode> iNode = nodeList.iterator();
		// clear the list of closest attractors on each node
		while( iNode.hasNext()){
			TreeNode thisNode = iNode.next();
			// calculate distance
			thisNode.attractors.clear();
			
		}
		
	}
	
	private void updateAttractors(){
		
		
		Iterator<Attractor> i = attractorList.iterator();
		
		
		while( i.hasNext() ){
			Attractor attr = i.next();
			if( !attr.active ) continue;
			
			// find the closest node
			Iterator<TreeNode> iNode = nodeList.iterator();
			TreeNode closestNode = new TreeNode(null, new Point2D.Double(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY), 1, 1);
			double closestDistance = Point2D.distanceSq(attr.pos.getX(), attr.pos.getY(), closestNode.pos.getX(), closestNode.pos.getY());
			
			while( iNode.hasNext()){
				TreeNode thisNode = iNode.next();
				// calculate distance
				double distance = Point2D.distanceSq(attr.pos.getX(), attr.pos.getY(), thisNode.pos.getX(), thisNode.pos.getY());
				if( distance < closestDistance){
					// this node is closer than the last one
					closestNode = thisNode;
					closestDistance = distance;
					
				}
				
			}
			
			// set the closest distance for this attractor
			
			attr.closestDistance = closestDistance;
			closestNode.attractors.add(attr);
			
			// we now have the closest node to this attractor
			attr.closestNode = closestNode;
			
			
		}
	}
	
	private void calculateAttractors(){
		
		// a list of new child nodes
		ArrayList<TreeNode> newChildNodes = new ArrayList();
		
		// calculate the new positions of nodes, based on the attractors
		Iterator<TreeNode> iNode = nodeList.iterator();
		
		
		
		// clear the list of closest attractors on each node
		while( iNode.hasNext()){
			TreeNode thisNode = iNode.next();

			if( thisNode.attractors.size() > 0){
				
				double totalX = 0;
				double totalY = 0;
				
				// calculate distance
				Iterator<Attractor> i = thisNode.attractors.iterator();
				
				//System.out.println("# of Attractors for node: " + thisNode.attractors.size());
				
				while( i.hasNext() ){
					
					Attractor attr = i.next();
					
					// sum all positions
					totalX += attr.pos.getX();
					totalY += attr.pos.getY();

				}
				
				// and divide them by the amount of attractors
				double dirX = totalX / thisNode.attractors.size();
				double dirY = totalY / thisNode.attractors.size();
				
				double thisX = thisNode.pos.getX();
				double thisY = thisNode.pos.getY();
				
				
				// but only move a step in that position
				double angle = Math.atan2(dirX - thisX, dirY - thisY);
				
				double newX = thisNode.pos.getX() + ( newNodeDistance * Math.sin(angle) );
				double newY = thisNode.pos.getY() + ( newNodeDistance * Math.cos(angle) );
				
				
				// create a new node
				TreeNode newChildNode = new TreeNode(thisNode, new Point2D.Double(newX, newY), 3, thisNode.thickness*branchScale);
				thisNode.children.add( newChildNode );
				//nodeL ist.add(newChildNode);
				newChildNodes.add(newChildNode);
			}
		}
		
		// add the new child nodes to the list of nodes
		nodeList.addAll(newChildNodes);
				
		//System.out.println( "Number of nodes: " + nodeList.size());
		
	}
	
	/**
	 * set all attractors to inactive that are to close to a vein
	 */
	private int invalidateAttractors(){
		
		int activeAttractors = 0;
		
		Iterator<Attractor> i = attractorList.iterator();
		
		while( i.hasNext() ){
			Attractor attr = i.next();
			
			if( attr.closestDistance < ( attractorKillDistance * attractorKillDistance)){
				attr.active = false;
			} else {
				if( attr.active ) activeAttractors++;
			}
		}
		
		//System.out.println("# of active attractors: " + activeAttractors);
		
		return activeAttractors;
		
	}
}
