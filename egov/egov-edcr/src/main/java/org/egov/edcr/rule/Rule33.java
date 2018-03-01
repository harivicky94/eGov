package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
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
public class Rule33 extends GeneralRule {

    private static final String SUB_RULE_33_1 = "33(1)";
    private static final String SUB_RULE_33_1_DESCRIPTION = "Access to building";

    private static final BigDecimal threeHundred = BigDecimal.valueOf(300);
    private static final BigDecimal sixHundred = BigDecimal.valueOf(600);
    private static final BigDecimal oneThousand = BigDecimal.valueOf(1000);
    private static final BigDecimal oneThousandFiveHundred = BigDecimal.valueOf(1500);
    private static final BigDecimal fourThousand = BigDecimal.valueOf(4000);
    private static final BigDecimal sixThousand = BigDecimal.valueOf(6000);
    private static final BigDecimal eightThousand = BigDecimal.valueOf(8000);
    private static final BigDecimal twelveThousand = BigDecimal.valueOf(12000);
    private static final BigDecimal eighteenThousand = BigDecimal.valueOf(18000);
    private static final BigDecimal twentyFourThousand = BigDecimal.valueOf(24000);

    private static final BigDecimal onePointTwoZero = BigDecimal.valueOf(1.20);
    private static final BigDecimal twoPointZero = BigDecimal.valueOf(2.0);
    private static final BigDecimal threePointZero = BigDecimal.valueOf(3.0);
    private static final BigDecimal threePointSix = BigDecimal.valueOf(3.6);
    private static final BigDecimal fivePointZero = BigDecimal.valueOf(5.0);
    private static final BigDecimal sixPointZero = BigDecimal.valueOf(6.0);
    private static final BigDecimal sevenPointZero = BigDecimal.valueOf(7.0);
    private static final BigDecimal tenPointZero = BigDecimal.valueOf(10.0);

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 33");

        if (planDetail != null) {
            if (planDetail.getPlanInformation() == null || planDetail.getPlanInformation().getAccessWidth() == null) {
                errors.put(DxfFileConstants.ACCESS_WIDTH, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DxfFileConstants.ACCESS_WIDTH }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getBuilding() == null || planDetail.getBuilding().getTotalFloorArea() == null) {
                errors.put(DcrConstants.TOTAL_FLOOR_AREA, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.TOTAL_FLOOR_AREA }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }

            if (planDetail.getPlanInformation() == null || planDetail.getPlanInformation().getOccupancy() == null) {
                errors.put(DcrConstants.OCCUPANCY, edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                        new String[] { DcrConstants.OCCUPANCY }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
        }

        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        rule33_1(planDetail);
        return planDetail;

    }

    private void rule33_1(PlanDetail planDetail) {

        BigDecimal floorArea = planDetail.getBuilding().getTotalFloorArea();
        BigDecimal accessWidth = planDetail.getPlanInformation().getAccessWidth();

        if (planDetail.getPlanInformation().getOccupancy().toUpperCase().equals(DcrConstants.RESIDENTIAL)) {

            if (floorArea.compareTo(BigDecimal.ZERO) > 0)
                // condition 1 occupancy is residential floor area up to 300 m2, minimum access width = 1.20 m
                if (floorArea.compareTo(threeHundred) <= 0) {
                    if (accessWidth.compareTo(onePointTwoZero) >= 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                onePointTwoZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                onePointTwoZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));

                }
                // condition 2 Occupancy is residential, floor area 300 to 600 m2 minmum access width = 2.0 m
                else if (floorArea.compareTo(threeHundred) > 0 && floorArea.compareTo(sixHundred) <= 0) {
                    if (accessWidth.compareTo(twoPointZero) >= 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                twoPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                twoPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 3 Occupancy is residential, floor area 600 to 1000 m2 minimum access width = 3.0 m
                else if (floorArea.compareTo(sixHundred) > 0 && floorArea.compareTo(oneThousand) <= 0) {
                    if (accessWidth.compareTo(threePointZero) >= 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 4 Occupancy is residential, floor area 1000 m2 to 4000 m2, minmum access width 3.6 m
                else if (floorArea.compareTo(oneThousand) > 0 && floorArea.compareTo(fourThousand) <= 0) {
                    if (accessWidth.compareTo(threePointSix) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointSix.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointSix.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 5 Occupancy is residential, floor area 4000 m2 to 8000 m2, minmum access width 5m
                else if (floorArea.compareTo(fourThousand) > 0 && floorArea.compareTo(eightThousand) <= 0) {
                    if (accessWidth.compareTo(fivePointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                fivePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                fivePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 6 Occupancy is residential, floor area 8000 m2 to 18000 m2, minmum access width 6 m
                else if (floorArea.compareTo(eightThousand) > 0 && floorArea.compareTo(eighteenThousand) <= 0) {
                    if (accessWidth.compareTo(sixPointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sixPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sixPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 7 Occupancy is residential, floor area 18000m2 to 24000m2, minimum access width of 7 m
                else if (floorArea.compareTo(eighteenThousand) > 0 && floorArea.compareTo(twentyFourThousand) <= 0) {
                    if (accessWidth.compareTo(sevenPointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sevenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sevenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 8 Occupancy is residential, floor area above 24000 m2, minimum access width of 10 m.
                else if (floorArea.compareTo(twentyFourThousand) > 0)
                    if (accessWidth.compareTo(tenPointZero) <= 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                tenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                tenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));

        } else if (!planDetail.getPlanInformation().getOccupancy().toUpperCase().equals(DcrConstants.RESIDENTIAL))
            if (floorArea.compareTo(BigDecimal.ZERO) > 0)
                // condition 9 Occupancy other than residential, floor area up to 300 m2, minimum access width =1.20m
                if (floorArea.compareTo(threeHundred) <= 0) {
                    if (accessWidth.compareTo(onePointTwoZero) >= 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                onePointTwoZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                onePointTwoZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 10 Occupancy other than residential, floor area 300 m2 to 1500 m2, minimum access width 3.60 m
                else if (floorArea.compareTo(threeHundred) > 0 && floorArea.compareTo(oneThousandFiveHundred) <= 0) {
                    if (accessWidth.compareTo(threePointSix) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointSix.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                threePointSix.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 11 Occupancy other than residential, floor area1500 m2 to 6000 m2, minimum access width = 5.0 m
                else if (floorArea.compareTo(oneThousandFiveHundred) > 0 && floorArea.compareTo(sixThousand) <= 0) {
                    if (accessWidth.compareTo(fivePointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                fivePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                fivePointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 12 Occupancy other than residential, floor area 6000 m2 to 12000, minimum access width 6.0 m
                else if (floorArea.compareTo(sixThousand) > 0 && floorArea.compareTo(twelveThousand) <= 0) {

                    if (accessWidth.compareTo(sixPointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sixPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sixPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }

                // condition 13 Occupancy other than residential, floor area 12000to 18000m2, minimum access width 7.0m
                else if (floorArea.compareTo(twelveThousand) > 0 && floorArea.compareTo(eighteenThousand) <= 0) {
                    if (accessWidth.compareTo(sevenPointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sevenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                sevenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
                }
                // condition 14 Occupancy other than residential, floor area above 18000 m2, minimum access width 10m
                else if (floorArea.compareTo(eighteenThousand) > 0)
                    if (accessWidth.compareTo(tenPointZero) > 0)
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                tenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Accepted, null));
                    else
                        planDetail.reportOutput.add(buildRuleOutputWithSubRule(DcrConstants.RULE33, SUB_RULE_33_1,
                                SUB_RULE_33_1_DESCRIPTION, DxfFileConstants.ACCESS_WIDTH,
                                tenPointZero.toString() + DcrConstants.IN_METER,
                                accessWidth.toString() + DcrConstants.IN_METER, Result.Not_Accepted, null));
    }

    @Override
    public void generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap) {
        List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
        for (RuleOutput ruleOutput : rules)
            if (ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE33)) {
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