package org.egov.edcr.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.RayCast;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;
import org.springframework.stereotype.Service;

@Service
public class MinDistance {

    public static BigDecimal getYardMinDistance(PlanDetail pl, String name) {
        Util util = new Util();

        DXFLWPolyline plotBoundary = pl.getPlot().getPolyLine();
        // Util.print(plotBoundary,"Plot Boundary");
        DXFLWPolyline buildFoorPrint = pl.getBuilding().getPolyLine();
        // Util.print(buildFoorPrint,"buildFoorPrint");

        DXFLWPolyline yard = null;
        if (name.equals(DxfFileConstants.FRONT_YARD))
            yard = pl.getPlot().getFrontYard().getPolyLine();
        if (name.equals(DxfFileConstants.REAR_YARD))
            yard = pl.getPlot().getRearYard().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_1))
            yard = pl.getPlot().getSideYard1().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_2))
            yard = pl.getPlot().getSideYard2().getPolyLine();
        if (plotBoundary == null || buildFoorPrint == null || yard == null) {
            pl.getErrors().put("Set back calculation Error",
                    "Either" + DxfFileConstants.BUILDING_FOOT_PRINT + "," + DxfFileConstants.PLOT_BOUNDARY
                            + " or " + name + " is not found");
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);
        }
        Iterator vertexIterator = yard.getVertexIterator();
        // Util.print(yard,name);
        List<Point> yardOutSidePoints = new ArrayList<>();
        List<Point> yardInSidePoints = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
      //  int i = 0;
     //   int count = plotBoundary.getVertexCount();
        double[][] shape = pointsOfPolygon(plotBoundary);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // System.out.println("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            outside: while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();

                // System.out.println("plotBIterator :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    // System.out.println(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }

            if (RayCast.contains(shape, new double[] { point.getX(), point.getY() }) == true)
                if (!yardOutSidePoints.contains(point))
                    // System.out.println(name+" adding point on a plot Boundary line using
                    // raycast---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

            Iterator footPrintIterator = buildFoorPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside: while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // System.out.println("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    yardInSidePoints.add(point);
                    // System.out.println("Inside :"+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }

        List<Point> toremove = new ArrayList<>();

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        // System.out.println(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints)
            for (Point p1 : yardInSidePoints)
                if (util.pointsEquals(p1, p))
                    toremove.add(p);
        // System.out.println(name+" Outside Points-------------");
        for (Point p : toremove)
            yardOutSidePoints.remove(p);
        // System.out.println(name+" remove Points-------------"+p.getX()+",,,,"+p.getY());
        // System.out.println(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints) {

            // System.out.println(p.getX()+","+p.getY());
        }

        // System.out.println(name+" Inside Points-------------");

        for (Point p : yardInSidePoints) {
            // System.out.println(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = findPointsOnPolylines(yardOutSidePoints);
        // System.out.println(outsidePoints.size());
        List<Point> insidePoints = findPointsOnPolylines(yardInSidePoints);
        // System.out.println(insidePoints.size());

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not properly on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        for (Point in : insidePoints)
            // System.out.println("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // System.out.println("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                // System.out.println("Distance : "+distance);
                distanceList.add(distance);

            }

        // System.out.println(distanceList);
        java.util.Collections.sort(distanceList);
        // System.out.println("the shortest Distance is " + distanceList.get(0));
        if (distanceList.size() > 0)
            return BigDecimal.valueOf(distanceList.get(0)).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    RoundingMode.HALF_UP);
        else
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);

    }

    public static double[][] pointsOfPolygon(DXFLWPolyline plotBoundary) {
       if(plotBoundary==null)
       {
           return null;
       }
       int i=0;
       int count=plotBoundary.getVertexCount();
        double[][] shape = new double[count + 1][2];
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            shape[i][0] = point1.getX();
            shape[i][1] = point1.getY();

            // System.out.println(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }
        // shape[i] = shape[0];
        return shape;
    }

    private static List<Point> findPointsOnPolylines(List<Point> yardInSidePoints) {
        Point old = null;
        Point first = null;
        Point point1 = new Point();
        List<Point> myPoints = new ArrayList<>();

        for (Point in : yardInSidePoints) {
            if (old == null) {
                old = in;
                first = in;
                continue;
            }
            if (first.equals(in))
                continue;

            // System.out.println("Points for line "+old.getX()+","+old.getY() +" And"+ in.getX()+","+in.getY());
            double distance = MathUtils.distance(old, in);
            // System.out.println("Distance"+distance);

            for (double j = .01; j < distance; j = j + .01) {
                point1 = new Point();
                double t = j / distance;
                point1.setX((1 - t) * old.getX() + t * in.getX());
                point1.setY((1 - t) * old.getY() + t * in.getY());
                myPoints.add(point1);
                // System.out.println(point1.getX()+"---"+point1.getY());
            }

            old = in;
        }
        return myPoints;
    }

}
