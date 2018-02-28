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
public class Rule32 extends GeneralRule {

    private static final String SUB_RULE_32_1A = "32(1A)";
    private static final String SUB_RULE_32_1A_DESCRIPTION = "Maximum height of building ";
    private static final String SUB_RULE_32_3 = "32(3)";

    private static final String SUB_RULE_32_3_DESCRIPTION = "Security zone ";
    private static final BigDecimal ten = BigDecimal.valueOf(10);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal two = BigDecimal.valueOf(2);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 32");

        if (planDetail != null && planDetail.getBuilding() != null && planDetail.getBuilding().getPresentInDxf()) {
            if (planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.BUILDING_HEIGHT }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding() != null
                    && planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT },
                                LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            /* if (planDetail.getBuilding().getBuildingTopMostHeight() != null
                     && planDetail.getBuilding().getBuildingTopMostHeight().compareTo(BigDecimal.ZERO) < 0) {
                 errors.put(DcrConstants.BUILDING_TOP_MOST_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                         new String[] { DcrConstants.BUILDING_TOP_MOST_HEIGHT }, LocaleContextHolder.getLocale()));
                 planDetail.addErrors(errors);
             }*/

        }

        return planDetail;
    }

    public PlanDetail generateReport(PlanDetail planDetail) {
        rule32_1a(planDetail);
        rule32_3(planDetail);
        return planDetail;
    }

    private void rule32_1a(PlanDetail planDetail) {
        boolean shortDistainceLessThan12 = false;

        if (planDetail.getNonNotifiedRoads() != null)
            for (NonNotifiedRoad nonnotifiedRoad : planDetail.getNonNotifiedRoads()) {
                if (nonnotifiedRoad.getMinimumDistance().compareTo(BigDecimal.ZERO) > 0 &&
                        nonnotifiedRoad.getMinimumDistance().compareTo(TWELVE) <= 0) {
                    shortDistainceLessThan12 = true;
                    return;
                }

            }
        if (planDetail.getNotifiedRoads() != null)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads()) {
                if (notifiedRoad.getMinimumDistance().compareTo(BigDecimal.ZERO) > 0 &&
                        notifiedRoad.getMinimumDistance().compareTo(TWELVE) <= 0) {
                    shortDistainceLessThan12 = true;
                    return;
                }

            }

        if (shortDistainceLessThan12 && planDetail.getBuilding() != null && planDetail.getMaxHeightCal() != null
                && planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd() != null &&
                planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(BigDecimal.ZERO) > 0) {
            if (planDetail.getBuilding().getHeight()
                    .compareTo(planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two)) > 0) {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                        SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT,
                        planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two).toString()
                                + DcrConstants.IN_METER,
                        planDetail.getBuilding().getHeight() + DcrConstants.IN_METER,
                        Result.Not_Accepted, null));
            } else {
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                        SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT,
                        planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two).toString()
                                + DcrConstants.IN_METER,
                        planDetail.getBuilding().getHeight() + DcrConstants.IN_METER,
                        Result.Accepted, null));
            }

        }
    }

    private void rule32_3(PlanDetail planDetail) {

        if (planDetail.getPlanInformation().getSecurityZone() == true) {
            if (planDetail.getBuilding().getBuildingHeight().compareTo(ten) <= 0) // TODO: LATER CHECK MAXIMUM HEIGHT OF BUILDING
                                                                                  // FROM FLOOR
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                        SUB_RULE_32_3_DESCRIPTION, DcrConstants.SECURITY_ZONE, null,
                        null,
                        Result.Verify, DcrConstants.SECURITY_ZONE + DcrConstants.OBJECTDEFINED_DESC));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_3,
                        SUB_RULE_32_3_DESCRIPTION, DcrConstants.SECURITY_ZONE, ten.toString() + DcrConstants.IN_METER,
                        planDetail.getBuilding().getBuildingHeight() + DcrConstants.IN_METER,
                        Result.Not_Accepted, null));
        }

    }

}
