package org.egov.edcr.service;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.edcr.entity.DcrDocument;
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
import org.springframework.transaction.annotation.Transactional;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJCalculation;
import ar.com.fdvs.dj.domain.DJCrosstab;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.CrosstabBuilder;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
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


    @Autowired
    private DcrDocumentService dcrDocumentService;

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

        saveOutputReport(dcrApplication, reportStream, planDetail);
        return reportOutput;

    }

    @Transactional
    public void saveOutputReport(EdcrApplication edcrApplication, InputStream reportOutput, PlanDetail planDetail) {

        List<DcrDocument> dcrDocuments = dcrDocumentService.fingByAppNo(edcrApplication.getId());
        final String fileName = edcrApplication.getApplicationNumber() + "-v" + dcrDocuments.size() + ".pdf";

        final FileStoreMapper fileStoreMapper = fileStoreService.store(reportOutput, fileName, "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        buildDocuments(edcrApplication, null, fileStoreMapper, planDetail);

        dcrDocumentService.saveAll(edcrApplication.getDcrDocuments());
    }


    public void buildDocuments(EdcrApplication edcrApplication, FileStoreMapper dxfFile, FileStoreMapper reportOutput, PlanDetail planDetail) {

        if (dxfFile != null) {
            DcrDocument dcrDocument = new DcrDocument();

            dcrDocument.setDxfFileId(dxfFile);
            dcrDocument.setApplication(edcrApplication);

            List<DcrDocument> dcrDocuments = new ArrayList<>();
            dcrDocuments.add(dcrDocument);
            edcrApplication.setSavedDcrDocument(dcrDocument);
            edcrApplication.setDcrDocuments(dcrDocuments);
        }

        if (reportOutput != null) {
            DcrDocument dcrDocument = edcrApplication.getDcrDocuments().get(0);

            if (planDetail.getEdcrPassed()) {
                dcrDocument.setStatus("Accepted");
            } else {
                dcrDocument.setStatus("Not Accepted");
            }
            dcrDocument.setCreatedDate(new Date());
            dcrDocument.setReportOutputId(reportOutput);
            List<DcrDocument> dcrDocuments = new ArrayList<>();
            dcrDocuments.add(dcrDocument);
            edcrApplication.setDcrDocuments(dcrDocuments);
        }
    }

    private JasperPrint prepareReportData(PlanDetail planDetail2, EdcrApplication dcrApplication) throws JRException {
        FastReportBuilder drb = new FastReportBuilder();
        SimpleDateFormat FORMATDDMMYYYY = new SimpleDateFormat("dd/MM/yyyy");

        StringBuilder reportBuilder = new StringBuilder();

        final Style titleStyle = new Style("titleStyle");
        titleStyle.setFont(new Font(50, Font._FONT_TIMES_NEW_ROMAN, true));
        titleStyle.setHorizontalAlign(HorizontalAlign.CENTER);

        titleStyle.setFont(new Font(2, Font._FONT_TIMES_NEW_ROMAN, false));
        String applicationNumber = StringUtils.isNotBlank(dcrApplication.getApplicationNumber())
                ? dcrApplication.getApplicationNumber() : "NA";
        String applicationDate = FORMATDDMMYYYY.format(dcrApplication.getApplicationDate());

        boolean reportStatus = false;
        StringBuilder errors = new StringBuilder();
        if (planDetail.getErrors() != null && planDetail.getErrors().size() > 0) {
            int i = 1;
            for (Map.Entry<String, String> entry : planDetail.getErrors().entrySet()) {
                errors.append(String.valueOf(i)).append(". ");
                errors.append(entry.getValue());
                errors.append("\\n");
                i++;
            }
        } else {
            reportStatus = true;
        }

        drb.setPageSizeAndOrientation(new Page(842, 595, true));
        final JRDataSource ds = new JRBeanCollectionDataSource(Collections.singletonList(planDetail2));
        final JRDataSource ds1 = new JRBeanCollectionDataSource(planDetail.getReportOutput().getRuleOutPuts());

        final Map valuesMap = new HashMap();
        valuesMap.put("applicationNumber", applicationNumber);
        valuesMap.put("applicationDate", applicationDate);
        valuesMap.put("errors", planDetail.getErrors());
        valuesMap.put("errorString", errors.toString());
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
                        reportStatus = bean.generateRuleReport(planDetail, drb, valuesMap, reportStatus);
                } else
                    LOG.error("Skipping rule " + ruleName + "Since rule cannot be injected");

            }
        }
        reportBuilder.append("Report Status : " + (reportStatus ? "Accepted" : "NotAccepted")).append("\\n").append("\\n");
        reportBuilder.append("Rules Verified : ").append("\\n");
        valuesMap.put("reportStatus", (reportStatus ? "Accepted" : "NotAccepted"));
        drb.setTemplateFile("/reports/templates/edcr_report.jrxml");
        drb.setMargins(5, 0, 33, 20);

        final DynamicReport dr = drb.build();
        planDetail2.setEdcrPassed(reportStatus);
        return DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds, valuesMap);

    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}