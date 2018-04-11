package org.egov.edcr.service;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Basement;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.ElectricLine;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanInformation;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.RoadOutput;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.measurement.WasteDisposal;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFBlock;
import org.kabeja.dxf.DXFConstants;
import org.kabeja.dxf.DXFDimension;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFEntity;
import org.kabeja.dxf.DXFLWPolyline;
import org.kabeja.dxf.DXFLayer;
import org.kabeja.dxf.DXFMText;
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

    private final Ray RAY_CASTING = new Ray(new Point(-1.123456789, -1.987654321, 0d));
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
            
            //TODO: move the occupancy to down side.
            pl.getPlanInformation().setOccupancy(dcrApplication.getPlanInformation().getOccupancy());
            pl.getPlanInformation().setOwnerName(dcrApplication.getPlanInformation().getOwnerName());

            extractPlotDetails(pl, doc);
         
            extractBuildingDetails(pl, doc);
            extractFloorDetails(doc, pl);
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

            if (pl.getBasement() != null) {
                pl.getPlot().setBsmtFrontYard(getYard(pl, doc, DxfFileConstants.BSMNT_FRONT_YARD));
                pl.getPlot().setBsmtRearYard(getYard(pl, doc, DxfFileConstants.BSMNT_REAR_YARD));
                pl.getPlot().setBsmtSideYard1(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_1));
                pl.getPlot().setBsmtSideYard2(getYard(pl, doc, DxfFileConstants.BSMNT_SIDE_YARD_2));

                pl.getPlot().getBsmtFrontYard()
                        .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_FRONT_YARD));
                pl.getPlot().getBsmtSideYard1()
                        .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_1));
                pl.getPlot().getBsmtSideYard2()
                        .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_SIDE_YARD_2));
                pl.getPlot().getBsmtRearYard()
                        .setMinimumDistance(MinDistance.getBasementYardMinDistance(pl, DxfFileConstants.BSMNT_REAR_YARD));
            }

            pl = extractRoadDetails(doc, pl);
            // pl.setNotifiedRoads(new ArrayList<>());
            // pl.setNonNotifiedRoads(new ArrayList<>());
            pl = extractUtilities(doc, pl);
            pl = extractOverheadElectricLines(doc, pl);
            pl = extractHeights(doc, pl);
        
            extractParkingDetails(doc, pl);

        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return pl;
    }

    private void extractParkingDetails(DXFDocument doc, PlanDetail pl) {

        List<DXFLWPolyline> residentialUnit = new LinkedList<DXFLWPolyline>();
        List<DXFLWPolyline> residentialUnitDeduction = new ArrayList<DXFLWPolyline>();
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
            Polygon polygon = Util.getPolygon(resUnit);
            Iterator vertexIterator = resUnit.getVertexIterator();
            List<Point> points = new ArrayList<>();
            while (vertexIterator.hasNext()) {
                DXFVertex next = (DXFVertex) vertexIterator.next();
                Point p = new Point(next.getX(), next.getY(), 0d);
                points.add(p);
            }

            BigDecimal deduction = BigDecimal.ZERO;
            for (DXFLWPolyline residentialDeduct : residentialUnitDeduction) {
                boolean contains = false;
                Iterator buildingIterator = residentialDeduct.getVertexIterator();
                while (buildingIterator.hasNext()) {
                    DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
                    Point point = dxfVertex.getPoint();
                    // Point point1=new org.egov.edcr.utility.math.Point(point.getX(), point.getY());
                    if (RAY_CASTING.contains(point, polygon)) {
                        contains = true;
                        Measurement measurement = new Measurement();
                        measurement.setPolyLine(residentialDeduct);
                        floorUnit.getDeductions().add(measurement);
                    }

                }
                if (contains) {
                    LOG.debug("current deduct " + deduction + "  :add deduct for rest unit " + i + " area added "
                            + Util.getPolyLineArea(residentialDeduct));
                    deduction = deduction.add(Util.getPolyLineArea(residentialDeduct));
                }

            }
            // unitWiseDeduction.put("resUnit"+i, deduction);

            floorUnit.setTotalUnitDeduction(deduction);
            pl.getFloorUnits().add(floorUnit);

        }

        layerPresent = doc.containsDXFLayer(DxfFileConstants.PARKING_SLOT);

        if (layerPresent) {
            List<DXFLWPolyline> bldparking = Util.getPolyLinesByLayer(doc, DxfFileConstants.PARKING_SLOT);
            if (!bldparking.isEmpty()) {
                if(LOG.isDebugEnabled()) LOG.debug("Parking slot ");
                for (DXFLWPolyline pline : bldparking) {
                    if(LOG.isDebugEnabled()) LOG.debug("Width:"+pline.getBounds().getWidth());
                    if(LOG.isDebugEnabled()) LOG.debug("Height"+pline.getBounds().getHeight());
                    Measurement measurement = new Measurement();
                    measurement.setWidth(BigDecimal.valueOf(pline.getBounds().getWidth()));
                    measurement.setHeight(BigDecimal.valueOf(pline.getBounds().getHeight()));
                    measurement.setPolyLine(pline);
                    pl.getParkingSlots().add(measurement);
                }
            }
        }
    }

    private void extractFloorDetails(DXFDocument doc, PlanDetail pl) {

        DXFLayer floorLayer = new DXFLayer();
        DXFLayer blockLayer = new DXFLayer();
    
        int blockNumber = 1;
        while (blockLayer != null) {
            int floorNo = -1;
            //Block already extracted, use the same to get floor details.
            Block block = pl.getBlockByName(DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber);
        if(block!=null ){
            Building building = block.getBuilding();

            while (floorLayer != null) {
                floorNo++;
                String floorName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber +"_"+ DxfFileConstants.FLOOR_NAME_PREFIX
                        + floorNo;
                floorLayer = doc.getDXFLayer(floorName);
                blockLayer = floorLayer;
                if (!floorLayer.getName().equalsIgnoreCase(floorName)) {
                    blockNumber++;
                    break;
                }
                Floor floor = new Floor();
                floor.setName(floorName);
                floor.setNumber(String.valueOf(floorNo));

                List dxfPolyLineEntities = floorLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
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
                if (!floor.getHabitableRooms().isEmpty() || !floor.getOpenSpaces().isEmpty() || floor.getExterior() != null)
                    building.getFloors().add(floor);

            }

            int negetivFloorNo = 0;
            while (block!=null && floorLayer != null) {
                negetivFloorNo--;
                String floorName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber +"_" + DxfFileConstants.FLOOR_NAME_PREFIX
                        + negetivFloorNo;
                floorLayer = doc.getDXFLayer(floorName);
                if (!floorLayer.getName().equalsIgnoreCase("FLOOR_" + negetivFloorNo)) {
                    negetivFloorNo--;
                    break;
                }
                Floor floor = new Floor();
                floor.setName(floorName);
                floor.setNumber(String.valueOf(negetivFloorNo));
                List dxfPolyLineEntities = floorLayer.getDXFEntities(DXFConstants.ENTITY_TYPE_LWPOLYLINE);
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
                if (!floor.getHabitableRooms().isEmpty() || !floor.getOpenSpaces().isEmpty() || floor.getExterior() != null)
                    building.getFloors().add(floor);

            }

        
            if (building.getFloors() != null && building.getFloors().size() > 0) {
                building.setMaxFloor(BigDecimal.valueOf(building.getFloors().size()));
                building.setFloorsAboveGround(BigDecimal.valueOf(building.getFloors().size()));
                building.setTotalFloors(BigDecimal.valueOf(building.getFloors().size()));
             //   block.setBuilding(building);
              //  pl.getBlocks().add(block);
            }
         } else
             break;
        }
        
    }

    private void extractBuildingDetails(PlanDetail pl, DXFDocument doc) {
        
        List<DXFLWPolyline> polyLinesByLayer;
        
        DXFLayer blockLayer = new DXFLayer();

        int blockNumber = 0;
        while (blockLayer != null) {
            blockNumber++;
            String blockName = DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber +"_" + DxfFileConstants.BUILDING_FOOT_PRINT;
            blockLayer = doc.getDXFLayer(blockName);
            if (!blockLayer.getName().equalsIgnoreCase(blockName)) {
                break;
            }
            polyLinesByLayer = Util.getPolyLinesByLayer(doc, blockName);

            Block block = new Block();
                block.setName(DxfFileConstants.BLOCK_NAME_PREFIX+blockNumber);
                block.setNumber(String.valueOf(blockNumber));

            Building building = new Building();
                building.setPolyLine(polyLinesByLayer.get(0));
                
                
            polyLinesByLayer = Util.getPolyLinesByLayer(doc,DxfFileConstants.BLOCK_NAME_PREFIX + blockNumber +"_" + DxfFileConstants.SHADE_OVERHANG);
            if (polyLinesByLayer.size() > 0) {
                Measurement shade = new Measurement();
                shade.setPolyLine(polyLinesByLayer.get(0));
                building.setShade(shade);
            }
            extractOpenStairs(doc, building);
            building.setPresentInDxf(true);
            block.setBuilding(building);
            pl.getBlocks().add(block);
         }
         
        if (pl.getBlocks().size()<=0 ) {
                      pl.addError(DxfFileConstants.BUILDING_FOOT_PRINT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.BUILDING_FOOT_PRINT }, null)); }


    }

    private void extractOpenStairs(DXFDocument doc, Building building) {
        //TODO: OPEN STAIR LOGIC CHANGE REQUIRED BASED ON BLOCKWISE.
        List<DXFDimension> lines = Util.getDimensionsByLayer(doc, DxfFileConstants.OPEN_STAIR);
        if (lines != null)
            for (Object dxfEntity : lines) {
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
                            Measurement openPlot = new Measurement();
                            openPlot
                                    .setMinimumDistance(value);
                            building.getOpenStairs().add(openPlot);
                        }

                    }
                }

            }
    }

    private void extractBasementDetails(PlanDetail pl, DXFDocument doc) {
        List<DXFLWPolyline> polyLinesByLayer = new ArrayList<>();

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
        // Plot plot = new Plot();
        polyLinesByLayer = Util.getPolyLinesByLayer(doc, DxfFileConstants.PLOT_BOUNDARY);
        if (polyLinesByLayer.size() > 0) {
            pl.getPlot().setPolyLine(polyLinesByLayer.get(0));
            // plot.setArea(Util.getPolyLineArea(plot.getPolyLine())); //The actual area of plot boundary not used. Plot area
            // decide by plan information table.
            // plot.setPresentInDxf(true);
        } else
            pl.addError(DxfFileConstants.PLOT_BOUNDARY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[] { DxfFileConstants.PLOT_BOUNDARY }, null));
        // pl.setPlot(plot);
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

        for(Block block: pl.getBlocks())
        {
            BigDecimal floorArea = BigDecimal.ZERO;

            for (Floor floor : block.getBuilding().getFloors()) {
                String buildUpAreaByFloor = DxfFileConstants.BLOCK_NAME_PREFIX + block.getNumber() + "_"
                        + DxfFileConstants.FLOOR_NAME_PREFIX
                        + floor.getNumber() + "_" + DxfFileConstants.BUILT_UP_AREA;

                List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, buildUpAreaByFloor);
                if (!bldgext.isEmpty()) {
                    for (DXFLWPolyline pline : bldgext) {

                        BigDecimal occupancyArea = Util.getPolyLineArea(pline);
                        floorArea = floorArea.add(occupancyArea);

                        Occupancy occupancy = new Occupancy();
                        occupancy.setPolyLine(pline);
                        occupancy.setArea(occupancyArea);
                        setOccupancyType(pline, occupancy);
                        floor.addOccupancy(occupancy);

                    }
                }

                String farDeductByFloor = DxfFileConstants.BLOCK_NAME_PREFIX + block.getNumber() + "_"
                        + DxfFileConstants.FLOOR_NAME_PREFIX
                        + floor.getNumber() + "_" + DxfFileConstants.FAR_DEDUCT;
                List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, farDeductByFloor);
                if (!bldDeduct.isEmpty())
                    for (DXFLWPolyline pline : bldDeduct) {
                        BigDecimal deductionArea = Util.getPolyLineArea(pline);

                        floorArea = floorArea.subtract(deductionArea);
                        Occupancy occupancy = new Occupancy();
                        occupancy.setDeductionPolyLine(pline);
                        occupancy.setDeduction(deductionArea);
                        setOccupancyType(pline, occupancy);
                        floor.subtractOccupancyArea(occupancy);

                    }
            }

            block.getBuilding().setTotalBuitUpArea(floorArea);
            if (LOG.isDebugEnabled())
                LOG.debug("floorArea:" + floorArea);
            block.getBuilding().setTotalFloorArea(floorArea);

            if (pl.getPlot().getArea() != null) {
                BigDecimal far = floorArea.divide(pl.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                        DcrConstants.ROUNDMODE_MEASUREMENTS);
                block.getBuilding().setFar(far);
            }

      

        if (block.getBuilding().getPolyLine() != null) {

            BigDecimal cvDeduct = BigDecimal.ZERO;
            BigDecimal buildingFootPrintArea = Util.getPolyLineArea(block.getBuilding().getPolyLine());
            List<DXFLWPolyline> cvDeductPlines = Util.getPolyLinesByLayer(doc, DxfFileConstants.COVERGAE_DEDUCT);
            if (!cvDeductPlines.isEmpty()) {
                for (DXFLWPolyline pline : cvDeductPlines)
                    cvDeduct.add(Util.getPolyLineArea(pline));
            }
            BigDecimal coverage = BigDecimal.valueOf(100);

            if (buildingFootPrintArea != null && pl.getPlanInformation().getPlotArea() != null
                    && pl.getPlanInformation().getPlotArea().intValue() > 0) {
                coverage = buildingFootPrintArea.subtract(cvDeduct).multiply(BigDecimal.valueOf(100)).divide(
                        pl.getPlanInformation().getPlotArea(),
                        DcrConstants.DECIMALDIGITS_MEASUREMENTS, DcrConstants.ROUNDMODE_MEASUREMENTS);
                pl.getBuilding().setCoverage(coverage);
                if(LOG.isDebugEnabled()) LOG.debug("coverage:" + coverage);
            } else {
                pl.addError(DxfFileConstants.COVERGAE_DEDUCT,
                        "Cannot calculate coverage as " + DxfFileConstants.BUILDING_FOOT_PRINT
                                + " or " + DxfFileConstants.PLOT_AREA + " is not defined");
            }

          }
        }
        return pl;

    }

    private void setOccupancyType(DXFLWPolyline pline, Occupancy occupancy) {
        if (pline.getColor() == DxfFileConstants.OCCUPANCY_A1_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_A1);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_A2_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_A2);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B1_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_B1);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B2_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_B2);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B3_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_B3);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_C_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_C);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_D_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_D);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_D1_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_D1);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_E_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_E);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_F_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_F);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_G1_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_G1);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_G2_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_G2);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_H_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_H);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_I1_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_I1);
        } else if (pline.getColor() == DxfFileConstants.OCCUPANCY_I2_COLOR_CODE) {
            occupancy.setType(OccupancyType.OCCUPANCY_I2);
        }
    }

    private Yard getYard(PlanDetail pl, DXFDocument doc, String yardName) {
        Yard yard = new Yard();
        List<DXFLWPolyline> frontYardLines = Util.getPolyLinesByLayer(doc, yardName);
        if (frontYardLines.size() > 0) {
            yard.setPolyLine(frontYardLines.get(0));
            yard.setArea(Util.getPolyLineArea(yard.getPolyLine()));
            yard.setMean(yard.getArea().divide(BigDecimal.valueOf(yard.getPolyLine().getBounds().getWidth()), 5,
                    RoundingMode.HALF_UP));
            if(LOG.isDebugEnabled()) LOG.debug(yardName + " Mean " + yard.getMean());
            yard.setPresentInDxf(true);

        } else
            pl.addError("", edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[] { yardName }, null));

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

        if (plotArea == null){
            Plot plot = new Plot();
            pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " is not defined in the Plan Information Layer");
            plot.setPresentInDxf(false);
            pl.setPlot(plot);
        }
        else
            try {
                Plot plot = new Plot();
                plotArea = plotArea.replaceAll("[^\\d.]", "");
                pi.setPlotArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
                plot.setArea(BigDecimal.valueOf(Double.parseDouble(plotArea)));
                plot.setPresentInDxf(true);
                pl.setPlot(plot);
            } catch (Exception e) {
                pl.addError(DxfFileConstants.PLOT_AREA, DxfFileConstants.PLOT_AREA + " contains non invalid values.");
            }
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

        // Find shortest distance for all road polylines

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

        /*
         * DXFLine horiz_clear_OHE = Util.getSingleLineByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2); if (horiz_clear_OHE !=
         * null) { line.setHorizontalDistance(BigDecimal.valueOf(horiz_clear_OHE.getLength())); line.setPresentInDxf(true); } else
         * {
         */
        BigDecimal dimension = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.HORIZ_CLEAR_OHE2, pl);
        if (dimension != null && dimension.compareTo(BigDecimal.ZERO) > 0) {
            line.setHorizontalDistance(dimension);
            line.setPresentInDxf(true);
        }
        // }
        /*
         * DXFLine vert_clear_OHE = Util.getSingleLineByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE); if (vert_clear_OHE != null) {
         * line.setVerticalDistance(BigDecimal.valueOf(vert_clear_OHE.getLength())); line.setPresentInDxf(true); } else {
         */
        // Util.getMtextByLayerName(doc, DxfFileConstants.VERT_CLEAR_OHE);

        BigDecimal dimensionVerticle = Util.getSingleDimensionValueByLayer(doc, DxfFileConstants.VERT_CLEAR_OHE, pl);
        if (dimensionVerticle != null && dimensionVerticle.compareTo(BigDecimal.ZERO) > 0) {
            line.setVerticalDistance(dimensionVerticle);
            line.setPresentInDxf(true);
        }

        // }

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
            if (dimension != null && dimension.compareTo(BigDecimal.ZERO) > 0)
                pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.HORIZ_CLEAR_OHE2);
            if (dimensionVerticle != null && dimensionVerticle.compareTo(BigDecimal.ZERO) > 0)
                pl.addError("VOLTAGE", "Voltage is not mentioned for the " + DxfFileConstants.VERT_CLEAR_OHE);
        }
        pl.setElectricLine(line);

        return pl;
    }

}
