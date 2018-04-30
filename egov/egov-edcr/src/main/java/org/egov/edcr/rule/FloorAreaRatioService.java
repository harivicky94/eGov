package org.egov.edcr.rule;

import static org.egov.edcr.constants.DxfFileConstants.BLOCK_NAME_PREFIX;
import static org.egov.edcr.constants.DxfFileConstants.BUILT_UP_AREA;
import static org.egov.edcr.constants.DxfFileConstants.FLOOR_NAME_PREFIX;
import static org.egov.edcr.utility.DcrConstants.DECIMALDIGITS_MEASUREMENTS;
import static org.egov.edcr.utility.DcrConstants.ROUNDMODE_MEASUREMENTS;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashMap;
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
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.stereotype.Service;

@Service
public class FloorAreaRatioService extends GeneralRule implements RuleService {

    private static final Logger LOG = Logger.getLogger(FloorAreaRatioService.class);

    private static final String RULE_NAME_KEY = "far.rulename";
    private static final String RULE_DESCRIPTION_KEY = "far.description";
    private static final String RULE_EXPECTED_KEY = "far.expected";
    private static final String RULE_ACTUAL_KEY = "far.actual";  
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
                errors.put(DcrConstants.FAR, getLocaleMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FAR));
            pl.addErrors(errors);
        }

        for (Block block : pl.getBlocks()) {
            if (block.getBuilding().getTotalFloorArea() == null)
                errors.put(DcrConstants.FAR, getLocaleMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FAR));
            pl.addErrors(errors);
        }

        return pl;
    }

    @Override
    public PlanDetail process(PlanDetail pl) {
        
        BigDecimal builtUpArea = pl.getVirtualBuilding().getTotalBuitUpArea();
        OccupancyType   mostRestrictiveOccupancy=  pl.getVirtualBuilding().getMostRestrictive();
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
                BigDecimal additonalFee = pl.getPlot().getArea().multiply(new BigDecimal(5000))
                        .multiply(far.subtract(additionFeeLimit));
                
                String actualResult = getLocaleMessage(RULE_ACTUAL_KEY, additonalFee.toString());
                String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY, additonalFee.toString());
                pl.reportOutput.add(buildResult(actualResult, expectedResult, Result.Verify));
            } else {

                String actualResult = getLocaleMessage(RULE_ACTUAL_KEY);
                String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY);
                pl.reportOutput.add(buildResult(actualResult, expectedResult, Result.Accepted));
            }
        } else {
            String actualResult = getLocaleMessage(RULE_ACTUAL_KEY);
            String expectedResult = getLocaleMessage(RULE_EXPECTED_KEY);
            pl.reportOutput.add(buildResult(actualResult, expectedResult, Result.Not_Accepted));

        }

    }

    private RuleOutput buildResult(String expectedResult, String actualResult, Result result) {
        RuleOutput ruleOutput = new RuleOutput();
        ruleOutput.key = getLocaleMessage(RULE_NAME_KEY);
        ruleOutput.ruleDescription = getLocaleMessage(RULE_DESCRIPTION_KEY);
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        RuleReportOutput ruleReportOutput = new RuleReportOutput();
        ruleReportOutput.setActualResult(actualResult);
        ruleReportOutput.setExpectedResult(expectedResult);
        ruleReportOutput.setFieldVerified(ruleOutput.key);
        ruleReportOutput.setStatus(result.name());
        subRuleOutput.add(ruleReportOutput);
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;

    }

    /**
     * @param doc
     * @param pl
     * @return 1) Floor area = (sum of areas of all polygon in Building_exterior_wall layer) - (sum of all polygons in FAR_deduct
     * layer) Color is not available here when color availble change to getPolyLinesByLayerAndColor Api if required
     */
    private PlanDetail extractTotalFloorArea(PlanDetail pl, DXFDocument doc) {
        EnumSet<OccupancyType> distinctOccupancyTypes = EnumSet.noneOf(OccupancyType.class);
        BigDecimal totalBuiltUpArea=BigDecimal.ZERO;
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
            totalBuiltUpArea.add(builtUpArea);
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
                    cvDeduct = cvDeduct.add(Util.getPolyLineArea(pline));

            }
        }
        pl.getVirtualBuilding().setOccupancies(distinctOccupancyTypes);
        pl.getVirtualBuilding().setTotalBuitUpArea(totalBuiltUpArea);
        distinctOccupancyTypes.size();
        pl.getVirtualBuilding().setMostRestrictive(OccupancyType.OCCUPANCY_A1);   
        
       /* OccupancyType mostRestrict;
        for (OccupancyType occupancy : distinctOccupancyTypes) {
            mostRestrict = occupancy;
            if (OccupancyType.valueOf(occupancy) > occupancies.indexOf(mostRestrict))
                mostRestrict = occupancy.getType();
        }*/
        return pl;

    }

    private OccupancyType getMostRestrictiveOccupancy(Building building) {
        OccupancyType mostRestrict = null;
      /*  List<OccupancyType> list;
        list.addAll( OccupancyType.values())
        OccupancyType[] values = OccupancyType.values();
        

        for (Floor floor : building.getFloors())
            for (Occupancy occupancy : floor.getOccupancies()) {
                mostRestrict = occupancy.getType();
                if (values.indexOf(occupancy.getType()) > occupancies.indexOf(mostRestrict))
                    mostRestrict = occupancy.getType();
            }*/
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
