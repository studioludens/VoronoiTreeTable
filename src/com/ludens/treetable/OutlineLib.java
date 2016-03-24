package com.ludens.treetable;
import javax.swing.JFileChooser;

import megamu.mesh.Outline;

/**
 * provides a library of outlines
 * @author rulkens
 *
 */

public class OutlineLib {
	

	public static Outline getOutline1(){
		
		Outline outline = new Outline();
		
		outline.addPoint( 131.227,732.496);
		outline.addPoint( 96.062,545.101);
		outline.addPoint( 33.467,470.115);
		outline.addPoint( 41.746,455.538);
		outline.addPoint( 161.901,233.886);
		outline.addPoint( 182.215,223.73);
		outline.addPoint( 292.227,241.29);
		outline.addPoint( 380.551,161.973);
		outline.addPoint( 529.419,71.981);
		outline.addPoint( 664.21,25.092);
		outline.addPoint( 725.284,38.727);
		outline.addPoint( 767.449,79.725);
		outline.addPoint( 881.245,165.345);
		outline.addPoint( 972.396,331.572);
		outline.addPoint( 942.562,561.176);
		outline.addPoint( 854.706,755.385);
		outline.addPoint( 833.144,759.924);
		outline.addPoint( 583.988,593.527);
		outline.addPoint( 536.932,571.562);
		outline.addPoint( 475.811,602.016);
		outline.addPoint( 219.41,764.998);
		outline.addPoint( 177.662,770.064 );
		
		return outline;
	}
	
	public static Outline getTable1(){
		
		Outline outline = new Outline();
		
		outline.addPoint( 37.473,312.817);
		outline.addPoint( 129.12,245.289);
		outline.addPoint( 225.589,232.425);
		outline.addPoint( 322.06,243.68);
		outline.addPoint( 399.235,219.563);
		outline.addPoint( 434.607,182.583);
		outline.addPoint( 560.019,177.759);
		outline.addPoint( 596.999,118.27);
		outline.addPoint( 669.352,74.858);
		outline.addPoint( 769.036,82.897);
		outline.addPoint( 960.369,192.229);
		outline.addPoint( 1119.545,245.289);
		outline.addPoint( 1220.839,287.092);
		outline.addPoint( 1270.681,343.366);
		outline.addPoint( 1264.25,420.542);
		outline.addPoint( 1166.172,491.286);
		outline.addPoint( 1095.428,571.678);
		outline.addPoint( 1050.408,666.541);
		outline.addPoint( 970.016,721.207);
		outline.addPoint( 854.252,713.167);
		outline.addPoint( 613.077,592.58);
		outline.addPoint( 518.215,562.031);
		outline.addPoint( 424.961,566.854);
		outline.addPoint( 330.099,610.267);
		outline.addPoint( 232.021,574.894);
		outline.addPoint( 154.845,471.993);
		outline.addPoint( 43.904,397.155);
		outline.addPoint( 24.61,349.797);
		
		return outline;
		
	}
	
	public static Outline getLargeSquare(){
		Outline outline = new Outline();
		
		outline.addPoint( 0, 0 );
		outline.addPoint( 600, 0 );
		outline.addPoint( 600, 600 );
		outline.addPoint( 0, 600 );
		
		return outline;
	}
	
	public static Outline getMiddleSquare(){
		Outline outline = new Outline();
		
		outline.addPoint( 200, 200 );
		outline.addPoint( 400, 200 );
		outline.addPoint( 400, 400 );
		outline.addPoint( 200, 400 );
		
		return outline;
	}
	
	public static Outline getSimpleTree(){
		
		Outline outline = new Outline();
		
		outline.addPoint( 279,527 );
		outline.addPoint( 221.765,531.865 );
		outline.addPoint( 283,515 );
		outline.addPoint( 262,451 );
		outline.addPoint( 194,461 );
		outline.addPoint( 239,438 );
		outline.addPoint( 201.867,416.105 );
		outline.addPoint( 171,468 );
		outline.addPoint( 188,411 );
		outline.addPoint( 135,408 );
		outline.addPoint( 130,378 );
		outline.addPoint( 150,395 );
		outline.addPoint( 195,346 );
		outline.addPoint( 166,394 );
		outline.addPoint( 225,398 );
		outline.addPoint( 258,421 );
		outline.addPoint( 325,335 );
		outline.addPoint( 357,267 );
		outline.addPoint( 333,184 );
		outline.addPoint( 292.5,188 );
		outline.addPoint( 279,238 );
		outline.addPoint( 282,179 );
		outline.addPoint( 170,277 );
		outline.addPoint( 266,169 );
		outline.addPoint( 259,153 );
		outline.addPoint( 203,192 );
		outline.addPoint( 88,175 );
		outline.addPoint( 117.376,170.402 );
		outline.addPoint( 202,182 );
		outline.addPoint( 203,157 );
		outline.addPoint( 170,118 );
		outline.addPoint( 181,112 );
		outline.addPoint( 216,155 );
		outline.addPoint( 242,132 );
		outline.addPoint( 211,100 );
		outline.addPoint( 264,140 );
		outline.addPoint( 275,100 );
		outline.addPoint( 292,177 );
		outline.addPoint( 363.825,165.872 );
		outline.addPoint( 434,155 );
		outline.addPoint( 497,112 );
		outline.addPoint( 443,170 );
		outline.addPoint( 347,187 );
		outline.addPoint( 358.138,226.248 );
		outline.addPoint( 368,261 );
		outline.addPoint( 356,308.5 );
		outline.addPoint( 394,308.5 );
		outline.addPoint( 415,336 );
		outline.addPoint( 384,314 );
		outline.addPoint( 343,328.488 );
		outline.addPoint( 323,369 );
		outline.addPoint( 354,390 );
		outline.addPoint( 378,369 );
		outline.addPoint( 360.364,402.617 );
		outline.addPoint( 314,379 );
		outline.addPoint( 273,431 );
		outline.addPoint( 298.5,505 );
		outline.addPoint( 303.53,520.478 );
		outline.addPoint( 349.033,556.978 );
		outline.addPoint( 298.5,525 );



		return outline;
	}
	
	public static Outline getSimpleConvex(){
		
		Outline outline = new Outline();
		
		outline.addPoint( 192,600 );
		outline.addPoint( 30,439 );
		outline.addPoint( 5,172 );
		outline.addPoint( 152,17 );
		outline.addPoint( 488,17 );
		outline.addPoint( 592,168 );
		outline.addPoint( 581,459 );
		outline.addPoint( 416,600 );

		return outline;
	}
	
	
	
}
