package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;

public class Rule60  extends GeneralRule {

    private static final BigDecimal _EXPECTED_PLOT_AREA = BigDecimal.valueOf(125);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
         HashMap<String, String> errors = new HashMap<String, String>();

        if (planDetail!= null &&
                (planDetail.getPlot()==null || planDetail.getPlot().getArea()==null)) {
            errors.put(DcrConstants.PLOT_AREA,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { DcrConstants.PLOT_AREA }, LocaleContextHolder.getLocale()));
                  //  DcrConstants.OBJECTNOTDEFINED+DcrConstants.PLOT_AREA );
            planDetail.addErrors(errors);
        }
     return planDetail;   
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        if (planDetail != null &&
                planDetail.getPlot() != null && planDetail.getPlot().getArea() != null) {
            if (planDetail.getPlot().getArea().compareTo(_EXPECTED_PLOT_AREA) <= 0) {
                planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE60, DcrConstants.PLOT_AREA,
                                DcrConstants.LESSTHANOREQUAL + _EXPECTED_PLOT_AREA.toString() + DcrConstants.IN_METER,
                                planDetail.getPlot().getArea().toString() + DcrConstants.IN_METER, Result.Accepted,
                                DcrConstants.EXPECTEDRESULT));
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutput(DcrConstants.RULE60, DcrConstants.PLOT_AREA,
                                DcrConstants.LESSTHANOREQUAL + _EXPECTED_PLOT_AREA.toString() + DcrConstants.IN_METER,
                                planDetail.getPlot().getArea().toString() + DcrConstants.IN_METER, Result.Not_Accepted,
                                DcrConstants.EXPECTEDRESULT));

            }
        }

        return planDetail;

    }
    
}
