package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.FloorUnit;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.measurement.Measurement;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
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
public class Rule34 extends GeneralRule {
    private static final String SUB_RULE_34_1 = "34(1)";
    private static final String SUB_RULE_34_1_DESCRIPTION = "Parking Slots Area";

    private static final String SUB_RULE_34_2 = "34(2)";
    private static final String SUB_RULE_34_2_DESCRIPTION = "Total number of Parking ";

    private static final BigDecimal PARKING_MIN_AREA = BigDecimal.valueOf(14.85);

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        if (planDetail != null) {

            // If either notified or non notified road width not defined, then show error.
            if ((planDetail.getFloorUnits() == null || planDetail.getParkingSlots() == null) &&
                    !(planDetail.getFloorUnits().size() > 0 &&
                            planDetail.getParkingSlots().size() > 0)) {
                errors.put(DcrConstants.RULE34,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.PARKINGSLOT_UNIT));
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

    public PlanDetail process(PlanDetail planDetail) {
        rule34_1(planDetail);
        rule34_2(planDetail);
        return planDetail;
    }

    private void rule34_2(PlanDetail planDetail) {
        if (planDetail.getFloorUnits() != null && planDetail.getParkingSlots() != null && planDetail.getParkingSlots().size() > 0
                &&
                planDetail.getFloorUnits().size() > 0) {
            Map<String, Integer> groupUnitByArea = new HashMap<String, Integer>();
            double totalParkingRequired = 0;
            for (FloorUnit floorUnit : planDetail.getFloorUnits()) {
                BigDecimal areaOfUnit = Util.getPolyLineArea(floorUnit.getPolyLine());
                if (areaOfUnit != null && floorUnit.getTotalUnitDeduction() != null)
                    areaOfUnit = areaOfUnit.subtract(floorUnit.getTotalUnitDeduction());
                if (areaOfUnit.compareTo(BigDecimal.valueOf(60)) < 0) {
                    groupUnitByArea.put("WITHIN60",
                            groupUnitByArea.get("WITHIN60") != null ? groupUnitByArea.get("WITHIN60") + 1 : 1);
                } else if (areaOfUnit.compareTo(BigDecimal.valueOf(60)) >= 0
                        && areaOfUnit.compareTo(BigDecimal.valueOf(150)) <= 0) {
                    groupUnitByArea.put("WITHIN150",
                            groupUnitByArea.get("WITHIN150") != null ? groupUnitByArea.get("WITHIN150") + 1 : 1);
                } else if (areaOfUnit.compareTo(BigDecimal.valueOf(150)) > 0
                        && areaOfUnit.compareTo(BigDecimal.valueOf(250)) <= 0) {
                    groupUnitByArea.put("WITHIN250",
                            groupUnitByArea.get("WITHIN250") != null ? groupUnitByArea.get("WITHIN250") + 1 : 1);
                } else if (areaOfUnit.compareTo(BigDecimal.valueOf(250)) > 0) {
                    groupUnitByArea.put("ABOVE250",
                            groupUnitByArea.get("ABOVE250") != null ? groupUnitByArea.get("ABOVE250") + 1 : 1);
                }
            }

            if (groupUnitByArea.size() > 0) {
                totalParkingRequired = (groupUnitByArea.get("WITHIN60") != null ? Math.floor(groupUnitByArea.get("WITHIN60") / 3)
                        : 0)
                        + (groupUnitByArea.get("WITHIN150") != null ? groupUnitByArea.get("WITHIN150") : 0)
                        + (groupUnitByArea.get("WITHIN250") != null ? Math.floor(groupUnitByArea.get("WITHIN250") * 1.5) : 0)
                        + (groupUnitByArea.get("ABOVE250") != null ? Math.floor(groupUnitByArea.get("ABOVE250") * 2) : 0);
            }
            if (totalParkingRequired > 0) {
                if (Math.floor(totalParkingRequired) > planDetail.getParkingSlots().size()) {
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE34, SUB_RULE_34_2, SUB_RULE_34_2_DESCRIPTION,
                                    "Total Parking",
                                    Math.floor(totalParkingRequired)
                                            + " Numbers ",
                                    planDetail.getParkingSlots().size()
                                            + " Numbers ",
                                    Result.Not_Accepted, null));
                } else {
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(DcrConstants.RULE34, SUB_RULE_34_2, SUB_RULE_34_2_DESCRIPTION,
                                    "Total Parking",
                                    Math.floor(totalParkingRequired)
                                            + " Numbers ",
                                    planDetail.getParkingSlots().size()
                                            + " Numbers " + " for Total floors " + planDetail.getFloorUnits(),
                                    Result.Accepted, null));
                }
            }
        }
    }

    private void rule34_1(PlanDetail planDetail) {

        if (planDetail.getParkingSlots() != null &&
                planDetail.getParkingSlots().size() > 0) {
            int parkingCount = 0;
            int areaOfparkingViolation = 0;
            for (Measurement parkingslot : planDetail.getParkingSlots()) {
                parkingCount++;
                if (parkingslot.getPolyLine() != null &&
                        Util.getPolyLineArea(parkingslot.getPolyLine()).compareTo(PARKING_MIN_AREA) < 0) {
                    areaOfparkingViolation++;

                }
            }
            if (areaOfparkingViolation > 0) {
                planDetail.reportOutput
                        .add(buildRuleOutputWithSubRule(DcrConstants.RULE34, SUB_RULE_34_1, SUB_RULE_34_1_DESCRIPTION,
                                DcrConstants.PARKINGSLOT,
                                PARKING_MIN_AREA + " MTR. Minimum Area of Each parking",
                                "Out of " + parkingCount + " parking " + areaOfparkingViolation
                                        + " parking violated minimum area.",
                                Result.Not_Accepted, null));
            }else
                planDetail.reportOutput
                .add(buildRuleOutputWithSubRule(DcrConstants.RULE34, SUB_RULE_34_1, SUB_RULE_34_1_DESCRIPTION,
                        DcrConstants.PARKINGSLOT,
                        PARKING_MIN_AREA + " MTR. Minimum Area of Each parking",
                        "No violation of area in " + parkingCount + " parking " ,
                        Result.Accepted, null));
        }

    }

    @Override
    public void generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE34)) {
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

                drb2.addConcatenatedReport(subRep);
                // return subRep;
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty())
                    for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs())
                        try {
                            valuesMap.put(subRuleOutput.getKey() + "DataSource",
                                    new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRuleOutput, drb2, valuesMap));
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                break;
            }
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

            drb.addColumn("Field Verified", "fieldVerified", String.class.getName(), 120, verifiedColumnStyle, columnHeaderStyle);
            drb.addColumn("Expected Result", "expectedResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
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
