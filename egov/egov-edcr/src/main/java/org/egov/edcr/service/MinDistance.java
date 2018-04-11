package org.egov.edcr.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;
import org.springframework.stereotype.Service;

@Service
public class MinDistance {
    private static Logger LOG = Logger.getLogger(MinDistance.class);

    public static BigDecimal getYardMinDistance(PlanDetail pl, String name) {
        Util util = new Util();

        DXFLWPolyline plotBoundary = pl.getPlot().getPolyLine();
        // Util.print(plotBoundary,"Plot Boundary");
        DXFLWPolyline buildFoorPrint = pl.getPlot().getBuildingFootPrint().getPolyLine();
        // Util.print(buildFoorPrint,"buildFoorPrint");

        DXFLWPolyline yard = null;
        if (name.equals(DxfFileConstants.FRONT_YARD))
            yard = pl.getPlot().getFrontYard().getPolyLine();
        if (name.equals(DxfFileConstants.REAR_YARD))
            yard = pl.getPlot().getRearYard().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_1))
            yard = pl.getPlot().getSideYard1().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_2)) {
            // if(LOG.isDebugEnabled()) LOG.debug("Starting side 2");
            yard = pl.getPlot().getSideYard2().getPolyLine();
            // if(LOG.isDebugEnabled()) LOG.debug("Starting side is closed"+yard.isClosed() +" plot"+plotBoundary.isClosed());
        }
        if (plotBoundary == null || buildFoorPrint == null || yard == null) {
            pl.getErrors().put("Set back calculation Error",
                    "Either " + DxfFileConstants.BUILDING_FOOT_PRINT + "," + DxfFileConstants.PLOT_BOUNDARY
                            + " or " + name + " is not found");
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);
        }
        Iterator vertexIterator = yard.getVertexIterator();
        // Util.print(yard,name);
        List<Point> yardOutSidePoints = new ArrayList<>();
        List<Point> yardInSidePoints = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        // int i = 0;
        // int count = plotBoundary.getVertexCount();
      //  double[][] shape = pointsOfPolygon(plotBoundary);
        List<Point> plotBoundaryEdges =Util.pointsOnPolygon(plotBoundary);
        // Util.print(plotBoundary,"plot");
        List<Point> pointsOnPlot = Util.findPointsOnPolylines(plotBoundaryEdges);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // if(LOG.isDebugEnabled()) LOG.debug("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            boolean pointAdded = false;

            outside: while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();

                // if(LOG.isDebugEnabled()) LOG.debug("plotBIterator :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    pointAdded = true;
                    yardOutSidePoints.add(point);

                    break outside;
                }

                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point with pointsEqualsWith2PercentError
                    // ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }
            /*
             * if(!pointAdded){ nextOutside: while (plotBIterator.hasNext()) { DXFVertex dxfVertex = (DXFVertex)
             * plotBIterator.next(); Point point1 = dxfVertex.getPoint(); //
             * if(LOG.isDebugEnabled()) LOG.debug("plotBIterator :"+point1.getX()+","+point1.getY()); if (util.pointsEqualsWith2PercentError(point1, point))
             * { // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
             * pointAdded=true; yardOutSidePoints.add(point); break nextOutside; } } }
             */

            Boolean added = false;
            if (pointsOnPlot.contains(point)) {
                yardOutSidePoints.add(point);
                added = true;
            }
            if (!added)
                for (Point p : pointsOnPlot) {
                    if (Util.pointsEquals(p, point)) {
                        yardOutSidePoints.add(point);
                        added = true;
                    }
                }

            if (!added) {
                /*
                 * if(name.equals(DxfFileConstants.SIDE_YARD_2)) { // if(LOG.isDebugEnabled()) LOG.debug("side yard 2 point"
                 * +point.getX()+","+point.getY()); }
                 */
                for (Point p : pointsOnPlot) {
                    /*
                     * if(name.equals(DxfFileConstants.SIDE_YARD_2)) { if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY()+"#"); }
                     */
                    if (Util.pointsEqualsWith2PercentError(p, point)) {

                        yardOutSidePoints.add(point);
                        added = true;
                    }
                }
            }
            // if(LOG.isDebugEnabled()) LOG.debug("completed outside "+name);

            /*
             * if (RayCast.contains(shape, new double[]{point.getX(), point.getY()}) == true) if
             * (!yardOutSidePoints.contains(point)) // if(LOG.isDebugEnabled()) LOG.debug(name+" adding point on a plot Boundary line using //
             * raycast---"+point.getX()+","+point.getY()); yardOutSidePoints.add(point);
             */

            Iterator footPrintIterator = buildFoorPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside: while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // if(LOG.isDebugEnabled()) LOG.debug("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    yardInSidePoints.add(point);
                    // if(LOG.isDebugEnabled()) LOG.debug("Inside :"+point.getX()+","+point.getY());
                    break inside;
                }
                // if(LOG.isDebugEnabled()) LOG.debug("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    yardInSidePoints.add(point);
                    // if(LOG.isDebugEnabled()) LOG.debug("Inside : with pointsEqualsWith2PercentError "+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }

        List<Point> toremove = new ArrayList<>();


        if(LOG.isDebugEnabled()) LOG.debug(name + " Outside Points-------------" + yardOutSidePoints.size() + " inside points " + yardInSidePoints.size());
        for (Point p : yardOutSidePoints)
            for (Point p1 : yardInSidePoints)
                if (util.pointsEquals(p1, p))
                    toremove.add(p);
        // if(LOG.isDebugEnabled()) LOG.debug(name+" Outside Points-------------");
        for (Point p : toremove)
            yardOutSidePoints.remove(p);
        // if(LOG.isDebugEnabled()) LOG.debug(name+" remove Points-------------"+p.getX()+",,,,"+p.getY());
        // if(LOG.isDebugEnabled()) LOG.debug(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints) {

            // if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY());
        }

        // if(LOG.isDebugEnabled()) LOG.debug(name+" Inside Points-------------");

        for (Point p : yardInSidePoints) {
            // if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = Util.findPointsOnPolylines(yardOutSidePoints);
        // if(LOG.isDebugEnabled()) LOG.debug(outsidePoints.size());
        List<Point> insidePoints = Util.findPointsOnPolylines(yardInSidePoints);
        // if(LOG.isDebugEnabled()) LOG.debug(insidePoints.size());

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation error for "+name,
                    "Points of " + name + " not properly on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        for (Point in : insidePoints)
            // if(LOG.isDebugEnabled()) LOG.debug("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // if(LOG.isDebugEnabled()) LOG.debug("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                // if(LOG.isDebugEnabled()) LOG.debug("Distance : "+distance);
                distanceList.add(distance);

            }

        // if(LOG.isDebugEnabled()) LOG.debug(distanceList);
        java.util.Collections.sort(distanceList);
        // if(LOG.isDebugEnabled()) LOG.debug("the shortest Distance is " + distanceList.get(0));
        if (distanceList.size() > 0)
            return BigDecimal.valueOf(distanceList.get(0)).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    RoundingMode.HALF_UP);
        else
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);

    }

    public static BigDecimal getBasementYardMinDistance(PlanDetail pl, String name) {
        Util util = new Util();

        DXFLWPolyline plotBoundary = pl.getPlot().getPolyLine();

        DXFLWPolyline bsmntFootPrint = pl.getBasement().getPolyLine();

        DXFLWPolyline yard = null;
        if (name.equals(DxfFileConstants.BSMNT_FRONT_YARD))
            yard = pl.getPlot().getBsmtFrontYard().getPolyLine();
        if (name.equals(DxfFileConstants.BSMNT_REAR_YARD))
            yard = pl.getPlot().getBsmtRearYard().getPolyLine();
        if (name.equals(DxfFileConstants.BSMNT_SIDE_YARD_1))
            yard = pl.getPlot().getBsmtSideYard1().getPolyLine();
        if (name.equals(DxfFileConstants.BSMNT_SIDE_YARD_2))
            yard = pl.getPlot().getBsmtSideYard2().getPolyLine();
        if (plotBoundary == null || bsmntFootPrint == null || yard == null) {
            pl.getErrors().put("Set back calculation Error",
                    "Either" + DxfFileConstants.BSMNT_FOOT_PRINT + "," + DxfFileConstants.PLOT_BOUNDARY
                            + " or " + name + " is not found");
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);
        }
        Iterator vertexIterator = yard.getVertexIterator();
        // Util.print(yard,name);
        List<Point> yardOutSidePoints = new ArrayList<>();
        List<Point> yardInSidePoints = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        int i = 0;
        int count = plotBoundary.getVertexCount();
       // Polygon boundaryPolygon = Util.getPolygon(plotBoundary);
        List<Point> plotBoundaryEdges = Util.pointsOnPolygon(plotBoundary);
        // Util.print(plotBoundary,"plot");
        List<Point> pointsOnPlot = Util.findPointsOnPolylines(plotBoundaryEdges);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // if(LOG.isDebugEnabled()) LOG.debug("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            boolean pointAdded = false;

            outside: while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();

                // if(LOG.isDebugEnabled()) LOG.debug("plotBIterator :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    pointAdded = true;
                    yardOutSidePoints.add(point);

                    break outside;
                }

                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point with pointsEqualsWith2PercentError
                    // ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }
            /*
             * if(!pointAdded){ nextOutside: while (plotBIterator.hasNext()) { DXFVertex dxfVertex = (DXFVertex)
             * plotBIterator.next(); Point point1 = dxfVertex.getPoint(); //
             * if(LOG.isDebugEnabled()) LOG.debug("plotBIterator :"+point1.getX()+","+point1.getY()); if (util.pointsEqualsWith2PercentError(point1, point))
             * { // if(LOG.isDebugEnabled()) LOG.debug(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
             * pointAdded=true; yardOutSidePoints.add(point); break nextOutside; } } }
             */

            Boolean added = false;
            if (pointsOnPlot.contains(point)) {
                yardOutSidePoints.add(point);
                added = true;
            }
            if (!added)
                for (Point p : pointsOnPlot) {
                    if (Util.pointsEquals(p, point)) {
                        yardOutSidePoints.add(point);
                        added = true;
                    }
                }

            if (!added) {
                /*
                 * if(name.equals(DxfFileConstants.SIDE_YARD_2)) { // if(LOG.isDebugEnabled()) LOG.debug("side yard 2 point"
                 * +point.getX()+","+point.getY()); }
                 */
                for (Point p : pointsOnPlot) {
                    /*
                     * if(name.equals(DxfFileConstants.SIDE_YARD_2)) { if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY()+"#"); }
                     */
                    if (Util.pointsEqualsWith2PercentError(p, point)) {

                        yardOutSidePoints.add(point);
                        added = true;
                    }
                }
            }
            // if(LOG.isDebugEnabled()) LOG.debug("completed outside "+name);

            /*
             * if (RayCast.contains(shape, new double[]{point.getX(), point.getY()}) == true) if
             * (!yardOutSidePoints.contains(point)) // if(LOG.isDebugEnabled()) LOG.debug(name+" adding point on a plot Boundary line using //
             * raycast---"+point.getX()+","+point.getY()); yardOutSidePoints.add(point);
             */

            Iterator footPrintIterator = bsmntFootPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside: while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // if(LOG.isDebugEnabled()) LOG.debug("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    yardInSidePoints.add(point);
                    // if(LOG.isDebugEnabled()) LOG.debug("Inside :"+point.getX()+","+point.getY());
                    break inside;
                }
                // if(LOG.isDebugEnabled()) LOG.debug("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    yardInSidePoints.add(point);
                    // if(LOG.isDebugEnabled()) LOG.debug("Inside : with pointsEqualsWith2PercentError "+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }

        List<Point> toremove = new ArrayList<>();

        // if(LOG.isDebugEnabled()) LOG.debug(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints)
            for (Point p1 : yardInSidePoints)
                if (util.pointsEquals(p1, p))
                    toremove.add(p);
        // if(LOG.isDebugEnabled()) LOG.debug(name+" Outside Points-------------");
        for (Point p : toremove)
            yardOutSidePoints.remove(p);
        // if(LOG.isDebugEnabled()) LOG.debug(name+" remove Points-------------"+p.getX()+",,,,"+p.getY());
        // if(LOG.isDebugEnabled()) LOG.debug(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints) {

            // if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY());
        }

        // if(LOG.isDebugEnabled()) LOG.debug(name+" Inside Points-------------");

        for (Point p : yardInSidePoints) {
            // if(LOG.isDebugEnabled()) LOG.debug(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = Util.findPointsOnPolylines(yardOutSidePoints);
        // if(LOG.isDebugEnabled()) LOG.debug(outsidePoints.size());
        List<Point> insidePoints = Util.findPointsOnPolylines(yardInSidePoints);
        // if(LOG.isDebugEnabled()) LOG.debug(insidePoints.size());

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation error for "+name,
                    "Points of " + name + " not properly on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        for (Point in : insidePoints)
            // if(LOG.isDebugEnabled()) LOG.debug("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // if(LOG.isDebugEnabled()) LOG.debug("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                // if(LOG.isDebugEnabled()) LOG.debug("Distance : "+distance);
                distanceList.add(distance);

            }

        // if(LOG.isDebugEnabled()) LOG.debug(distanceList);
        java.util.Collections.sort(distanceList);
        // if(LOG.isDebugEnabled()) LOG.debug("the shortest Distance is " + distanceList.get(0));
        if (distanceList.size() > 0)
            return BigDecimal.valueOf(distanceList.get(0)).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    RoundingMode.HALF_UP);
        else
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);

    }

    

  
   

    

   

}
