package org.egov.edcr.rule;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.BUILT_UP_AREA;
import static org.egov.edcr.constants.DxfFileConstants.FLOOR_NAME_PREFIX;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Block;
import org.egov.edcr.entity.Building;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.Occupancy;
import org.egov.edcr.entity.OccupancyType;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.context.i18n.LocaleContextHolder;

public class FloorAreaRatioService extends GeneralRule implements RuleService {

    private static final Logger LOG = Logger.getLogger(FloorAreaRatioService.class);

    private static final String SUB_RULE_31_1 = "31(1)";
    private static final String SUB_RULE_31_1_DESCRIPTION = "FAR";
    private static final String FAR_WITH_ADDN_FEE = "Should be less than ?  with additional fee ? ";
    private static final String FAR = "Should be less than ? ";

    private static final BigDecimal onePointFive = BigDecimal.valueOf(1.5);
    private static final BigDecimal two = BigDecimal.valueOf(2.0);
    private static final BigDecimal twoPointFive = BigDecimal.valueOf(2.5);
    private static final BigDecimal three = BigDecimal.valueOf(3.0);
    private static final BigDecimal threePointFive = BigDecimal.valueOf(3.5);
    private static final BigDecimal four = BigDecimal.valueOf(4.0);

    String buildUpAreaByFloor = BLOCK_NAME_PREFIX + "?" + "_" + FLOOR_NAME_PREFIX + "?" + "_" + BUILT_UP_AREA;
    String farDeductByFloor = DxfFileConstants.BLOCK_NAME_PREFIX + "?" + "_" + DxfFileConstants.FLOOR_NAME_PREFIX
            + "?" + "_" + DxfFileConstants.FAR_DEDUCT;

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        extractTotalFloorArea(pl, doc);
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        HashMap<String, String> errors = new HashMap<>();
        for (Block block : pl.getBlocks()) {
            if (block.getBuilding().getTotalBuitUpArea() == null)
                errors.put(DcrConstants.FAR, getMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FAR));
            pl.addErrors(errors);
        }

        for (Block block : pl.getBlocks()) {
            if (block.getBuilding().getTotalFloorArea() == null)
                errors.put(DcrConstants.FAR, getMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FAR));
            pl.addErrors(errors);
        }

        return pl;
    }

    private String getMessage(String code, String args) {
        return edcrMessageSource.getMessage(code, new String[] { args }, LocaleContextHolder.getLocale());

    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        BigDecimal builtUpArea = BigDecimal.ZERO;
        OccupancyType mostRestrictiveOccupancy = OccupancyType.OCCUPANCY_A1;
        for (Block block : pl.getBlocks()) {
            builtUpArea = builtUpArea.add(block.getBuilding().getTotalBuitUpArea());
            mostRestrictiveOccupancy = block.getBuilding().getMostRestrictiveOccupancy();
        }
        BigDecimal far = builtUpArea.divide(pl.getPlot().getArea(), DECIMALDIGITS_MEASUREMENTS, ROUNDMODE_MEASUREMENTS);

        switch (mostRestrictiveOccupancy) {
        case OCCUPANCY_A1:
            processFar(pl, far, four, three);
            break;
        case OCCUPANCY_A2:
            processFar(pl, far, four, twoPointFive);
            break;
        case OCCUPANCY_B1:
        case OCCUPANCY_B2:
        case OCCUPANCY_B3:
            processFar(pl, far, three, twoPointFive);
            break;
        case OCCUPANCY_C:
            processFar(pl, far, threePointFive, twoPointFive);
            break;
        case OCCUPANCY_D:

        case OCCUPANCY_D1:
            processFar(pl, far, twoPointFive, onePointFive);
            break;
        case OCCUPANCY_E:
            processFar(pl, far, four, three);
            break;
        case OCCUPANCY_F:
            processFar(pl, far, four, three);
            break;
        case OCCUPANCY_G1:
            processFar(pl, far, twoPointFive, twoPointFive);
            break;
        case OCCUPANCY_G2:
            processFar(pl, far, four, threePointFive);
            break;
        case OCCUPANCY_H:
            processFar(pl, far, four, three);
            break;
        case OCCUPANCY_I1:
            processFar(pl, far, two, two);
            break;
        case OCCUPANCY_I2:
            processFar(pl, far, onePointFive, onePointFive);
            break;
        default:
            break;

        }

        return pl;
    }

    private void processFar(PlanDetail pl, BigDecimal far, BigDecimal upperLimit, BigDecimal additionFeeLimit) {
       
        if (far.doubleValue() <= upperLimit.doubleValue()) {
            if (far.doubleValue() > additionFeeLimit.doubleValue()) {
                BigDecimal additonalFee = pl.getPlot().getArea().multiply(new BigDecimal(5000)).multiply(far.subtract(additionFeeLimit));

                pl.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        String.format(FAR_WITH_ADDN_FEE, upperLimit, additonalFee),
                        far.toString(), Result.Verify, null));
            } else
                pl.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        String.format(FAR, upperLimit),
                        far.toString(), Result.Accepted, null));

        } else
            pl.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                    SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                    String.format(FAR, upperLimit),
                    far.toString(), Result.Not_Accepted, null));
    }

    /**
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
     */
    private PlanDetail extractTotalFloorArea(PlanDetail pl, DXFDocument doc) {
        EnumSet<OccupancyType> distinctOccupancyTypes = EnumSet.noneOf(OccupancyType.class);
        for (Block block : pl.getBlocks()) {
            BigDecimal floorArea = BigDecimal.ZERO;
            BigDecimal builtUpArea = BigDecimal.ZERO;

            for (Floor floor : block.getBuilding().getFloors()) {
                String layerName = String.format(buildUpAreaByFloor, block.getNumber(), floor.getNumber());
                List<DXFLWPolyline> bldgext = Util.getPolyLinesByLayer(doc, layerName);
                for (DXFLWPolyline pline : bldgext) {

                    BigDecimal occupancyArea = Util.getPolyLineArea(pline);
                    floorArea = floorArea.add(occupancyArea);
                    builtUpArea = builtUpArea.add(occupancyArea);

                    Occupancy occupancy = new Occupancy();
                    occupancy.setPolyLine(pline);
                    occupancy.setArea(occupancyArea);
                    setOccupancyType(pline, occupancy);
                    floor.addOccupancy(occupancy);
                    distinctOccupancyTypes.add(occupancy.getType());

                }

                String deductLayerName = String.format(farDeductByFloor, block.getNumber(), floor.getNumber());
                List<DXFLWPolyline> bldDeduct = Util.getPolyLinesByLayer(doc, deductLayerName);
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

            block.getBuilding().setTotalBuitUpArea(builtUpArea);
            block.getBuilding().setMostRestrictiveOccupancy(getMostRestrictiveOccupancy(block.getBuilding()));

            LOG.debug("floorArea:" + floorArea);
            block.getBuilding().setTotalFloorArea(floorArea);
            BigDecimal far = floorArea.divide(pl.getPlot().getArea(), DcrConstants.DECIMALDIGITS_MEASUREMENTS,
                    DcrConstants.ROUNDMODE_MEASUREMENTS);
            block.getBuilding().setFar(far);

            if (block.getBuilding().getPolyLine() != null) {

                BigDecimal cvDeduct = BigDecimal.ZERO;
                List<DXFLWPolyline> cvDeductPlines = Util.getPolyLinesByLayer(doc, DxfFileConstants.COVERGAE_DEDUCT);
                for (DXFLWPolyline pline : cvDeductPlines)
                    cvDeduct=   cvDeduct.add(Util.getPolyLineArea(pline));
                
            }
        }
        pl.getVirtualBuilding().setOccupancies(distinctOccupancyTypes);
        return pl;

    }

    private OccupancyType getMostRestrictiveOccupancy(Building building) {
        OccupancyType mostRestrict = null;
        LinkedList<OccupancyType> occupancies = new LinkedList<>();
        occupancies.add(OccupancyType.OCCUPANCY_A1);
        occupancies.add(OccupancyType.OCCUPANCY_A2);
        occupancies.add(OccupancyType.OCCUPANCY_B1);
        occupancies.add(OccupancyType.OCCUPANCY_B2);
        occupancies.add(OccupancyType.OCCUPANCY_B3);
        occupancies.add(OccupancyType.OCCUPANCY_C);
        occupancies.add(OccupancyType.OCCUPANCY_D);
        occupancies.add(OccupancyType.OCCUPANCY_D1);
        occupancies.add(OccupancyType.OCCUPANCY_E);
        occupancies.add(OccupancyType.OCCUPANCY_F);
        occupancies.add(OccupancyType.OCCUPANCY_G1);
        occupancies.add(OccupancyType.OCCUPANCY_G2);
        occupancies.add(OccupancyType.OCCUPANCY_H);
        occupancies.add(OccupancyType.OCCUPANCY_I1);
        occupancies.add(OccupancyType.OCCUPANCY_I2);

        for (Floor floor : building.getFloors())
            for (Occupancy occupancy : floor.getOccupancies())
            {
                  mostRestrict = occupancy.getType();
                  if (occupancies.indexOf(occupancy.getType()) > occupancies.indexOf(mostRestrict))
                    mostRestrict = occupancy.getType();
            }
        return mostRestrict;
    }

    private void setOccupancyType(DXFLWPolyline pline, Occupancy occupancy) {
        if (pline.getColor() == DxfFileConstants.OCCUPANCY_A1_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_A1);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_A2_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_A2);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B1_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_B1);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B2_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_B2);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_B3_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_B3);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_C_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_C);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_D_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_D);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_D1_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_D1);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_E_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_E);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_F_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_F);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_G1_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_G1);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_G2_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_G2);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_H_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_H);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_I1_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_I1);
        else if (pline.getColor() == DxfFileConstants.OCCUPANCY_I2_COLOR_CODE)
            occupancy.setType(OccupancyType.OCCUPANCY_I2);
    }

}
