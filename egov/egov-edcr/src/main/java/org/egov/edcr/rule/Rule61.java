package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;

public class Rule61 extends GeneralRule {
    private static final BigDecimal MAXIMUM_NUMBER_OF_FLOORS = BigDecimal.valueOf(3);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<String, String>();

        if (planDetail != null &&
                (planDetail.getBuilding() == null || planDetail.getBuilding().getMaxFloor() == null)) {
            errors.put(DcrConstants.MAXIMUM_NUMBEROF_FLOOR,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { DcrConstants.MAXIMUM_NUMBEROF_FLOOR }, LocaleContextHolder.getLocale()));
            // DcrConstants.OBJECTNOTDEFINED+DcrConstants.MAXIMUM_NUMBEROF_FLOOR );
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        if (planDetail != null &&
                planDetail.getBuilding() != null && planDetail.getBuilding().getMaxFloor() != null) {
            if (planDetail.getBuilding().getMaxFloor().compareTo(MAXIMUM_NUMBER_OF_FLOORS) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE61, DcrConstants.MAXIMUM_NUMBEROF_FLOOR,
                                DcrConstants.LESSTHANOREQUAL + MAXIMUM_NUMBER_OF_FLOORS.toString(),
                                planDetail.getBuilding().getMaxFloor().toString(), Result.Accepted,
                                DcrConstants.EXPECTEDRESULT));
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE61, DcrConstants.MAXIMUM_NUMBEROF_FLOOR,
                                DcrConstants.LESSTHANOREQUAL + MAXIMUM_NUMBER_OF_FLOORS.toString(),
                                planDetail.getBuilding().getMaxFloor().toString(), Result.Not_Accepted,
                                DcrConstants.EXPECTEDRESULT));

            }
        }

        return planDetail;

    }
}
