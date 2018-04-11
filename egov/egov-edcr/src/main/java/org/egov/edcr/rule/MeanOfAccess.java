package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFMText;
public class MeanOfAccess implements RuleService {

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
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
        List<DXFLWPolyline> culdSacRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.CULD_1);
        for (DXFLWPolyline roadPline : culdSacRoads) {

            CulDeSacRoad road = new CulDeSacRoad();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getCuldeSacRoads().add(road);

        }
        List<DXFLWPolyline> laneRoads = Util.getPolyLinesByLayer(doc, DxfFileConstants.LANE_1);
        for (DXFLWPolyline roadPline : laneRoads) {
            Lane road = new Lane();
            road.setPresentInDxf(true);
            road.setPolyLine(roadPline);
            pl.getLaneRoads().add(road);

        }

        extractShortestDistanceToPlotFromRoadCenter(doc, pl);
        extractShortestDistanceToPlot(doc, pl);
        extractDistanceFromBuildingToRoadEnd(doc, pl);

        return pl;

    }

    private void extractShortestDistanceToPlotFromRoadCenter(DXFDocument doc, PlanDetail pl) {
        List<DXFDimension> shortestDistanceCentralLineRoadDimension = Util.getDimensionsByLayer(doc,
                DxfFileConstants.DIST_CL_ROAD);
        List<RoadOutput> shortDistainceFromCenter = new ArrayList<RoadOutput>();

        shortDistainceFromCenter = roadDistanceWithColourCode(doc, shortestDistanceCentralLineRoadDimension,
                shortDistainceFromCenter);

        List<BigDecimal> notifiedRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> nonNotifiedRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> culdesacRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> laneDistance = new ArrayList<BigDecimal>();

        for (RoadOutput roadOutput : shortDistainceFromCenter) {
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD) {
                notifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD) {
                nonNotifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC) {
                culdesacRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_LANE) {
                laneDistance.add(roadOutput.roadDistainceToPlot);
            }
        }

        for (int i = 0; i < pl.getNotifiedRoads().size(); i++) {
            if (i < notifiedRoadDistance.size())
                pl.getNotifiedRoads().get(i).setDistanceFromCenterToPlot(notifiedRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getNonNotifiedRoads().size(); i++) {
            if (i < nonNotifiedRoadDistance.size())
                pl.getNonNotifiedRoads().get(i).setDistanceFromCenterToPlot(nonNotifiedRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getCuldeSacRoads().size(); i++) {
            if (i < culdesacRoadDistance.size())
                pl.getCuldeSacRoads().get(i).setDistanceFromCenterToPlot(culdesacRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getLaneRoads().size(); i++) {
            if (i < laneDistance.size())
                pl.getLaneRoads().get(i).setDistanceFromCenterToPlot(laneDistance.get(i));
        }
    }

    private void extractShortestDistanceToPlot(DXFDocument doc, PlanDetail pl) {
        List<DXFDimension> shortestDistanceDimension = Util.getDimensionsByLayer(doc, DxfFileConstants.SHORTEST_DISTANCE_TO_ROAD);
        List<RoadOutput> shortDistaineToPlot = new ArrayList<RoadOutput>();

        shortDistaineToPlot = roadDistanceWithColourCode(doc, shortestDistanceDimension, shortDistaineToPlot);

        List<BigDecimal> notifiedRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> nonNotifiedRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> culdesacRoadDistance = new ArrayList<BigDecimal>();
        List<BigDecimal> laneDistance = new ArrayList<BigDecimal>();

        for (RoadOutput roadOutput : shortDistaineToPlot) {
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD) {
                notifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD) {
                nonNotifiedRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC) {
                culdesacRoadDistance.add(roadOutput.roadDistainceToPlot);
            }
            if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_LANE) {
                laneDistance.add(roadOutput.roadDistainceToPlot);
            }
        }

        for (int i = 0; i < pl.getNotifiedRoads().size(); i++) {
            if (i < notifiedRoadDistance.size())
                pl.getNotifiedRoads().get(i).setShortestDistanceToRoad(notifiedRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getNonNotifiedRoads().size(); i++) {
            if (i < nonNotifiedRoadDistance.size())
                pl.getNonNotifiedRoads().get(i).setShortestDistanceToRoad(nonNotifiedRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getCuldeSacRoads().size(); i++) {
            if (i < culdesacRoadDistance.size())
                pl.getCuldeSacRoads().get(i).setShortestDistanceToRoad(culdesacRoadDistance.get(i));
        }
        for (int i = 0; i < pl.getLaneRoads().size(); i++) {
            if (i < laneDistance.size())
                pl.getLaneRoads().get(i).setShortestDistanceToRoad(laneDistance.get(i));
        }
    }

    private List<RoadOutput> roadDistanceWithColourCode(DXFDocument doc,
            List<DXFDimension> shortestDistanceCentralLineRoadDimension,
            List<RoadOutput> clcolourCodeWithDimension) {
        if (null != shortestDistanceCentralLineRoadDimension) {

            for (Object dxfEntity : shortestDistanceCentralLineRoadDimension) {
                BigDecimal value = BigDecimal.ZERO;

                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();
                        if (text2.contains(";")) {
                            text2 = text2.split(";")[1];
                        } else

                            text2 = text2.replaceAll("[^\\d.]", "");
                        ;
                        if (!text2.isEmpty()) {
                            value = BigDecimal.valueOf(Double.parseDouble(text2));
                            RoadOutput roadOutput = new RoadOutput();
                            roadOutput.roadDistainceToPlot = value;
                            roadOutput.colourCode = String.valueOf(line.getColor());
                            clcolourCodeWithDimension.add(roadOutput);
                        }

                    }
                }

            }
        }
        return clcolourCodeWithDimension;
    }

    private void extractDistanceFromBuildingToRoadEnd(DXFDocument doc, PlanDetail pl) {
        List<DXFDimension> dxfLineEntities = Util.getDimensionsByLayer(doc, DxfFileConstants.MAX_HEIGHT_CAL);
        BigDecimal dimension = BigDecimal.ZERO;
        BigDecimal minDimension = BigDecimal.ZERO;
        if (null != dxfLineEntities) {
            for (Object dxfEntity : dxfLineEntities) {
                DXFDimension line = (DXFDimension) dxfEntity;
                String dimensionBlock = line.getDimensionBlock();
                DXFBlock dxfBlock = doc.getDXFBlock(dimensionBlock);
                Iterator dxfEntitiesIterator = dxfBlock.getDXFEntitiesIterator();
                while (dxfEntitiesIterator.hasNext()) {
                    DXFEntity e = (DXFEntity) dxfEntitiesIterator.next();
                    if (e.getType().equals(DXFConstants.ENTITY_TYPE_MTEXT)) {
                        DXFMText text = (DXFMText) e;
                        String text2 = text.getText();
                        if (text2.contains(";")) {
                            text2 = text2.split(";")[1];
                        } else
                            text2 = text2.replaceAll("[^\\d.]", "");
                        ;
                        if (!text2.isEmpty()) {
                            dimension = BigDecimal.valueOf(Double.parseDouble(text2));
                            if (minDimension.compareTo(BigDecimal.ZERO) > 0 &&
                                    dimension.compareTo(minDimension) <= 0) // TODO: CHECK WHETHER SHORTEST OR LONGEST ROAD TO BE
                                // SELECTED .
                                minDimension = dimension;
                            else
                                minDimension = dimension;
                        }

                    }
                }

            }
            pl.getBuilding().setDistanceFromBuildingFootPrintToRoadEnd(minDimension);

        }
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        // TODO Auto-generated method stub
        return null;
    }

}
