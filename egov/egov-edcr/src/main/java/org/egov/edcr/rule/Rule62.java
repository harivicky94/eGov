package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Rule62 extends GeneralRule {
    private static final BigDecimal _NOTIFIEDROADDISTINCE = BigDecimal.valueOf(3);
    private static final BigDecimal _NONNOTIFIEDROADDISTINCE = BigDecimal.valueOf(2);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1.2);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE = BigDecimal.valueOf(1.8);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE = BigDecimal.valueOf(0.5);
    private static final BigDecimal REARYARDMEAN_DISTANCE = BigDecimal.valueOf(1);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE = BigDecimal.valueOf(0.9);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(0.6);
    private static final String SUB_RULE_62_2 = "SUB_RULE_62_2";
    private static final String SUB_RULE_62_2DESCRIPTION = "SUB_RULE_62_2";
    private static final String SUB_RULE_62_1 = "SUB_RULE_62_1";
    private static final String SUB_RULE_62_1DESCRIPTION = "SUB_RULE_62_1";
    private static final String SUB_RULE_62_1A = "SUB_RULE_62_1A";
    private static final String SUB_RULE_62_1A_DESCRIPTION = "SUB_RULE_62_1A";
    private static final String SUB_RULE_62_3 = "SUB_RULE_62_3";
    private static final String SUB_RULE_62_3_DESCRIPTION = "SUB_RULE_62_3";
    private String MEAN_MINIMUM = "(Mean distince,Minimum distance) ";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null) {

            // If either notified or non notified road width not defined, then show error.
            if ((planDetail.getNotifiedRoads() == null || planDetail.getNonNotifiedRoads() == null) &&
                    !(planDetail.getNotifiedRoads().size() > 0 ||
                            planDetail.getNonNotifiedRoads().size() > 0)) {
                errors.put(DcrConstants.RULE62,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.ROAD));
                planDetail.addErrors(errors);
            }
            if (planDetail.getNotifiedRoads() != null &&
                    planDetail.getNotifiedRoads().size() > 0)
                for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                    if (notifiedRoad.getShortestDistanceToRoad() == null ||
                            notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                        planDetail.addErrors(errors);
                    }
            if (planDetail.getNonNotifiedRoads() != null &&
                    planDetail.getNonNotifiedRoads().size() > 0)
                for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads())
                    if (nonNotifiedRoad.getShortestDistanceToRoad() == null ||
                            nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                        planDetail.addErrors(errors);
                    }

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
                new String[] { args }, LocaleContextHolder.getLocale());
        // return code+" "+args;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule62_1(planDetail);
        rule62_1A(planDetail);
        rule62_2(planDetail);
        rule62_3(planDetail);
        return planDetail;
    }

    private void rule62_2(PlanDetail planDetail) {
        BigDecimal sideyard1 = BigDecimal.ZERO;
        BigDecimal sideyard2 = BigDecimal.ZERO;

        // Either side1 or side2 must be 90 cm.
        // If one side >90, then other side must be 60.
        // if less than 60, then noc check required.

        // Some side may be not defined.

        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard1() != null &&
                planDetail.getPlot().getSideYard1().getPresentInDxf()
                && planDetail.getPlot().getSideYard1().getMinimumDistance() != null)
            sideyard1 = planDetail.getPlot().getSideYard1().getMinimumDistance();
        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard2() != null &&
                planDetail.getPlot().getSideYard2().getPresentInDxf()
                && planDetail.getPlot().getSideYard2().getMinimumDistance() != null)
            sideyard2 = planDetail.getPlot().getSideYard2().getMinimumDistance();

        if (sideyard1.compareTo(sideyard2) > 0) {
            if (sideyard1.compareTo(SIDE1MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(SIDE2MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        } else {
            if (sideyard2.compareTo(SIDE1MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(SIDE2MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Verify, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }

    private void rule62_1(PlanDetail planDetail) {
        // If both roads are not defined.
        if (planDetail.getNotifiedRoads() != null &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                        planDetail.getNonNotifiedRoads().size() > 0))
            planDetail.reportOutput
                    .add(buildRuleOutputWithMainRule(DcrConstants.RULE62, DcrConstants.ROAD,
                            Result.Verify, DcrConstants.ROAD + DcrConstants.OBJECTNOTDEFINED));
        else if (planDetail.getNotifiedRoads() != null &&
                planDetail.getNotifiedRoads().size() > 0)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                if (notifiedRoad.getShortestDistanceToRoad() != null &&
                        notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (notifiedRoad.getShortestDistanceToRoad().compareTo(_NOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
        // If non notified road present then check 2 mts distance should maintain
        if (planDetail.getNonNotifiedRoads() != null &&
                planDetail.getNonNotifiedRoads().size() > 0)
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads())
                if (nonNotifiedRoad.getShortestDistanceToRoad() != null &&
                        nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (nonNotifiedRoad.getShortestDistanceToRoad().compareTo(_NONNOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
    }

    private void rule62_1A(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getFrontYard() != null &&
                planDetail.getPlot().getFrontYard().getPresentInDxf()
                && planDetail.getPlot().getFrontYard().getMinimumDistance() != null
                && planDetail.getPlot().getFrontYard().getMean() != null)
            if (planDetail.getPlot().getFrontYard().getMinimumDistance().compareTo(FRONTYARDMINIMUM_DISTANCE) >= 0 &&
                    planDetail.getPlot().getFrontYard().getMean().compareTo(FRONTYARDMEAN_DISTANCE) >= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1A, SUB_RULE_62_1A_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1A, SUB_RULE_62_1A_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }

    private void rule62_3(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                planDetail.getPlot().getRearYard().getPresentInDxf()
                && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getRearYard().getMean() != null)
            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE) >= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE) >= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_3, SUB_RULE_62_3_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_3, SUB_RULE_62_3_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                        + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                        planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }
}
