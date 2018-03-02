package org.egov.edcr.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Basement;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.measurement.WasteDisposal;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.RayCast;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.kabeja.parser.DXFParser;
import org.kabeja.parser.ParseException;
import org.kabeja.parser.Parser;
import org.kabeja.parser.ParserBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

@Service
public class DXFExtractService {
    private static Logger LOG = Logger.getLogger(DcrService.class);

    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;

    public PlanDetail extract(File dxfFile, EdcrApplication dcrApplication) {
        PlanDetail pl = new PlanDetail();

        try {
            Parser parser = ParserBuilder.createDefaultParser();
            parser.parse(dxfFile.getPath(), DXFParser.DEFAULT_ENCODING);
            // Extract DXF Data
            DXFDocument doc = parser.getDocument();

            pl.setPlanInformation(extractPlanInfo(doc, pl));
            pl.getPlanInformation().setOccupancy(dcrApplication.getPlanInformation().getOccupancy());
            pl.getPlanInformation().setOwnerName(dcrApplication.getPlanInformation().getOwnerName());

            extractPlotDetails(pl, doc);
            extractBuildingDetails(pl, doc);
            extractTotalFloorArea(doc, pl);
            extractBasementDetails(pl, doc);

            pl.getPlot().setFrontYard(getYard(pl, doc, DxfFileConstants.FRONT_YARD));
            pl.getPlot().setRearYard(getYard(pl, doc, DxfFileConstants.REAR_YARD));
            pl.getPlot().setSideYard1(getYard(pl, doc, DxfFileConstants.SIDE_YARD_1));
            pl.getPlot().setSideYard2(getYard(pl, doc, DxfFileConstants.SIDE_YARD_2));

            pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.FRONT_YARD));
            pl.getPlot().getSideYard1().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_1));
            pl.getPlot().getSideYard2().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_2));
            pl.getPlot().getRearYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.REAR_YARD));

            if (pl.getBasement()!=null) {
            pl.getPlot().setBsmtFrontYard(getYard(pl, doc, DxfFileConstants.BSMNT_FRONT_YARD));
            pl.getPlot().setBsmtRearYard(getYard(pl, doc, DxfFileConstants.BSMNT_REAR_YARD));
            pl.getPlot().setBsmtSideYard1(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_1));
            pl.getPlot().setBsmtSideYard2(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_2));

            pl.getPlot().getBsmtFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.BSMNT_FRONT_YARD));
            pl.getPlot().getBsmtSideYard1().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_1));
            pl.getPlot().getBsmtSideYard2().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_2));
            pl.getPlot().getBsmtRearYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.BSMNT_REAR_YARD));
            }

            pl = extractRoadDetails(doc, pl);
            pl.setNotifiedRoads(new ArrayList<>());
            pl.setNonNotifiedRoads(new ArrayList<>());
            pl = extractUtilities(doc, pl);
            pl = extractOverheadElectricLines(doc, pl);
            pl = extractHeights(doc, pl);
            extractFloorDetails(doc, pl);
            extractParkingDetails(doc, pl);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pl;
    }

    private void extractParkingDetails(DXFDocument doc, PlanDetail pl) {

        List<DXFLWPolyline> residentialUnit = new ArrayList<DXFLWPolyline>();
        List<DXFLWPolyline> residentialUnitDeduction = new ArrayList<DXFLWPolyline>();
        List<DXFLWPolyline> removeDeduction = new ArrayList<DXFLWPolyline>();
        boolean layerPresent = true;

        layerPresent = doc.containsDXFLayer(DxfFileConstants.RESI_UNIT);

        if (layerPresent) {
            List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, DxfFileConstants.RESI_UNIT);

            if (!bldgext.isEmpty())
                for (DXFLWPolyline pline : bldgext) {
                    residentialUnit.add(pline);
                }
        }
        layerPresent = doc.containsDXFLayer(DxfFileConstants.RESI_UNIT_DEDUCT);
        if (layerPresent) {
            List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, DxfFileConstants.RESI_UNIT_DEDUCT);
            if (!bldDeduct.isEmpty())
                for (DXFLWPolyline pline : bldDeduct) {
                    residentialUnitDeduction.add(pline);
                }
        }

        int i = 0;
        for (DXFLWPolyline resUnit : residentialUnit) {

            FloorUnit floorUnit = new FloorUnit();
            floorUnit.setPolyLine(resUnit);

            i++;
            double[][] pointsOfPlot = MinDistance.pointsOfPolygon(resUnit);

            BigDecimal deduction = BigDecimal.ZERO;
            for (DXFLWPolyline residentialDeduct : residentialUnitDeduction) {
                boolean contains = false;
                Iterator buildingIterator = residentialDeduct.getVertexIterator();
                while (buildingIterator.hasNext()) {
                    DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
                    Point point = dxfVertex.getPoint();
                    if (RayCast.contains(pointsOfPlot, new double[] { point.getX(), point.getY() }) == true) {
                        contains = true;
                        removeDeduction.add(residentialDeduct);
                        Measurement measurement = new Measurement();
                        measurement.setPolyLine(residentialDeduct);
                        floorUnit.getDeductions().add(measurement);
                    }
                }
                if (contains) {
                    /*
                     * System.out.println("current deduct " + deduction + "    :add deduct for rest unit " + i + " area added" +
                     * Util.getPolyLineArea(residentialDeduct));
                     */
                    deduction = deduction.add(Util.getPolyLineArea(residentialDeduct));
                }

            }
            // Each blocks are adjecent to each other. Raycast repeat the same points in each block.
            if (removeDeduction.size() > 0) {
                residentialUnitDeduction.removeAll(removeDeduction);
                removeDeduction = new ArrayList<DXFLWPolyline>();
            }
            floorUnit.setTotalUnitDeduction(deduction);
            pl.getFloorUnits().add(floorUnit);
        }

        layerPresent = doc.containsDXFLayer(DxfFileConstants.PARKING_SLOT);

        if (layerPresent) {
            List<DXFLWPolyline> bldparking = Util.getPolyLinesByLayer(doc, DxfFileConstants.PARKING_SLOT);
            if (!bldparking.isEmpty())
                for (DXFLWPolyline pline : bldparking) {
                    Measurement measurement = new Measurement();
                    measurement.setPolyLine(pline);
                    pl.getParkingSlots().add(measurement);
                    // System.out.println("parking " + Util.getPolyLineArea(pline));
                }
        }
    }

    private void extractFloorDetails(DXFDocument doc, PlanDetail pl) {

        DXFLayer layer = new DXFLayer();
        int floorNo = -1;
        while (layer != null) {
            floorNo++;
            layer = doc.getDXFLayer(DxfFileConstants.FLOOR_NAME_PREFIX + floorNo);
            if (!layer.getName().equalsIgnoreCase("FLOOR_" + floorNo))
                break;

            Floor floor = new Floor();
            List dxfPolyLineEntities = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            if (dxfPolyLineEntities != null)
                for (Object dxfEntity : dxfPolyLineEntities) {
                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;
                    floor.setPolyLine(dxflwPolyline);
                    if (dxflwPolyline.getColor() == DxfFileConstants.HABITABLE_ROOM_COLOR) {
                        Room habitable = new Room();
                        habitable.setPolyLine(dxflwPolyline);
                        floor.getHabitableRooms().add(habitable);
                    }
                    if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_EXTERIOR_WALL_COLOR) {
                        Measurement extWall = new Measurement();
                        extWall.setPolyLine(dxflwPolyline);
                        floor.setExterior(extWall);
                    }
                    if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_OPENSPACE_COLOR) {
                        Measurement openSpace = new Measurement();
                        openSpace.setPolyLine(dxflwPolyline);
                        floor.getOpenSpaces().add(openSpace);
                    }
                }
            pl.getBuilding().getFloors().add(floor);

        }
        pl.getBuilding().setMaxFloor(BigDecimal.valueOf(floorNo));

        pl.getBuilding().setFloorsAboveGround(BigDecimal.valueOf(floorNo - 1));

        int negetivFloorNo = 0;
        while (layer != null) {
            negetivFloorNo--;
            layer = doc.getDXFLayer(DxfFileConstants.FLOOR_NAME_PREFIX + negetivFloorNo);
            if (!layer.getName().equalsIgnoreCase("FLOOR_" + floorNo)) {
                negetivFloorNo++;
                break;
            }
            Floor floor = new Floor();
            List dxfPolyLineEntities = layer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
            if (dxfPolyLineEntities != null)
                for (Object dxfEntity : dxfPolyLineEntities) {
                    DXFLWPolyline dxflwPolyline = (DXFLWPolyline) dxfEntity;
                    floor.setPolyLine(dxflwPolyline);
                    if (dxflwPolyline.getColor() == DxfFileConstants.HABITABLE_ROOM_COLOR) {
                        Room habitable = new Room();
                        habitable.setPolyLine(dxflwPolyline);
                        floor.getHabitableRooms().add(habitable);
                    }
                    if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_EXTERIOR_WALL_COLOR) {
                        Measurement extWall = new Measurement();
                        extWall.setPolyLine(dxflwPolyline);
                        floor.setExterior(extWall);
                    }
                    if (dxflwPolyline.getColor() == DxfFileConstants.FLOOR_OPENSPACE_COLOR) {
                        Measurement openSpace = new Measurement();
                        openSpace.setPolyLine(dxflwPolyline);
                        floor.getOpenSpaces().add(openSpace);
                    }
                }
            pl.getBuilding().getFloors().add(floor);

        }

        pl.getBuilding().setTotalFloors(BigDecimal.valueOf(Math.abs(negetivFloorNo) + floorNo));
    }

    private void extractBuildingDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;
        Building building = new Building();
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.BUILDING_FOOT_PRINT);

        if (polyLinesByLayer.size() > 0) {
            building.setPolyLine(polyLinesByLayer.get(0));
            building.setPresentInDxf(true);
        } else
            pl.addError(DxfFileConstants.BUILDING_FOOT_PRINT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[]{DxfFileConstants.BUILDING_FOOT_PRINT}, null));

        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.SHADE_OVERHANG);

        Measurement shade = new Measurement();
        if (polyLinesByLayer.size() > 0) {
            shade.setPolyLine(polyLinesByLayer.get(0));
        }
        building.setShade(shade);

        // TODO : Verify it is line only or dimension
        List<DXFLine> lines = Util.getLinesByLayer(doc, DxfFileConstants.OPEN_STAIR);
        for (DXFLine line : lines) {
            Measurement shortDistanceToPlot = new Measurement();
            shortDistanceToPlot
                    .setMinimumDistance(BigDecimal.valueOf(line.getLength()).setScale(DcrConstants.DECIMALDIGITS_MEASUREMENTS));
            building.getOpenStairs().add(shortDistanceToPlot);
        }

        pl.setBuilding(building);

    }

    private void extractBasementDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer= new ArrayList<>();
        
        if (doc.containsDXFLayer(DxfFileConstants.BSMNT_FOOT_PRINT)) {
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.BSMNT_FOOT_PRINT);
            if (polyLinesByLayer.size() > 0) {
                Basement basement = new Basement();
                basement.setPolyLine(polyLinesByLayer.get(0));
                basement.setPresentInDxf(true);
                pl.setBasement(basement);
            }
          
        }
    }

    private void extractPlotDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer;
        Plot plot = new Plot();
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.PLOT_BOUNDARY);
        if (polyLinesByLayer.size() > 0) {
            plot.setPolyLine(polyLinesByLayer.get(0));
            plot.setArea(Util.getPolyLineArea(plot.getPolyLine()));
            plot.setPresentInDxf(true);
        } else
            pl.addError(DxfFileConstants.PLOT_BOUNDARY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[]{DxfFileConstants.PLOT_BOUNDARY}, null));
        pl.setPlot(plot);
    }

    private PlanDetail extractHeights(DXFDocument doc, PlanDetail pl) {

        BigDecimal ht = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.HEIGHT_OF_BUILDING, pl);
        pl.getBuilding().setBuildingHeight(ht);
        return pl;
    }

    /**
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
     */
    private PlanDetail extractTotalFloorArea(DXFDocument doc, PlanDetail pl) {

        BigDecimal floorArea = BigDecimal.ZERO;
        List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, DxfFileConstants.BLDG_EXTERIOR_WALL);
        if (!bldgext.isEmpty())
            for (DXFLWPolyline pline : bldgext)
                floorArea = floorArea.add(Util.getPolyLineArea(pline));
        List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, DxfFileConstants.FAR_DEDUCT);
        if (!bldDeduct.isEmpty())
            for (DXFLWPolyline pline : bldDeduct)
                floorArea = floorArea.subtract(Util.getPolyLineArea(pline));

        LOG.info("floorArea:" + floorArea);
        pl.getBuilding().setTotalFloorArea(floorArea);

        if (pl.getPlot().getArea() != null) {
            BigDecimal far = floorArea.divide(pl.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    DcrConstants.ROUNDMODE_MEASUREMENTS);
            pl.getBuilding().setFar(far);
        }

        if (pl.getBuilding().getPolyLine() != null) {

            BigDecimal cvDeduct = BigDecimal.ZERO;
            BigDecimal buildingFootPrintArea = Util.getPolyLineArea(pl.getBuilding().getPolyLine());
            List<DXFLWPolyline> cvDeductPlines = Util.getPolyLinesByLayer(doc, DxfFileConstants.COVERGAE_DEDUCT);
            if (!cvDeductPlines.isEmpty()) {
                for (DXFLWPolyline pline : cvDeductPlines)
                    cvDeduct.add(Util.getPolyLineArea(pline));
            }
            BigDecimal coverage=BigDecimal.valueOf(100);

            if (buildingFootPrintArea != null && pl.getPlanInformation().getPlotArea() != null
                    && pl.getPlanInformation().getPlotArea().intValue() > 0) {
                coverage = buildingFootPrintArea.subtract(cvDeduct).multiply(BigDecimal.valueOf(100)).divide(
                        pl.getPlanInformation().getPlotArea(),
                        DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
                pl.getBuilding().setCoverage(coverage);
                LOG.info("coverage:" + coverage);
            } else {
                pl.addError(DxfFileConstants.COVERGAE_DEDUCT,
                        "Cannot calculate coverage as " + DxfFileConstants.BUILDING_FOOT_PRINT
                                + " or " + DxfFileConstants.PLOT_AREA + " is not defined");

            }

        }

        return pl;

    }

    private Yard getYard(PlanDetail pl, DXFDocument doc, String yardName) {
        Yard yard = new Yard();
        List<DXFLWPolyline> frontYardLines = Util.getPolyLinesByLayer(doc, yardName);
        if (frontYardLines.size() > 0) {
            yard.setPolyLine(frontYardLines.get(0));
            yard.setArea(Util.getPolyLineArea(yard.getPolyLine()));
            yard.setMean(yard.getArea().divide(BigDecimal.valueOf(yard.getPolyLine().getBounds().getWidth()), 5,
                    RoundingMode.HALF_UP));
            LOG.info(yardName + " Mean " + yard.getMean());
            yard.setPresentInDxf(true);

        } else
            pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[]{yardName}, null));

        return yard;

    }

    /**
     * @param doc
     * @return add condition for what are mandatory
     */
    private PlanInformation extractPlanInfo(DXFDocument doc, PlanDetail pl) {
        PlanInformation pi = new PlanInformation();
        Map<String, String> planInfoProperties = Util.getPlanInfoProperties(doc);
        if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
            pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));
        String plotArea = planInfoProperties.get(DxfFileConstants.PLOT_AREA);

        if (plotArea == null)
            pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " is not defined in the Plan Information Layer");
        else
            try {
                plotArea = plotArea.replaceAll("[^\\d.]", "");
                pi.setPlotArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
            } catch (Exception e) {
                pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " contains non invalid values.");
            }
        // The below code must be deleted once plot area is defined properly in dxf file
        {
            if (plotArea == null) {
                Set<String> keySet = planInfoProperties.keySet();
                for (String s : keySet)
                    if (s.contains(DxfFileConstants.PLOT_AREA)) {
                        plotArea = planInfoProperties.get(s);
                        pl.addError(DxfFileConstants.PLOT_AREA,
                                DxfFileConstants.PLOT_AREA + " is invalid .Text in dxf file is " + s);
                    }

                try {
                    plotArea = plotArea.replaceAll("[^\\d.]", "");
                    pi.setPlotArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
                } catch (Exception e) {
                    pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " contains non invalid values.");

                }
            }
        }
        // till here

        if (planInfoProperties.get(DxfFileConstants.CRZ_ZONE) != null) {
            String value = planInfoProperties.get(DxfFileConstants.CRZ_ZONE);
            if (value.equalsIgnoreCase(DcrConstants.YES))
                pi.setCrzZoneArea(true);
            else
                pi.setCrzZoneArea(false);
        }

        if (planInfoProperties.get(DxfFileConstants.SECURITY_ZONE) != null) {
            String securityZone = planInfoProperties.get(DxfFileConstants.SECURITY_ZONE);
            if (securityZone.equalsIgnoreCase(DcrConstants.YES))
                pi.setSecurityZone(true);
            else
                pi.setSecurityZone(false);
        }
        if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M) != null) {
            String openingBelow2mside = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M);
            if (openingBelow2mside.equalsIgnoreCase(DcrConstants.YES))
                pi.setOpeningOnSide(true);
            else
                pi.setOpeningOnSide(false);
        }
        if (planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M) != null) {
            String openingBelow2mrear = planInfoProperties.get(DxfFileConstants.OPENING_BELOW_2_1_ON_REAR_LESS_1M);
            if (openingBelow2mrear.equalsIgnoreCase(DcrConstants.YES))
                pi.setOpeningOnRear(true);
            else
                pi.setOpeningOnRear(false);
        }
        if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE) != null) {
            String nocAbutSide = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_SIDE);
            if (nocAbutSide.equalsIgnoreCase(DcrConstants.YES))
                pi.setNocToAbutSide(true);
            else
                pi.setNocToAbutSide(false);
        }
        if (planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR) != null) {
            String nocAbutRear = planInfoProperties.get(DxfFileConstants.NOC_TO_ABUT_REAR);
            if (nocAbutRear.equalsIgnoreCase(DcrConstants.YES))
                pi.setNocToAbutRear(true);
            else
                pi.setNocToAbutRear(false);
        }


        if (planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME) != null)
            pi.setArchitectInformation(planInfoProperties.get(DxfFileConstants.ARCHITECT_NAME));

        String accwidth = "";
        if (planInfoProperties.size() > 0) {
            String accessWidth = planInfoProperties.get(DxfFileConstants.ACCESS_WIDTH);
            accwidth = accessWidth;
            if (accessWidth == null) {

                Set<String> keySet = planInfoProperties.keySet();
                for (String s : keySet)
                    if (s.contains(DxfFileConstants.ACCESS_WIDTH)) {
                        accessWidth = planInfoProperties.get(s);
                        pl.addError(DxfFileConstants.ACCESS_WIDTH,
                                DxfFileConstants.ACCESS_WIDTH + " is invalid .Text in dxf file is " + s);
                    }

            }

            if (accessWidth == null)
                pl.addError(DxfFileConstants.ACCESS_WIDTH, DxfFileConstants.ACCESS_WIDTH + "  Is not defined");
            else {
                accessWidth = accessWidth.replaceAll("[^\\d.]", "");
                if (!accessWidth.isEmpty())
                    pi.setAccessWidth(BigDecimal.valueOf(Double.parseDouble(accessWidth)));
                else
                    pl.addError(DxfFileConstants.ACCESS_WIDTH,
                            "The value for " + DxfFileConstants.ACCESS_WIDTH + " '" + accwidth + "' Is Invalid");

            }
        } else
            pi.setAccessWidth(BigDecimal.ZERO);

        return pi;
    }

    private PlanDetail extractRoadDetails(DXFDocument doc, PlanDetail pl) {

        // TODO: Find shortest distance for all road polylines

        // List<DXFLine> distancesToRoads = Util.getLinesByLayer(doc, DxfFileConstants.SHORTEST_DISTANCE_TO_ROAD);
        List<DXFLWPolyline> notifiedRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.NOTIFIED_ROADS);
        for (DXFLWPolyline roadPline : notifiedRoads) {

            NotifiedRoad road = new NotifiedRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getNotifiedRoads().add(road);

        }
        List<DXFLWPolyline> nonNotifiedRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.NON_NOTIFIED_ROAD);
        for (DXFLWPolyline roadPline : nonNotifiedRoads) {

            NonNotifiedRoad road = new NonNotifiedRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getNonNotifiedRoads().add(road);

        }

        BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.MAX_HEIGHT_CAL, pl);
        if (dimension != null) {
            pl.getBuilding().setDistanceFromBuildingFootPrintToRoadEnd(dimension);
        }


        return pl;

    }

    private PlanDetail extractUtilities(DXFDocument doc, PlanDetail pl) {
        // Waste Disposal
        List<DXFLWPolyline> wasterDisposalPolyLines = Util.getPolyLinesByLayer(doc, DxfFileConstants.LAYER_NAME_WASTE_DISPOSAL);
        if (wasterDisposalPolyLines.size() > 0)
            for (DXFLWPolyline pline : wasterDisposalPolyLines) {
                WasteDisposal disposal = new WasteDisposal();
                disposal.setPresentInDxf(true);
                disposal.setPolyLine(pline);
                pl.getUtility().addWasteDisposal(disposal);
            }

        return pl;

    }

    private PlanDetail extractOverheadElectricLines(DXFDocument doc, PlanDetail pl) {
        ElectricLine line = new ElectricLine();

        DXFLine horiz_clear_OHE = Util.getSingleLineByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2);

        if (horiz_clear_OHE != null) {
            line.setHorizontalDistance(BigDecimal.valueOf(horiz_clear_OHE.getLength()));
            line.setPresentInDxf(true);
        } else {
            BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2, pl);
            if (dimension != null) {
                line.setHorizontalDistance(dimension);
                line.setPresentInDxf(true);
            }
        }
        DXFLine vert_clear_OHE = Util.getSingleLineByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE);

        if (vert_clear_OHE != null) {
            line.setVerticalDistance(BigDecimal.valueOf(vert_clear_OHE.getLength()));
            line.setPresentInDxf(true);
        } else {
            Util.getMtextByLayerName(doc, DxfFileConstants.VERT_CLEAR_OHE);

            BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE, pl);
            if (dimension != null) {
                line.setVerticalDistance(dimension);
                line.setPresentInDxf(true);
            }

        }

        String voltage = Util.getMtextByLayerName(doc, "VOLTAGE");
        if (voltage != null)
            try {
                voltage = voltage.replaceAll("[^\\d.]", "");
                BigDecimal volt = BigDecimal.valueOf(Double.parseDouble(voltage));
                line.setVoltage(volt);
                line.setPresentInDxf(true);
            } catch (NumberFormatException e) {

                pl.addError("VOLTAGE",
                        "Voltage value contains non numeric character.Voltage must be Number specified in  KW unit, without the text KW");

            }
        else {
            if (horiz_clear_OHE != null || vert_clear_OHE != null)
                pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.HORIZ_CLEAR_OHE2 + " or "
                        + DxfFileConstants.VERT_CLEAR_OHE);
        }
        pl.setElectricLine(line);

        return pl;
    }

}
