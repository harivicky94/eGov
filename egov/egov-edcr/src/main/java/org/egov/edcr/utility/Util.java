package org.egov.edcr.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.Plot;
import org.egov.edcr.utility.math.RayCast;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFText;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;

public class Util {
    private static String FLOOR_NAME_PREFIX = "FLOOR_";
    private static final int DECIMALDIGITS = 10;

    public List<DXFLWPolyline> getPolyLinesByColor(DXFDocument dxfDocument, Integer colorCode) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities) {
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    if (colorCode == dxflwPolyline.getColor()) {
                        dxflwPolylines.add(dxflwPolyline);
                    }
                }
            }
        }

        return dxflwPolylines;
    }

    public List<DXFLine> getLinesByColor(DXFDocument dxfDocument, Integer color) {

        List<DXFLine> lines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);

            if (null != dxfPolyLineEntities) {
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (color == line.getColor()) {
                        lines.add(line);
                    }

                }
            }
        }

        return lines;
    }

    public List<DXFLine> getLinesByLayer(DXFDocument dxfDocument, String name) {

        List<DXFLine> lines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);

            if (null != dxfPolyLineEntities) {
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (name == line.getLayerName()) {
                        lines.add(line);
                    }

                }
            }
        }

        return lines;
    }

    public static DXFLine getSingleLineByLayer(DXFDocument dxfDocument, String name) {

        if(dxfDocument==null)
            return null;
        if(name==null)
            return null;

        List<DXFLine> lines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);

            if (null != dxfLineEntities) {
                for (Object dxfEntity : dxfLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (name.equalsIgnoreCase(line.getLayerName())) {
                        lines.add(line);
                    }

                }
            }
        }
        if (lines.size() == 1) {
            return lines.get(0);
        } else
            return null;

    }

    public List<DXFLWPolyline> getPolyLinesByColors(DXFDocument dxfDocument, List<Integer> colorCodes) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities) {
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    for (int colorCode : colorCodes) {
                        if (colorCode == dxflwPolyline.getColor()) {
                            dxflwPolylines.add(dxflwPolyline);
                        }
                    }
                }
            }
        }

        return dxflwPolylines;
    }

    public static List<DXFLWPolyline> getPolyLinesByLayer(DXFDocument dxfDocument, String name) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities) {
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    if (name.equalsIgnoreCase(dxflwPolyline.getLayerName())) {
                        dxflwPolylines.add(dxflwPolyline);

                    }
                }
            }
        }

        return dxflwPolylines;
    }

    public static BigDecimal getPolyLineArea(DXFPolyline dxfPolyline) {

        ArrayList x = new ArrayList();
        ArrayList y = new ArrayList();

        Iterator vertexIterator = dxfPolyline.getVertexIterator();

        // Vertex and coordinates of Polyline
        while (vertexIterator.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
            Point point = dxfVertex.getPoint();

            // values needed to calculate area
            x.add(point.getX());
            y.add(point.getY());

        }

        return polygonArea(x, y, dxfPolyline.getVertexCount());
    }

    // Using ShoeLace Formula to calculate area of polygon
    private static BigDecimal polygonArea(ArrayList<Double> x, ArrayList<Double> y, int numPoints) {

        double area = 0; // Accumulates area in the loop
        int j = numPoints - 1; // The last vertex is the 'previous' one to the
        // first

        for (int i = 0; i < numPoints; i++) {
            area = area + (x.get(j) + x.get(i)) * (y.get(j) - y.get(i));
            j = i; // j is previous vertex to i
        }

        BigDecimal convertedArea = new BigDecimal(area / 2);

        return convertedArea.setScale(4, RoundingMode.HALF_UP).abs();

    }

    public static	String	getMtextByLayerName(DXFDocument doc, String layerName)
    {
        DXFLayer planInfoLayer = doc.getDXFLayer(layerName);
        List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
        String param="";
        DXFText	text=null;
        Iterator iterator = texts.iterator();

        while(iterator.hasNext())
        {
            text= (DXFText)iterator.next();
            if(text!=null && text.getText()!=null ){
                param=text.getText();
                /*if(new Float(param).isNaN())	
				{
					throw  new RuntimeException("Texts in the layer" + layerName +"Does not follow standard ");
				}*/

                param=param.replace("VOLTS", "").trim();
            }
        }
        return param;
    }

    public static	Map<String,String>	getPlanInfoProperties(DXFDocument doc)
    {

        DXFLayer planInfoLayer = doc.getDXFLayer("Plan info");
        List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
        String param="";
        DXFText	text=null;
        Map<String,String> planInfoProperties=new HashMap<>();
        Iterator iterator = texts.iterator();
        String[] split;
        String s="\\";
        while(iterator.hasNext())
        {
            text= (DXFText)iterator.next();

            param=text.getText();
            param=param.replace(s, "#");
            //	System.out.println(param);
            if(param.contains("#P"))
            {
                //	System.out.println("inside");
                split = param.split("#P");
            }
            else
            {
                split=new String [1];
                split[0]=param;
            }

            for (int j=0;j<split.length;j++)
            {

                String[] data = split[j].split("=");
                if(data.length==2)
                {
                    //	System.out.println(data[0]+"---"+data[1]);
                    planInfoProperties.put(data[0], data[1]);
                }else
                {
                    //throw new RuntimeException("Plan info sheet data not following standard '=' for " +param);
                }
            }
        }
        return planInfoProperties;

    }

    protected static int getTotalFloorCount(DXFDocument dxfDocument, Integer colorCode) {

        int i = 0;
        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();
        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            if ((colorCode != null && dxfLayer.getColor() == colorCode)
                    || dxfLayer.getName().startsWith(FLOOR_NAME_PREFIX)) {
                i++;
            }

        }

        return i;
    }

    protected static int getFloorCountExcludingCeller(DXFDocument dxfDocument, Integer colorCode) {
        int i = 0;
        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();
        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            if ((colorCode != null && dxfLayer.getColor() == colorCode)
                    || dxfLayer.getName().startsWith(FLOOR_NAME_PREFIX)) {
                try {

                    if (colorCode != null && dxfLayer.getColor() == colorCode) {
                        i++;
                    } else {
                        String[] floorName = dxfLayer.getName().split(FLOOR_NAME_PREFIX);
                        if (floorName.length > 0 && floorName[1] != null && Integer.parseInt(floorName[1]) >= 0) {
                            i++;
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // throw new RuntimeException("Floor number not in format");
                    // //TODO: HANDLE THIS LATER
                }

            }

        }

        return i;
    }

    public boolean pointsEquals(Point point1, Point point) {
        BigDecimal px=BigDecimal.valueOf(point.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal py=BigDecimal.valueOf(point.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1x=BigDecimal.valueOf(point1.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1y=BigDecimal.valueOf(point1.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        if(px.compareTo(p1x)==0 && py.compareTo(p1y)==0)
            return true;
        else 
            return false;
    }


    private static Double getYardMinDistance(DXFDocument doc, String name) {
        Util util = new Util();
        List<DXFLWPolyline> polyLinesByLayer = util.getPolyLinesByLayer(doc, DcrConstants.PLOT_BOUNDARY);
        DXFLWPolyline plotBoundary = polyLinesByLayer.get(0);

        List<DXFLWPolyline> polyLinesByLayer1 = util.getPolyLinesByLayer(doc, DcrConstants.BUILDING_FOOT_PRINT);
        DXFLWPolyline buildFoorPrint = polyLinesByLayer1.get(0);
        // DXFLWPolyline buildFoorPrint1 = polyLinesByLayer1.get(1);
        int rows = buildFoorPrint.getRows();

        List<DXFLWPolyline> polyLinesByLayer2 = util.getPolyLinesByLayer(doc, name);
        DXFLWPolyline yard = polyLinesByLayer2.get(0);
       

        Iterator vertexIterator = yard.getVertexIterator();
        List<Point> yardOutSidePoints = new ArrayList<>();
        List<Point> yardInSidePoints = new ArrayList<>();
        List<Double> distanceList = new ArrayList<>();
        int i = 0;
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        int count=      plotBoundary.getVertexCount();
        double[][] shape = pointsOnPolygon(i, plotBoundary, count);

        while (vertexIterator.hasNext()) {
            DXFVertex next = (DXFVertex) vertexIterator.next();
            Point point = next.getPoint();
            // System.out.println("yard Point :"+point.getX()+","+point.getY());

            Iterator plotBIterator = plotBoundary.getVertexIterator();

            // Vertex and coordinates of Polyline
            outside: while (plotBIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) plotBIterator.next();
                Point point1 = dxfVertex.getPoint();

                // System.out.println("Outside                           :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1,point)) {
                    //System.out.println(name+" adding on points on a plot boundary Point ---"+point.getX()+","+point.getY());
                    yardOutSidePoints.add(point);

                    break outside;
                }
            }
          
           

                if (RayCast.contains(shape, new double[] { point.getX(), point.getY() }) == true) {

                    // System.out.println(yardOutSidePoints+"---"+!yardOutSidePoints.contains(point));

                    if (!yardOutSidePoints.contains(point)) {
                        //System.out.println(name+" adding point on a   plot Boundary line using raycast---"+point.getX()+","+point.getY());
                        yardOutSidePoints.add(point);
                    }
                }
            

            Iterator footPrintIterator = buildFoorPrint.getVertexIterator();

            // Vertex and coordinates of Polyline
            inside: while (footPrintIterator.hasNext()) {

                DXFVertex dxfVertex = (DXFVertex) footPrintIterator.next();
                Point point1 = dxfVertex.getPoint();
                // System.out.println("Foot Print  :"+point1.getX()+","+point1.getY());
                if (util.pointsEquals(point1,point)) {
                    yardInSidePoints.add(point);
                    // System.out.println("Inside    :"+point.getX()+","+point.getY());
                    break inside;
                }
            }

        }
      
        List<Point> toremove=new ArrayList<>();

        //System.out.println(name+"   Outside Points-------------");
        for(Point p:yardOutSidePoints)
        {
            for(Point p1:yardInSidePoints)
            {
                if(util.pointsEquals(p1,p))
                {
                    toremove.add(p);
                }
            }
            //System.out.println(p.getX()+","+p.getY());
        }
        //System.out.println(name+"   Outside Points-------------");
        for(Point p:toremove)
        {
            yardOutSidePoints.remove(p);
            //System.out.println(name+"   remove Points-------------"+p.getX()+",,,,"+p.getY());    
        }


        for(Point p:yardOutSidePoints)
        {

            //System.out.println(p.getX()+","+p.getY());
        }

        //System.out.println(name+"   Inside Points-------------");

        for(Point p:yardInSidePoints)
        {
            //System.out.println(p.getX()+","+p.getY());
        }

        List<Point> outsidePoints = findPointsOnPolylines(yardOutSidePoints);
        //System.out.println(outsidePoints.size());
        List<Point> insidePoints = findPointsOnPolylines(yardInSidePoints);
        //System.out.println(insidePoints.size());

        for (Point in : insidePoints) {
            //System.out.println("Inside : "+in.getX()+","+in.getY());
            for (Point out : outsidePoints) {
                // System.out.println("Outside : "+out.getX()+","+out.getY());
                double distance = MathUtils.distance(in, out);
                //       System.out.println("Distance : "+distance);
                distanceList.add(distance);

            }
        }

        //System.out.println(distanceList);
        java.util.Collections.sort(distanceList);
        //System.out.println("the shortest Distance is " + distanceList.get(0));
        if (distanceList.size()>0)
            return distanceList.get(0);
        else return 0.0;

    }

    private static double[][] pointsOnPolygon(int i, DXFLWPolyline plotBoundary, int count) {
        double[][] shape = new double[count+1][2];
        Iterator    plotBIterator1   =  plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            shape[i][0] = point1.getX();
            shape[i][1] = point1.getY();

            //System.out.println(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }
        shape[i]=shape[0];
        return shape;
    }
    private static List<Point> findPointsOnPolylines(List<Point> yardInSidePoints) {
        Point old=null;
        Point first=null;
        Point point1=new Point();
        List<Point> myPoints=new ArrayList<>();

        for (Point in : yardInSidePoints) {
            {
                if(old==null)
                {
                    old=in;
                    first=in;
                    continue;
                }
                if(first.equals(in))
                {
                    continue;
                }

                //System.out.println("Points for line "+old.getX()+","+old.getY() +" And"+ in.getX()+","+in.getY());
                double distance = MathUtils.distance(old, in);
                //System.out.println("Distance"+distance);

                for(double j=.01;j<distance;j=j+.01)
                {
                    point1=new Point();
                    double t=j/distance;
                    point1.setX((1-t)*old.getX()+t*in.getX());
                    point1.setY((1-t)*old.getY()+t*in.getY());
                    myPoints.add(point1);
                    //System.out.println(point1.getX()+"---"+point1.getY());
                }


                old=in;
            }

        }
        return myPoints;
    }

}
