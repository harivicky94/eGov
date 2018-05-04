package org.egov.edcr.rule;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.constants.DxfFileConstants;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RoadOutput;
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
public class Rule104 extends GeneralRule {
    private static final BigDecimal three = BigDecimal.valueOf(3);
    private static final BigDecimal TWO_MTR = BigDecimal.valueOf(2);
    private static final BigDecimal ONE_ANDHALF_MTR = BigDecimal.valueOf(1.5);

    private static final BigDecimal a = BigDecimal.valueOf(1.8);
    private static final String SUB_RULE_104_1_DESCRIPTION = "Open well: Minimum distance between street boundary and the well ";
    private static final String SUB_RULE_104_2_DESCRIPTION = "Minimum distance nearest point of boundary and the well ";
    private static final String SUB_RULE_104_4_DESCRIPTION = "Minimum distance from well to nearest point on leach pit, soak pit, refuse pit, earth closet or septic tanks ";

    private static final String SUB_RULE_104_1 = "104(1)";
    private static final String SUB_RULE_104_2 = "104(2)";
    private static final String SUB_RULE_104_4 = "104(4)";

    @Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();

            if (planDetail != null && !planDetail.getUtility().getWells().isEmpty() && planDetail.getUtility().getWasteDisposalUnits().isEmpty()) {
                errors.put(DcrConstants.WELL_DISTANCE_FROMROAD,
                        prepareMessage(DcrConstants.OBJECTNOTDEFINED, DcrConstants.WELL_DISTANCE_FROMROAD));
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
        rule1041(planDetail);
        return planDetail;
    }

    private void rule1041(PlanDetail planDetail) {
        String rule = DcrConstants.RULE104;
        String subRule = SUB_RULE_104_1;
        String subRuleDesc = SUB_RULE_104_1_DESCRIPTION;
        boolean valid = false;

        if (!planDetail.getUtility().getWells().isEmpty()) {
            BigDecimal minimumDistance = BigDecimal.valueOf(0);
            for (RoadOutput roadOutput : planDetail.getUtility().getWellDistance()) {
                valid = false;
                if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NOTIFIEDROAD ||
                        Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_NONNOTIFIEDROAD) {
                    minimumDistance = three;
                } else if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_CULDESAC) {
                    minimumDistance = TWO_MTR;
                } else if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_LANE) {
                    minimumDistance = ONE_ANDHALF_MTR;
                } else if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_WELLTOBOUNDARY) {
                    subRule = SUB_RULE_104_2;
                    subRuleDesc = SUB_RULE_104_2_DESCRIPTION;
                    minimumDistance = ONE_ANDHALF_MTR;
                } else if (Integer.valueOf(roadOutput.colourCode) == DxfFileConstants.COLOUR_CODE_WELLTOLEACHPIT) {
                    subRule = SUB_RULE_104_4;
                    subRuleDesc = SUB_RULE_104_4_DESCRIPTION;
                    minimumDistance = BigDecimal.valueOf(7.5);
                }

                if (roadOutput.distance != null &&
                        roadOutput.distance.compareTo(BigDecimal.ZERO) > 0 && roadOutput.distance.compareTo(minimumDistance) >= 0)
                    valid = true;

                if (valid){
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                    DcrConstants.WELL,
                                    minimumDistance.toString()+ DcrConstants.IN_METER,
                                    roadOutput.distance +DcrConstants.IN_METER,
                                    Result.Accepted, DcrConstants.WELL)); 
                }
                else{
                    planDetail.reportOutput
                            .add(buildRuleOutputWithSubRule(rule, subRule, subRuleDesc,
                                    DcrConstants.WELL,
                                    minimumDistance.toString()+ DcrConstants.IN_METER,
                                    roadOutput.distance +DcrConstants.IN_METER,
                                    Result.Not_Accepted, DcrConstants.WELL));
                }

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
                SubRuleOutput subRule1041 = new SubRuleOutput();
                SubRuleOutput subRule1042 = new SubRuleOutput();
                SubRuleOutput subRule1044 = new SubRuleOutput();

                valuesMap.put(ruleOutput.getKey(), new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                if (ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty())
                    try {
                        for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs()) {
                            reportStatus = reportService.getReportStatus(subRuleOutput.getRuleReportOutputs(), reportStatus);
                            if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_104_1)) {
                                subRule1041.setKey(subRuleOutput.getKey());
                                subRule1041.setMessage(subRuleOutput.getMessage());
                                subRule1041.setRuleDescription(subRuleOutput.getRuleDescription());
                                subRule1041.getRuleReportOutputs().addAll(subRuleOutput.getRuleReportOutputs());
                            } else if (subRuleOutput.getKey().equalsIgnoreCase(SUB_RULE_104_4)) {
                                subRule1044.setKey(subRuleOutput.getKey());
                                subRule1044.setMessage(subRuleOutput.getMessage());
                                subRule1044.setRuleDescription(subRuleOutput.getRuleDescription());
                                subRule1044.getRuleReportOutputs().addAll(subRuleOutput.getRuleReportOutputs());
                            } else {
                                subRule1042.setKey(subRuleOutput.getKey());
                                subRule1042.setMessage(subRuleOutput.getMessage());
                                subRule1042.setRuleDescription(subRuleOutput.getRuleDescription());

                            }
                        }
                        if (subRule1041 != null) {
                            valuesMap.put(SUB_RULE_104_1 + "DataSource",
                                    new JRBeanCollectionDataSource(subRule1041.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule1041, drb2, valuesMap));
                        }
                        if (subRule1044 != null) {
                            valuesMap.put(SUB_RULE_104_4 + "DataSource",
                                    new JRBeanCollectionDataSource(subRule1041.getRuleReportOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule1044, drb2, valuesMap));
                        }
                        if (subRule1042 != null) {
                            valuesMap.put(SUB_RULE_104_2 + "DataSource",
                                    new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()));
                            drb2.addConcatenatedReport(generateSubRuleReport(subRule1042, drb2, valuesMap));
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