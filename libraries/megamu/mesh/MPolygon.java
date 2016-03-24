package megamu.mesh;

import java.awt.geom.Point2D;

import processing.core.*;

public class MPolygon {

	double[][] coords;
	int count;
	
	public MPolygon(){
		this(0);
	}

	public MPolygon(int points){
		coords = new double[points][2];
		count = 0;
	}

	public void add(double x, double y){
		coords[count][0] = x;
		coords[count++][1] = y;
	}
	
	public Point2D.Double get(int index){
		return new Point2D.Double(coords[index][0], coords[index][1]);
	}

	public void draw(PApplet p){
		draw(p.g);
	}

	public void draw(PGraphics g){
		g.beginShape();
		for(int i=0; i<count; i++){
			g.vertex((float)coords[i][0], (float)coords[i][1]);
		}
		g.endShape(PApplet.CLOSE);
	}

	public int count(){
		return count;
	}

	public double[][] getCoords(){
		return coords;
	}

}