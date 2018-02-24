package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.util.HashMap;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class Rule32 extends GeneralRule {

    private static final String SUB_RULE_32_1A = "32(1A)";
    private static final String SUB_RULE_32_1A_DESCRIPTION = "Maximum height of building ";
    private static final String SUB_RULE_32_3 = "32(3)";

    private static final String SUB_RULE_32_3_DESCRIPTION = "Security zone ";
    private static final BigDecimal ten =  BigDecimal.valueOf(10);
    private static final BigDecimal max =  BigDecimal.valueOf(12);
    private static final BigDecimal two =  BigDecimal.valueOf(2);




    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 32");

        if (planDetail != null && planDetail.getBuilding() != null && planDetail.getBuilding().getPresentInDxf()) {
            if (planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.BUILDING_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getMaxHeightCal() != null && planDetail.getMaxHeightCal().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.BUILDING_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.BUILDING_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding().getBuildingTopMostHeight() != null && planDetail.getBuilding().getBuildingTopMostHeight().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.BUILDING_TOP_MOST_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[]{DcrConstants.BUILDING_TOP_MOST_HEIGHT}, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

        }

        return planDetail;
    }

    public PlanDetail generateReport(PlanDetail planDetail) {
        rule32_1a(planDetail);
        rule32_3(planDetail);
        return planDetail;
    }

    private void rule32_1a(PlanDetail planDetail) {
        if (two.multiply(planDetail.getMaxHeightCal()).compareTo(max) == -1)
            planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                    SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT, max.toString(),
                    two.multiply(planDetail.getMaxHeightCal()) + DcrConstants.IN_METER,
                    Result.Accepted, null));
        else
            planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                    SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT, max.toString(),
                    two.multiply(planDetail.getMaxHeightCal()) + DcrConstants.IN_METER,
                    Result.Not_Accepted, null));

    }

    private void rule32_3(PlanDetail planDetail) {

        if (planDetail.getPlanInformation().getSecurityZone() == true)
            if (planDetail.getBuilding().getBuildingTopMostHeight().compareTo(ten) == -1)
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                        SUB_RULE_32_3_DESCRIPTION, DcrConstants.BUILDING_TOP_MOST_HEIGHT, ten.toString(),
                        planDetail.getBuilding().getBuildingTopMostHeight() + DcrConstants.IN_METER,
                        Result.Accepted, null));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                        SUB_RULE_32_3_DESCRIPTION, DcrConstants.BUILDING_TOP_MOST_HEIGHT, ten.toString(),
                        planDetail.getBuilding().getBuildingTopMostHeight() + DcrConstants.IN_METER,
                        Result.Not_Accepted, null));
    }

}
