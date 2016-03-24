package megamu.mesh;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.seisw.util.geom.Poly;

import quickhull3d.QuickHull3D;

public class Voronoi {

	public double dualPoints[][];
	public int faces[][];
	
	public double[][] points;
	
	private int artifact = 0;
	private IntArray[] pointBuckets;
	private LinkedArray faceNet;
	
	Edge[] edges;
	ArrayList<Outline> regions;
	
	public ArrayList<Edge> clippedEdges;
	//public ArrayList<EdgeNode> clippedNodes;
	

	public Voronoi( double[][] points ){
		this.points = points;
		
		
	}
	
	public void generateAll(){
		generateDualPoints();
		generateEdges();
		generateRegions();
	}
	
	
	public void generateDualPoints(){
		regions = new ArrayList();
		
		if( points.length < 1 ){
			edges = new Edge[0];
			
			return;
		}

		// build points array for qhull
		double qPoints[] = new double[ points.length*3 + 9 ];
		for(int i=0; i<points.length; i++){
			qPoints[i*3] = points[i][0];
			qPoints[i*3+1] = points[i][1];
			qPoints[i*3+2] = -(points[i][0]*points[i][0] + points[i][1]*points[i][1]); // standard half-squared eucledian distance
		}
		// 1
		qPoints[ qPoints.length-9 ] = -8000D;
		qPoints[ qPoints.length-8 ] = 0D;
		qPoints[ qPoints.length-7 ] = -64000000D;
		// 2
		qPoints[ qPoints.length-6 ] = 8000D;
		qPoints[ qPoints.length-5 ] = 8000D;
		qPoints[ qPoints.length-4 ] = -128000000D;
		// 3
		qPoints[ qPoints.length-3 ] = 8000D;
		qPoints[ qPoints.length-2 ] = -8000D;
		qPoints[ qPoints.length-1 ] = -128000000D;

		// prepare quickhull
		QuickHull3D quickHull = new QuickHull3D(qPoints);
		faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE);
		//int artifact = 0;
		artifact = 0;

		// compute dual points
		dualPoints = new double[faces.length][2];
		for(int i = 0; i < faces.length; i++){

			// test if it's the artifact
			if( faces[i][0] >= points.length && faces[i][1] >= points.length && faces[i][2] >= points.length )
				artifact = i;

			double x0 = qPoints[faces[i][0]*3+0];
			double y0 = qPoints[faces[i][0]*3+1];
			double x1 = qPoints[faces[i][1]*3+0];
			double y1 = qPoints[faces[i][1]*3+1];
			double x2 = qPoints[faces[i][2]*3+0];
			double y2 = qPoints[faces[i][2]*3+1];

			double v1x = 2 * (x1-x0);
			double v1y = 2 * (y1-y0);
			double v1z = x0*x0 - x1*x1 + y0*y0 - y1*y1;

			double v2x = 2 * (x2-x0);
			double v2y = 2 * (y2-y0);
			double v2z = x0*x0 - x2*x2 + y0*y0 - y2*y2;

			double tmpx = v1y * v2z - v1z * v2y;
			double tmpy = v1z * v2x - v1x * v2z;
			double tmpz = v1x * v2y - v1y * v2x;

			dualPoints[i][0] = tmpx/tmpz;
			dualPoints[i][1] = tmpy/tmpz;
		}
	}
	
	public void generateEdges(){
		// create edge/point/face network
		edges = new Edge[1];
		int edgeCount = 0;
		faceNet = new LinkedArray(faces.length);
		pointBuckets = new IntArray[points.length];
		for(int i=0; i<points.length; i++)
			pointBuckets[i] = new IntArray();

		// discover edges
		for(int i = 0; i < faces.length; i++){

			// bin faces to the points they belong with
			for(int f=0; f<faces[i].length; f++)
				if(faces[i][f] < points.length)
					pointBuckets[ faces[i][f] ].add(i);

			for(int j = 0; j < i; j++){
				if( i!=artifact && j!=artifact && isEdgeShared(faces[i], faces[j]) ){

					faceNet.link(i, j);

					if( edges.length <= edgeCount ){
						Edge[] tmpedges = new Edge[edges.length*2];
						System.arraycopy(edges, 0, tmpedges, 0, edges.length);
						edges = tmpedges;
					}
					edges[edgeCount] = new Edge();
					edges[edgeCount].start = new Point2D.Double(dualPoints[i][0], dualPoints[i][1]);
					edges[edgeCount].end = new Point2D.Double(dualPoints[j][0], dualPoints[j][1]);
					edgeCount++;

				}
			}
		}

		// trim edges down
		Edge[] tmpedges = new Edge[edgeCount];
		//for(int i=0; i<tmpedges.length; i++)
		//	tmpedges[i] = edges[i];
		System.arraycopy(edges, 0, tmpedges, 0, tmpedges.length);
		edges = tmpedges;
		
		// calculate the neighbours of all edges
		
		for(int i = 0; i < edges.length; i++){
			for(int j = 0; j < edges.length; j++){
				if( i != j){
					Edge e1 = edges[i];
					Edge e2 = edges[j];
					
					if( e1.start.equals(e2.start) || e1.start.equals(e2.end)){
						e1.startNeighbours.add(e2);
					}
					
					if( e1.end.equals(e2.start) || e1.end.equals(e2.end)){
						e1.endNeighbours.add(e2);
					}
				}
				
				
			}
		}
	}
	
	public void generateRegions(){
		// calculate the region for each point
		//regions = new MPolygon[points.length];
		for(int i=0; i<points.length; i++){
			if( pointBuckets[i].length == 0) continue;
			
			IntArray faceOrder = new IntArray(pointBuckets[i].length);

			// add coords of the region in the order they touch, starting with the convenient first
			int p = pointBuckets[i].get(0);
			while(p>=0){

				faceOrder.add( p );

				// find the next coordinate that is in this set that we haven't used yet
				int newP = -1;
				for( int k=0; k<faceNet.get(p).linkCount; k++ ){
					int neighbor = faceNet.get(p).links[k];
					if( !faceOrder.contains(neighbor) && pointBuckets[i].contains(neighbor) ){
						newP = neighbor;
						break;
					}
				}
				p = newP;

			}

			// turn the coordinates into a polygon
			Outline region = new Outline();
			for( int f=0; f<faceOrder.length; f++ ){
				int face = faceOrder.get(f);
				region.addPoint( (float) dualPoints[face][0], (float) dualPoints[face][1] );
			}
			
			regions.add( region );

		}

	}
	
	/*
	public void generateAround( ArrayList<Attractor> attr, Outline inside ){
		
		double[][] points = new double[attr.size()][2];
		for(int i = 0; i < attr.size(); i++){
			points[i][0] = attr.get(i).pos.getX();
			points[i][1] = attr.get(i).pos.getY();
		}
		
		regions = new ArrayList();
		
		if( points.length < 1 ){
			edges = new Edge[0];
			
			return;
		}

		// build points array for qhull
		double qPoints[] = new double[ points.length*3 + 9 ];
		for(int i=0; i<points.length; i++){
			qPoints[i*3] = points[i][0];
			qPoints[i*3+1] = points[i][1];
			qPoints[i*3+2] = -(points[i][0]*points[i][0] + points[i][1]*points[i][1]); // standard half-squared eucledian distance
		}
		// 1
		qPoints[ qPoints.length-9 ] = -8000D;
		qPoints[ qPoints.length-8 ] = 0D;
		qPoints[ qPoints.length-7 ] = -64000000D;
		// 2
		qPoints[ qPoints.length-6 ] = 8000D;
		qPoints[ qPoints.length-5 ] = 8000D;
		qPoints[ qPoints.length-4 ] = -128000000D;
		// 3
		qPoints[ qPoints.length-3 ] = 8000D;
		qPoints[ qPoints.length-2 ] = -8000D;
		qPoints[ qPoints.length-1 ] = -128000000D;

		// prepare quickhull
		QuickHull3D quickHull = new QuickHull3D(qPoints);
		int[][] faces = quickHull.getFaces(QuickHull3D.POINT_RELATIVE + QuickHull3D.CLOCKWISE);
		
		int artifact = 0;

		// compute dual points
		dualPoints = new double[faces.length][2];
		for(int i = 0; i < faces.length; i++){

			// test if it's the artifact
			if( faces[i][0] >= points.length && faces[i][1] >= points.length && faces[i][2] >= points.length )
				artifact = i;

			double x0 = qPoints[faces[i][0]*3+0];
			double y0 = qPoints[faces[i][0]*3+1];
			double x1 = qPoints[faces[i][1]*3+0];
			double y1 = qPoints[faces[i][1]*3+1];
			double x2 = qPoints[faces[i][2]*3+0];
			double y2 = qPoints[faces[i][2]*3+1];

			double v1x = 2 * (x1-x0);
			double v1y = 2 * (y1-y0);
			double v1z = x0*x0 - x1*x1 + y0*y0 - y1*y1;

			double v2x = 2 * (x2-x0);
			double v2y = 2 * (y2-y0);
			double v2z = x0*x0 - x2*x2 + y0*y0 - y2*y2;

			double tmpx = v1y * v2z - v1z * v2y;
			double tmpy = v1z * v2x - v1x * v2z;
			double tmpz = v1x * v2y - v1y * v2x;

			dualPoints[i][0] = tmpx/tmpz;
			dualPoints[i][1] = tmpy/tmpz;
		}

		// create edge/point/face network
		edges = new Edge[1];
		int edgeCount = 0;
		LinkedArray faceNet = new LinkedArray(faces.length);
		IntArray[] pointBuckets = new IntArray[points.length];
		for(int i=0; i<points.length; i++)
			pointBuckets[i] = new IntArray();

		// discover edges
		for(int i = 0; i < faces.length; i++){

			// bin faces to the points they belong with
			for(int f=0; f<faces[i].length; f++)
				if(faces[i][f] < points.length)
					pointBuckets[ faces[i][f] ].add(i);

			for(int j = 0; j < i; j++){
				if( i!=artifact && j!=artifact && isEdgeShared(faces[i], faces[j]) ){

					faceNet.link(i, j);

					if( edges.length <= edgeCount ){
						Edge[] tmpedges = new Edge[edges.length*2];
						System.arraycopy(edges, 0, tmpedges, 0, edges.length);
						edges = tmpedges;
					}
					edges[edgeCount] = new Edge();
					edges[edgeCount].start = new Point2D.Double(dualPoints[i][0], dualPoints[i][1]);
					edges[edgeCount].end = new Point2D.Double(dualPoints[j][0], dualPoints[j][1]);
					edgeCount++;

				}
			}
		}

		// trim edges down
		Edge[] tmpedges = new Edge[edgeCount];
		//for(int i=0; i<tmpedges.length; i++)
		//	tmpedges[i] = edges[i];
		System.arraycopy(edges, 0, tmpedges, 0, tmpedges.length);
		edges = tmpedges;
		
		// calculate the neighbours of all edges
		
		for(int i = 0; i < edges.length; i++){
			for(int j = 0; j < edges.length; j++){
				if( i != j){
					Edge e1 = edges[i];
					Edge e2 = edges[j];
					
					if( e1.start.equals(e2.start) || e1.start.equals(e2.end)){
						e1.startNeighbours.add(e2);
					}
					
					if( e1.end.equals(e2.start) || e1.end.equals(e2.end)){
						e1.endNeighbours.add(e2);
					}
				}
				
				
			}
		}

		// calculate the region for each point
		//regions = new MPolygon[points.length];
		for(int i=0; i<points.length; i++){
			if( pointBuckets[i].length == 0) continue;
			
			IntArray faceOrder = new IntArray(pointBuckets[i].length);

			// add coords of the region in the order they touch, starting with the convenient first
			int p = pointBuckets[i].get(0);
			while(p>=0){

				faceOrder.add( p );

				// find the next coordinate that is in this set that we haven't used yet
				int newP = -1;
				for( int k=0; k<faceNet.get(p).linkCount; k++ ){
					int neighbor = faceNet.get(p).links[k];
					if( !faceOrder.contains(neighbor) && pointBuckets[i].contains(neighbor) ){
						newP = neighbor;
						break;
					}
				}
				p = newP;

			}

			// turn the coordinates into a polygon
			Outline region = new Outline();
			for( int f=0; f<faceOrder.length; f++ ){
				int face = faceOrder.get(f);
				region.addPoint( (float) dualPoints[face][0], (float) dualPoints[face][1] );
			}
			
			regions.add( region );

		}
		
	}*/
	
	// trim the list of dual points with the specified shape
	public void trimTriangulationWith( Outline shape, double trimDistance ){
		for(int i = 0; i < dualPoints.length; i++){
			
		}
	}

	public ArrayList<Outline> getRegions(){
		return regions;
	}

	public Edge[] getEdges(){
		return edges;
	}
	
	public ArrayList<Edge> getEdgeList(){
		ArrayList<Edge> edgeList = new ArrayList();
		
		for(int i = 0; i < edges.length; i++){
			edgeList.add(edges[i]);
		}
		
		return edgeList;
	}
	
	/**
	 * return a list of edge nodes
	 * @return
	 */
	/*
	public ArrayList<EdgeNode> getClippedEdgeNodes( Outline outline ){
		
		ArrayList<EdgeNode> nodes = new ArrayList();
		
		ArrayList<Edge> edges = clippedEdges;
		HashSet<EdgeNode> nodeSet = new HashSet();
		
		
		for(int i = 0; i < edges.size(); i++){
			Edge edge = edges.get(i);
			EdgeNode startNode = new EdgeNode(edge.start);
			startNode.neighbourEdges.add(edge);
			startNode.neighbourEdges.addAll(edge.startNeighbours);
			
			
			//startNode.neighbours = edge.startNeighbours;
			
			EdgeNode endNode = new EdgeNode(edge.end);
			endNode.neighbourEdges.add(edge);
			endNode.neighbourEdges.addAll(edge.endNeighbours);
			
			
			nodeSet.add(startNode);
			nodeSet.add(endNode);
		}
		
		// loop through the node set and put them in the nodes list
		
		Iterator<EdgeNode> i = nodeSet.iterator();
		while(i.hasNext()){
			nodes.add(i.next());
		}
		
		//System.out.println("Total nodes:" + nodes.size());
		
		clippedNodes = nodes;
		
		return nodes;
	}
	*/
	
	
	public ArrayList<Edge> getClippedEdges( Outline outline ){
		
		ArrayList<Edge> clippedEdges = new ArrayList();
		
		
	
		
		//ArrayList<EdgeNode> clippedNodes = getClippedEdgeNodes(outline);
		
		// draw the edges
		for(int i=0; i<edges.length; i++)
		{
			
			// check if an edge point is outside of the outline
			double startX = edges[i].start.getX();
			double startY = edges[i].start.getY();
			double endX = edges[i].end.getX();
			double endY = edges[i].end.getY();
			
			boolean startInside = outline.contains(edges[i].start);
			boolean endInside = outline.contains(edges[i].end);
			
			if( startInside || endInside){
				// if one of either points are inside, calculate possible intersections
				
				Point2D.Double intersection = edges[i].intersectPolygon(outline);
				if( intersection != null){
					// check which part of the edge is inside of the polygon
					
					if( startInside && !endInside ){
						// point 1 is inside
						
						endX = intersection.getX();
						endY = intersection.getY();
						
						// remove end neighbours
						edges[i].endNeighbours = new ArrayList();
						
						edges[i].end.x = endX;
						edges[i].end.y = endY;
						
						
						
					} else if( endInside && !startInside){
						
						startX = intersection.getX();
						startY = intersection.getY();
						
						// remove start neighbours
						edges[i].startNeighbours = new ArrayList();
						
						edges[i].start.x = startX;
						edges[i].start.y = startY;
					}
					
					
					
				}
				
				// add the edge to the list
				clippedEdges.add( edges[i] );
				
			}
			
			
			
			
		}
		
		this.clippedEdges = clippedEdges;
		
		return clippedEdges;
	}
	
	public ArrayList<Outline> getClippedRegions( Outline outline ){
		ArrayList<Outline> clippedRegions = new ArrayList();
		
		for(int i = 0; i < regions.size(); i++){
			boolean includeRegion = true;
			
			Outline region = regions.get(i);
			
			// clip the region
			Poly regionPoly = region.getPoly();
			
			Poly clippedPoly = regionPoly.intersection(outline.getPoly());
			if(!clippedPoly.isEmpty())
				clippedRegions.add(Outline.fromPoly(clippedPoly));
			
			// include region?
			//if( includeRegion )
			//	clippedRegions.add(region);
		}
		
		return clippedRegions;
	}
	
	/*
	public void linkNodesToEdges(){
		// link all nodes
		for(int i=0; i<clippedEdges.size(); i++){
			
			Edge e = clippedEdges.get(i);
			
			for( int j = 0; j < clippedNodes.size(); j++){
				EdgeNode node = clippedNodes.get(j);
				
				if( node.pos.equals(e.start)){
					// the node is at the starting position
					e.startNode = node;
				}
				
				if( node.pos.equals(e.end)){
					// the node is at the starting position
					e.endNode = node;
				}
			}
		}
		
	}*/

	protected boolean isEdgeShared(int face1[], int face2[]){
		for(int i = 0; i < face1.length; i++){
			int cur = face1[i];
			int next = face1[(i + 1) % face1.length];
			for(int j = 0; j < face2.length; j++){
				int from = face2[j];
				int to = face2[(j + 1) % face2.length];
				if(cur == from && next == to || cur == to && next == from)
					return true;
			}
		}
		return false;
	}

}
