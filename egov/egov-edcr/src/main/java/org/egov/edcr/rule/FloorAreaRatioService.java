package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;

public class FloorAreaRatioService implements RuleService {
    private static Logger LOG = Logger.getLogger(FloorAreaRatioService.class);
    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractTotalFloorArea(pl,doc) ;
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        // TODO Auto-generated method stub
        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        // TODO Auto-generated method stub
        return pl;
    }
    
    /**
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
     */
    private PlanDetail extractTotalFloorArea(PlanDetail pl,DXFDocument doc ) {

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
                block.getBuilding().setCoverage(coverage);
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
}
