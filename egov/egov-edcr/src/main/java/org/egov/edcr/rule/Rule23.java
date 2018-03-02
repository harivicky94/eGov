package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
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
public class Rule23 extends GeneralRule {

    private static final String SUB_RULE_5_DESCRIPTION = "Overhead Electric line and Voltage";
    private static final String SUB_RULE_5 = "23(5)";

    private static final BigDecimal VERTICAL_DISTANCE_11000 = BigDecimal.valueOf(2.4);
    private static final BigDecimal VERTICAL_DISTANCE_33000 = BigDecimal.valueOf(3.7);
    private static final BigDecimal HORIZONTAL_DISTANCE_33000 = BigDecimal.valueOf(1.85);

    private static final int VOLTAGE_11000 = 11;
    private static final int VOLTAGE_33000 = 33;
    private static final BigDecimal HORIZONTAL_DISTANCE_11000 = BigDecimal.valueOf(1.2);
    private static final String SUB_RULE_23_4 = "23(4)";
    private static final String SUB_RULE_23_4_DESCRIPTION = " Plot present in CRZ Zone";

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 23");
        if (planDetail.getElectricLine().getPresentInDxf()) {
            if (planDetail.getElectricLine().getVoltage() == null) {
                errors.put(DcrConstants.VOLTAGE,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.VOLTAGE }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
            if (planDetail.getElectricLine().getHorizontalDistance() == null
                && planDetail.getElectricLine().getVerticalDistance() == null) {
                errors.put(DcrConstants.ELECTRICLINE_DISTANCE,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.ELECTRICLINE_DISTANCE }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        rule23_4(planDetail);
        rule23_5(planDetail);

        return planDetail;

    }

    private void rule23_4(PlanDetail planDetail) {
        if (planDetail.getPlanInformation().getCrzZoneArea())
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_23_4, SUB_RULE_23_4_DESCRIPTION,
                            DcrConstants.CRZZONE,
                            null,
                            null,
                            Result.Verify, DcrConstants.CRZZONE + DcrConstants.OBJECTDEFINED_DESC));
    }

    private void rule23_5(PlanDetail planDetail) {
        if (planDetail.getElectricLine().getPresentInDxf())
            if (planDetail.getElectricLine().getVoltage() != null
                && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.ZERO) > 0
                && (planDetail.getElectricLine().getHorizontalDistance() != null
                    || planDetail.getElectricLine().getVerticalDistance() != null))
                if (planDetail.getElectricLine().getHorizontalDistance() != null) {
                    if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {
                        if (planDetail.getElectricLine().getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_11000) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));
                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >= 0
                               && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {

                        if (planDetail.getElectricLine().getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_33000) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));

                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {

                        Double totalHorizontalOHE = HORIZONTAL_DISTANCE_33000.doubleValue() + 0.3 *
                                                                                              Math.ceil(
                                                                                                      planDetail.getElectricLine().getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000))
                                                                                                                .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP)
                                                                                                                .doubleValue());

                        if (planDetail.getElectricLine().getHorizontalDistance()
                                      .compareTo(BigDecimal.valueOf(totalHorizontalOHE)) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            totalHorizontalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            totalHorizontalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));
                    }

                } else if (planDetail.getElectricLine().getVerticalDistance() != null)
                    if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {
                        if (planDetail.getElectricLine().getVerticalDistance().compareTo(VERTICAL_DISTANCE_11000) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));
                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >= 0
                               && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {

                        if (planDetail.getElectricLine().getVerticalDistance().compareTo(VERTICAL_DISTANCE_33000) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));

                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {

                        Double totalVertficalOHE = VERTICAL_DISTANCE_33000.doubleValue() + 0.3 *
                                                                                           Math.ceil(
                                                                                                   planDetail.getElectricLine().getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000))
                                                                                                             .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP)
                                                                                                             .doubleValue());

                        if (planDetail.getElectricLine().getVerticalDistance()
                                      .compareTo(BigDecimal.valueOf(totalVertficalOHE)) >= 0)
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            totalVertficalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Accepted, null));
                        else
                            planDetail.reportOutput
                                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE23, SUB_RULE_5, SUB_RULE_5_DESCRIPTION,
                                            DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            totalVertficalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                            + DcrConstants.IN_METER,
                                            Result.Not_Accepted, null));
                    }

    }

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE23)) {
                FastReportBuilder drb = new FastReportBuilder();
                StringBuilder stringBuilder = new StringBuilder();
                if (ruleOutput.getMessage() != null)
                    stringBuilder.append("Message : ").append(ruleOutput.getMessage()).append("\\n");
                if (ruleOutput.getRuleDescription() != null)
                    stringBuilder.append("Description : ").append(ruleOutput.getRuleDescription()).append("\\n");
                drb.setMargins(0, 0, 10, 10);
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

        drb.setMargins(0, 0, 10, 10);
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