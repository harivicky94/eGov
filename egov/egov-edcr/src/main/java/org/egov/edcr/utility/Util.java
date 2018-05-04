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
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.ReportOutput;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.math.Polygon;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFCircle;
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
    public static final int COMPARE_WITH_2_PERCENT_ERROR_DIGITS = 2;
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

        DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
        //if layer with name not found kabeja will return default layer or create new layer and gives
        if (dxfLayer.getName().equalsIgnoreCase(name)) {
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

        new ArrayList<>();

        DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
        //if layer with name not found kabeja will return default layer or create new layer and gives
        if (dxfLayer.getName().equalsIgnoreCase(name)) {

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
            if (dxfLayer.getName().equalsIgnoreCase(name))
            {

            List dxfLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_DIMENSION);

            if (null != dxfLineEntities)
                for (Object dxfEntity : dxfLineEntities) {

                    DXFDimension line = (DXFDimension) dxfEntity;
                    String dimensionBlock = line.getDimensionBlock();
                    DXFBlock dxfBlock = dxfDocument.getDXFBlock(dimensionBlock);
                    if(LOG.isDebugEnabled()) LOG.debug("BLOCK data" + dxfBlock.getDescription());
                    DXFDimensionStyle dxfDimensionStyle = dxfDocument.getDXFDimensionStyle(line.getDimensionStyleID());
                    if(LOG.isDebugEnabled()) LOG.debug("---" + dxfDimensionStyle.getProperty(DXFDimensionStyle.PROPERTY_DIMEXO));
                    // if(LOG.isDebugEnabled()) LOG.debug(line.getInclinationHelpLine()+"HELP LINE"+line.getDimensionText()
                    // +"--"+line.getLayerName()+"--"+line.getDimensionArea());

                    if (name.contains(line.getLayerName().toUpperCase()))
                        dimensions.add(line);

                }
            }
        }
        if (dimensions.size() == 1)
            return dimensions.get(0);
        else
            return null;

    }

    public static List<DXFDimension> getDimensionsByLayer(DXFDocument dxfDocument, String name) {
        if (dxfDocument == null)
            return null;
        if (name == null)
            return null;
        name = name.toUpperCase();
        BigDecimal value = BigDecimal.ZERO;

        if (dxfDocument.containsDXFLayer(name)) {
            DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
            return dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_DIMENSION);
        }
        return null;
    }

    public static BigDecimal getSingleDimensionValueByLayer(DXFDocument dxfDocument, String name, PlanDetail pl) {

        if (dxfDocument == null)
            return null;
        if (name == null)
            return null;
        name = name.toUpperCase();
        BigDecimal value = BigDecimal.ZERO;

        DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
        if (dxfLayer.getName().equalsIgnoreCase(name)){
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
                      //  text2 = text2.replaceAll("[^\\d.]", "");
                        if (text2.contains(";")) {
                            text2 = text2.split(";")[1];
                        } else
                            text2 = text2.replaceAll("[^\\d.]", "");
                        
                        if (!text2.isEmpty())
                            value = BigDecimal.valueOf(Double.parseDouble(text2));

                    }
                }
            }

            }
     /*   if (BigDecimal.ZERO.compareTo(value) == 0)
            pl.addError(name, "Dimension value is invalid for layer " + name);*/
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

    public static List<DXFLWPolyline> getPolyLinesByLayerAndColor(DXFDocument dxfDocument, String layerName, int colorCode,
            PlanDetail pl) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();

        DXFLayer dxfLayer = dxfDocument.getDXFLayer(layerName);

        List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);

        if (null != dxfPolyLineEntities)
            for (Object dxfEntity : dxfPolyLineEntities) {

                DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;

                if (colorCode == dxflwPolyline.getColor())
                    dxflwPolylines.add(dxflwPolyline);
            }

        return dxflwPolylines;
    }

    public static List<DXFLWPolyline> getPolyLinesByLayer(DXFDocument dxfDocument, String name) {

        List<DXFLWPolyline> dxflwPolylines = new ArrayList<>();
        if (name == null)
            return dxflwPolylines;
        if(dxfDocument.containsDXFLayer(name)){
        DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
        if (dxfLayer.getName().equalsIgnoreCase(name)){
        
        if (dxfLayer.hasDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE)) {
            List dxfPolyLineEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            for (Object dxfEntity : dxfPolyLineEntities) {
                DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;
                dxflwPolylines.add(dxflwPolyline);
            }
            
        } else {
            // TODO: add what if polylines not found

        }
        }
        }
        return dxflwPolylines;
    
    }
    public static List<DXFCircle> getPolyCircleByLayer(DXFDocument dxfDocument, String name) {

        List<DXFCircle> dxfCircles = new ArrayList<>();
        if (name == null)
            return dxfCircles;
        if(dxfDocument.containsDXFLayer(name)){
        DXFLayer dxfLayer = dxfDocument.getDXFLayer(name);
        if (dxfLayer.getName().equalsIgnoreCase(name)){
        
        if (dxfLayer.hasDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE)) {
            List dxfCircleEntities = dxfLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_CIRCLE);
            for (Object dxfEntity : dxfCircleEntities) {
                DXFCircle dxflwPolyline = (DXFCircle) dxfEntity;
                dxfCircles.add(dxflwPolyline);
            }
            
        } 
        }
        }
        return dxfCircles;
    
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
                // if(LOG.isDebugEnabled()) LOG.debug("----------"+planInfoLayer.getName()+"---------------------------------------------------");
                if (planInfoLayer != null) {
                    List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);
                    if (texts != null) {
                        Iterator iterator = texts.iterator();

                        while (iterator.hasNext()) {
                            DXFText text = (DXFText) iterator.next();
                            // if(LOG.isDebugEnabled()) LOG.debug("Mtext :"+text.getText());
                        }
                    }

                }
                if (layerName.equals(next.getName().toUpperCase())) {
                    found = true;
                    layerName = next.getName();
                }
            }
            if (!found)
                LOG.error("No Layer Found with name" + layerName);

            DXFLayer planInfoLayer = doc.getDXFLayer(layerName);
            // if(LOG.isDebugEnabled()) LOG.debug(planInfoLayer.getName());
            if (planInfoLayer != null) {
                List texts = planInfoLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_MTEXT);

                // if(LOG.isDebugEnabled()) LOG.debug("Texts list is null ");
                DXFText text = null;
                if (texts != null) {
                    Iterator iterator = texts.iterator();

                    while (iterator.hasNext()) {
                        text = (DXFText) iterator.next();
                        // if(LOG.isDebugEnabled()) LOG.debug("Mtext :"+text.getText());
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

        if (texts != null) {

            Iterator iterator = texts.iterator();
            String[] split;
            String s = "\\";
            while (iterator.hasNext()) {
                text = (DXFText) iterator.next();

                param = text.getText();
                param = param.replace(s, "#");
                // if(LOG.isDebugEnabled()) LOG.debug(param);
                if (param.contains("#P"))
                    // if(LOG.isDebugEnabled()) LOG.debug("inside");
                    split = param.split("#P");
                else {
                    split = new String[1];
                    split[0] = param;
                }

                for (String element : split) {

                    String[] data = element.split("=");
                    if (data.length == 2)
                        // if(LOG.isDebugEnabled()) LOG.debug(data[0]+"---"+data[1]);
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

    public static boolean pointsEquals(Point point1, Point point) {
        BigDecimal px = BigDecimal.valueOf(point.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal py = BigDecimal.valueOf(point.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1x = BigDecimal.valueOf(point1.getX()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1y = BigDecimal.valueOf(point1.getY()).setScale(DECIMALDIGITS, BigDecimal.ROUND_DOWN);
        if (px.compareTo(p1x) == 0 && py.compareTo(p1y) == 0)
            return true;
        else
            return false;
    }
    
    public static boolean pointsEqualsWith2PercentError(Point point1, Point point) {
        BigDecimal px = BigDecimal.valueOf(point.getX()).setScale(COMPARE_WITH_2_PERCENT_ERROR_DIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal py = BigDecimal.valueOf(point.getY()).setScale(COMPARE_WITH_2_PERCENT_ERROR_DIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1x = BigDecimal.valueOf(point1.getX()).setScale(COMPARE_WITH_2_PERCENT_ERROR_DIGITS, BigDecimal.ROUND_DOWN);
        BigDecimal p1y = BigDecimal.valueOf(point1.getY()).setScale(COMPARE_WITH_2_PERCENT_ERROR_DIGITS, BigDecimal.ROUND_DOWN);
        double d = 0.01;

       
        
        if (px.compareTo(p1x) == 0 && py.compareTo(p1y) == 0)
            return true;
        else if ((Math.abs(px.doubleValue() - p1x.doubleValue()) <= d) && (Math.abs(py.doubleValue() - p1y.doubleValue()) <= d))
        {
            return true;
        }
        else
            return false;
    }

    

    

    public static void print(DXFLWPolyline yard, String name) {
        if (yard != null) {
            Iterator vertexIterator = yard.getVertexIterator();
            if(LOG.isDebugEnabled()) LOG.debug("Points on the " + name);
            if(LOG.isDebugEnabled()) LOG.debug("Max x: " + yard.getBounds().getMaximumX() +" Min x:"+yard.getBounds().getMinimumX());
            if(LOG.isDebugEnabled()) LOG.debug("Max y: " + yard.getBounds().getMaximumY() +" Min x:"+yard.getBounds().getMinimumY());
            
            
            while (vertexIterator.hasNext()) {
                DXFVertex next = (DXFVertex) vertexIterator.next();
                if(LOG.isDebugEnabled()) LOG.debug(next.getPoint().getX() + "," + next.getPoint().getY());
               // if(LOG.isDebugEnabled()) LOG.debug(next.getX() + "," + next.getY());
            }
        }

    }

    public static void print(HashMap<String, String> errors) {
        if(LOG.isDebugEnabled()) LOG.debug(errors.getClass().getName());
        Iterator<Entry<String, String>> iterator = errors.entrySet().iterator();
        while (iterator.hasNext()) {
            Entry<String, String> next = iterator.next();
            if(LOG.isDebugEnabled()) LOG.debug(next.getKey() + "---" + next.getValue());
        }
    }

    public static void print(ReportOutput ro) {
        try {
            if(LOG.isDebugEnabled()) LOG.debug("ReportOutput");
            if (ro.getRuleOutPuts() != null)
                for (RuleOutput rp : ro.getRuleOutPuts()) {
                    if(LOG.isDebugEnabled()) LOG.debug(rp.key + " -- " + rp.getMessage() + " -- " + rp.getResult());
                    if (rp.getSubRuleOutputs() != null)
                        for (SubRuleOutput so : rp.getSubRuleOutputs()) {
                            if(LOG.isDebugEnabled()) LOG.debug(so.key + " , " + so.message + " , " + so.ruleDescription);
                            List<RuleReportOutput> ruleReportOutputs = so.ruleReportOutputs;
                            if (ruleReportOutputs != null)
                                for (RuleReportOutput rro : ruleReportOutputs) {
                                    if(LOG.isDebugEnabled()) LOG.debug("Actual: " + rro.actualResult);
                                    if(LOG.isDebugEnabled()) LOG.debug("Expected: " + rro.expectedResult);
                                    if(LOG.isDebugEnabled()) LOG.debug("Filed Verified: " + rro.fieldVerified);
                                    if(LOG.isDebugEnabled()) LOG.debug("Status: " + rro.status);

                                }
                        }

                }
        } catch (Exception e) {

            LOG.error("Ignoring since it is logging error", e);
        }

        if(LOG.isDebugEnabled()) LOG.debug("ReportOutput Completed");

    }
    
    public static void print (List<Floor> floors)
    {
     for(Floor floor:floors)
     {
//         if(LOG.isDebugEnabled()) LOG.debug("Floor Name"+floor.getName());
         if(floor.getExterior()!=null)
         {
         if(LOG.isDebugEnabled()) LOG.debug("Ext points Count"+floor.getExterior().getPolyLine().getVertexCount());
         if(LOG.isDebugEnabled()) LOG.debug("maxx  : "+floor.getExterior().getPolyLine().getBounds().getMaximumX()+" minx :  "+floor.getExterior().getPolyLine().getBounds().getMinimumX());
         if(LOG.isDebugEnabled()) LOG.debug("maxy  : "+floor.getExterior().getPolyLine().getBounds().getMaximumY()+" minx :  "+floor.getExterior().getPolyLine().getBounds().getMinimumY());
          print(floor.getExterior().getPolyLine(),floor.getName());
         }
         if(LOG.isDebugEnabled()) LOG.debug("Habitable Rooms count"+floor.getHabitableRooms().size());
         int i=0;
         for(Room r:floor.getHabitableRooms())
         {
             if(LOG.isDebugEnabled()) LOG.debug("maxx  : "+r.getPolyLine().getBounds().getMaximumX()+" minx :  "+r.getPolyLine().getBounds().getMinimumX());
             if(LOG.isDebugEnabled()) LOG.debug("maxy  : "+r.getPolyLine().getBounds().getMaximumY()+" minx :  "+r.getPolyLine().getBounds().getMinimumY());
             print(r.getPolyLine(),floor.getName()+"_Room_"+i++);
         }
         
     }
        
    }

    public static void print(PlanDetail pl) {
        if(LOG.isDebugEnabled()) LOG.debug("Set Backs");
        if(LOG.isDebugEnabled()) LOG.debug("Front Yard \n " + pl.getPlot().getFrontYard());
        if(LOG.isDebugEnabled()) LOG.debug("Side Yard1 \n " + pl.getPlot().getSideYard1());
        if(LOG.isDebugEnabled()) LOG.debug("Side Yard2 \n " + pl.getPlot().getSideYard2());
        if(LOG.isDebugEnabled()) LOG.debug("Rear Yard \n " + pl.getPlot().getRearYard());
        if(LOG.isDebugEnabled()) LOG.debug(pl.getElectricLine());
        if(LOG.isDebugEnabled()) LOG.debug(pl.getBuilding());
        
    }
  
    public static Polygon getPolygon(DXFLWPolyline plotBoundary) {
         List<Point> pointsOnPolygon = pointsOnPolygon(plotBoundary);
        return new Polygon(pointsOnPolygon);
    }
    
    public static List<Point> pointsOnPolygon(DXFLWPolyline plotBoundary) {
        if (plotBoundary == null) {
            return null;
        }
        int i = 0;
        int count = plotBoundary.getVertexCount();
        List<Point> points = new ArrayList<>();
        Iterator plotBIterator1 = plotBoundary.getVertexIterator();
        while (plotBIterator1.hasNext()) {

            DXFVertex dxfVertex = (DXFVertex) plotBIterator1.next();
            Point point1 = dxfVertex.getPoint();

            points.add(point1);

            // if(LOG.isDebugEnabled()) LOG.debug(name+"===Shape=="+shape[i][0]+"--"+shape[i][1]);
            i++;

        }

        points.add(points.get(0));
        return points;
    }
    
    public static List<Point> findPointsOnPolylines(List<Point> yardInSidePoints) {
        Point old = null;
        Point first = null;
        Point point1 = new Point();
        List<Point> myPoints = new ArrayList<>();

        for (Point in : yardInSidePoints) {

            // if(LOG.isDebugEnabled()) LOG.debug(" IN: "+ in.getX()+","+in.getY());
            if (old == null) {
                old = in;
                first = in;
                continue;
            }
            // commented to fix yard min in sample_17.dxf
            /*
             * if (first.equals(in)) continue;
             */

            // if(LOG.isDebugEnabled()) LOG.debug("Points for line ------"+old.getX()+","+old.getY() +" And "+ in.getX()+","+in.getY());
            double distance = MathUtils.distance(old, in);

            // if(LOG.isDebugEnabled()) LOG.debug("Distance"+distance);

            for (double j = .01; j < distance; j = j + .01) {
                point1 = new Point();
                double t = j / distance;
                point1.setX((1 - t) * old.getX() + t * in.getX());
                point1.setY((1 - t) * old.getY() + t * in.getY());
                myPoints.add(point1);
                // if(LOG.isDebugEnabled()) LOG.debug(point1.getX()+"---"+point1.getY());
            }

            old = in;
        }
        return myPoints;
    }


}
