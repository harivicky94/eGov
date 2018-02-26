package org.egov.edcr.utility;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.ReportOutput;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDimensionStyle;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFMText;
import org.kabeja.dxf.DXFPolyline;
import org.kabeja.dxf.DXFText;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.math.MathUtils;

public class Util {
    private static String FLOOR_NAME_PREFIX = "FLOOR_";
    private static final int DECIMALDIGITS = 10;
    private static Logger LOG = Logger.getLogger(Util.class);

    public List<DXFLWPolyline> getPolyLinesByColor(DXFDocument dxfDocument, Integer colorCode) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    if (colorCode == dxflwPolyline.getColor())
                        dxflwPolylines.add(dxflwPolyline);
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

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (color == line.getColor())
                        lines.add(line);

                }
        }

        return lines;
    }

    public static List<DXFLine> getLinesByLayer(DXFDocument dxfDocument, String name) {
        List<DXFLine> lines = new ArrayList<>();
        if (name == null)
            return lines;
        name = name.toUpperCase();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (name.contains(line.getLayerName().toUpperCase()))
                        lines.add(line);

                }
        }

        return lines;
    }

    public static DXFLine getSingleLineByLayer(DXFDocument dxfDocument, String name) {

        if (name == null)
            return null;
        if (dxfDocument == null)
            return null;
        if (name == null)
            return null;

        name = name.toUpperCase();

        List<DXFLine> lines = new ArrayList<>();

        List<DXFDimension> dimensions = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LINE);

            if (null != dxfLineEntities)
                for (Object dxfEntity : dxfLineEntities) {

                    DXFLine line = (DXFLine) dxfEntity;

                    if (name.contains(line.getLayerName().toUpperCase()))
                        lines.add(line);

                }

        }
        if (lines.size() == 1)
            return lines.get(0);
        else
            return null;

    }

    public static DXFDimension getSingleDimensionByLayer(DXFDocument dxfDocument, String name) {

        if (dxfDocument == null)
            return null;
        if (name == null)
            return null;
        name = name.toUpperCase();

        List<DXFDimension> dimensions = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_DIMENSION);

            if (null != dxfLineEntities)
                for (Object dxfEntity : dxfLineEntities) {

                    DXFDimension line = (DXFDimension) dxfEntity;
                    String dimensionBlock = line.getDimensionBlock();
                    DXFBlock dxfBlock = dxfDocument.getDXFBlock(dimensionBlock);
                    LOG.info("BLOCK data" + dxfBlock.getDescription());
                    DXFDimensionStyle dxfDimensionStyle = dxfDocument.getDXFDimensionStyle(line.getDimensionStyleID());
                    LOG.info("---" + dxfDimensionStyle.getProperty(DXFDimensionStyle.PROPERTY_DIMEXO));
                    // LOG.info(line.getInclinationHelpLine()+"HELP LINE"+line.getDimensionText()
                    // +"--"+line.getLayerName()+"--"+line.getDimensionArea());

                    if (name.contains(line.getLayerName().toUpperCase()))
                        dimensions.add(line);

                }
        }
        if (dimensions.size() == 1)
            return dimensions.get(0);
        else
            return null;

    }

    public static BigDecimal getSingleDimensionValueByLayer(DXFDocument dxfDocument, String name, PlanDetail pl) {

        if (dxfDocument == null)
            return null;
        if (name == null)
            return null;
        name = name.toUpperCase();
        BigDecimal value = BigDecimal.ZERO;

        DXFLayer dxfLayer = (DXFLayer) dxfDocument.getDXFLayer(name);
        if (dxfLayer == null) {
            pl.addError(name, name + " layer not defined");
        }

        List dxfLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_DIMENSION);

        if (null != dxfLineEntities)
            for (Object dxfEntity : dxfLineEntities) {

                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                // String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = dxfDocument.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();
                        text2 = text2.replaceAll("[^\\d.]", "");
                        ;
                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                        }

                    }
                }

            }
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            pl.addError(name, "Dimension value is invalid for layer " + name);
        }
        return value;

    }

    public List<DXFLWPolyline> getPolyLinesByColors(DXFDocument dxfDocument, List<Integer> colorCodes) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    for (int colorCode : colorCodes)
                        if (colorCode == dxflwPolyline.getColor())
                            dxflwPolylines.add(dxflwPolyline);
                }
        }

        return dxflwPolylines;
    }
    
    public  static List<DXFLWPolyline> getPolyLinesByLayerAndColor(DXFDocument dxfDocument,String layerName,int colorCode,PlanDetail pl) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

       

            DXFLayer dxfLayer = (DXFLayer) dxfDocument.getDXFLayer(layerName);

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    
                      //  if (colorCode == dxflwPolyline.getColor())
                            dxflwPolylines.add(dxflwPolyline);
                }
       

        return dxflwPolylines;
    }
    

    public static List<DXFLWPolyline> getPolyLinesByLayer(DXFDocument dxfDocument, String name) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();
        if (name == null)
            return dxflwPolylines;

        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();

        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

            if (null != dxfPolyLineEntities)
                for (Object dxfEntity : dxfPolyLineEntities) {

                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                    if (name.contains(dxflwPolyline.getLayerName().toUpperCase()))
                        dxflwPolylines.add(dxflwPolyline);
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

    public static String getMtextByLayerName(DXFDocument doc, String name) {
        if (name == null)
            return null;
        String param = null;
        name = name.toUpperCase();
        String[] split = name.split(",");
        for (String layerName : split) {

            Boolean found = false;
            Iterator dxfLayerIterator = doc.getDXFLayerIterator();
            while (dxfLayerIterator.hasNext()) {
                DXFLayer next = (DXFLayer) dxfLayerIterator.next();
                DXFLayer planInfoLayer = doc.getDXFLayer(next.getName());
                // LOG.info("----------"+planInfoLayer.getName()+"---------------------------------------------------");
                if (planInfoLayer != null) {
                    List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
                    if (texts != null) {
                        Iterator iterator = texts.iterator();

                        while (iterator.hasNext()) {
                            DXFText text = (DXFText) iterator.next();
                            // LOG.info("Mtext :"+text.getText());
                        }
                    }

                }
                if (layerName.equals(next.getName().toUpperCase())) {
                    found = true;
                    layerName = next.getName();
                }
            }
            if (!found) {
                LOG.error("No Layer Found with name" + layerName);
            }

            DXFLayer planInfoLayer = doc.getDXFLayer(layerName);
            // LOG.info(planInfoLayer.getName());
            if (planInfoLayer != null) {
                List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);

                // LOG.info("Texts list is null ");
                DXFText text = null;
                if (texts != null) {
                    Iterator iterator = texts.iterator();

                    while (iterator.hasNext()) {
                        text = (DXFText) iterator.next();
                        // LOG.info("Mtext :"+text.getText());
                        if (text != null && text.getText() != null) {
                            param = text.getText();
                            /*
                             * if(new Float(param).isNaN()) { throw new RuntimeException("Texts in the layer" + layerName
                             * +"Does not follow standard "); }
                             */

                            param = param.replace("VOLTS", "").trim();
                        }
                    }
                }
            }
        }
        return param;
    }

    public static Map<String, String> getPlanInfoProperties(DXFDocument doc) {

        DXFLayer planInfoLayer = doc.getDXFLayer(DxfFileConstants.PLAN_INFO);
        List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
        String param = "";
        DXFText text = null;
        Map<String, String> planInfoProperties = new HashMap<>();
        
       if(texts!=null){
           
        Iterator iterator = texts.iterator();
        String[] split;
        String s = "\\";
        while (iterator.hasNext()) {
            text = (DXFText) iterator.next();

            param = text.getText();
            param = param.replace(s, "#");
            // System.out.println(param);
            if (param.contains("#P"))
                // System.out.println("inside");
                split = param.split("#P");
            else {
                split = new String[1];
                split[0] = param;
            }

            for (String element : split) {

                String[] data = element.split("=");
                if (data.length == 2)
                    // System.out.println(data[0]+"---"+data[1]);
                    planInfoProperties.put(data[0], data[1]);
                else {
                    // throw new RuntimeException("Plan info sheet data not following standard '=' for " +param);
                }
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

            if (colorCode != null && dxfLayer.getColor() == colorCode
                    || dxfLayer.getName().startsWith(FLOOR_NAME_PREFIX))
                i++;

        }

        return i;
    }

    protected static int getFloorCountExcludingCeller(DXFDocument dxfDocument, Integer colorCode) {
        int i = 0;
        Iterator dxfLayerIterator = dxfDocument.getDXFLayerIterator();
        while (dxfLayerIterator.hasNext()) {

            DXFLayer dxfLayer = (DXFLayer) dxfLayerIterator.next();

            if (colorCode != null && dxfLayer.getColor() == colorCode
                    || dxfLayer.getName().startsWith(FLOOR_NAME_PREFIX))
                try {

                    if (colorCode != null && dxfLayer.getColor() == colorCode)
                        i++;
                    else {
                        String[] floorName = dxfLayer.getName().split(FLOOR_NAME_PREFIX);
                        if (floorName.length > 0 && floorName[1] != null && Integer.parseInt(floorName[1]) >= 0)
                            i++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // throw new RuntimeException("Floor number not in format");
                    // //TODO: HANDLE THIS LATER
                }

        }

        return i;
    }

    public boolean pointsEquals(Point point1, Point point) {
        BigDecimal px = BigDecimal.valueOf(point.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal py = BigDecimal.valueOf(point.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1x = BigDecimal.valueOf(point1.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1y = BigDecimal.valueOf(point1.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        if (px.compareTo(p1x) == 0 && py.compareTo(p1y) == 0)
            return true;
        else
            return false;
    }

    private static double[][] pointsOnPolygon(int i, DXFLWPolyline plotBoundary, int count) {
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
        shape[i] = shape[0];
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

    public static void print(DXFLWPolyline yard, String name) {
        if (yard != null) {
            Iterator vertexIterator = yard.getVertexIterator();
            LOG.info("Points on the " + name);
            while (vertexIterator.hasNext()) {
                DXFVertex next = (DXFVertex) vertexIterator.next();
                LOG.info(next.getPoint().getX() + "," + next.getPoint().getY());
                LOG.info(next.getX() + "," + next.getY());
            }
        }

    }

    public static void print(HashMap<String, String> errors) {
        LOG.info(errors.getClass().getName());
        Iterator<Entry<String, String>> iterator = errors.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> next = iterator.next();
            LOG.info(next.getKey() + "---" + next.getValue());
        }
    }

    public static void print(ReportOutput ro) {
        try {
            LOG.info("ReportOutput");
            if (ro.getRuleOutPuts() != null)
                for (RuleOutput rp : ro.getRuleOutPuts()) {
                    LOG.info(rp.key + " -- " + rp.getMessage() + " -- " + rp.getResult());
                    if (rp.getSubRuleOutputs() != null)
                        for (SubRuleOutput so : rp.getSubRuleOutputs()) {
                            LOG.info(so.key + " , " + so.message + " , " + so.ruleDescription);
                            List<RuleReportOutput> ruleReportOutputs = so.ruleReportOutputs;
                            if (ruleReportOutputs != null)
                                for (RuleReportOutput rro : ruleReportOutputs) {
                                    LOG.info("Actual: " + rro.actualResult);
                                    LOG.info("Expected: " + rro.expectedResult);
                                    LOG.info("Filed Verified: " + rro.fieldVerified);
                                    LOG.info("Status: " + rro.status);

                                }
                        }

                }
        } catch (Exception e) {

            LOG.error("Ignoring since it is logging error", e);
        }

        LOG.info("ReportOutput Completed");

    }

    public static void print(PlanDetail pl) {
        LOG.info("Set Backs");
        LOG.info("Front Yard \n " + pl.getPlot().getFrontYard());
        LOG.info("Side Yard1 \n " + pl.getPlot().getSideYard1());
        LOG.info("Side Yard2 \n " + pl.getPlot().getSideYard2());
        LOG.info("Rear Yard \n " + pl.getPlot().getRearYard());
        LOG.info(pl.getElectricLine());
    }

}
