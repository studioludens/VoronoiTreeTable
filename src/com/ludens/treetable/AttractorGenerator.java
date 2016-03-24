package com.ludens.treetable;
import java.awt.Rectangle;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import megamu.mesh.Attractor;
import megamu.mesh.Edge;
import megamu.mesh.Outline;
import megamu.mesh.Polygon2D;

import za.co.luma.geom.Vector2DDouble;
import za.co.luma.math.function.Falloff;
import za.co.luma.math.function.PerlinFunction2D;
import za.co.luma.math.function.RealFunction2DWrapper;
import za.co.luma.math.sampling.PoissonDiskMultiSampler;
import za.co.luma.math.sampling.PoissonDiskSampler;
import za.co.luma.math.sampling.Sampler;
import za.co.luma.math.sampling.UniformPoissonDiskSampler;

/**
 * this class generates a list of attractors
 * @author rulkens
 *
 */
public class AttractorGenerator {

	public static ArrayList<Attractor> generate( Outline outline, int distance ){
		
		// get the outline bounds
		Rectangle bounds = outline.getBounds();
		
		Sampler<Vector2DDouble> sampler = new UniformPoissonDiskSampler(bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getMaxY(), distance);
		List<Vector2DDouble> pointList = sampler.sample();
		
		
		
		// loop through all the samples and only add ones that are inside of the outline
		Iterator<Vector2DDouble> i = pointList.iterator();
		
		// return value
		ArrayList<Attractor> ret = new ArrayList();
		
		while( i.hasNext()){
			Vector2DDouble item = i.next();
			
			Point2D.Double point = new Point2D.Double(item.getX(), item.getY() );
			
			if( outline.contains( point ) ){
				ret.add( new Attractor( point, 10) );
			}
			
		}
		
		return ret;
		
	}
	
	public static ArrayList<Attractor> generateInOut(Outline outside, ArrayList<Outline> insides, double distance, double edgeTrim ){
		
		// get the outline bounds
		Rectangle bounds = outside.getBounds();
		
		Sampler<Vector2DDouble> sampler = new UniformPoissonDiskSampler(bounds.getX(), bounds.getY(), bounds.getMaxX(), bounds.getMaxY(), distance);
		List<Vector2DDouble> pointList = sampler.sample();
		
		
		
		// loop through all the samples and only add ones that are inside of the outline
		Iterator<Vector2DDouble> i = pointList.iterator();
		
		// return value
		ArrayList<Attractor> ret = new ArrayList();
		
		while( i.hasNext()){
			Vector2DDouble item = i.next();
			
			Point2D.Double point = new Point2D.Double(item.getX(), item.getY() );
			
			
			
			
			
			double distanceOutside = outside.distanceTo(point);
			
			if( outside.contains( point ) && distanceOutside > edgeTrim){
				// at least it's inside the outside
				
				boolean doInclude = true;
				
				for( int j = 0; j < insides.size(); j++){
					
					Outline inside = insides.get(j);
					
					double distanceInside = inside.distanceTo(point);

					if(		inside.contains( point ) ||
							distanceInside <= edgeTrim ){
						doInclude = false;
					}
				}
				
				if( doInclude ) ret.add( new Attractor( point, 10) );
			}
			
			
			
			
			
			
			
		}
		
		// trim 
		
		return ret;
				
	}
}
