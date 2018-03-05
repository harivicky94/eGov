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
import org.egov.edcr.utility.math.RayCast;
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
        DXFLWPolyline buildFoorPrint = pl.getBuilding().getPolyLine();
       //  Util.print(buildFoorPrint,"buildFoorPrint");

        DXFLWPolyline yard = null;
        if (name.equals(DxfFileConstants.FRONT_YARD))
            yard = pl.getPlot().getFrontYard().getPolyLine();
        if (name.equals(DxfFileConstants.REAR_YARD))
            yard = pl.getPlot().getRearYard().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_1))
            yard = pl.getPlot().getSideYard1().getPolyLine();
        if (name.equals(DxfFileConstants.SIDE_YARD_2))
        {
           // LOG.info("Starting side 2");
            yard = pl.getPlot().getSideYard2().getPolyLine();
           // LOG.info("Starting side is closed"+yard.isClosed() +" plot"+plotBoundary.isClosed());
            }
        if (plotBoundary == null || buildFoorPrint == null || yard == null) {
            pl.getErrors().put("Set back calculation Error",
                    "Either" + DxfFileConstants.BUILDING_FOOT_PRINT + "," + DxfFileConstants.PLOT_BOUNDARY
                            + " or " + name + " is not found");
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);
        }
        Iterator vertexIterator = yard.getVertexIterator();
       //  Util.print(yard,name);
        List<Point> yardOutSidePoints = new ArrayList<>();
        List<Point> yardInSidePoints = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        //  int i = 0;
        //   int count = plotBoundary.getVertexCount();
        double[][] shape = pointsOfPolygon(plotBoundary);
        List<Point> plotBoundaryEdges = listOfPointsOfPolygon(plotBoundary);
       // Util.print(plotBoundary,"plot");
          List<Point> pointsOnPlot = findPointsOnPolylines(plotBoundaryEdges);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // LOG.info("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            boolean pointAdded=false;

            outside:
            while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();
         
                
                // LOG.info("plotBIterator :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    // LOG.info(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    pointAdded=true;
                    yardOutSidePoints.add(point);

                    break outside;   
                }
               
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    // LOG.info(name+" adding on points on a plot boundary Point with pointsEqualsWith2PercentError ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }
            /*if(!pointAdded){
            nextOutside:
                while (plotBIterator.hasNext()) {

                    DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                    Point point1 = dxfVertex.getPoint();
                    
                    // LOG.info("plotBIterator :"+point1.getX()+","+point1.getY());
                    if (util.pointsEqualsWith2PercentError(point1, point)) {
                        // LOG.info(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                        pointAdded=true;
                        yardOutSidePoints.add(point);

                        break nextOutside;
                    }
                   
                }
            }*/

            
            Boolean added=false;
            if(pointsOnPlot.contains(point))  
            {
                yardOutSidePoints.add(point);
                added=true;
            }  
            if(!added)
            for(Point p:pointsOnPlot)
            {
                if(Util.pointsEquals(p, point))
                {
                    yardOutSidePoints.add(point);
                    added=true;
                }
            }
            
            if(!added)
            {
               /* if(name.equals(DxfFileConstants.SIDE_YARD_2))
                {
                   // LOG.info("side yard 2 point" +point.getX()+","+point.getY());
                }   */ 
                for(Point p:pointsOnPlot)  
                {
                   /* if(name.equals(DxfFileConstants.SIDE_YARD_2))
                    {
                        LOG.info(p.getX()+","+p.getY()+"#");
                    }*/
                    if(Util.pointsEqualsWith2PercentError(p, point))
                    {
                        
                        yardOutSidePoints.add(point);
                        added=true;
                    }
                }
            }   
           // LOG.info("completed outside "+name);

           /* if (RayCast.contains(shape, new double[]{point.getX(), point.getY()}) == true)
                if (!yardOutSidePoints.contains(point))
                    // LOG.info(name+" adding point on a plot Boundary line using
                    // raycast---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);*/

            Iterator footPrintIterator = buildFoorPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside:
            while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // LOG.info("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    yardInSidePoints.add(point);
                    // LOG.info("Inside :"+point.getX()+","+point.getY());
                    break inside;
                }
                // LOG.info("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    yardInSidePoints.add(point);
                   // LOG.info("Inside : with pointsEqualsWith2PercentError "+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }

        List<Point> toremove = new ArrayList<>();

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not on " + DxfFileConstants.BUILDING_FOOT_PRINT);

         LOG.info(name+" Outside Points-------------"+yardOutSidePoints.size() +" inside points "+yardInSidePoints.size());
        for (Point p : yardOutSidePoints)
            for (Point p1 : yardInSidePoints)
                if (util.pointsEquals(p1, p))
                    toremove.add(p);
        // LOG.info(name+" Outside Points-------------");
        for (Point p : toremove)
            yardOutSidePoints.remove(p);
        // LOG.info(name+" remove Points-------------"+p.getX()+",,,,"+p.getY());
        // LOG.info(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints) {

            // LOG.info(p.getX()+","+p.getY());
        }

        // LOG.info(name+" Inside Points-------------");

        for (Point p : yardInSidePoints) {
            // LOG.info(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = findPointsOnPolylines(yardOutSidePoints);
        // LOG.info(outsidePoints.size());
        List<Point> insidePoints = findPointsOnPolylines(yardInSidePoints);
        // LOG.info(insidePoints.size());

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not properly on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        for (Point in : insidePoints)
            // LOG.info("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // LOG.info("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                // LOG.info("Distance : "+distance);
                distanceList.add(distance);

            }

        // LOG.info(distanceList);
        java.util.Collections.sort(distanceList);
        // LOG.info("the shortest Distance is " + distanceList.get(0));
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
        double[][] shape = pointsOfPolygon(plotBoundary);
        List<Point> plotBoundaryEdges = listOfPointsOfPolygon(plotBoundary);
        // Util.print(plotBoundary,"plot");
           List<Point> pointsOnPlot = findPointsOnPolylines(plotBoundaryEdges);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // LOG.info("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            boolean pointAdded=false;

            outside:
            while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();
         
                
                // LOG.info("plotBIterator :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    // LOG.info(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    pointAdded=true;
                    yardOutSidePoints.add(point);

                    break outside;   
                }
               
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    // LOG.info(name+" adding on points on a plot boundary Point with pointsEqualsWith2PercentError ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }
            /*if(!pointAdded){
            nextOutside:
                while (plotBIterator.hasNext()) {

                    DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                    Point point1 = dxfVertex.getPoint();
                    
                    // LOG.info("plotBIterator :"+point1.getX()+","+point1.getY());
                    if (util.pointsEqualsWith2PercentError(point1, point)) {
                        // LOG.info(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                        pointAdded=true;
                        yardOutSidePoints.add(point);

                        break nextOutside;
                    }
                   
                }
            }*/

            
            Boolean added=false;
            if(pointsOnPlot.contains(point))  
            {
                yardOutSidePoints.add(point);
                added=true;
            }  
            if(!added)
            for(Point p:pointsOnPlot)
            {
                if(Util.pointsEquals(p, point))
                {
                    yardOutSidePoints.add(point);
                    added=true;
                }
            }
            
            if(!added)
            {
               /* if(name.equals(DxfFileConstants.SIDE_YARD_2))
                {
                   // LOG.info("side yard 2 point" +point.getX()+","+point.getY());
                }   */ 
                for(Point p:pointsOnPlot)  
                {
                   /* if(name.equals(DxfFileConstants.SIDE_YARD_2))
                    {
                        LOG.info(p.getX()+","+p.getY()+"#");
                    }*/
                    if(Util.pointsEqualsWith2PercentError(p, point))
                    {
                        
                        yardOutSidePoints.add(point);
                        added=true;
                    }
                }
            }   
           // LOG.info("completed outside "+name);

           /* if (RayCast.contains(shape, new double[]{point.getX(), point.getY()}) == true)
                if (!yardOutSidePoints.contains(point))
                    // LOG.info(name+" adding point on a plot Boundary line using
                    // raycast---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);*/

            Iterator footPrintIterator = bsmntFootPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside:
            while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // LOG.info("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1, point)) {
                    yardInSidePoints.add(point);
                    // LOG.info("Inside :"+point.getX()+","+point.getY());
                    break inside;
                }
                // LOG.info("Foot Print :"+point1.getX()+","+point1.getY());
                if (util.pointsEqualsWith2PercentError(point1, point)) {
                    yardInSidePoints.add(point);
                   // LOG.info("Inside : with pointsEqualsWith2PercentError "+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }

        List<Point> toremove = new ArrayList<>();

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        // LOG.info(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints)
            for (Point p1 : yardInSidePoints)
                if (util.pointsEquals(p1, p))
                    toremove.add(p);
        // LOG.info(name+" Outside Points-------------");
        for (Point p : toremove)
            yardOutSidePoints.remove(p);
        // LOG.info(name+" remove Points-------------"+p.getX()+",,,,"+p.getY());
        // LOG.info(name+" Outside Points-------------");
        for (Point p : yardOutSidePoints) {

            // LOG.info(p.getX()+","+p.getY());
        }

        // LOG.info(name+" Inside Points-------------");

        for (Point p : yardInSidePoints) {
            // LOG.info(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = findPointsOnPolylines(yardOutSidePoints);
        // LOG.info(outsidePoints.size());
        List<Point> insidePoints = findPointsOnPolylines(yardInSidePoints);
        // LOG.info(insidePoints.size());

        if (yardInSidePoints.isEmpty())
            pl.getErrors().put("Set back calculation Error",
                    "Points of " + name + " not properly on " + DxfFileConstants.BUILDING_FOOT_PRINT);

        for (Point in : insidePoints)
            // LOG.info("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // LOG.info("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                // LOG.info("Distance : "+distance);
                distanceList.add(distance);

            }

        // LOG.info(distanceList);
        java.util.Collections.sort(distanceList);
        // LOG.info("the shortest Distance is " + distanceList.get(0));
        if (distanceList.size() > 0)
            return BigDecimal.valueOf(distanceList.get(0)).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    RoundingMode.HALF_UP);
        else
            return BigDecimal.ZERO.setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS);

    }

    public static double[][] pointsOfPolygon(DXFLWPolyline plotBoundary) {
        if (plotBoundary == null) {
            return null;
        }
        int i = 0;
        int count = plotBoundary.getVertexCount();
        double[][] shape = new double[count + 1][2];
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            shape[i][0] = point1.getX();
            shape[i][1] = point1.getY();

            // LOG.info(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }
         shape[i] = shape[0];
        return shape;
    }
    
    public static List<Point> listOfPointsOfPolygon(DXFLWPolyline plotBoundary) {
        if (plotBoundary == null) {
            return null;
        }
        int i = 0;
        int count = plotBoundary.getVertexCount();
        List<Point> points=new ArrayList<>();
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            points.add(point1);

            // LOG.info(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }
        
        points.add(points.get(0));
        return points;
    }
    
    public static double[][] pointsOfPolygonWith(DXFLWPolyline plotBoundary) {
        if (plotBoundary == null) {
            return null;
        }
        int i = 0;
        int count = plotBoundary.getVertexCount();
        double[][] shape = new double[count + 1][2];
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            shape[i][0] = point1.getX();
            shape[i][1] = point1.getY();

            // LOG.info(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }
        // shape[i] = shape[0];
        return shape;
    }

    public static List<Point> findPointsOnPolylines(List<Point> yardInSidePoints) {
        Point old = null;
        Point first = null;
        Point point1 = new Point();
        List<Point> myPoints = new ArrayList<>();

        for (Point in : yardInSidePoints) {
            
           // LOG.info(" IN: "+ in.getX()+","+in.getY());
            if (old == null) {
                old = in;
                first = in;
                continue;
            }
            //commented to fix yard min in sample_17.dxf
            /*if (first.equals(in))
                continue;*/

           //  LOG.info("Points for line ------"+old.getX()+","+old.getY() +" And "+ in.getX()+","+in.getY());
            double distance = MathUtils.distance(old, in);
            
            // LOG.info("Distance"+distance);

            for (double j = .01; j < distance; j = j + .01) {
                point1 = new Point();
                double t = j / distance;
                point1.setX((1 - t) * old.getX() + t * in.getX());
                point1.setY((1 - t) * old.getY() + t * in.getY());
                myPoints.add(point1);
                // LOG.info(point1.getX()+"---"+point1.getY());
            }

            old = in;
        }
        return myPoints;
    }

}
