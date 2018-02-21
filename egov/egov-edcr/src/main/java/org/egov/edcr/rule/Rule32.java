package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Rule32 extends GeneralRule {

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<String, String>();
        System.out.println("validate 32");

        if (planDetail != null && planDetail.getBuilding() != null && planDetail.getBuilding().getPresentInDxf()) {
            if (planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[]{DcrConstants.BUILDING_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getMaxHeightCal() != null && planDetail.getMaxHeightCal().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.BUILDING_HEIGHT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[]{DcrConstants.BUILDING_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding().getBuildingTopMostHeight() != null
                    && planDetail.getBuilding().getBuildingTopMostHeight().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.BUILDING_TOP_MOST_HEIGHT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[]{DcrConstants.BUILDING_TOP_MOST_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

        }

        return planDetail;
    }


    public List<RuleReportOutput> generateReport(PlanDetail planDetail) {
        List<RuleReportOutput> ruleReportOutputs = new ArrayList<>();
        rule32_1a(planDetail, ruleReportOutputs);
        rule32_3(planDetail, ruleReportOutputs);
        return ruleReportOutputs;
    }

    private List<RuleReportOutput> rule32_1a(PlanDetail planDetail, List<RuleReportOutput> ruleReportOutputs) {
        RuleReportOutput ruleReportOutput = new RuleReportOutput();
        BigDecimal max = new BigDecimal("12");
        BigDecimal constant = new BigDecimal("2");
        if ((constant.multiply(planDetail.getMaxHeightCal())).compareTo(max) == -1) {

            ruleReportOutput.setRuleKey(DcrConstants.RULE32);
            ruleReportOutput.setFieldVerified(DcrConstants.BUILDING_HEIGHT);
            ruleReportOutput.setActualResult(Result.Accepted.toString());
            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
            ruleReportOutputs.add(ruleReportOutput);

        } else {
            ruleReportOutput = new RuleReportOutput();
            ruleReportOutput.setRuleKey(DcrConstants.RULE32);
            ruleReportOutput.setFieldVerified(DcrConstants.BUILDING_HEIGHT);
            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
            ruleReportOutputs.add(ruleReportOutput);

        }
        return ruleReportOutputs;

    }

    private List<RuleReportOutput> rule32_3(PlanDetail planDetail, List<RuleReportOutput> ruleReportOutputs) {

        RuleReportOutput ruleReportOutput = new RuleReportOutput();
        BigDecimal max = new BigDecimal("10");
        if (planDetail.getPlanInformation().getSecurityZone() == true) {
            if ((planDetail.getBuilding().getBuildingTopMostHeight()).compareTo(max) == -1) {

                ruleReportOutput.setRuleKey(DcrConstants.RULE32);
                ruleReportOutput.setFieldVerified(DcrConstants.BUILDING_TOP_MOST_HEIGHT);
                ruleReportOutput.setActualResult(Result.Accepted.toString());
                ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                ruleReportOutputs.add(ruleReportOutput);

            } else {
                ruleReportOutput = new RuleReportOutput();
                ruleReportOutput.setRuleKey(DcrConstants.RULE32);
                ruleReportOutput.setFieldVerified(DcrConstants.BUILDING_TOP_MOST_HEIGHT);
                ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                ruleReportOutputs.add(ruleReportOutput);

            }
        }
        return ruleReportOutputs;
    }

}
