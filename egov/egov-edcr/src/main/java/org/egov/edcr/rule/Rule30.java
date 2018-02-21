package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;


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
                            new String[]{DcrConstants.OCCUPANCY}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

}