package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Rule31 extends GeneralRule {

    private static final String SUB_RULE_31_1 = "31(1)";
    private static final String SUB_RULE_31_2 = "31(2)";
    private static final String SUB_RULE_31_1_DESCRIPTION = "FAR";
    private static final String SUB_RULE_31_2_DESCRIPTION = "FAR Coverage";

    private static final BigDecimal thirtyFive = BigDecimal.valueOf(35);
    private static final BigDecimal fourty = BigDecimal.valueOf(40);
    private static final BigDecimal fourtyFive = BigDecimal.valueOf(45);
    private static final BigDecimal sixty = BigDecimal.valueOf(60);
    private static final BigDecimal sixtyFive = BigDecimal.valueOf(65);
    private static final BigDecimal seventy = BigDecimal.valueOf(70);
    private static final BigDecimal seventyFive = BigDecimal.valueOf(75);
    private static final BigDecimal eighty = BigDecimal.valueOf(80);
    private static final BigDecimal hundred = BigDecimal.valueOf(100);

    private static final BigDecimal onePointFive = BigDecimal.valueOf(1.5);
    private static final BigDecimal two = BigDecimal.valueOf(2.0);
    private static final BigDecimal twoPointFive = BigDecimal.valueOf(2.5);
    private static final BigDecimal three = BigDecimal.valueOf(3.0);
    private static final BigDecimal threePointFive = BigDecimal.valueOf(3.5);
    private static final BigDecimal four = BigDecimal.valueOf(4.0);
    private static final BigDecimal fiveThousand = BigDecimal.valueOf(5000);

    BigDecimal sumOfBuildingExteriorWall = BigDecimal.valueOf(0);
    BigDecimal sumOfFARDeduct = BigDecimal.valueOf(0);
    BigDecimal substractFarAndBuildingExterior = BigDecimal.valueOf(0);
    BigDecimal floorAreaRatio = BigDecimal.valueOf(0);
    BigDecimal plotArea = BigDecimal.valueOf(0);
    BigDecimal additionalFee = BigDecimal.valueOf(0);
    BigDecimal coverage = BigDecimal.valueOf(0);
    BigDecimal sumOfCoverageDeduct = BigDecimal.valueOf(0);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 31");
        if(planDetail != null) {
            if ( planDetail.getBuilding().getFar() == null) {
                errors.put(DcrConstants.FAR, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.FAR}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if ( planDetail.getPlanInformation() == null
                    || planDetail.getPlanInformation().getOccupancy() == null) {
                errors.put(DcrConstants.OCCUPANCY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.OCCUPANCY}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if ( planDetail.getBuilding().getFar() == null
                    || planDetail.getPlanInformation().getOccupancy() == null) {
                errors.put(DcrConstants.FAR, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.FAR}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
        }else {
            errors.put(DcrConstants.PLAN_DETAIL, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[]{DcrConstants.PLAN_DETAIL}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }

        return planDetail;

    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule_31_1(planDetail);
        rule_31_2(planDetail);
        return planDetail;
    }

    /* need to set coverage data and occupency type and future change condition on basis of occupancy types */
    private void rule_31_1(PlanDetail planDetail) {

        floorAreaRatio = planDetail.getBuilding().getFar();
        if (floorAreaRatio == null)
            floorAreaRatio = BigDecimal.ZERO;
        // currently we arer using  occupany as  RESIDENTIAL,
        if (planDetail.getPlanInformation().getOccupancy().toUpperCase().equals(DcrConstants.RESIDENTIAL)) {

            // 3) If occupany is A1 FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-3)*PLot area
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(three).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 4) If occupany is A2, FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-2.5)*Plot area
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(twoPointFive).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

/*            // 5) If occupany is B, FAR should be less than 3, with additional fee of @ Rs.5000 x (FAR-2.5)*Plot
            // area
            if (floorAreaRatio.compareTo(three) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(twoPointFive).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+three.toString() ,
                        floorAreaRatio.toString() , Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+three.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 6) If occupany is C, FAR should be less than 3.5, with additional fee of @ Rs.5000 x (FAR-2.5)*Plot
            // area
            if (floorAreaRatio.compareTo(threePointFive) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(twoPointFive).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+threePointFive.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+threePointFive.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 7) If occupany is D, FAR should be less than 2.5, with additional fee of @ Rs.5000 x (FAR-1.5)*Plot
            // area
            if (floorAreaRatio.compareTo(twoPointFive) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(onePointFive).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+twoPointFive.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+twoPointFive.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 8) If occupany is E FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-3)*Plot area
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(three).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 9) If occupany is F, FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-3)
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(three).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 10) If occupany is G1, FAR should be less than 2.5
            if (floorAreaRatio.compareTo(twoPointFive) == -1)
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+twoPointFive.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+twoPointFive.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 11) If occupany is G2, FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-3.5)*Plot
            // area
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(threePointFive).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 12) If occupany is H, FAR should be less than 4, with additional fee of @ Rs.5000 x (FAR-3)*Plot area
            if (floorAreaRatio.compareTo(four) == -1) {
                additionalFee = fiveThousand.multiply(floorAreaRatio.subtract(three).multiply(plotArea));
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            } else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+four.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 13) If occupany is I1, FAR should be less than 2
            if (floorAreaRatio.compareTo(two) == -1)
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+two.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+two.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));

            // 14) If occupany is I2, FAR should be less than 1.5
            if (floorAreaRatio.compareTo(onePointFive) == -1)
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+onePointFive.toString(),
                        floorAreaRatio.toString(), Result.Accepted, null));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_1,
                        SUB_RULE_31_1_DESCRIPTION, DcrConstants.FAR,
                        "Should be less than "+onePointFive.toString(),
                        floorAreaRatio.toString(), Result.Not_Accepted, null));*/

        }

    }

    /* need to set coverage data and occupency type and future change condition on basis of occupancy types */
    private void rule_31_2(PlanDetail planDetail) {
        // Occpancy RECIDENTIAL
        if (planDetail.getPlanInformation().getOccupancy().toUpperCase().equals( DcrConstants.RESIDENTIAL)) {

            // 1)Coverage =( (Area of building_footprint polygon ) - (Sum of areas of polygons in coverage_deduct layer)) x 100 / plot area
            coverage = planDetail.getBuilding().getCoverage();

            // 2) If occupany is A1 , Coverage should be less than 65
            if (coverage.compareTo(BigDecimal.ZERO) > 0) {
                if (coverage.compareTo(sixtyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            "should less than " + sixtyFive.toString(),
                            coverage.toString(), Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            "should less than " + sixtyFive.toString(),
                            coverage.toString(), Result.Not_Accepted, null));

                // 3) If occupany is A2, Coverage should be less than 65
                if (coverage.compareTo(sixtyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            "should less than " + sixtyFive.toString(),
                            coverage.toString(), Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            "should less than " + sixtyFive.toString(),
                            coverage.toString(), Result.Not_Accepted, null));

                /*// 4) If occupany is B, Coverage should be less than 35
                if (coverage.compareTo(thirtyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 5) If occupany is C, Coverage should be less than 60
                if (coverage.compareTo(sixty) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 6) If occupany is D, Coverage should be less than 40
                if (coverage.compareTo(fourty) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 7) If occupany is E, Coverage should be less than 70
                if (coverage.compareTo(seventy) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 8) If occupany is F, Coverage should be less than 70
                if (coverage.compareTo(seventy) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 9) If occupany is G1, Coverage should be less than 65
                if (coverage.compareTo(sixtyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 10) If occupany is G2, Coverage should be less than 75
                if (coverage.compareTo(seventyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 11) If occupany is H, Coverage should be less than 80
                if (coverage.compareTo(eighty) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));

                // 12) If occupany is I1, Coverage should be less than 45
                if (coverage.compareTo(fourtyFive) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));


                // 13) If occupany is I2, Coverage should be less than 40
                if (coverage.compareTo(fourty) == -1) {
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Accepted, null));
                } else
                    planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                            SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                            coverage.toString() ,
                            coverage.toString() , Result.Not_Accepted, null));*/

            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE31, SUB_RULE_31_2,
                        SUB_RULE_31_2_DESCRIPTION, DcrConstants.COVERAGE,
                        "Should be greater than Zero",
                        coverage.toString(), Result.Not_Accepted, null));
            }
        }
    }
}
