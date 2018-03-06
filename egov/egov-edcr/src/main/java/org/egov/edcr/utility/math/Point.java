package org.egov.edcr.utility.math;


	public class Point {
	    private final double x;
	    private final double y;

	    public Point(final double x, final double y) {
	        this.x = x;
	        this.y = y;
	    }

	    public double getX() {
	        return x;
	    }

	    public double getY() {
	        return y;
	    }
	    
	    public float getFX() {
	        return Double.valueOf(x).floatValue();
	    }
	    
	    public float getFY() {
	        return Double.valueOf(y).floatValue();
	    }

	}


