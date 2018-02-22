package org.egov.edcr.rule;

import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class Rule30 extends GeneralRule {

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 30");
        if (planDetail != null && planDetail.getPlanInformation() != null
                && StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy())) {
            errors.put(DcrConstants.OCCUPANCY,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { DcrConstants.OCCUPANCY }, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        rule30(planDetail);
        return planDetail;

    }

    private void rule30(PlanDetail planDetail) {
        if (planDetail != null && planDetail.getPlanInformation() != null
                && !StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy()))
            planDetail.reportOutput
                    .add(buildRuleOutputWithMainRule(DcrConstants.RULE30, DcrConstants.OCCUPANCY,
                            Result.Verify, DcrConstants.OCCUPANCY + DcrConstants.OBJECTDEFINED_DESC));
    }

}