package org.egov.edcr.utility.math;

import java.awt.geom.Line2D;

public class Ray {

	
	
	    // this point is to determine where to start casting from.
	    // e.g. in a 2d grid where all values are positive, the origin (0,0) is a safe start.
	    // e.g. with lat/lon you should start outside the valid range for lat/lon to
	    // guarantee you do not start casting a ray from within the polygon!
	    private final Point rayStartPoint;

	    public Ray(final Point rayStartPoint) {
	        this.rayStartPoint = rayStartPoint;
	    }

	    public boolean contains(final Point point, final Polygon polygon) {
	        if (polygon.points.size() < 3) {
	            throw new IllegalStateException("Polygons must have at least 3 points.");
	        }
	        // first do a handy-dandy rectangle contains check so we don't waste time
	        // computing intersections on each line in the polygon
	        if (!contains(polygon.bounds, point)) { return false; }

	        int intersections = 0;
	        // walk all vertices of polygon
	        for (int i = 0; i < polygon.points.size() - 1; i++) {
	            if (isIntersects(point, polygon.points.get(i), polygon.points.get(i + 1))) {
	                intersections++;
	            }
	        }
	        // don't forget the tail-front end connection!
	        if (isIntersects(point,
	                         polygon.points.get(polygon.points.size() - 1),
	                         polygon.points.get(0))) {
	            intersections++;
	        }

	        return intersections % 2 != 0; // odd-in / even-out
	    }

	    // use already existing java libraries for line intersection
        private boolean isIntersects(final Point testPoint,
	                                 final Point p1,
	                                 final Point p2) {
	        return Line2D.linesIntersect(
	                rayStartPoint.getFX(), rayStartPoint.getFY(),
	                testPoint.getFX(), testPoint.getFY(),
	                p1.getFX(), p1.getFY(), p2.getFX(), p2.getFY()); 
	    }

	    // our cheap rectangle collision method.
	    private boolean contains(final Rectangle rectangle, final Point point) {
	        return point.getX() >= rectangle.getX() &&
	                point.getY() >= rectangle.getY() &&
	                point.getX() <= rectangle.getX() + rectangle.getD() &&
	                point.getY() <= rectangle.getY() + rectangle.getE();
	    }
}
