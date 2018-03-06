package org.egov.edcr.utility.math;


public class RayTest {

	
	public static void main(String[] args)
	{
		RayTest t=new RayTest();
		t.square();
		t.weirdShape();
	}
	 
	    // start ray casting from a fairly random start point to avoid vertex overlap
	    // for trivial test cases. vertex overlap is much less likely in real geo cases,
	    // and should occur infrequently enough to ignore.
	    private static final Ray RAY_CASTING = new Ray(
	                                    new Point(-1.123456789, -1.987654321));

	    public void square() {
	        final Polygon square = new Polygon(
	                new Point(1.0, 1.0),
	                new Point(2.0, 1.0),
	                new Point(2.0, 2.0),
	                new Point(1.0, 2.0));

	        // center of square
	        System.out.println(RAY_CASTING.contains(new Point(1.5, 1.5), square));

	        // other side of square
	        System.out.println(RAY_CASTING.contains(new Point(2.5, 1.5), square));
	    }

	    
	    public void weirdShape() {
	        // the ultimate shape, 凹
	        final Polygon 凹 = new Polygon(
	                new Point(1.0, 1.0),
	                new Point(4.0, 1.0),
	                new Point(4.0, 3.0),
	                new Point(3.0, 3.0),
	                new Point(3.0, 2.0),
	                new Point(2.0, 2.0),
	                new Point(2.0, 3.0),
	                new Point(1.0, 3.0));

	        // inside 凹
	        System.out.println(RAY_CASTING.contains(new Point(1.5, 1.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(1.5, 2.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(2.0, 1.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(2.5, 1.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(3.5, 1.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(3.5, 2.5), 凹));

	        // outside of 凹
	         System.out.println(RAY_CASTING.contains(new Point(1.5, 3.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(2.5, 2.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(3.5, 3.5), 凹));

	         System.out.println(RAY_CASTING.contains(new Point(4.5, 0.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(4.5, 1.0), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(4.5, 1.5), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(4.5, 2.0), 凹));
	         System.out.println(RAY_CASTING.contains(new Point(4.5, 2.5), 凹));
	    }

	}


