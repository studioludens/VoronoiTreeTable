package com.ludens.treetable.old;
import processing.core.*; 
import java.awt.*;
import java.awt.geom.*;
import java.util.*;

import megamu.mesh.TreeNode;
/**
 * renders a beautiful tree
 * @author rulkens
 *
 */
public class TreeRenderer {

	public void render( PGraphics g, ArrayList<TreeNode> rootNodes){
		// do some rendering here
		
		
		// loop through the nodes
		Iterator<TreeNode> iNode = rootNodes.iterator();
				
		while( iNode.hasNext()){
			TreeNode thisNode = iNode.next();
					
			renderNode( g, thisNode );
		}
	}
	
	private void renderNode( PGraphics g, TreeNode node){
		// loop through all the children
		
		// draw a line from each child position to the parent node position
		Point2D parentPos = node.pos;
		Iterator i = node.children.iterator();
		
		while( i.hasNext() ){
			TreeNode child = (TreeNode) i.next();
			
			g.strokeWeight((float)child.thickness);
			g.line((float)parentPos.getX(), (float)parentPos.getY(), (float)child.pos.getX(), (float)child.pos.getY());
			
			renderNode( g, child );
		}
		
		
	}
}
