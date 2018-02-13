package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;

public class Rule26 extends GeneralRule {
    private static final BigDecimal _NOTIFIEDROADDISTINCE = BigDecimal.valueOf(3);
    private static final BigDecimal _NONNOTIFIEDROADDISTINCE = BigDecimal.valueOf(1.8);

    @Autowired
    @Qualifier("parentMessageSource")
    private MessageSource edcrMessageSource;

    // protected HashMap<String, HashMap<String, Object>> reportOutput = new HashMap<String, HashMap<String, Object>>();
    protected HashMap<String, String> errors = new HashMap<String, String>();
    protected HashMap<String, String> generalInformation = new HashMap<String, String>();

    @Override
    public PlanDetail validate(PlanDetail planDetail) {

        if (planDetail != null && planDetail.getLandDetail() != null) {

            // TODO: CHECK WHETHER ANY ROAD SHOULD PASS THROUGH SITE IS MANDATORY ??????

            // If either notified or non notified road width not defined, then show error.
            if (planDetail.getNotifiedRoads() != null &&
                    !(planDetail.getNotifiedRoads().size() > 0 ||
                            planDetail.getNonNotifiedRoads().size() > 0)) {
                errors.put(DcrConstants.ROAD,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.ROAD }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getNotifiedRoads() != null &&
                    planDetail.getNotifiedRoads().size() > 0) {
                for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads()) {
                    if (notifiedRoad.getShortestDistanceToRoad() == null ||
                            notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.SHORTESTDISTINCTTOROAD,
                                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                        new String[] { DcrConstants.SHORTESTDISTINCTTOROAD }, LocaleContextHolder.getLocale()));
                        planDetail.addErrors(errors);
                    }
                }
            }
            if (planDetail.getNonNotifiedRoads() != null &&
                    planDetail.getNonNotifiedRoads().size() > 0) {
                for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads()) {
                    if (nonNotifiedRoad.getShortestDistanceToRoad() == null ||
                            nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.SHORTESTDISTINCTTOROAD,
                                edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                        new String[] { DcrConstants.SHORTESTDISTINCTTOROAD }, LocaleContextHolder.getLocale()));
                        planDetail.addErrors(errors);
                    }
                }
            }

        }

        if (planDetail != null && planDetail.getBuildingDetail() != null) {
            // waste disposal defined or not
            if (planDetail.getBuildingDetail().getWasteDisposal() != null &&
                    !planDetail.getBuildingDetail().getWasteDisposal().getPresentInDxf()) {
                errors.put(DcrConstants.WASTEDISPOSAL,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.WASTEDISPOSAL }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
        }

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        // TODO: NEED TO ADD APPLICABLE NOT APPLICABLE.. IRRESPECTIVE OF DATA PROVIDED or not.

        if (planDetail.getNotifiedRoads() != null &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                        planDetail.getNonNotifiedRoads().size() > 0)) {
            planDetail.reportOutput.add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.ROAD, Result.Not_Accepted,
                    DcrConstants.OBJECTNOTDEFINED));
        }else if (planDetail.getNotifiedRoads() != null &&
                planDetail.getNotifiedRoads().size() > 0) {

            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads()) {
                if (notifiedRoad.getShortestDistanceToRoad() != null &&
                        notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (notifiedRoad.getShortestDistanceToRoad().compareTo(_NOTIFIEDROADDISTINCE) >=0) {//TDDO CHECK
                        planDetail.reportOutput
                                .add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD, _NOTIFIEDROADDISTINCE.toString(),
                                        notifiedRoad.getShortestDistanceToRoad().toString(),Result.Accepted, DcrConstants.EXPECTEDRESULT));
                    } else {
                            planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD, _NOTIFIEDROADDISTINCE.toString(),
                                notifiedRoad.getShortestDistanceToRoad().toString(),Result.Not_Accepted, DcrConstants.EXPECTEDRESULT));
      
                    }
            }
        }
        if (planDetail.getNonNotifiedRoads() != null &&
                planDetail.getNonNotifiedRoads().size() > 0) {
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads()) {
                if (nonNotifiedRoad.getShortestDistanceToRoad() != null &&
                        nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (nonNotifiedRoad.getShortestDistanceToRoad().compareTo(_NONNOTIFIEDROADDISTINCE) >=0) {//TDDO CHECK
                        planDetail.reportOutput
                                .add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD, _NONNOTIFIEDROADDISTINCE.toString(),
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString(),Result.Accepted, DcrConstants.EXPECTEDRESULT));
                    } else {
                            planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD, _NONNOTIFIEDROADDISTINCE.toString(),
                                nonNotifiedRoad.getShortestDistanceToRoad().toString(),Result.Not_Accepted, DcrConstants.EXPECTEDRESULT));
      
                    }
            }
        }


        rule26A(planDetail);
        return planDetail;
    }

    private RuleOutput buildRuleOutput(String rule26, String notifiedShortestdistincttoroad, String notifiedroaddistince,
            String shortestDistanceToRoad, Result accepted, String expectedresult) {
        RuleOutput ruleOutput = new RuleOutput();
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        ruleOutput.key = rule26;
        subRuleOutput.message = edcrMessageSource.getMessage(expectedresult,
                new String[] { notifiedShortestdistincttoroad, notifiedroaddistince,shortestDistanceToRoad}, LocaleContextHolder.getLocale());
        subRuleOutput.result = accepted;
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;
    }

    private void rule26A(PlanDetail planDetail) {

        if (planDetail.getBuildingDetail().getWasteDisposal() != null &&
                planDetail.getBuildingDetail().getWasteDisposal().getPresentInDxf()) {

            planDetail.reportOutput.add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.WASTEDISPOSAL, Result.Accepted,
                    DcrConstants.OBJECTDEFINED));
        } else
            planDetail.reportOutput.add(buildRuleOutput(DcrConstants.RULE26, DcrConstants.WASTEDISPOSAL, Result.Not_Accepted,
                    DcrConstants.OBJECTNOTDEFINED));

    }

    private RuleOutput buildRuleOutput(String reportOutputKey, String messageParam, Result result, String messageKey) {
        RuleOutput ruleOutput = new RuleOutput();
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        ruleOutput.key = reportOutputKey;
        subRuleOutput.message = edcrMessageSource.getMessage(messageKey,
                new String[] { messageParam }, LocaleContextHolder.getLocale());
        subRuleOutput.result = result;
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;
    }

}
