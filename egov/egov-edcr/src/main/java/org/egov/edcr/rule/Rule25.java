package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.CulDeSacRoad;
import org.egov.edcr.entity.measurement.Lane;
import org.egov.edcr.entity.measurement.NonNotifiedRoad;
import org.egov.edcr.entity.measurement.NotifiedRoad;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
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
public class Rule25 extends GeneralRule {
   // private static final String SUB_RULE_25 = "Rule 25";
    private static final String SUB_RULE_25_1 = "25(1)";
    private static final String SUB_RULE_25_1_DESCRIPTION = "Minimum distance between central line of a street and building";
    private static BigDecimal five = BigDecimal.valueOf(5);
    private static BigDecimal three = BigDecimal.valueOf(3);
    private static BigDecimal seven = BigDecimal.valueOf(7);
    private static BigDecimal two = BigDecimal.valueOf(2);
    private static BigDecimal onePointFive = BigDecimal.valueOf(1.5);

    @Autowired
    private ReportService reportService;
    
    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 25");
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule25_1(planDetail);
        return planDetail;
    }

    private void rule25_1(PlanDetail planDetail) {

        // validating minimum distance in non-notified roads minimum 5m
        if (planDetail.getNonNotifiedRoads() != null) {
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads()) {

                if (nonNotifiedRoad.getDistanceFromCenterToPlot() != null)
                    if (nonNotifiedRoad.getDistanceFromCenterToPlot().compareTo(five) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.NONNOTIFIED_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                                         "Min. " + five.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.NONNOTIFIED_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.NONNOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + five.toString() + DcrConstants.IN_METER,
                                        nonNotifiedRoad.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
            }
        }

        // validating minimum distance in notified roads minimum 5m
        if (planDetail.getNotifiedRoads() != null) {
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads()) {
                if (notifiedRoad.getDistanceFromCenterToPlot() != null)
                    if (notifiedRoad.getDistanceFromCenterToPlot().compareTo(five) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.NOTIFIED_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + five.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.NOTIFIED_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.NOTIFIED_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + five.toString() + DcrConstants.IN_METER,
                                        notifiedRoad.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
            }
        }

        // validating minimum distance in culd_sac_road minimum 2 or 3 based on height of building
        if (planDetail.getCuldeSacRoads() != null && planDetail.getBuilding().getBuildingHeight() != null) {
            BigDecimal comparisionDecimal = BigDecimal.ZERO;
            for (CulDeSacRoad culdeSac : planDetail.getCuldeSacRoads()) {
                if (culdeSac.getDistanceFromCenterToPlot() != null) {
                    if (planDetail.getBuilding().getBuildingHeight().compareTo(seven) <= 0)
                        comparisionDecimal = two;
                    else
                        comparisionDecimal = three;

                    if (culdeSac.getDistanceFromCenterToPlot().compareTo(comparisionDecimal) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.CULDESAC_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + comparisionDecimal.toString() + DcrConstants.IN_METER,
                                        culdeSac.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, DcrConstants.CULDESAC_ROAD + SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.CULDESAC_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + comparisionDecimal.toString() + DcrConstants.IN_METER,
                                        culdeSac.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
                }
            }
        }

        // validating minimum distance in lane roads minimum 5m
        if (planDetail.getLaneRoads() != null) {
            for (Lane lane : planDetail.getLaneRoads()) {
                if (lane.getDistanceFromCenterToPlot() != null)
                    if (lane.getDistanceFromCenterToPlot().compareTo(onePointFive) >= 0)
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1,  DcrConstants.LANE_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.LANE_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + onePointFive.toString() + DcrConstants.IN_METER,
                                        lane.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Accepted, null));
                    else
                        planDetail.reportOutput
                                .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1,  DcrConstants.LANE_ROAD +SUB_RULE_25_1_DESCRIPTION,
                                        DcrConstants.LANE_SHORTESTDISTINCTTOROADFROMCENTER,
                                        "Min. " + onePointFive.toString() + DcrConstants.IN_METER,
                                        lane.getDistanceFromCenterToPlot().toString()
                                                + DcrConstants.IN_METER,
                                        Result.Not_Accepted, null));
            }
        }

    }

    @Override
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap, boolean reportStatus) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE25)) {
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
                SubRuleOutput subRule25 = new SubRuleOutput(); 
                valuesMap.put(ruleOutput.getKey(), new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty())
                    try {  
                        for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs()) {
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_25_1)) {
                                subRule25.setKey(subRuleOutput.getKey());
                                subRule25.setMessage(subRuleOutput.getMessage());
                                subRule25.setRuleDescription(subRuleOutput.getRuleDescription());
                                subRule25.getRuleReportOutputs().addAll(subRuleOutput.getRuleReportOutputs());
                            } 
                        }
                        if(subRule25 != null) 
                            valuesMap.put(SUB_RULE_25_1 + "DataSource",
                                    new JRBeanCollectionDataSource(subRule25.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule25, drb2, valuesMap));
                        
                       
                      
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
    }}
