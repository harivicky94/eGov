package org.egov.edcr.rule;

import static org.egov.edcr.utility.DcrConstants.RAINWATER_HARVESTING;
import static org.egov.edcr.utility.DcrConstants.SOLAR;

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
public class Rule109 extends GeneralRule {
    private static final BigDecimal ONEHUNDREDFIFTY = BigDecimal.valueOf(150);
    private static final BigDecimal TWENTYFIVE = BigDecimal.valueOf(25);
    private static final BigDecimal FOURHUNDRED = BigDecimal.valueOf(400);
    private static final BigDecimal THREEHUNDREDANDTWENTY = BigDecimal.valueOf(320);

    private static final String SUB_RULE_109_B_DESCRIPTION = "RainWater Storage Arrangement ";
    private static final String SUB_RULE_109_C_DESCRIPTION = "Solar Assisted Water Heating / Lighting system ";

    private static final String SUB_RULE_109_B = "109(B)";
    private static final String SUB_RULE_109_C = "109(C)";

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

        if (planDetail != null && planDetail.getBuilding().getTotalFloorArea() != null
                && planDetail.getBuilding().getTotalFloorArea().compareTo(ONEHUNDREDFIFTY) > 0
                && planDetail.getPlot().getArea().compareTo(THREEHUNDREDANDTWENTY) > 0
                && !planDetail.getUtility().getRainWaterHarvest().isEmpty()
                && planDetail.getUtility().getRaintWaterHarvestingTankCapacity() == null) {
            errors.put(DcrConstants.RAINWATER_HARVES_TANKCAPACITY,
                    prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.RAINWATER_HARVES_TANKCAPACITY));
            planDetail.addErrors(errors);
        }
        if (planDetail != null && planDetail.getBuilding().getTotalBuitUpArea() != null
                && planDetail.getBuilding().getTotalBuitUpArea().compareTo(FOURHUNDRED) > 0
                && planDetail.getUtility().getSolar().isEmpty()) {
            errors.put(DcrConstants.SOLAR_SYSTEM,
                    prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SOLAR_SYSTEM));
            planDetail.addErrors(errors);
        }

        return planDetail;
    }

    private String prepareMessage(String code, String args) {
        return edcrMessageSource.getMessage(code,
                new String[] { args }, LocaleContextHolder.getLocale());
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule109B(planDetail);
        rule109C(planDetail);
        return planDetail;
    }

    private void rule109B(PlanDetail planDetail) {
        String rule = DcrConstants.RULE109;
        String subRule = SUB_RULE_109_B;
        String subRuleDesc = SUB_RULE_109_B_DESCRIPTION;
        BigDecimal exptectedTankCapacity;

        if (planDetail != null && planDetail.getBuilding().getTotalFloorArea() != null
                && planDetail.getBuilding().getTotalFloorArea().compareTo(ONEHUNDREDFIFTY) > 0
                && planDetail.getPlot().getArea().compareTo(THREEHUNDREDANDTWENTY) > 0
                && !planDetail.getUtility().getRainWaterHarvest().isEmpty()
                && planDetail.getUtility().getRaintWaterHarvestingTankCapacity() != null) {
            exptectedTankCapacity = TWENTYFIVE.multiply(planDetail.getBuilding().getTotalFloorArea()).setScale(2,
                    RoundingMode.HALF_UP);

            if ((planDetail.getUtility().getRaintWaterHarvestingTankCapacity()).compareTo(exptectedTankCapacity) >= 0) { 
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                RAINWATER_HARVESTING,
                                exptectedTankCapacity.toString(),
                                planDetail.getUtility().getRaintWaterHarvestingTankCapacity().toString(),
                                Result.Accepted, RAINWATER_HARVESTING));
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                RAINWATER_HARVESTING,
                                exptectedTankCapacity.toString(),
                                planDetail.getUtility().getRaintWaterHarvestingTankCapacity().toString(),
                                Result.Not_Accepted, RAINWATER_HARVESTING));
            }
        }

    }

    private void rule109C(PlanDetail planDetail) {
        String rule = DcrConstants.RULE109;
        String subRule = SUB_RULE_109_C;
        String subRuleDesc = SUB_RULE_109_C_DESCRIPTION;

        if (planDetail != null && planDetail.getBuilding().getTotalBuitUpArea() != null
                && planDetail.getBuilding().getTotalBuitUpArea().compareTo(FOURHUNDRED) > 0) {

            if (!planDetail.getUtility().getSolar().isEmpty()) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                SOLAR,
                                null,
                                null,
                                Result.Accepted, SOLAR + " " + DcrConstants.OBJECTDEFINED_DESC));
            } else {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                SOLAR,
                                null,
                                null,
                                Result.Not_Accepted, SOLAR + " " + DcrConstants.OBJECTNOTDEFINED_DESC));
            }
        }

    }

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE104)) {
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
                SubRuleOutput subRule109B = new SubRuleOutput();
                SubRuleOutput subRule109C = new SubRuleOutput();

                valuesMap.put(ruleOutput.getKey(), new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty())
                    try {
                        for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs()) {
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_109_B)) {
                                subRule109B.setKey(subRuleOutput.getKey());
                                subRule109B.setMessage(subRuleOutput.getMessage());
                                subRule109B.setRuleDescription(subRuleOutput.getRuleDescription());
                                subRule109B.getRuleReportOutputs().addAll(subRuleOutput.getRuleReportOutputs());
                            } else if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_109_C)) {
                                subRule109C.setKey(subRuleOutput.getKey());
                                subRule109C.setMessage(subRuleOutput.getMessage());
                                subRule109C.setRuleDescription(subRuleOutput.getRuleDescription());
                            }
                        }
                        if (subRule109B != null) {
                            valuesMap.put(SUB_RULE_109_B + "DataSource",
                                    new JRBeanCollectionDataSource(subRule109B.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule109B, drb2, valuesMap));
                        }
                        if (subRule109C != null) {
                            valuesMap.put(SUB_RULE_109_C + "DataSource",
                                    new JRBeanCollectionDataSource(subRule109B.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule109C, drb2, valuesMap));
                        }

                    } catch (Exception e) {
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