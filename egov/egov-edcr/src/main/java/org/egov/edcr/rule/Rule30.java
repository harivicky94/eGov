package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


@Service
public class Rule30 extends GeneralRule {

    public static final String OCCUPANCY_A1 = "Occupancy A1";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<String, String>();
        System.out.println("validate 30");
        if (planDetail != null && planDetail.getPlanInformation() != null
                && StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy())) {
            errors.put(DcrConstants.OCCUPANCY,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[]{DcrConstants.OCCUPANCY}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        List<RuleReportOutput> ruleReportOutputs = new ArrayList<>();

        rule30(planDetail, ruleReportOutputs);

        return planDetail;
    }

    private List<RuleReportOutput> rule30(PlanDetail planDetail, List<RuleReportOutput> ruleReportOutputs) {

        RuleReportOutput ruleReportOutput;

        if (planDetail != null && planDetail.getPlanInformation() != null
                && !StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy())
                && planDetail.getPlanInformation().getOccupancy().equalsIgnoreCase(OCCUPANCY_A1)) {
            ruleReportOutput = new RuleReportOutput();
            ruleReportOutput.setRuleKey(DcrConstants.RULE30);
            ruleReportOutput.setFieldVerified(DcrConstants.OCCUPANCY);
            ruleReportOutput.setActualResult(Result.Accepted.toString());
            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
            ruleReportOutputs.add(ruleReportOutput);
        } else {
            ruleReportOutput = new RuleReportOutput();
            ruleReportOutput.setRuleKey(DcrConstants.RULE30);
            ruleReportOutput.setFieldVerified(DcrConstants.OCCUPANCY);
            ruleReportOutput.setActualResult(Result.Accepted.toString());
            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
            ruleReportOutputs.add(ruleReportOutput);
        }
        return ruleReportOutputs;

    }

}