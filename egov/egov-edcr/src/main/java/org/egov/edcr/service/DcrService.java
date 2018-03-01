package org.egov.edcr.service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.rule.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

/*General rule class contains validations which are required for all types of building plans*/
@Service
public class DcrService {
    private Logger LOG = Logger.getLogger(DcrService.class);

    private PlanDetail planDetail;

    @Autowired
    private GeneralRule generalRule;

    @Autowired
    private DXFExtractService extractService;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private PlanRuleService planRuleService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private FileStoreService fileStoreService;

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    public PlanDetail process(File dxf1File, EdcrApplication dcrApplication) {

        LOG.info("hello ");
        // TODO:
        // BASIC VALIDATION

        // File dxfFile = new File("/home/mani/Desktop/BPA/kozhi/SAMPLE 4.dxf");

        generalRule.validate(planDetail);

        // dxfFile.getf
        // EXTRACT DATA FROM DXFFILE TO planDetail;

        // planDetail= generalRule.validate(planDetail);
        // EXTRACT DATA FROM DXFFILE TO planDetail;
        planDetail = extractService.extract(dxf1File, dcrApplication);
        if (planDetail.getBuilding().getBuildingHeight().intValue() > 10)
            planDetail.addError("Cannot Process",
                    " This report is not complete . Not all rules are not processed. Only Buildings up to 10 Mtr height will be considered for  processing");
        // return planDetail;

        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);

        for (PlanRule pl : planRules) {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                LOG.info(s);
                Object ruleBean = getRuleBean(ruleName);
                if (ruleBean != null) {
                    GeneralRule bean = (GeneralRule) ruleBean;
                    if (bean != null)
                        planDetail = bean.validate(planDetail);
                } else
                    LOG.error("Skipping rule " + ruleName + "Since rule cannot be injected");

            }
        }

        for (PlanRule pl : planRules) {

            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");

            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                Object ruleBean = getRuleBean(ruleName);
                if (ruleBean != null) {
                    GeneralRule bean = (GeneralRule) ruleBean;
                    if (bean != null)
                        planDetail = bean.process(planDetail);
                } else
                    LOG.error("Skipping rule " + ruleName + "Since rule cannot be injected");

            }
        }
        generateDCRReport(planDetail, dcrApplication);

        Util.print(planDetail);
        Util.print(planDetail.getErrors());
        Util.print(planDetail.getGeneralInformation());
        Util.print(planDetail.getReportOutput());
        return planDetail;
    }

    private Object getRuleBean(String ruleName) {
        Object bean = null;
        try {
            bean = applicationContext.getBean(ruleName);
        } catch (BeansException e) {
            LOG.error("No Bean Defined for the Rule" + ruleName);
        }
        return bean;
    }

    public ReportOutput generateDCRReport(PlanDetail planDetail, EdcrApplication dcrApplication) {

        final ReportOutput reportOutput = null;
        JasperPrint jasper;
        InputStream reportStream = null;
        try {
            jasper = prepareReportData(planDetail, dcrApplication);
            reportStream = reportService.exportPdf(jasper);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        FileStoreMapper fileStoreId = fileStoreService.store(reportStream, "EDCR", "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        if (fileStoreId != null)
            dcrApplication.getSavedDcrDocument().setReportOutputId(fileStoreId);

        return reportOutput;

    }

    private JasperPrint prepareReportData(PlanDetail planDetail2, EdcrApplication dcrApplication) throws JRException {
        FastReportBuilder drb = new FastReportBuilder();
        SimpleDateFormat FORMATDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");

        StringBuilder reportBuilder = new StringBuilder();

        final Style titleStyle = new Style("titleStyle");
        titleStyle.setFont(new Font(50, Font._FONT_TIMES_NEW_ROMAN, true));
        titleStyle.setHorizontalAlign(HorizontalAlign.CENTER);

        final Style subTitleStyle = new Style("subtitleStyle");
        titleStyle.setFont(new Font(2, Font._FONT_TIMES_NEW_ROMAN, false));
        String applicationNumber = StringUtils.isNotBlank(dcrApplication.getApplicationNumber())
                                   ? dcrApplication.getApplicationNumber() : "NA";
        BigDecimal plotArea = planDetail2.getPlanInformation().getPlotArea() != null
                              ? planDetail2.getPlanInformation().getPlotArea() : BigDecimal.ZERO;
        String applicationDate = FORMATDDMMYYYY.format(dcrApplication.getApplicationDate());
        String occupancy = dcrApplication.getPlanInformation().getOccupancy() != null
                           ? dcrApplication.getPlanInformation().getOccupancy() : "NA";
        String architectName = StringUtils.isNotBlank(dcrApplication.getPlanInformation().getArchitectInformation())
                               ? dcrApplication.getPlanInformation().getArchitectInformation() : "NA";
        reportBuilder.append("\\n").append("Application Details").append("\\n").append("\\n")
                     .append("Application Number :   ").append(applicationNumber)
                     .append("                                                ")
                     .append("Plot Area          :   ").append(plotArea).append("\\n").append("\\n")
                     .append("Application Date   :   ").append(applicationDate)
                     .append("                                                ")
                     .append("Occupancy          :   ").append(occupancy).append("\\n").append("\\n")
                     .append("Architect name     :   ").append(architectName);

        if (planDetail.getErrors() != null && planDetail.getErrors().size() > 0) {
            int i = 1;
            reportBuilder.append("\\n").append("\\n").append("Errors").append("\\n");
            for (Map.Entry<String, String> entry : planDetail.getErrors().entrySet()) {
                reportBuilder.append(String.valueOf(i)).append(". ");
                reportBuilder.append(entry.getValue());
                reportBuilder.append("\\n");
                i++;
            }
        }

        drb.setTitle("Building Plan Approval" + "\\n" + "EDCR Report")
           .setSubtitle(reportBuilder.toString())
           .setPrintBackgroundOnOddRows(false)
           .setSubtitleStyle(subTitleStyle)
           .setTitleHeight(40).setSubtitleHeight(20).setUseFullPageWidth(true);

        List<Map<String, String>> errors = new ArrayList<Map<String, String>>();
        errors.add(planDetail.getErrors());

        drb.setPageSizeAndOrientation(new Page(842, 595, true));

        final JRDataSource ds = new JRBeanCollectionDataSource(planDetail.getReportOutput().getRuleOutPuts());
        final JRDataSource ds1 = new JRBeanCollectionDataSource(planDetail.getReportOutput().getRuleOutPuts());

        final Map valuesMap = new HashMap();
        valuesMap.put("ruleOutput", ds1);
        valuesMap.put("dcrApplication", dcrApplication);

        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);

        for (PlanRule pl : planRules) {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                LOG.info(s);
                Object ruleBean = getRuleBean(ruleName);
                if (ruleBean != null) {
                    GeneralRule bean = (GeneralRule) ruleBean;
                    if (bean != null)
                        bean.generateRuleReport(planDetail, drb, valuesMap);
                } else
                    LOG.error("Skipping rule " + ruleName + "Since rule cannot be injected");

            }
        }

        drb.setMargins(20, 20, 20, 20);

        final DynamicReport dr = drb.build();

        return DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds, valuesMap);

    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}