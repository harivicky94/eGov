package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.Floor;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Plot;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.Room;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.entity.measurement.Yard;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.edcr.utility.math.Polygon;
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
    private Logger LOG = Logger.getLogger(Rule24.class);

    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1.8);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE = BigDecimal.valueOf(3);

    private static final BigDecimal REARYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal REARYARDMEAN_DISTANCE = BigDecimal.valueOf(2);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE = BigDecimal.valueOf(1);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(1.2);

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
    private static final BigDecimal OPENSTAIR_DISTANCE = BigDecimal.valueOf(0.60);

    private static final String SUB_RULE_24_3 = "24(3)";
    private static final String SUB_RULE_24_3_DESCRIPTION = "Front yard distance";

    private static final String SUB_RULE_24_12_3 = "24(12(3))";
    private static final String SUB_RULE_24_12_3_DESCRIPTION = "Basement Front yard distance";

    private static final String SUB_RULE_24_4 = "24(4)";
    private static final String SUB_RULE_24_4_DESCRIPTION = "Rear yard distance";

    private static final String SUB_RULE_24_12_4 = "24(12(4))";
    private static final String SUB_RULE_24_12_4_DESCRIPTION = "Basement Rear yard distance";

    private static final String SUB_RULE_24_5 = "24(5)";
    private static final String SUB_RULE_24_5_SIDE1 = "24(5) Side Yard 1";
    private static final String SUB_RULE_24_5_SIDE2 = "24(5) Side Yard 2";
    private static final String SUB_RULE_24_5_SIDE1_DESCRIPTION = "Side yard 1 distance";
    private static final String SUB_RULE_24_5_SIDE2_DESCRIPTION = "Side yard 2 distance";

    private static final String SUB_RULE_24_12_5 = "24(12(5))";
    private static final String SUB_RULE_24_12_5_DESCRIPTION = " Basement Side yard distance";

    private static final String SIDE_YARD_1_DESC = "Minimum open space on Side 1";
    private static final String SIDE_YARD_2_DESC = "Minimum open space on Side 2";
    private static final String BASEMENT_SIDE_YARD_1_DESC = "Minimum open space on Basement Side 1";
    private static final String BASEMENT_SIDE_YARD_2_DESC = "Minimum open space on Basement Side 2";

    private static final String SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING = " 0 MTR With No opening on side up to 2.1 MTR height and NOC to Abut next plot";
    private static final String SIDE_YARD_1_EXPECTED_NO_OPENING = " Minimum .75 MTR With no opening on side up to 2.1 MTR height";
    private static final String SIDE_YARD_1_EXPECTED = " Minimum 1 MTR ";
    private static final String SIDE_YARD_2_EXPECTED = "Minimum 1 MTR ";

    private static final String SUB_RULE_24_11 = "24(11)";
    private static final String SUB_RULE_24_11_DESCRIPTION = "Open space for open stair";

    private static final String SUB_RULE_24_10 = "24(10)";
    private static final String SUB_RULE_24_10_DESCRIPTION = "No construction or hangings outside the boundaries of the site";

    private static final String SUB_RULE_24_1 = "24(1)";
    private static final String SUB_RULE_24_1_DESCRIPTION = "Every habitable room shall abut on an exterior or interior open space or verandah";

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

            rule24_1(planDetail);
            rule24_5(planDetail, DcrConstants.NON_BASEMENT);
            // For building of heights less than or equal to 10
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null

                    && planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
                    && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0)
                rule24_3(planDetail, DcrConstants.NON_BASEMENT);

            // Building Height between 7 to 10
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                    && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
                    && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {
                // rule24_5(planDetail, SIDE1MINIMUM_DISTANCE, SIDE2MINIMUM_DISTANCE, DcrConstants.NON_BASEMENT);
                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE, REARYARDMEAN_DISTANCE, DcrConstants.NON_BASEMENT);
            }

            // Building Height less than 7
            if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                    && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) <= 0)
                // OPENING PRESENT
                if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningOnRear())
                    rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOPENING, REARYARDMEAN_DISTANCE_WITHOPENING,
                            DcrConstants.NON_BASEMENT);
                else {

                    // rule24_5(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING,
                    // SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOPENING,
                    // DcrConstants.NON_BASEMENT);
                    rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING, REARYARDMEAN_DISTANCE_WITHOUTOPENING,
                            DcrConstants.NON_BASEMENT);
                    rule24_4_LessThan7_WithoutOpeningCorrespondingFloor(planDetail, DcrConstants.NON_BASEMENT);

                    // WITHOUT OPENING AND NOC PRESENT
                    if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocToAbutSide())
                        rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING,
                                SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.NON_BASEMENT);

                    rule24_4_LessThan7WithoutOpeningNoc(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING,
                            DcrConstants.NON_BASEMENT);

                }
        }
        rule24_10(planDetail);
        rule24_11(planDetail);

        if (planDetail.getBasement() != null)
            rule24_12(planDetail);
        return planDetail;
    }

    private void rule24_11(PlanDetail planDetail) {
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getOpenStairs() != null
                && planDetail.getBuilding().getOpenStairs().size() > 0) {
            for (Measurement measurement : planDetail.getBuilding().getOpenStairs()) {
                if (measurement.getMinimumDistance().compareTo(OPENSTAIR_DISTANCE) >= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_11, SUB_RULE_24_11_DESCRIPTION,
                                    DcrConstants.OPEN_STAIR_DESC,
                                    "Min" + OPENSTAIR_DISTANCE.toString()
                                            + DcrConstants.IN_METER,
                                    measurement.getMinimumDistance().toString()
                                            + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_11, SUB_RULE_24_11_DESCRIPTION,
                                    DcrConstants.OPEN_STAIR_DESC,
                                    "Min" + OPENSTAIR_DISTANCE.toString()
                                            + DcrConstants.IN_METER,
                                    measurement.getMinimumDistance().toString()
                                            + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
            }
        }

    }

    private void rule24_12(PlanDetail planDetail) {

        rule24_5(planDetail, DcrConstants.BASEMENT);

        // For building of heights less than or equal to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0)
            rule24_3(planDetail, DcrConstants.BASEMENT);

        // Building Height between 7 to 10
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) > 0
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(10)) <= 0) {

            rule24_4(planDetail, REARYARDMINIMUM_DISTANCE, REARYARDMEAN_DISTANCE, DcrConstants.BASEMENT);
        }

        // Building Height less than 7
        if (planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
                && planDetail.getBuilding().getBuildingHeight().compareTo(new BigDecimal(7)) <= 0)
            // OPENING PRESENT
            if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getOpeningOnRear())
                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOPENING, REARYARDMEAN_DISTANCE_WITHOPENING,
                        DcrConstants.BASEMENT);
            else {

                rule24_4(planDetail, REARYARDMINIMUM_DISTANCE_WITHOUTOPENING, REARYARDMEAN_DISTANCE_WITHOUTOPENING,
                        DcrConstants.BASEMENT);
                rule24_4_LessThan7_WithoutOpeningCorrespondingFloor(planDetail, DcrConstants.BASEMENT);

                // WITHOUT OPENING AND NOC PRESENT
                if (planDetail.getPlanInformation() != null && planDetail.getPlanInformation().getNocToAbutSide())
                    rule24_4(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING,
                            SIDE2MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING, DcrConstants.BASEMENT);

                rule24_4_LessThan7WithoutOpeningNoc(planDetail, SIDE1MINIMUM_DISTANCE_LESSTHAN7_WITHOUTOPENING,
                        DcrConstants.BASEMENT);

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
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_12_3, SUB_RULE_24_12_3_DESCRIPTION,
                                    DcrConstants.BSMT_FRONT_YARD_DESC,
                                    MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                            + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtFrontYard().getMinimumDistance().toString() + "," +
                                            planDetail.getPlot().getBsmtFrontYard().getMean().toString() + ")"
                                            + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_12_3, SUB_RULE_24_12_3_DESCRIPTION,
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

    private void rule24_5(PlanDetail planDetail, String type) {

        Yard sideYard1 = null;
        Yard sideYard2 = null;
        String subRule = "";
        String side1Desc = "";
        String side2Desc = "";
        String side1FieldName = "";
        String side2FieldName = "";
        String side1Expected = "";
        String side2Expected = "";
        if (planDetail.getPlot() == null)
            return;

        Plot plot = planDetail.getPlot();

        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {
            subRule = SUB_RULE_24_12_5;
            if (plot.getBsmtSideYard1() == null)
                return;
            sideYard1 = plot.getBsmtSideYard1();

            if (plot.getBsmtSideYard2() == null)
                return;
            sideYard2 = plot.getBsmtSideYard2();
            side1Desc = BASEMENT_SIDE_YARD_1_DESC;
            side2Desc = BASEMENT_SIDE_YARD_2_DESC;
            side1FieldName = DcrConstants.BSMT_SIDE_YARD1_DESC;
            side2FieldName = DcrConstants.BSMT_SIDE_YARD2_DESC;
        }

        else {
            subRule = SUB_RULE_24_5;
            if (plot.getSideYard1() == null)
                return;
            sideYard1 = plot.getSideYard1();

            if (plot.getSideYard2() == null)
                return;
            sideYard2 = plot.getSideYard2();
            side1Desc = SIDE_YARD_1_DESC;
            side2Desc = SIDE_YARD_2_DESC;
            side1FieldName = DcrConstants.SIDE_YARD1_DESC;
            side2FieldName = DcrConstants.SIDE_YARD2_DESC;
        }

        if (sideYard1.getMean() == null)
            return;

        if (sideYard1.getMinimumDistance() == null)
            return;

        if (sideYard2.getMean() == null)
            return;

        if (sideYard2.getMinimumDistance() == null)
            return;

        if (planDetail.getBuilding() == null || planDetail.getBuilding().getBuildingHeight() == null)
            return;

        Boolean valid1 = false;
        Boolean valid2 = false;
        BigDecimal buildingHeight = planDetail.getBuilding().getBuildingHeight();
        double min = 0;
        double max = 0;
        if (sideYard1.getMinimumDistance().doubleValue() > sideYard1.getMinimumDistance().doubleValue()) {
            min = sideYard1.getMinimumDistance().doubleValue();
            max = sideYard2.getMinimumDistance().doubleValue();
        } else {
            min = sideYard2.getMinimumDistance().doubleValue();
            max = sideYard1.getMinimumDistance().doubleValue();
        }

        if (max >= SIDE2MINIMUM_DISTANCE.doubleValue())
            valid2 = true;

        if (buildingHeight.intValue() <= 7) {
            if (planDetail.getPlanInformation().getNocToAbutSide() && !planDetail.getPlanInformation().getOpeningOnSide()) {
                side1Expected = SIDE_YARD_1_EXPECTED_WITHNOC_NO_OPENING;
                if (min >= 0d) {
                    valid1 = true;

                }
            } else if (!planDetail.getPlanInformation().getOpeningOnSide()) {
                side1Expected = SIDE_YARD_1_EXPECTED_NO_OPENING;
                if (min >= 0.75) {
                    valid1 = true;

                }
            } else {
                side1Expected = SIDE_YARD_1_EXPECTED;  
                if (min >= 1) {
                    valid1 = true;

                }
            }

        } else if (buildingHeight.intValue() > 7 && buildingHeight.intValue() <= 10) {
            side1Expected = SIDE_YARD_1_EXPECTED;
            if (min >= 1) {
                valid1 = true;
            }
        }

        String opening=planDetail.getPlanInformation().getOpeningOnSide()?"Yes":"No";
        String noc = planDetail.getPlanInformation().getNocToAbutSide()?"Yes":"No";
        String message= DxfFileConstants.OPENING_BELOW_2_1_ON_SIDE_LESS_1M +": "+ opening
                       +" , "+DxfFileConstants.NOC_TO_ABUT_SIDE+": "+ noc ;                  
                                     
        if (valid1 == true) {

            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, subRule+" Side Yard 1", side1Desc,
                            side1FieldName,
                            side1Expected,
                            min + DcrConstants.IN_METER,
                            Result.Accepted,message ));
        } else {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, subRule+" Side Yard 1", side1Desc,
                            side1FieldName,
                            side1Expected,
                            min + DcrConstants.IN_METER, 
                            Result.Not_Accepted,message));

        }
        if (valid2 == true) {

            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, subRule+" Side Yard 2", side2Desc,
                            side2FieldName,
                            SIDE2MINIMUM_DISTANCE.toString(),
                            max + DcrConstants.IN_METER,
                            Result.Accepted, null));
        } else {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, subRule+" Side Yard 2", side2Desc,
                            side2FieldName,
                            SIDE2MINIMUM_DISTANCE.toString(),
                            max + DcrConstants.IN_METER,
                            Result.Not_Accepted,
                            null));

        }

    }

    private void rule24_4(PlanDetail planDetail, BigDecimal rearYardMin, BigDecimal rearYardMean, String type) {
        if (DcrConstants.BASEMENT.equalsIgnoreCase(type)) {

            if (planDetail.getPlot() != null && planDetail.getPlot().getBsmtRearYard() != null &&
                    planDetail.getPlot().getBsmtRearYard().getPresentInDxf()
                    && planDetail.getPlot().getBsmtRearYard().getMinimumDistance() != null
                    && planDetail.getPlot().getBsmtRearYard().getMean() != null)
                if (planDetail.getPlot().getBsmtRearYard().getMinimumDistance().compareTo(rearYardMin) >= 0 &&
                        planDetail.getPlot().getBsmtRearYard().getMean().compareTo(rearYardMean) >= 0)
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_12_4, SUB_RULE_24_12_4_DESCRIPTION,
                                    DcrConstants.BSMT_REAR_YARD_DESC,
                                    MEAN_MINIMUM + "(" + rearYardMin.toString() + "," + rearYardMean + ")"
                                            + DcrConstants.IN_METER,
                                    "(" + planDetail.getPlot().getBsmtRearYard().getMinimumDistance().toString() + "," +
                                            planDetail.getPlot().getBsmtRearYard().getMean().toString() + ")"
                                            + DcrConstants.IN_METER,
                                    Result.Accepted, null));
                else
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_12_4, SUB_RULE_24_12_4_DESCRIPTION,
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
            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(rearYardMin) >= 0 &&
                    planDetail.getPlot().getRearYard().getMean().compareTo(rearYardMean) >= 0)
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
                        .compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) >= 0)
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
                        .compareTo(REARYARDMINIMUM_DISTANCE_WITHOUTOPENING_CORRESPONDINGFLOOR) >= 0)
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
                SubRuleOutput subRule24_5 = new SubRuleOutput();
                valuesMap.put(ruleOutput.getKey(), new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty()){
                    for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs()){
                        try {
                            
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_24_5)) { 
                                subRule24_5.setKey(subRuleOutput.getKey());
                                subRule24_5.setMessage(subRuleOutput.getMessage());
                                subRule24_5.setRuleDescription(subRuleOutput.getRuleDescription());
                                subRule24_5.getRuleReportOutputs().addAll(subRuleOutput.getRuleReportOutputs());
                            } else {
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            valuesMap.put(subRuleOutput.getKey() + "DataSource",
                                    new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRuleOutput, drb2, valuesMap));
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                }
                    if(subRule24_5 != null) {
                        valuesMap.put(SUB_RULE_24_5 + "DataSource",
                                new JRBeanCollectionDataSource(subRule24_5.getRuleReportOutputs()));
                        try {
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule24_5, drb2, valuesMap));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
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

        drb.setMargins(10, 10, 10, 10);
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

        Polygon plotPolygon = Util.getPolygon(pl.getPlot().getPolyLine());
        Iterator buildingIterator = pl.getBuilding().getPolyLine().getVertexIterator();
        Boolean buildingOutSideBoundary = false;
        while (buildingIterator.hasNext()) {
            DXFVertex dxfVertex = (DXFVertex) buildingIterator.next();
            Point point = dxfVertex.getPoint();
            if (!RAY_CASTING.contains(point, plotPolygon))
                buildingOutSideBoundary = true;

        }
        if (buildingOutSideBoundary)
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_10, SUB_RULE_24_10_DESCRIPTION,
                            DcrConstants.BUILDING_FOOT_PRINT,
                            DcrConstants.BUILDING_FOOT_PRINT + " Should be inside Plot Boundary",
                            DcrConstants.BUILDING_FOOT_PRINT + " is outside Plot Boundary",
                            Result.Not_Accepted, null));

        Iterator shadeIterator = pl.getBuilding().getShade().getPolyLine().getVertexIterator();
        Boolean shadeOutSideBoundary = false;

        while (shadeIterator.hasNext()) {
            DXFVertex dxfVertex = (DXFVertex) shadeIterator.next();
            Point point = dxfVertex.getPoint();
            if (!RAY_CASTING.contains(point, plotPolygon))
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

    private void rule24_1(PlanDetail pl) {

        if (pl.getBuilding().getFloors().size() <= 0)
            return;

        List<Point> vertices = new ArrayList<>();

        int faileCount = 0;

        Util.print(pl.getBuilding().getFloors());
        for (Floor floor : pl.getBuilding().getFloors()) {
            if (floor.getExterior() == null) {
                continue;
            }
            // LOG.info("ext wall points");
            // DXFLWPolyline extWall = floor.getExterior().getPolyLine();
            vertices = Util.pointsOnPolygon(floor.getExterior().getPolyLine());

            List<Point> extPoints = Util.findPointsOnPolylines(vertices);

            List<Point> openSpacePoints = new ArrayList<>();
            // get all open space points
            for (Measurement openSpace : floor.getOpenSpaces()) {
                List<Point> pointsOnPolygon = Util.pointsOnPolygon(openSpace.getPolyLine());
                openSpacePoints.addAll(Util.findPointsOnPolylines(pointsOnPolygon));
            }

            if (floor.getHabitableRooms().size() >= 1) {

                int habitableRoomNo = 0;
                for (Room room : floor.getHabitableRooms()) {
                    habitableRoomNo++;
                    Boolean habitable = false;
                    Iterator habitableRoomPointItr = room.getPolyLine().getVertexIterator();

                    habitable: while (habitableRoomPointItr.hasNext()) {

                        DXFVertex next = (DXFVertex) habitableRoomPointItr.next();
                        // LOG.info(" Contains "+next.getPoint().getX()+","+next.getPoint().getY());
                        if (extPoints.contains(next.getPoint())) {
                            // Point point = extPoints.get(extPoints.indexOf(next.getPoint()));
                            // LOG.info("Contains found+" +point.getX() +","+point.getY());
                            habitable = true;
                            break habitable;

                        }
                        if (!habitable) {
                            if (openSpacePoints.contains(next.getPoint())) {
                                habitable = true;
                                break habitable;

                            }
                        }

                    }

                    habitable1: if (!habitable) {
                        // TODO :Verify this
                        Iterator habitableRoomPointItr2 = room.getPolyLine().getVertexIterator();
                        habitableRoomLoop2: while (habitableRoomPointItr2.hasNext()) {

                            DXFVertex next = (DXFVertex) habitableRoomPointItr2.next();
                            // LOG.info(" Hroorm "+next.getPoint().getX()+","+next.getPoint().getY());
                            // LOG.info("Looping"+extPoints.size());
                            for (Point p : extPoints) {
                                // LOG.info("Comparing"+p.getX()+","+p.getY());
                                // LOG.info("next"+next.getPoint().getX()+","+next.getPoint().getY());
                                if (Util.pointsEqualsWith2PercentError(p, next.getPoint())) {
                                    // LOG.info("Matched");
                                    habitable = true;
                                    break habitable1;

                                }

                            }
                            if (!habitable) {
                                for (Point p : openSpacePoints) {
                                    // LOG.info("Comparing"+p.getX()+","+p.getY());
                                    // LOG.info("next"+next.getPoint().getX()+","+next.getPoint().getY());
                                    if (Util.pointsEqualsWith2PercentError(p, next.getPoint())) {
                                        // LOG.info("Matched");
                                        habitable = true;
                                        break habitable1;

                                    }

                                }
                            }

                        }
                    }

                    if (!habitable) {

                        LOG.info("since rooom is not habitable " + habitableRoomNo + " floor no" + floor.getName());
                        faileCount++;

                        pl.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_1, SUB_RULE_24_1_DESCRIPTION,
                                        DcrConstants.HABITABLE_ROOM,
                                        DcrConstants.HABITABLE_ROOM
                                                + " shall abut on an exterior or interior open space or verandah ",
                                        DcrConstants.HABITABLE_ROOM + " number " + habitableRoomNo + " in " + floor.getName()
                                                + " is not abuting an exterior or interior open space or verandah",
                                        Result.Not_Accepted, null));
                    }

                }
            }

        }
        if (faileCount > 0) {
            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_1, SUB_RULE_24_1_DESCRIPTION,
                            DcrConstants.HABITABLE_ROOM,
                            DcrConstants.HABITABLE_ROOM + " shall abut on an exterior or interior open space or verandah ",
                            " " + faileCount + " " + DcrConstants.HABITABLE_ROOM
                                    + " (s)  not abuting an exterior or interior open space or verandah",
                            Result.Not_Accepted, null));
        } else {

            pl.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE24, SUB_RULE_24_1, SUB_RULE_24_1_DESCRIPTION,
                            DcrConstants.HABITABLE_ROOM,
                            DcrConstants.HABITABLE_ROOM + " shall abut on an exterior or interior open space or verandah ",
                            " All " + DcrConstants.HABITABLE_ROOM
                                    + " (s)  are abuting an exterior or interior open space or verandah",
                            Result.Accepted, null));

        }

    }

}
