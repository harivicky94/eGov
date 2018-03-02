package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.service.MinDistance;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.math.RayCast;
import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.entities.Subreport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class Rule24 extends GeneralRule {

    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE = BigDecimal.valueOf(3);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE = BigDecimal.valueOf(2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(2.2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING = BigDecimal.valueOf(0.75);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING = BigDecimal.valueOf(1.2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING = BigDecimal.ZERO;
    private static final BigDecimal SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING = BigDecimal.valueOf(1.2);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOPENING = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_WITHOPENING = BigDecimal.valueOf(2);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE_WITHOUTOPENING = BigDecimal.valueOf(1.5);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR = BigDecimal.valueOf(0.75);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC = BigDecimal.ZERO;

    private static final String SUB_RULE_24_3 = "24(3)";
    private static final String SUB_RULE_24_3_DESCRIPTION = "Front yard distance";

    private static final String SUB_RULE_24_4 = "24(4)";
    private static final String SUB_RULE_24_4_DESCRIPTION = "Rear yard distance";

    private static final String SUB_RULE_24_5 = "24(5)";
    private static final String SUB_RULE_24_5_DESCRIPTION = "Side yard distance";

    private static final String SUB_RULE_24_10 = "24(10)";
    private static final String SUB_RULE_24_10_DESCRIPTION = "No construction or hangings outside the boundaries of the site";

    private String MEAN_MINIMUM = "(Minimum distance,Mean distance) ";

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null) {

            if (planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BUILDING_HEIGHT_DESC));
                planDetail.addErrors(errors);
            }

            if (planDetail.getPlot() != null && (planDetail.getPlot().getFrontYard() == null ||
                                                 !planDetail.getPlot().getFrontYard().getPresentInDxf())) {
                errors.put(DcrConstants.FRONT_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FRONT_YARD_DESC));
                planDetail.addErrors(errors);
            }

            // check either of side yard present or not
            if (planDetail.getPlot() != null
                && (planDetail.getPlot().getSideYard1() == null || !planDetail.getPlot().getSideYard1().getPresentInDxf())
                && (planDetail.getPlot().getSideYard2() == null || !planDetail.getPlot().getSideYard2().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD_DESC));
                planDetail.addErrors(errors);
            }

            if (planDetail.getPlot() != null && (planDetail.getPlot().getRearYard() == null ||
                                                 !planDetail.getPlot().getRearYard().getPresentInDxf())) {
                errors.put(DcrConstants.REAR_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.REAR_YARD_DESC));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBasement() != null) {

                if (planDetail.getPlot() != null && (planDetail.getPlot().getBsmtFrontYard() == null ||
                                                     !planDetail.getPlot().getBsmtFrontYard().getPresentInDxf())) {
                    errors.put(DcrConstants.BSMT_FRONT_YARD_DESC,
                            prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BSMT_FRONT_YARD_DESC));
                    planDetail.addErrors(errors);
                }

                // check either of basement side yard present or not
                if (planDetail.getPlot() != null
                    && (planDetail.getPlot().getBsmtSideYard1() == null
                        || !planDetail.getPlot().getBsmtSideYard1().getPresentInDxf())
                    && (planDetail.getPlot().getBsmtSideYard2() == null
                        || !planDetail.getPlot().getBsmtSideYard2().getPresentInDxf())) {
                    errors.put(DcrConstants.BSMT_SIDE_YARD_DESC,
                            prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BSMT_SIDE_YARD_DESC));
                    planDetail.addErrors(errors);
                }

                if (planDetail.getPlot() != null && (planDetail.getPlot().getBsmtRearYard() == null ||
                                                     !planDetail.getPlot().getBsmtRearYard().getPresentInDxf())) {
                    errors.put(DcrConstants.BSMT_REAR_YARD_DESC,
                            prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BSMT_REAR_YARD_DESC));
                    planDetail.addErrors(errors);
                }
            }
        }

        return planDetail;
    }

    private String prepareMessage(String code, String args) {
        return edcrMessageSource.getMessage(code,
                new String[] { args }, LocaleContextHolder.getLocale());
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        {
            // For building of heights less than or equal to 10
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0)
                rule24_3(planDetail, DcrConstants.NON_BASEMENT);

            // Building Height between 7 to 10
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {
                rule24_5(planDetail, SIDE1MINIMUM_DISTANCE, SIDE2MINIMUM_DISTANCE, DcrConstants.NON_BASEMENT);
                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE, REARYARDMEAN_DISTANCE, DcrConstants.NON_BASEMENT);
            }

            // Building Height less than 7
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) <= 0)
                // OPENING PRESENT
                if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningOnRear())
                    rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOPENING, REARYARDMEAN_DISTANCE_WITHOPENING, DcrConstants.NON_BASEMENT);
                else {

                    rule24_5(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING, DcrConstants.NON_BASEMENT);
                    rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING, REARYARDMEAN_DISTANCE_WITHOUTOPENING, DcrConstants.NON_BASEMENT);
                    rule24_4_LessThan7_WithoutOpeningCorrespondingFloor(planDetail, DcrConstants.NON_BASEMENT);

                    // WITHOUT OPENING AND NOC PRESENT
                    if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocToAbutSide())
                        rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.NON_BASEMENT);

                    rule24_4_LessThan7WithoutOpeningNoc(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.NON_BASEMENT);

                }
        }
        rule24_10(planDetail);
        if (planDetail.getBasement() != null)
            rule24_12(planDetail);
        return planDetail;
    }

    private void rule24_12(PlanDetail planDetail) {
        // For building of heights less than or equal to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
            && planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
            && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0)
            rule24_3(planDetail, DcrConstants.BASEMENT);

        // Building Height between 7 to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
            && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
            && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {
            rule24_5(planDetail, SIDE1MINIMUM_DISTANCE, SIDE2MINIMUM_DISTANCE, DcrConstants.BASEMENT);
            rule24_4(planDetail, REARYARDMINIMUM_DISTANCE, REARYARDMEAN_DISTANCE, DcrConstants.BASEMENT);
        }

        // Building Height less than 7
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
            && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) <= 0)
            // OPENING PRESENT
            if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningOnRear())
                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOPENING, REARYARDMEAN_DISTANCE_WITHOPENING, DcrConstants.BASEMENT);
            else {

                rule24_5(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING, DcrConstants.BASEMENT);
                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING, REARYARDMEAN_DISTANCE_WITHOUTOPENING, DcrConstants.BASEMENT);
                rule24_4_LessThan7_WithoutOpeningCorrespondingFloor(planDetail, DcrConstants.BASEMENT);

                // WITHOUT OPENING AND NOC PRESENT
                if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocToAbutSide())
                    rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.BASEMENT);

                rule24_4_LessThan7WithoutOpeningNoc(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.BASEMENT);

            }
    }

    private void rule24_3(PlanDetail planDetail, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {
            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtFrontYard() != null &&
                planDetail.getPlot().getBsmtFrontYard().getPresentInDxf()
                && planDetail.getPlot().getBsmtFrontYard().getMinimumDistance() != null
                && planDetail.getPlot().getBsmtFrontYard().getMean() != null)
                if (planDetail.getPlot().getBsmtFrontYard().getMinimumDistance().compareTo(FRONTYARDMINIMUM_DISTANCE) >= 0 &&
                    planDetail.getPlot().getBsmtFrontYard().getMean().compareTo(FRONTYARDMEAN_DISTANCE) >= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                    DcrConstants.BSMT_FRONT_YARD_DESC,
                                    MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                    + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtFrontYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtFrontYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                    DcrConstants.BSMT_FRONT_YARD_DESC,
                                    MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                    + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtFrontYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtFrontYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
        } else if (planDetail.getPlot() != null && planDetail.getPlot().getFrontYard() != null &&
                   planDetail.getPlot().getFrontYard().getPresentInDxf()
                   && planDetail.getPlot().getFrontYard().getMinimumDistance() != null
                   && planDetail.getPlot().getFrontYard().getMean() != null)
            if (planDetail.getPlot().getFrontYard().getMinimumDistance().compareTo(FRONTYARDMINIMUM_DISTANCE) >= 0 &&
                planDetail.getPlot().getFrontYard().getMean().compareTo(FRONTYARDMEAN_DISTANCE) >= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getFrontYard().getMean().toString() + ")"
                                + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_3, SUB_RULE_24_3_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getFrontYard().getMean().toString() + ")"
                                + DcrConstants.IN_METER,

                                Result.Not_Accepted, null));
    }

    private void rule24_5(PlanDetail planDetail, BigDecimal sideYard1MinDist, BigDecimal sideYard2MinDist, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {
            BigDecimal bsmntSideyard1 = BigDecimal.ZERO;
            BigDecimal bsmntSideyard2 = BigDecimal.ZERO;

            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtSideYard1() != null &&
                planDetail.getPlot().getBsmtSideYard1().getPresentInDxf()
                && planDetail.getPlot().getBsmtSideYard1().getMinimumDistance() != null)
                bsmntSideyard1 = planDetail.getPlot().getBsmtSideYard1().getMinimumDistance();
            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtSideYard2() != null &&
                planDetail.getPlot().getBsmtSideYard2().getPresentInDxf()
                && planDetail.getPlot().getBsmtSideYard2().getMinimumDistance() != null)
                bsmntSideyard2 = planDetail.getPlot().getBsmtSideYard2().getMinimumDistance();

            if (bsmntSideyard1.compareTo(bsmntSideyard2) > 0) {
                if (bsmntSideyard1.compareTo(sideYard1MinDist) == 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD1_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD1_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));

                if (bsmntSideyard2.compareTo(sideYard2MinDist) < 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD2_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Verify, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD2_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
            } else {
                if (bsmntSideyard2.compareTo(sideYard1MinDist) == 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD2_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD2_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));

                if (bsmntSideyard1.compareTo(sideYard2MinDist) < 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD1_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Verify, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.BSMT_SIDE_YARD1_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    bsmntSideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
            }
        } else {
            BigDecimal sideyard1 = BigDecimal.ZERO;
            BigDecimal sideyard2 = BigDecimal.ZERO;

            if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard1() != null &&
                planDetail.getPlot().getSideYard1().getPresentInDxf()
                && planDetail.getPlot().getSideYard1().getMinimumDistance() != null)
                sideyard1 = planDetail.getPlot().getSideYard1().getMinimumDistance();
            if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard2() != null &&
                planDetail.getPlot().getSideYard2().getPresentInDxf()
                && planDetail.getPlot().getSideYard2().getMinimumDistance() != null)
                sideyard2 = planDetail.getPlot().getSideYard2().getMinimumDistance();

            if (sideyard1.compareTo(sideyard2) > 0) {
                if (sideyard1.compareTo(sideYard1MinDist) == 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD1_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD1_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));

                if (sideyard2.compareTo(sideYard2MinDist) < 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD2_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Verify, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD2_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
            } else {
                if (sideyard2.compareTo(sideYard1MinDist) == 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD2_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD2_DESC,
                                    sideYard1MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard2.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));

                if (sideyard1.compareTo(sideYard2MinDist) < 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD1_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Verify, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_5, SUB_RULE_24_5_DESCRIPTION,
                                    DcrConstants.SIDE_YARD1_DESC,
                                    sideYard2MinDist.toString() + DcrConstants.IN_METER,
                                    sideyard1.toString()
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
            }

        }
    }

    private void rule24_4(PlanDetail planDetail, BigDecimal rearYardMin, BigDecimal rearYardMean, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {

            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtRearYard() != null &&
                planDetail.getPlot().getBsmtRearYard().getPresentInDxf()
                && planDetail.getPlot().getBsmtRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getBsmtRearYard().getMean() != null)
                if (planDetail.getPlot().getBsmtRearYard().getMinimumDistance().compareTo(rearYardMin) <= 0 &&
                    planDetail.getPlot().getBsmtRearYard().getMean().compareTo(rearYardMean) <= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                    + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                    + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));

        } else if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                   planDetail.getPlot().getRearYard().getPresentInDxf()
                   && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                   && planDetail.getPlot().getRearYard().getMean() != null)
            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(rearYardMin) <= 0 &&
                planDetail.getPlot().getRearYard().getMean().compareTo(rearYardMean) <= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }

    private void rule24_4_LessThan7WithoutOpeningNoc(PlanDetail planDetail, BigDecimal rearYardMin, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {
            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtRearYard() != null &&
                planDetail.getPlot().getBsmtRearYard().getPresentInDxf()
                && planDetail.getPlot().getBsmtRearYard().getMinimumDistance() != null)
                if (planDetail.getPlot().getBsmtRearYard().getMinimumDistance().compareTo(rearYardMin) >= 0
                    && planDetail.getPlanInformation().getNocToAbutRear())
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    "Minimum " + "(" + rearYardMin.toString() + ")"
                                    + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    "Minimum " + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC.toString() + ")"
                                    + DcrConstants.IN_METER + "NOC is not present ",
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() + "," +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
        } else if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                   planDetail.getPlot().getRearYard().getPresentInDxf()
                   && planDetail.getPlot().getRearYard().getMinimumDistance() != null)
            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(rearYardMin) >= 0
                && planDetail.getPlanInformation().getNocToAbutRear())
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                "Minimum " + "(" + rearYardMin.toString() + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                "Minimum " + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_NOC.toString() + ")"
                                + DcrConstants.IN_METER + "NOC is not present ",
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }

    private void rule24_4_LessThan7_WithoutOpeningCorrespondingFloor(PlanDetail planDetail, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type))
            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtRearYard() != null &&
                planDetail.getPlot().getBsmtRearYard().getPresentInDxf()
                && planDetail.getPlot().getBsmtRearYard().getMinimumDistance() != null
                && planDetail.getPlot().getBsmtRearYard().getMean() != null) {

                if (planDetail.getPlot().getBsmtRearYard().getMinimumDistance()
                              .compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) <= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    "Minimum" + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR.toString(),
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    "Minimum" + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR.toString(),
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() +
                                    planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));

            } else if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
                       planDetail.getPlot().getRearYard().getPresentInDxf()
                       && planDetail.getPlot().getRearYard().getMinimumDistance() != null
                       && planDetail.getPlot().getRearYard().getMean() != null)
                if (planDetail.getPlot().getRearYard().getMinimumDistance()
                              .compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) <= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.REAR_YARD_DESC,
                                    "Minimum" + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR.toString(),
                                    "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() +
                                    planDetail.getPlot().getRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_4, SUB_RULE_24_4_DESCRIPTION,
                                    DcrConstants.REAR_YARD_DESC,
                                    "Minimum" + "(" + REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR.toString(),
                                    "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() +
                                    planDetail.getPlot().getRearYard().getMean().toString() + ")"
                                    + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
    }

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE24)) {
                FastReportBuilder drb = new FastReportBuilder();
                StringBuilder stringBuilder = new StringBuilder();
                if (ruleOutput.getMessage() != null)
                    stringBuilder.append("Message : ").append(ruleOutput.getMessage()).append("\\n");
                if (ruleOutput.getRuleDescription() != null)
                    stringBuilder.append("Description : ").append(ruleOutput.getRuleDescription()).append("\\n");
                drb.setMargins(5, 0, 10, 10);
                drb.setTitle("Rule : " + ruleOutput.getKey() + "\\n")
                   .setSubtitle(stringBuilder.toString())
                   .setPrintBackgroundOnOddRows(false).setWhenNoData("", null)
                   .setTitleStyle(reportService.getTitleStyle())
                   .setSubtitleStyle(reportService.getSubTitleStyle())
                   .setSubtitleHeight(30);

                new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs());
                final DJDataSource djds = new DJDataSource(ruleOutput.getKey(), DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                        DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);

                final Subreport subRep = new Subreport();
                subRep.setLayoutManager(new ClassicLayoutManager());
                subRep.setDynamicReport(drb.build());
                subRep.setDatasource(djds);
                subRep.setUseParentReportParameters(true);
                subRep.setSplitAllowed(true);
                drb2.addConcatenatedReport(subRep);
                valuesMap.put(ruleOutput.getKey(), new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty())
                    for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs())
                        try {
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            valuesMap.put(subRuleOutput.getKey() + "DataSource",
                                    new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRuleOutput, drb2, valuesMap));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                break;
            }
       return reportStatus;
    }

    public Subreport generateSubRuleReport(final SubRuleOutput subRuleOutput, FastReportBuilder drb2, Map valuesMap)
            throws JRException, IOException, Exception {
        FastReportBuilder drb = new FastReportBuilder();
        final Style columnStyle = reportService.getColumnStyle();
        final Style columnHeaderStyle = reportService.getColumnHeaderStyle();
        final Style verifiedColumnStyle = reportService.getVerifiedColumnStyle();
        StringBuilder stringBuilder = new StringBuilder();
        if (subRuleOutput.getMessage() != null)
            stringBuilder.append("Message : ").append(subRuleOutput.getMessage()).append("\\n");
        if (subRuleOutput.getRuleDescription() != null)
            stringBuilder.append("Description : ").append(subRuleOutput.getRuleDescription()).append("\\n");

        drb.setMargins(0, 10, 10, 10);
        drb.setTitle("SubRule : " + subRuleOutput.getKey())
           .setSubtitle(stringBuilder.toString())
           .setPrintBackgroundOnOddRows(false).setWhenNoData("", null)
           .setTitleStyle(reportService.getTitleStyle())
           .setSubtitleStyle(reportService.getSubTitleStyle())
           .setSubtitleHeight(30).setTitleHeight(40);


        if (subRuleOutput.getRuleReportOutputs() != null && !subRuleOutput.getRuleReportOutputs().isEmpty()) {

            drb.addColumn("Field Verified", "fieldVerified", String.class.getName(), 120, verifiedColumnStyle, columnHeaderStyle,
                    true);
            drb.addColumn("Expected Result", "expectedResult", String.class.getName(), 120, columnStyle, columnHeaderStyle, true);
            drb.addColumn("Actual Result", "actualResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
            drb.addColumn("Status", "status", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        }

        new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs());
        final DJDataSource djds = new DJDataSource(subRuleOutput.getKey() + "DataSource",
                DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);

        final Subreport subRep = new Subreport();
        subRep.setLayoutManager(new ClassicLayoutManager());
        subRep.setDynamicReport(drb.build());
        subRep.setDatasource(djds);
        subRep.setUseParentReportParameters(true);
        subRep.setSplitAllowed(true);
        return subRep;
    }

    private void rule24_10(PlanDetail pl) {
        if (pl.getBuilding().getPolyLine() == null) {
            pl.addError("24-10", prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BUILDING_FOOT_PRINT));
            return;
        }
        if (pl.getPlot().getPolyLine() == null) {
            pl.addError("24-10", prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.PLOT_BOUNDARY));
            return;
        }

        if (pl.getBuilding().getShade().getPolyLine() == null) {
            pl.addError("24-10", prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHADE));
            return;
        }

        double[][] pointsOfPlot = MinDistance.pointsOfPolygon(pl.getPlot().getPolyLine());
        Iterator buildingIterator = pl.getBuilding().getPolyLine().getVertexIterator();
        Boolean buildingOutSideBoundary = false;
        while (buildingIterator.hasNext()) {
            DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
            Point point = dxfVertex.getPoint();
            if (RayCast.contains(pointsOfPlot, new double[] { point.getX(), point.getY() }) == false)
                buildingOutSideBoundary = true;

        }
        if (buildingOutSideBoundary)
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_10, SUB_RULE_24_10_DESCRIPTION,
                            DcrConstants.BUILDING_FOOT_PRINT,
                            DcrConstants.BUILDING_FOOT_PRINT + " Should be inside Plot Boundary",
                            DcrConstants.BUILDING_FOOT_PRINT + " is outside Plot Boundary",
                            Result.Not_Accepted, null));

        buildingIterator = pl.getBuilding().getShade().getPolyLine().getVertexIterator();
        Boolean shadeOutSideBoundary = false;
        while (buildingIterator.hasNext()) {
            DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
            Point point = dxfVertex.getPoint();
            if (RayCast.contains(pointsOfPlot, new double[] { point.getX(), point.getY() }) == false)
                shadeOutSideBoundary = true;

        }
        if (shadeOutSideBoundary)
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_10, SUB_RULE_24_10_DESCRIPTION,
                            DcrConstants.SHADE,
                            DcrConstants.SHADE + " Should be inside Plot Boundary",
                            DcrConstants.SHADE + " is outside Plot Boundary",
                            Result.Not_Accepted, null));

        if (!shadeOutSideBoundary && !buildingOutSideBoundary)
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_10, SUB_RULE_24_10_DESCRIPTION,
                            DcrConstants.BUILDING_FOOT_PRINT,
                            DcrConstants.SHADE + "," + DcrConstants.BUILDING_FOOT_PRINT + "  Should be inside Plot Boundary",
                            DcrConstants.SHADE + "," + DcrConstants.BUILDING_FOOT_PRINT + "  are  inside Plot Boundary",
                            Result.Accepted, null));

    }

}