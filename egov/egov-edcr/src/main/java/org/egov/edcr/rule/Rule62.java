package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
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
public class Rule62 extends GeneralRule {
    private static final BigDecimal _NOTIFIEDROADDISTINCE = BigDecimal.valueOf(3);
    private static final BigDecimal _NONNOTIFIEDROADDISTINCE = BigDecimal.valueOf(2);
    private static final BigDecimal FRONTYARDMINIMUM_DISTANCE = BigDecimal.valueOf(1.2);
    private static final BigDecimal FRONTYARDMEAN_DISTANCE = BigDecimal.valueOf(1.8);
    private static final BigDecimal REARYARDMINIMUM_DISTANCE = BigDecimal.valueOf(0.5);
    private static final BigDecimal REARYARDMEAN_DISTANCE = BigDecimal.valueOf(1);

    private static final BigDecimal SIDE1MINIMUM_DISTANCE = BigDecimal.valueOf(0.9);
    private static final BigDecimal SIDE2MINIMUM_DISTANCE = BigDecimal.valueOf(0.6);
    private static final String SUB_RULE_62_2 = "62(2)";
    private static final String SUB_RULE_62_2DESCRIPTION = "Any Side yard validation";
    private static final String SUB_RULE_62_1 = "62(1)";
    private static final String SUB_RULE_62_1DESCRIPTION = "Minimum distance between plot boundary and abutting Street.";
    private static final String SUB_RULE_62_1A = "62(1)A";
    private static final String SUB_RULE_62_1A_DESCRIPTION = "Front yard validation";
    private static final String SUB_RULE_62_3 = "62(3)";
    private static final String SUB_RULE_62_3_DESCRIPTION = "Rear yard validation";
    private String MEAN_MINIMUM = "(Minimum distince,Mean distance) ";

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null) {

            // If either notified or non notified road width not defined, then show error.
            if ((planDetail.getNotifiedRoads() == null || planDetail.getNonNotifiedRoads() == null) &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                  planDetail.getNonNotifiedRoads().size() > 0)) {
                errors.put(DcrConstants.RULE62,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.ROAD));
                planDetail.addErrors(errors);
            }
            if (planDetail.getNotifiedRoads() != null &&
                planDetail.getNotifiedRoads().size() > 0)
                for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                    if (notifiedRoad.getShortestDistanceToRoad() == null ||
                        notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                        planDetail.addErrors(errors);
                    }
            if (planDetail.getNonNotifiedRoads() != null &&
                planDetail.getNonNotifiedRoads().size() > 0)
                for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads())
                    if (nonNotifiedRoad.getShortestDistanceToRoad() == null ||
                        nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                        errors.put(DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                        planDetail.addErrors(errors);
                    }

            if (planDetail.getPlot() != null && (planDetail.getPlot().getFrontYard() == null ||
                                                 !planDetail.getPlot().getFrontYard().getPresentInDxf())) {
                errors.put(DcrConstants.FRONT_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.FRONT_YARD_DESC));
                planDetail.addErrors(errors);
            }
            if (planDetail.getPlot() != null && (planDetail.getPlot().getSideYard1() == null ||
                                                 !planDetail.getPlot().getSideYard1().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD1_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD1_DESC));
                planDetail.addErrors(errors);
            }
            if (planDetail.getPlot() != null && (planDetail.getPlot().getSideYard2() == null ||
                                                 !planDetail.getPlot().getSideYard2().getPresentInDxf())) {
                errors.put(DcrConstants.SIDE_YARD2_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SIDE_YARD2_DESC));
                planDetail.addErrors(errors);
            }
            if (planDetail.getPlot() != null && (planDetail.getPlot().getRearYard() == null ||
                                                 !planDetail.getPlot().getRearYard().getPresentInDxf())) {
                errors.put(DcrConstants.REAR_YARD_DESC,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.REAR_YARD_DESC));
                planDetail.addErrors(errors);
            }

        }

        return planDetail;
    }

    private String prepareMessage(String code, String args) {
        return edcrMessageSource.getMessage(code,
                new String[] { args }, LocaleContextHolder.getLocale());
        // return code+" "+args;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule62_1(planDetail);
        rule62_1A(planDetail);
        if (planDetail != null && planDetail.getPlanInformation() != null
            && planDetail.getPlanInformation().getOpeningOnSide())
            rule62_2(planDetail);
        rule62_3(planDetail);
        return planDetail;
    }

    private void rule62_2(PlanDetail planDetail) {
        BigDecimal sideyard1 = BigDecimal.ZERO;
        BigDecimal sideyard2 = BigDecimal.ZERO;

        // Either side1 or side2 must be 90 cm.
        // If one side >90, then other side must be 60.
        // if less than 60, then noc check required.

        // Some side may be not defined.

        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard1() != null &&
            planDetail.getPlot().getSideYard1().getPresentInDxf()
            && planDetail.getPlot().getSideYard1().getMinimumDistance() != null)
            sideyard1 = planDetail.getPlot().getSideYard1().getMinimumDistance();
        if (planDetail.getPlot() != null && planDetail.getPlot().getSideYard2() != null &&
            planDetail.getPlot().getSideYard2().getPresentInDxf()
            && planDetail.getPlot().getSideYard2().getMinimumDistance() != null)
            sideyard2 = planDetail.getPlot().getSideYard2().getMinimumDistance();

        if (sideyard1.compareTo(sideyard2) > 0) {
            if (sideyard1.compareTo(SIDE1MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard2.compareTo(SIDE2MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                + DcrConstants.IN_METER,
                                Result.Verify, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                + DcrConstants.IN_METER,
                                Result.Accepted, null));

        } else {
            if (sideyard2.compareTo(SIDE1MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD2_DESC,
                                SIDE1MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard2.toString()
                                + DcrConstants.IN_METER,
                                Result.Accepted, null));

            if (sideyard1.compareTo(SIDE2MINIMUM_DISTANCE) < 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                + DcrConstants.IN_METER,
                                Result.Verify, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_2, SUB_RULE_62_2DESCRIPTION,
                                DcrConstants.SIDE_YARD1_DESC,
                                SIDE2MINIMUM_DISTANCE.toString() + DcrConstants.IN_METER,
                                sideyard1.toString()
                                + DcrConstants.IN_METER,
                                Result.Accepted, null));

        }

    }

    private void rule62_1(PlanDetail planDetail) {
        // If both roads are not defined.
        if (planDetail.getNotifiedRoads() != null &&
            !(planDetail.getNotifiedRoads().size() > 0 ||
              planDetail.getNonNotifiedRoads().size() > 0))
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                            DcrConstants.ROAD,
                            null,
                            null,
                            Result.Verify, DcrConstants.ROAD + DcrConstants.OBJECTNOTDEFINED_DESC));

        else if (planDetail.getNotifiedRoads() != null &&
                 planDetail.getNotifiedRoads().size() > 0)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                if (notifiedRoad.getShortestDistanceToRoad() != null &&
                    notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (notifiedRoad.getShortestDistanceToRoad().compareTo(_NOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                        + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getShortestDistanceToRoad().toString()
                                        + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
        // If non notified road present then check 2 mts distance should maintain
        if (planDetail.getNonNotifiedRoads() != null &&
            planDetail.getNonNotifiedRoads().size() > 0)
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads())
                if (nonNotifiedRoad.getShortestDistanceToRoad() != null &&
                    nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0)
                    if (nonNotifiedRoad.getShortestDistanceToRoad().compareTo(_NONNOTIFIEDROADDISTINCE) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                        + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1, SUB_RULE_62_1DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROAD,
                                        _NONNOTIFIEDROADDISTINCE.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getShortestDistanceToRoad().toString()
                                        + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
    }

    private void rule62_1A(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getFrontYard() != null &&
            planDetail.getPlot().getFrontYard().getPresentInDxf()
            && planDetail.getPlot().getFrontYard().getMinimumDistance() != null
            && planDetail.getPlot().getFrontYard().getMean() != null)
            if (planDetail.getPlot().getFrontYard().getMinimumDistance().compareTo(FRONTYARDMINIMUM_DISTANCE) >= 0 &&
                planDetail.getPlot().getFrontYard().getMean().compareTo(FRONTYARDMEAN_DISTANCE) >= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1A, SUB_RULE_62_1A_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_1A, SUB_RULE_62_1A_DESCRIPTION,
                                DcrConstants.FRONT_YARD_DESC,
                                MEAN_MINIMUM + "(" + FRONTYARDMINIMUM_DISTANCE.toString() + "," + FRONTYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getFrontYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getFrontYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }

    private void rule62_3(PlanDetail planDetail) {

        if (planDetail.getPlot() != null && planDetail.getPlot().getRearYard() != null &&
            planDetail.getPlot().getRearYard().getPresentInDxf()
            && planDetail.getPlot().getRearYard().getMinimumDistance() != null
            && planDetail.getPlot().getRearYard().getMean() != null)
            if (planDetail.getPlot().getRearYard().getMinimumDistance().compareTo(REARYARDMINIMUM_DISTANCE) >= 0 &&
                planDetail.getPlot().getRearYard().getMean().compareTo(REARYARDMEAN_DISTANCE) >= 0)
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_3, SUB_RULE_62_3_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Accepted, null));
            else
                planDetail.reportOutput  
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE62, SUB_RULE_62_3, SUB_RULE_62_3_DESCRIPTION,
                                DcrConstants.REAR_YARD_DESC,
                                MEAN_MINIMUM + "(" + REARYARDMINIMUM_DISTANCE.toString() + "," + REARYARDMEAN_DISTANCE + ")"
                                + DcrConstants.IN_METER,
                                "(" + planDetail.getPlot().getRearYard().getMinimumDistance().toString() + "," +
                                planDetail.getPlot().getRearYard().getMean().toString() + ")" + DcrConstants.IN_METER,
                                Result.Not_Accepted, null));
    }

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE62)) {
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


}