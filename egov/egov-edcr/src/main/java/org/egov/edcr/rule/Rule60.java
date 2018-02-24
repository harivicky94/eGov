package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Rule60 extends GeneralRule {

    private static final BigDecimal _EXPECTED_PLOT_AREA = BigDecimal.valueOf(125);
    private static final String SUB_RULE_60_DESCRIPTION = "Constructions in small plots";

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null &&
                (planDetail.getPlot() == null || planDetail.getPlot().getArea() == null)) {
            errors.put(DcrConstants.PLOT_AREA,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { DcrConstants.PLOT_AREA }, LocaleContextHolder.getLocale()));
            // DcrConstants.OBJECTNOTDEFINED+DcrConstants.PLOT_AREA );
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        if (planDetail != null &&
                planDetail.getPlot() != null && planDetail.getPlot().getArea() != null)
            if (planDetail.getPlot().getArea().compareTo(_EXPECTED_PLOT_AREA) <= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE60, DcrConstants.RULE60, SUB_RULE_60_DESCRIPTION,
                                DcrConstants.PLOT_AREA,
                                _EXPECTED_PLOT_AREA.toString() + DcrConstants.IN_METER,
                                planDetail.getPlot().getArea().toString()
                                        + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE60, DcrConstants.RULE60, SUB_RULE_60_DESCRIPTION,
                                DcrConstants.PLOT_AREA,
                                _EXPECTED_PLOT_AREA.toString() + DcrConstants.IN_METER,
                                planDetail.getPlot().getArea().toString()
                                        + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));

        return planDetail;

    }

}
