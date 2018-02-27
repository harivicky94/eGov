package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;

@Service
public class Rule24 extends GeneralRule {

    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE = BigDecimal.valueOf(3);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE = BigDecimal.valueOf(2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(2.2);


    private static final BigDecimal SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING = BigDecimal.valueOf(0.75);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING = BigDecimal.valueOf(1.2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING = BigDecimal.ZERO;
    private static final BigDecimal SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING = BigDecimal.valueOf(1.2);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOPENING = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_WITHOPENING = BigDecimal.valueOf(2);


    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_WITHOUTOPENING = BigDecimal.valueOf(1.5);


    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR = BigDecimal.valueOf(1.5);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC = BigDecimal.ZERO;

    private static final String SUB_RULE_24_3 = "24(3)";
    private static final String SUB_RULE_24_3_DESCRIPTION = "Front yard distance";
    private static final String SUB_RULE_24_4 = "24(4)";
    private static final String SUB_RULE_24_4_DESCRIPTION = "Rear yard distance";
    private static final String SUB_RULE_24_5 = "23(5)";
    private static final String SUB_RULE_24_5_DESCRIPTION = "Side yard distance";
    private String MEAN_MINIMUM = "(Minimum distance,Mean distance) ";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null) {

            if (planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BUILDING_HEIGHT_DESC));
                planDetail.addErrors(errors);
            }

            if (planDetail.getPlot() != null && (planDetail.getPlot().getFrontYard() == null ||
                    !planDetail.getPlot().getFrontYard().getPresentInDxf())) {
                errors.put(DcrConstants.FRONT_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FRONT_YARD_DESC));
                planDetail.addErrors(errors);
            }

            //check either of side yard present or not
            if (planDetail.getPlot() != null
                    && ((planDetail.getPlot().getSideYard1() == null || !planDetail.getPlot().getSideYard1().getPresentInDxf()))
                    || (planDetail.getPlot().getSideYard2() == null || !planDetail.getPlot().getSideYard2().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD_DESC));
                planDetail.addErrors(errors);
            }

            if (planDetail.getPlot() != null && (planDetail.getPlot().getRearYard() == null ||
                    !planDetail.getPlot().getRearYard().getPresentInDxf())) {
                errors.put(DcrConstants.REAR_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.REAR_YARD_DESC));
                planDetail.addErrors(errors);
            }
        }

        return planDetail;
    }

    private String prepareMessage(String code, String args) {
        return edcrMessageSource.getMessage(code,
                new String[]{args}, LocaleContextHolder.getLocale());
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        //For building of heights less than or equal to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && ((planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0))) {
            rule24_3(planDetail);
        }

        //Building Height between 7 to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {
            rule24_4(planDetail, SIDE1MINIMUM_DISTANCE, SIDE2MINIMUM_DISTANCE);
            rule24_5(planDetail, REARYARDMINIMUM_DISTANCE, REARYARDMEAN_DISTANCE);
        }

        //Building Height less than 7
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) < 0) {

            //OPENING PRESENT
            if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningPresent()) {
                rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING);
                rule24_5(planDetail, REARYARDMINIMUM_DISTANCE_WITHOPENING, REARYARDMEAN_DISTANCE_WITHOPENING);
            } else {
                rule24_5(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING, REARYARDMEAN_DISTANCE_WITHOUTOPENING);
                rule24_5(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR, REARYARDMEAN_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR);

                //WITHOUT OPENING AND NOC PRESENT
                if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocPresent()) {
                    rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING);
                    rule24_5(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC);
                }
            }

        }
        return planDetail;
    }

    private void rule24_3(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getFrontYard() != null &&
                planDetail.getPlot().getFrontYard().getPresentInDxf()
                && planDetail.getPlot().getFrontYard().getMinimumDistance() != null
                && planDetail.getPlot().getFrontYard().getMean() != null) {

            if (planDetail.getPlot().getFrontYard().getMinimumDistance().compareTo(FRONTYARDMINIMUM_DISTANCE) >= 0 &&
                    planDetail.getPlot().getFrontYard().getMean().compareTo(FRONTYARDMEAN_DISTANCE) >= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION, DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION, DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }

        }
    }


    private void rule24_4(PlanDetail planDetail, BigDecimal sideYard1MinDist, BigDecimal sideYard2MinDist) {
        BigDecimal sideyard1 = BigDecimal.ZERO;
        BigDecimal sideyard2 = BigDecimal.ZERO;

        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard1() != null &&
                planDetail.getPlot().getSideYard1().getPresentInDxf()
                && planDetail.getPlot().getSideYard1().getMinimumDistance() != null
                ) {
            sideyard1 = planDetail.getPlot().getSideYard1().getMinimumDistance();
        }
        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard2() != null &&
                planDetail.getPlot().getSideYard2().getPresentInDxf()
                && planDetail.getPlot().getSideYard2().getMinimumDistance() != null
                ) {
            sideyard2 = planDetail.getPlot().getSideYard2().getMinimumDistance();
        }

        if (sideyard1.compareTo(sideyard2) > 0) {
            if (sideyard1.compareTo(sideYard1MinDist) == 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(sideYard2MinDist) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));


        } else {
            if (sideyard2.compareTo(sideYard1MinDist) == 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(sideYard2MinDist) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }


    private void rule24_5(PlanDetail planDetail, BigDecimal rearYardMin, BigDecimal rearYardMean) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(rearYardMin) <= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(rearYardMean) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }

}
