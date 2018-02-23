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


    private static final String SUB_RULE_24_3 = "SUB_RULE_24_3";
    private static final String SUB_RULE_24_3DESCRIPTION = "SUB_RULE_24_3";
    private static final String SUB_RULE_24_4 = "SUB_RULE_24_4";
    private static final String SUB_RULE_24_4DESCRIPTION = "SUB_RULE_24_4";
    private static final String SUB_RULE_24_5 = "SUB_RULE_23_5";
    private static final String SUB_RULE_24_5DESCRIPTION = "SUB_RULE_24_5";
    private String MEAN_MINIMUM = "(Mean distince,Minimum distance) ";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null) {

            if (planDetail.getPlot() != null && (planDetail.getPlot().getFrontYard() == null ||
                    !planDetail.getPlot().getFrontYard().getPresentInDxf())) {
                errors.put(DcrConstants.FRONT_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FRONT_YARD_DESC));
                planDetail.addErrors(errors);
            }
            if (planDetail.getPlot() != null && (planDetail.getPlot().getSideYard1() == null ||
                    !planDetail.getPlot().getSideYard1().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD1_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD1_DESC));
                planDetail.addErrors(errors);
            }
            if (planDetail.getPlot() != null && (planDetail.getPlot().getSideYard2() == null ||
                    !planDetail.getPlot().getSideYard2().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD2_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD2_DESC));
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

        rule24_3(planDetail);

        //Building Height between 7 to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {
            rule24_4_Between7to10(planDetail);
            rule24_5_Between7to10(planDetail);
        }

        //Building Height less than 7
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) < 0) {

            //OPENING PRESENT
            if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningPresent()) {
                rule24_4_lessThan7WithOpening(planDetail);
                rule24_5_LessThan7WithOpening(planDetail);
            }
            //NOC PRESENT
            if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocPresent()) {
                rule24_4_lessThan7WithoutOpeningNoc(planDetail);
                rule24_5_LessThan7WithoutOpeningNoc(planDetail);
            }
            //OPENING NOT PRESENT
            if (planDetail.getPlanInformation() != null && !planDetail.getPlanInformation().getOpeningPresent()) {
                rule24_5_LessThan7_WithoutOpening(planDetail);
                rule24_5_LessThan7_WithoutOpeningCorrespondingFloor(planDetail);
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
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4DESCRIPTION, DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4DESCRIPTION, DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }

        }
    }

    private void rule24_4_Between7to10(PlanDetail planDetail) {
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
            if (sideyard1.compareTo(SIDE1MINIMUM_DISTANCE) < 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(SIDE2MINIMUM_DISTANCE) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));


        } else {
            if (sideyard2.compareTo(SIDE1MINIMUM_DISTANCE) < 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(SIDE2MINIMUM_DISTANCE) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }

    private void rule24_5_Between7to10(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE) <= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }

    private void rule24_4_lessThan7WithOpening(PlanDetail planDetail) {
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
            if (sideyard1.compareTo(SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING) < 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));


        } else {
            if (sideyard2.compareTo(SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING) < 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }

    private void rule24_4_lessThan7WithoutOpeningNoc(PlanDetail planDetail) {
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
            if (sideyard1.compareTo(SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING) == 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));


        } else {
            if (sideyard2.compareTo(SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING) == 0)//side1>1
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING) < 0)//side2>2.2
            {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));

            } else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }

    private void rule24_5_LessThan7WithOpening(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE_WITHOPENING) <= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE_WITHOPENING) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOPENING.toString() + "," + REARYARDMEAN_DISTANCE_WITHOPENING + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOPENING.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }


    private void rule24_5_LessThan7_WithoutOpening(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING) <= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE_WITHOUTOPENING) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING.toString() + "," + REARYARDMEAN_DISTANCE_WITHOUTOPENING + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING.toString() + "," + REARYARDMEAN_DISTANCE_WITHOUTOPENING + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }

    private void rule24_5_LessThan7_WithoutOpeningCorrespondingFloor(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) <= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR.toString() + "," + REARYARDMEAN_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));


            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING.toString() + "," + REARYARDMEAN_DISTANCE_WITHOUTOPENING + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }

    private void rule24_5_LessThan7WithoutOpeningNoc(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null) {

            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC) == 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC.toString() + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_24_5, SUB_RULE_24_5DESCRIPTION, DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC.toString() + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            }
        }
    }
}
