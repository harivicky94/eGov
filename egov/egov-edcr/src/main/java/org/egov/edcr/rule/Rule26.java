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
public class Rule26 extends GeneralRule {
    private static final BigDecimal _NOTIFIEDROADDISTINCE = BigDecimal.valueOf(3);
    private static final BigDecimal _NONNOTIFIEDROADDISTINCE = BigDecimal.valueOf(1.8);
    private static final String SUB_RULE_26_DESCRIPTION = "SUB_RULE__DESCRIPTION";
    private static final String SUB_RULE_26 = "Rule 26";
    private static final String SUB_RULE_26A = "26A";
    private static final String SUB_RULE_26A_DESCRIPTION = "Water Disposal";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 23");
        if (planDetail != null) {

            // TODO: CHECK WHETHER ANY ROAD SHOULD PASS THROUGH SITE IS MANDATORY ??????

            // If either notified or non notified road width not defined, then show error.
            if ((planDetail.getNotifiedRoads() == null || planDetail.getNonNotifiedRoads() == null) &&
                    !(planDetail.getNotifiedRoads().size() > 0 ||
                            planDetail.getNonNotifiedRoads().size() > 0)) {
                errors.put(DcrConstants.ROAD,
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

        }

        if (planDetail != null && planDetail.getBuilding() != null)
            // waste disposal defined or not
            if (planDetail.getBuilding().getWasteDisposal() == null ||
                    !planDetail.getBuilding().getWasteDisposal().getPresentInDxf()) {
                errors.put(DcrConstants.WASTEDISPOSAL,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.WASTEDISPOSAL));
                planDetail.addErrors(errors);
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
        // TODO: NEED TO ADD APPLICABLE NOT APPLICABLE.. IRRESPECTIVE OF DATA PROVIDED or not.
        rule26(planDetail);
        rule26A(planDetail);
        return planDetail;
    }

    private void rule26(PlanDetail planDetail) {
        // If both roads are not defined.
        if (planDetail.getNotifiedRoads() != null &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                        planDetail.getNonNotifiedRoads().size() > 0))
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26, SUB_RULE_26_DESCRIPTION,
                            DcrConstants.ROAD,
                            null,
                            null,
                            Result.Not_Accepted, DcrConstants.ROAD + DcrConstants.OBJECTNOTDEFINED_DESC));
        
        else if (planDetail.getNotifiedRoads() != null &&
                planDetail.getNotifiedRoads().size() > 0)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                if (notifiedRoad.getShortestDistanceToRoad() != null &&
                        notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (notifiedRoad.getShortestDistanceToRoad().compareTo(_NOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26, SUB_RULE_26_DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26, SUB_RULE_26_DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
        // If non notified road present then check 1.8 mts distance should maintain
        if (planDetail.getNonNotifiedRoads() != null &&
                planDetail.getNonNotifiedRoads().size() > 0)
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads())
                if (nonNotifiedRoad.getShortestDistanceToRoad() != null &&
                        nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (nonNotifiedRoad.getShortestDistanceToRoad().compareTo(_NONNOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26, SUB_RULE_26_DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26, SUB_RULE_26_DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
    }

    private void rule26A(PlanDetail planDetail) {
        if (planDetail.getBuilding().getWasteDisposal() != null &&
                planDetail.getBuilding().getWasteDisposal().getPresentInDxf())

            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION,
                            DcrConstants.WASTEDISPOSAL,
                            null,
                            null,
                            Result.Accepted, DcrConstants.WASTEDISPOSAL + DcrConstants.OBJECTDEFINED_DESC));

        else
            planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE26, SUB_RULE_26A, SUB_RULE_26A_DESCRIPTION,
                    DcrConstants.WASTEDISPOSAL,
                    null,
                    null,
                    Result.Not_Accepted, DcrConstants.WASTEDISPOSAL + DcrConstants.OBJECTNOTDEFINED_DESC));

    }

}
