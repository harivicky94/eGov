package org.egov.edcr.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.measurement.WasteDisposal;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.jfree.util.Log;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
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
        List<DXFLWPolyline> polyLinesByLayer;
        PlanDetail pl = new PlanDetail();

        try {
            Parser parser = ParserBuilder.createDefaultParser();
            parser.parse(dxfFile.getPath(), DXFParser.DEFAULT_ENCODING);
            // Extract DXF Data
            DXFDocument doc = parser.getDocument();

            pl.setPlanInformation(extractPlanInfo(doc));

            Plot plot = new Plot();
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, DcrConstants.PLOT_BOUNDARY);
            if (polyLinesByLayer.size() > 0){
                plot.setPolyLine(polyLinesByLayer.get(0));
                plot.setPresentInDxf(true);
            }
            else
                pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.PLOT_BOUNDARY }, null));
            pl.setPlot(plot);
            Building building = new Building();
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, DcrConstants.BUILDING_FOOT_PRINT);
            
            if (polyLinesByLayer.size() > 0)
            {
                building.setPolyLine(polyLinesByLayer.get(0));
                building.setPresentInDxf(true);
            }
            else
                pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.BUILDING_FOOT_PRINT }, null));
            pl.setBuilding(building);

            pl.getPlot().setFrontYard(getYard(pl, doc, DcrConstants.FRONT_YARD));
            pl.getPlot().setRearYard(getYard(pl, doc, DcrConstants.REAR_YARD));
            pl.getPlot().setSideYard1(getYard(pl, doc, DcrConstants.SIDE_YARD_1));
            pl.getPlot().setSideYard2(getYard(pl, doc, DcrConstants.SIDE_YARD_2));

            pl.getPlot().getFrontYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.FRONT_YARD));
            pl.getPlot().getSideYard1().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.SIDE_YARD_1));
            pl.getPlot().getSideYard2().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.SIDE_YARD_2));
            pl.getPlot().getRearYard().setMinimumDistance(MinDistance.getYardMinDistance(pl, DcrConstants.REAR_YARD));

            pl = extractRoadDetails(doc, pl);
            pl.setNotifiedRoads(new ArrayList<>());
            pl.setNonNotifiedRoads(new ArrayList<>());
            pl = extractUtilities(doc, pl);
            pl = extractOverheadElectricLines(doc, pl);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
            LOG.info(yardName+" Mean "+yard.getMean());
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
    private PlanInformation extractPlanInfo(DXFDocument doc) {
        PlanInformation pi = new PlanInformation();
        Map<String, String> planInfoProperties = Util.getPlanInfoProperties(doc);
        pi.setArchitectInformation(planInfoProperties.get(DcrConstants.ARCHITECTNAME));
        return pi;
    }

    private PlanDetail extractRoadDetails(DXFDocument doc, PlanDetail pl) {
        Util.getPolyLinesByLayer(doc, DcrConstants.NON_NOTIFIED_ROAD);
        Util.getPolyLinesByLayer(doc, DcrConstants.NOTIFIED_ROADS);

        List<DXFLine> distancesToRoads = Util.getLinesByLayer(doc, DcrConstants.SHORTEST_DISTANCE_TO_ROAD);
        if (distancesToRoads.size() > 0) {
            List<NotifiedRoad> notifiedRoads = new ArrayList<>();
            NotifiedRoad road = new NotifiedRoad();
            road.setPresentInDxf(true);
            road.setShortestDistanceToRoad(BigDecimal.valueOf(distancesToRoads.get(0).getLength()));
            notifiedRoads.add(road);
            pl.setNotifiedRoads(notifiedRoads);
        }

        return pl;

    }

    private PlanDetail extractUtilities(DXFDocument doc, PlanDetail pl) {
        List<DXFLWPolyline> wasterDisposalPolyLines = Util.getPolyLinesByLayer(doc, DcrConstants.LAYER_NAME_WASTE_DISPOSAL);
        if (wasterDisposalPolyLines.size() > 0) {
            WasteDisposal disposal = new WasteDisposal();
            disposal.setPresentInDxf(true);
            pl.getBuilding().setWasteDisposal(disposal);
        }

        return pl;
    }

    private PlanDetail extractOverheadElectricLines(DXFDocument doc, PlanDetail pl) {
        ElectricLine line = new ElectricLine();
        String voltage = Util.getMtextByLayerName(doc, DcrConstants.VOLTAGE);
        if (voltage != null)
            try {
                BigDecimal volt = BigDecimal.valueOf(Double.parseDouble(voltage));
                line.setVoltage(volt);
                line.setPresentInDxf(true);
            } catch (NumberFormatException e) {

            }

        DXFLine horiz_clear_OHE = Util.getSingleLineByLayer(doc, DcrConstants.HORIZ_CLEAR_OHE2);
        if (horiz_clear_OHE != null) {
            line.setHorizontalDistance(BigDecimal.valueOf(horiz_clear_OHE.getLength()));
            line.setPresentInDxf(true);
        }
        DXFLine vert_clear_OHE = Util.getSingleLineByLayer(doc, DcrConstants.VERT_CLEAR_OHE);

        if (vert_clear_OHE != null) {
            line.setVerticalDistance(BigDecimal.valueOf(vert_clear_OHE.getLength()));
            line.setPresentInDxf(true);
        }
        pl.setElectricLine(line);
        return pl;
    }

}
