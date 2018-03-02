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
public class Rule32 extends GeneralRule {

    private static final String SUB_RULE_32_1A = "32(1A)";
    private static final String SUB_RULE_32_1A_DESCRIPTION = "Maximum height of building ";
    private static final String SUB_RULE_32_3 = "32(3)";

    private static final String SUB_RULE_32_3_DESCRIPTION = "Security zone ";
    private static final BigDecimal ten = BigDecimal.valueOf(10);
    private static final BigDecimal TWELVE = BigDecimal.valueOf(12);
    private static final BigDecimal two = BigDecimal.valueOf(2);

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 32");

        if (planDetail != null && planDetail.getBuilding() != null) {
            if (planDetail.getBuilding().getBuildingHeight() == null ||
                planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.BUILDING_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.BUILDING_HEIGHT }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding() != null && (planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd() == null
                                                     || planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(BigDecimal.ZERO) < 0)) {
                errors.put(DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT },
                                LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            /*
             * if (planDetail.getBuilding().getBuildingTopMostHeight() != null &&
             * planDetail.getBuilding().getBuildingTopMostHeight().compareTo(BigDecimal.ZERO) < 0) {
             * errors.put(DcrConstants.BUILDING_TOP_MOST_HEIGHT, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED, new
             * String[] { DcrConstants.BUILDING_TOP_MOST_HEIGHT }, LocaleContextHolder.getLocale()));
             * planDetail.addErrors(errors); }
             */

        }

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule32_1a(planDetail);
        rule32_3(planDetail);
        return planDetail;
    }

    private void rule32_1a(PlanDetail planDetail) {
        boolean shortDistainceLessThan12 = false;

        if (planDetail.getNonNotifiedRoads() != null)
            for (NonNotifiedRoad nonnotifiedRoad : planDetail.getNonNotifiedRoads())
                if (nonnotifiedRoad.getShortestDistanceToRoad() != null
                    && nonnotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0 &&
                    nonnotifiedRoad.getShortestDistanceToRoad().compareTo(TWELVE) <= 0) {
                    shortDistainceLessThan12 = true;
                   // return;
                }
        if (planDetail.getNotifiedRoads() != null)
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads())
                if (notifiedRoad.getShortestDistanceToRoad() != null && notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                    &&
                    notifiedRoad.getShortestDistanceToRoad().compareTo(TWELVE) <= 0) {
                    shortDistainceLessThan12 = true;
                   // return; 
                }

        if (shortDistainceLessThan12 && planDetail.getBuilding() != null && planDetail.getBuilding().getBuildingHeight() != null
            && planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd() != null &&
            planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(BigDecimal.ZERO) > 0)
            if (planDetail.getBuilding().getBuildingHeight()
                          .compareTo(planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two)) > 0)
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                        SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT,
                        "Upto " + planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two).toString()
                        + DcrConstants.IN_METER,
                        planDetail.getBuilding().getBuildingHeight() + DcrConstants.IN_METER,
                        Result.Not_Accepted, null));
            else
                planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE32, SUB_RULE_32_1A,
                        SUB_RULE_32_1A_DESCRIPTION, DcrConstants.BUILDING_HEIGHT,
                        "Upto " + planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().multiply(two).toString()
                        + DcrConstants.IN_METER,
                        planDetail.getBuilding().getBuildingHeight() + DcrConstants.IN_METER,
                        Result.Accepted, null));
    }

    private void rule32_3(PlanDetail planDetail) {

        if (planDetail.getPlanInformation().getSecurityZone())
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

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE32)) {
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