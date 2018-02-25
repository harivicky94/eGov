package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Rule33 extends GeneralRule {

    private static final String SUB_RULE_33_1 = "33(1)";
    private static final String SUB_RULE_33_1_DESCRIPTION = "Access to building";

    private static final BigDecimal threeHundred =  BigDecimal.valueOf(300);
    private static final BigDecimal sixHundred =  BigDecimal.valueOf(600);
    private static final BigDecimal oneThousand =  BigDecimal.valueOf(1000);
    private static final BigDecimal oneThousandFiveHundred =  BigDecimal.valueOf(1500);
    private static final BigDecimal fourThousand =  BigDecimal.valueOf(4000);
    private static final BigDecimal sixThousand =  BigDecimal.valueOf(6000);
    private static final BigDecimal eightThousand =  BigDecimal.valueOf(8000);
    private static final BigDecimal twelveThousand =  BigDecimal.valueOf(12000);
    private static final BigDecimal eighteenThousand =  BigDecimal.valueOf(18000);
    private static final BigDecimal twentyFourThousand =  BigDecimal.valueOf(24000);

    private static final BigDecimal onePointTwoZero =  BigDecimal.valueOf(1.20);
    private static final BigDecimal twoPointZero =  BigDecimal.valueOf(2.0);
    private static final BigDecimal threePointZero =  BigDecimal.valueOf(3.0);
    private static final BigDecimal threePointSix =  BigDecimal.valueOf(3.6);
    private static final BigDecimal fivePointZero =  BigDecimal.valueOf(5.0);
    private static final BigDecimal sixPointZero =  BigDecimal.valueOf(6.0);
    private static final BigDecimal sevenPointZero =  BigDecimal.valueOf(7.0);
    private static final BigDecimal tenPointZero =  BigDecimal.valueOf(10.0);


    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<String, String>();
        System.out.println("validate 33");

        if (planDetail != null && planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getAccessWidth() != null) {
            errors.put(DxfFileConstants.ACCESS_WIDTH, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[]{DxfFileConstants.ACCESS_WIDTH}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }

        if (planDetail != null && planDetail.getBuilding() != null && planDetail.getBuilding().getTotalFloorArea() != null) {
            errors.put(DcrConstants.TOTAL_FLOOR_AREA, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[]{DcrConstants.TOTAL_FLOOR_AREA}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }

        if (planDetail != null && planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOccupancy() != null) {
            errors.put(DcrConstants.OCCUPANCY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new String[]{DcrConstants.OCCUPANCY}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }

        return planDetail;
    }

    public PlanDetail process(PlanDetail planDetail) {
        rule33_1(planDetail);
        return planDetail;

    }

    private void rule33_1(PlanDetail planDetail) {

        BigDecimal floorArea = planDetail.getBuilding().getTotalFloorArea();
        BigDecimal accessWidth = planDetail.getPlanInformation().getAccessWidth();


        if (planDetail.getPlanInformation().getOccupancy().toUpperCase().equals(DcrConstants.RESIDENTIAL)) {

            // condition 1 occupancy is residential floor area up to 300 m2, minimum access width = 1.20 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(threeHundred) != 1 && accessWidth.compareTo(onePointTwoZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        onePointTwoZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        onePointTwoZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 2 Occupancy is residential, floor area 300 to 600 m2 minmum access width = 2.0 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(threeHundred) != -1 && floorArea.compareTo(sixHundred) != 1 && accessWidth.compareTo(twoPointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        twoPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        twoPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 3 Occupancy is residential, floor area  600 to 1000 m2 minimum access width = 3.0 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(sixHundred) != -1 && floorArea.compareTo(oneThousand) != 1 && accessWidth.compareTo(threePointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        threePointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        threePointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 4 Occupancy is residential, floor area 1000 m2 to 4000 m2, minmum access width 3.6 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(oneThousand) != -1 && floorArea.compareTo(fourThousand) != 1 && accessWidth.compareTo(threePointSix) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        threePointSix.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        threePointSix.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 5 Occupancy is residential, floor area 4000 m2 to 8000 m2, minmum access width 5m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(fourThousand) != -1 && floorArea.compareTo(eightThousand) != 1 && accessWidth.compareTo(fivePointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        fivePointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        fivePointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 6 Occupancy is residential, floor area 8000 m2 to 18000 m2, minmum access width 6 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(eightThousand) != -1 && floorArea.compareTo(eighteenThousand) != 1 && accessWidth.compareTo(sixPointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        sixPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        sixPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 7 Occupancy is residential, floor area 18000m2 to 24000m2, minimum access width of 7 m
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(eighteenThousand) != -1 && floorArea.compareTo(twentyFourThousand) != 1 && accessWidth.compareTo(sevenPointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        sevenPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        sevenPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }

            // condition 8 Occupancy is residential, floor area above 24000 m2, minimum access width of 10 m.
            if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(twentyFourThousand) != -1 && accessWidth.compareTo(tenPointZero) != -1) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        tenPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                        SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                        tenPointZero.toString() + DcrConstants.IN_METER,
                        accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
            }
        } else {
            if (!planDetail.getPlanInformation().getOccupancy().toUpperCase().equals(DcrConstants.RESIDENTIAL)) {

                //condition 9 Occupancy other than residential, floor area up to 300 m2, minimum access width =1.20m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(threeHundred) != 1 && accessWidth.compareTo(onePointTwoZero) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            onePointTwoZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            onePointTwoZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                //condition 10 Occupancy other than residential, floor area  300 m2 to 1500 m2, minimum access width 3.60 m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(threeHundred) != -1 && floorArea.compareTo(oneThousandFiveHundred) != 1 && accessWidth.compareTo(threePointSix) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            threePointSix.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            threePointSix.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                //condition 11 Occupancy other than residential, floor area1500   m2 to 6000 m2, minimum access width = 5.0 m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(oneThousandFiveHundred) != -1 && floorArea.compareTo(sixThousand) != 1 && accessWidth.compareTo(fivePointZero) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            fivePointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            fivePointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                //condition 12 Occupancy other than residential, floor area 6000 m2 to 12000, minimum access width 6.0 m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(sixThousand) != -1 && floorArea.compareTo(twelveThousand) != 1 && accessWidth.compareTo(sixPointZero) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            sixPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            sixPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                //condition 13 Occupancy other than residential, floor area 12000to 18000m2, minimum access width 7.0m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(twelveThousand) != -1 && floorArea.compareTo(eighteenThousand) != 1 && accessWidth.compareTo(sevenPointZero) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            sevenPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            sevenPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                //condition 14 Occupancy other than residential, floor area above 18000 m2, minimum access width 10m
                if (floorArea.compareTo(BigDecimal.ZERO) != 0 && floorArea.compareTo(eighteenThousand) != -1 && accessWidth.compareTo(tenPointZero) != -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            tenPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER_SQR, Result.Accepted, null));
                } else {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                            SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                            tenPointZero.toString() + DcrConstants.IN_METER,
                            accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

            }

        }
    }
}