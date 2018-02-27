package org.egov.edcr.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.Floor;
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
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFLine;
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

            pl.getPlot().setFrontYard(getYard(pl, doc, DxfFileConstants.FRONT_YARD));
            pl.getPlot().setRearYard(getYard(pl, doc, DxfFileConstants.REAR_YARD));
            pl.getPlot().setSideYard1(getYard(pl, doc, DxfFileConstants.SIDE_YARD_1));
            pl.getPlot().setSideYard2(getYard(pl, doc, DxfFileConstants.SIDE_YARD_2));

            pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.FRONT_YARD));
            pl.getPlot().getSideYard1().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_1));
            pl.getPlot().getSideYard2().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.SIDE_YARD_2));
            pl.getPlot().getRearYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DxfFileConstants.REAR_YARD));

            pl = extractRoadDetails(doc, pl);
            pl.setNotifiedRoads(new ArrayList<>());
            pl.setNonNotifiedRoads(new ArrayList<>());
            pl = extractUtilities(doc, pl);
            pl = extractOverheadElectricLines(doc, pl);
            pl = extractHeights(doc, pl);
            extractFloorDetails(doc, pl);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pl;
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
                    new String[] { DxfFileConstants.BUILDING_FOOT_PRINT }, null));
        pl.setBuilding(building);
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
                    new String[] { DxfFileConstants.PLOT_BOUNDARY }, null));
        pl.setPlot(plot);
    }

    private PlanDetail extractHeights(DXFDocument doc, PlanDetail pl) {

        BigDecimal ht = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.HEIGHT_OF_BUILDING, pl);
        pl.getBuilding().setBuildingHeight(ht);
        return pl;
    }

    /**
     *
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer)
     */
    private PlanDetail extractTotalFloorArea(DXFDocument doc, PlanDetail pl) {

        BigDecimal floorArea = BigDecimal.ZERO;
        List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayerAndColor(doc, DxfFileConstants.BLDG_EXTERIOR_WALL,
                DxfFileConstants.BLDG_EXTERIOR_WALL_COLOR, pl);
        if (!bldgext.isEmpty())
            for (DXFLWPolyline pline : bldgext)
                floorArea = floorArea.add(Util.getPolyLineArea(pline));
        List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayerAndColor(doc, DxfFileConstants.FAR_DEDUCT,
                DxfFileConstants.FAR_DEDUCT_COLOR, pl);
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
            List<DXFLWPolyline> cvDeductPlines = Util.getPolyLinesByLayerAndColor(doc, DxfFileConstants.COVERGAE_DEDUCT, 0, pl);
            if (!cvDeductPlines.isEmpty()) {
                for (DXFLWPolyline pline : cvDeductPlines)
                    cvDeduct.add(Util.getPolyLineArea(pline));
                if (cvDeduct.compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal coverage = buildingFootPrintArea.multiply(BigDecimal.valueOf(100)).divide(cvDeduct);
                    pl.getBuilding().setCoverage(coverage);
                    LOG.info("coverage:" + coverage);
                }
            } else {
                pl.addError(DxfFileConstants.COVERGAE_DEDUCT, DxfFileConstants.COVERGAE_DEDUCT + " layer is not defined");
                pl.getBuilding().setCoverage(BigDecimal.ZERO);
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
            pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[] { yardName }, null));

        return yard;

    }

    /**
     *
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

        if (planInfoProperties.get(DxfFileConstants.SECURITY_ZONE) != null)
            pi.setSecurityZone(true);
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
            DXFDimension dimension = Util.getSingleDimensionByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2);
            if (dimension != null) {
                // LOG.info(dimension.getHorizontalAlign()+dimension.getLeadingLineLength()+"xxx:"+(dimension.getBounds().getMaximumX()-dimension.getBounds().getMinimumX()));
                // LOG.info(dimension.getLeadingLineLength()+"yyy:"+(dimension.getBounds().getMaximumY()-dimension.getBounds().getMinimumY()));
                double x = dimension.getBounds().getMaximumY() - dimension.getBounds().getMinimumY();
                line.setHorizontalDistance(BigDecimal.valueOf(x));

                line.setPresentInDxf(true);
            }
        }
        DXFLine vert_clear_OHE = Util.getSingleLineByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE);

        if (vert_clear_OHE != null) {
            line.setVerticalDistance(BigDecimal.valueOf(vert_clear_OHE.getLength()));
            line.setPresentInDxf(true);
        } else {
            Util.getMtextByLayerName(doc, DxfFileConstants.VERT_CLEAR_OHE);

            DXFDimension dimension = Util.getSingleDimensionByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE);
            if (dimension != null) {

                // LOG.info(dimension.getHorizontalAlign()+dimension.getLeadingLineLength()+"xxx:"+(dimension.getBounds().getMaximumX()-dimension.getBounds().getMinimumX()));
                // LOG.info(dimension.getLeadingLineLength()+"yyy:"+(dimension.getBounds().getMaximumY()-dimension.getBounds().getMinimumY()));
                double x = dimension.getBounds().getMaximumY() - dimension.getBounds().getMinimumY();
                line.setVerticalDistance(BigDecimal.valueOf(x));
                line.setPresentInDxf(true);
            } else {

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
        else
            pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.HORIZ_CLEAR_OHE2 + " or "
                    + DxfFileConstants.VERT_CLEAR_OHE);
        pl.setElectricLine(line);

        return pl;
    }

}
