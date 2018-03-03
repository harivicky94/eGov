package org.egov.edcr.rule;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.entities.Subreport;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.*;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rule25 extends GeneralRule {

    private static final String SUB_RULE_25_1 = "25(1)";
    private static final String SUB_RULE_25_1_DESCRIPTION = "Distance from center line  of road";
    private static BigDecimal five = BigDecimal.valueOf(5);
    private static BigDecimal three = BigDecimal.valueOf(3);
    private static BigDecimal seven = BigDecimal.valueOf(7);
    private static BigDecimal two = BigDecimal.valueOf(2);
    private static BigDecimal onePointFive = BigDecimal.valueOf(1.5);

    private static BigDecimal notifiedDistClRoadAccept = BigDecimal.valueOf(0);
    private static BigDecimal notifiedDistClRoadReject = BigDecimal.valueOf(0);
    private static BigDecimal nonNotifieddistClRoadAccept = BigDecimal.valueOf(0);
    private static BigDecimal nonNotifieddistClRoadReject = BigDecimal.valueOf(0);
    private static BigDecimal culdDistClRoadAccept = BigDecimal.valueOf(0);
    private static BigDecimal culdDistClRoadReject = BigDecimal.valueOf(0);
    private static BigDecimal laneDistClRoadAccept = BigDecimal.valueOf(0);
    private static BigDecimal laneDistClRoadReject = BigDecimal.valueOf(0);
    private static BigDecimal laneDistancesReject = BigDecimal.valueOf(0);
    private static BigDecimal laneDistancesAccept = BigDecimal.valueOf(0);
    private static BigDecimal culdDeSacAccept = BigDecimal.valueOf(0);
    private static BigDecimal culdDeSacReject = BigDecimal.valueOf(0);

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 25");
/*
        if (planDetail.getLaneRoads() == null || planDetail.getLaneRoads().size() <= 0) {
            errors.put(DcrConstants.LANE_1, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[]{DcrConstants.LANE_1}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }

        if (planDetail.getCuldeSacRoads() == null || planDetail.getCuldeSacRoads().size() <= 0) {
            errors.put(DcrConstants.CULD_DE_SAC, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                    new String[]{DcrConstants.CULD_DE_SAC}, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }*/

        // If either notified or non notified road width not defined, then show error.
        if ((planDetail.getNotifiedRoads() == null || planDetail.getNonNotifiedRoads() == null) &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                        planDetail.getNonNotifiedRoads().size() > 0)) {
            errors.put(DcrConstants.ROAD,
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

        if (planDetail.getLaneRoads() != null &&
                planDetail.getLaneRoads().size() > 0)
            for (Lane laneRoad : planDetail.getLaneRoads())
                if (laneRoad.getShortestDistanceToRoad() == null ||
                        laneRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.put(DcrConstants.LANE_SHORTESTDISTINCTTOROAD,
                            prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                    planDetail.addErrors(errors);
                }

        if (planDetail.getCuldeSacRoads() != null &&
                planDetail.getCuldeSacRoads().size() > 0)
            for (CulDeSacRoad culdSac : planDetail.getCuldeSacRoads())
                if (culdSac.getShortestDistanceToRoad() == null ||
                        culdSac.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) <= 0) {
                    errors.put(DcrConstants.CULD_SAC_SHORTESTDISTINCTTOROAD,
                            prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINCTTOROAD));
                    planDetail.addErrors(errors);
                }

            if (planDetail.getBuilding() == null || planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(BigDecimal.ZERO) < 0) {
                errors.put(DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding() == null || planDetail.getBuilding().getBuildingHeight() == null) {
                errors.put(DcrConstants.BUILDING_HEIGHT,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.BUILDING_HEIGHT));
                planDetail.addErrors(errors);
            }


        return super.validate(planDetail);
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule25_1(planDetail);
        return super.process(planDetail);
    }

    private void rule25_1(PlanDetail planDetail) {
        boolean notifiedShortDistainceLessThan5 = false;
        boolean nonNotifiedShortDistainceLessThan5 = false;
        boolean laneShortDistainceLessThan5 = false;
        boolean culdShortDistainceLessThan5 = false;
        boolean culdDeSacDistaince2or3 = false;
        boolean laneDistainceOnePointFive = false;

        //validating minimum distance in non-notified roads minimum 5m
         if (planDetail.getNonNotifiedRoads() != null) {
            for (NonNotifiedRoad nonNotifiedRoad : planDetail.getNonNotifiedRoads()) {
                if (nonNotifiedRoad.getShortestDistanceToRoad() != null
                        && nonNotifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                            && nonNotifiedRoad.getShortestDistanceToRoad().compareTo(five) >= 0) {
                                nonNotifiedShortDistainceLessThan5 = true;
                                        nonNotifieddistClRoadAccept = nonNotifiedRoad.getShortestDistanceToRoad();
                } else
                    nonNotifieddistClRoadReject = nonNotifiedRoad.getShortestDistanceToRoad();
            }
        }

        //validating minimum distance in notified roads minimum 5m
        if (planDetail.getNotifiedRoads() != null) {
            for (NotifiedRoad notifiedRoad : planDetail.getNotifiedRoads()) {
                if (notifiedRoad.getShortestDistanceToRoad() != null
                            && notifiedRoad.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                                && notifiedRoad.getShortestDistanceToRoad().compareTo(five) >= 0) {
                                    notifiedShortDistainceLessThan5 = true;
                                        notifiedDistClRoadAccept = notifiedRoad.getShortestDistanceToRoad();
                } else
                    notifiedDistClRoadReject = notifiedRoad.getShortestDistanceToRoad();
            }
        }

        //validating minimum distance in culd_sac_road minimum 5m
        if (planDetail.getCuldeSacRoads() != null) {
            for (CulDeSacRoad culdeSac : planDetail.getCuldeSacRoads()) {
                if (culdeSac.getShortestDistanceToRoad() != null
                            && culdeSac.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                                 &&  culdeSac.getShortestDistanceToRoad().compareTo(five) >= 0) {
                                        culdShortDistainceLessThan5 = true;
                                            culdDistClRoadAccept = culdeSac.getShortestDistanceToRoad();
                } else
                    culdDistClRoadReject = culdeSac.getShortestDistanceToRoad();
            }
        }

        //validating minimum distance in lane roads minimum 5m
        if (planDetail.getLaneRoads() != null) {
            for (Lane lane : planDetail.getLaneRoads()) {
                if (lane.getShortestDistanceToRoad() != null
                        && lane.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                            && lane.getShortestDistanceToRoad().compareTo(five) >= 0) {
                                laneShortDistainceLessThan5 = true;
                                    laneDistClRoadAccept = lane.getShortestDistanceToRoad();
                } else
                    laneDistClRoadReject = lane.getShortestDistanceToRoad();
            }
        }


        //minimum distance in Culd-De-Sac roads and distance shall be min 2.0 else 3.0
        if (planDetail.getCuldeSacRoads() != null) {
            for (CulDeSacRoad culdDeSacs : planDetail.getCuldeSacRoads()) {
                if (culdDeSacs.getShortestDistanceToRoad() != null
                            && culdDeSacs.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                                && culdDeSacs.getShortestDistanceToRoad().compareTo(two) >= 0 ) {
                                        culdDeSacDistaince2or3 = true;
                                            culdDeSacAccept = culdDeSacs.getShortestDistanceToRoad();
                } else
                    culdDeSacReject = culdDeSacs.getShortestDistanceToRoad();
            }
        }


        //minimum distance in Lane roads should be min 1.5
        if (planDetail.getLaneRoads() != null) {
            for (Lane lane : planDetail.getLaneRoads()) {
                if (lane.getShortestDistanceToRoad() != null
                            && lane.getShortestDistanceToRoad().compareTo(BigDecimal.ZERO) > 0
                                 && lane.getShortestDistanceToRoad().compareTo(onePointFive) >= 0) {
                                        laneDistainceOnePointFive = true;
                                            laneDistancesAccept = lane.getShortestDistanceToRoad();
                } else
                    laneDistancesReject = lane.getShortestDistanceToRoad();
            }
        }

    // If both roads are not defined.
        if (planDetail.getNotifiedRoads() != null &&
                !(planDetail.getNotifiedRoads().size() > 0 ||
                        planDetail.getNonNotifiedRoads().size() > 0 ||
                        planDetail.getCuldeSacRoads().size() > 0 ||
                        planDetail.getLaneRoads().size() > 0))
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.ROAD,
                            null,
                            null,
                            Result.Not_Accepted, DcrConstants.ROAD + DcrConstants.OBJECTNOTDEFINED_DESC));

        // 1)All the distances measured shall be minimum 5.0 m (DIST_CL_ROAD)
        else if (nonNotifiedShortDistainceLessThan5
                && notifiedShortDistainceLessThan5 && laneShortDistainceLessThan5
                    && culdShortDistainceLessThan5 && planDetail.getBuilding() != null) {
                        planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                                   DcrConstants.DIST_CL_ROAD,
                                     "Should be minimum " + five.toString() + DcrConstants.IN_METER,
                                     notifiedDistClRoadAccept.toString() + DcrConstants.IN_METER,
                                            Result.Accepted, null));
        } else
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.DIST_CL_ROAD,
                                "Should be minimum " + five.toString() + DcrConstants.IN_METER,
                                nonNotifieddistClRoadReject.toString() + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));



        // 2) All the distances measured shall be minimum 3.0 m
        if (planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().compareTo(three) >= 0) {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                                DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT,
                                "Minimum  " + two + " or " + three.toString() + DcrConstants.IN_METER,
                                planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().toString() + DcrConstants.IN_METER,
                                    Result.Accepted, null));
        } else
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.SHORTESTDISTINACETOBUILDINGFOOTPRINT,
                                "Minimum  " + two + " or " + three.toString() + DcrConstants.IN_METER,
                                planDetail.getBuilding().getDistanceFromBuildingFootPrintToRoadEnd().toString() + DcrConstants.IN_METER,
                                     Result.Not_Accepted, null));


        // 3)If building height is up to 7 m, (CULD_1) the shortest distance from building footprint layer to the nearest point on the cul_1 layer  shall be minimum 2.0 m, else 3 m.
        if (culdDeSacDistaince2or3
                && planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) > 0
                    && planDetail.getBuilding().getBuildingHeight().compareTo(seven) <= 0) {
                     planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                                 DcrConstants.BUILDING_HEIGHT,
                                    "Height of Building Should be less than  " + seven.toString() + DcrConstants.IN_METER,
                                    planDetail.getBuilding().getBuildingHeight().toString() + DcrConstants.IN_METER,
                                         Result.Accepted, null));
        } else
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.BUILDING_HEIGHT,
                                "Height of Building Should be less than  " + seven + DcrConstants.IN_METER + " culd_de_sac should be minimum " + two + " or " + three.toString() + DcrConstants.IN_METER,
                                "Height of Building is " + planDetail.getBuilding().getBuildingHeight() + DcrConstants.IN_METER + " culd_de_sac is " + culdDeSacReject.toString() + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));

        // 4)the shortest distance from building footprint layer to the nearest point on the LANE_1 layer shall be 1.5 m (culdDeSac)
        if (planDetail.getBuilding() != null && laneDistainceOnePointFive) {
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.LANE_1,
                                "Minimum  " + onePointFive.toString() + DcrConstants.IN_METER,
                                laneDistancesAccept.toString() + DcrConstants.IN_METER,
                                    Result.Accepted, null));
        } else
            planDetail.reportOutput
                    .add(buildRuleOutputWithSubRule(DcrConstants.RULE25, SUB_RULE_25_1, SUB_RULE_25_1_DESCRIPTION,
                            DcrConstants.LANE_1,
                                "Minimum  " + onePointFive.toString() + DcrConstants.IN_METER,
                                laneDistancesReject.toString() + DcrConstants.IN_METER,
                                    Result.Not_Accepted, null));
    }

    private String prepareMessage(String code, String args) {
        return edcrMessageSource.getMessage(code,
                new String[] { args }, LocaleContextHolder.getLocale());
        // return code+" "+args;
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
