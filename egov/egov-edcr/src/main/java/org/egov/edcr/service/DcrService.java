package org.egov.edcr.service;

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
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.edcr.autonumber.DcrApplicationNumberGenerator;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.EdcrApplicationDetail;
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

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.egov.infra.security.utils.SecureCodeUtils.generatePDF417Code;

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
    private DcrApplicationNumberGenerator dcrApplicationNumberGenerator;


    @Autowired
    private EdcrApplicationDetailService edcrApplicationDetailService;

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

        List<EdcrApplicationDetail> edcrApplicationDetails = edcrApplicationDetailService.fingByAppNo(edcrApplication.getId());
        final String fileName = edcrApplication.getApplicationNumber() + "-v" + edcrApplicationDetails.size() + ".pdf";

        final FileStoreMapper fileStoreMapper = fileStoreService.store(reportOutput, fileName, "application/pdf",
                DcrConstants.FILESTORE_MODULECODE);

        buildDocuments(edcrApplication, null, fileStoreMapper, planDetail);

        edcrApplicationDetailService.saveAll(edcrApplication.getEdcrApplicationDetails());
    }


    public void buildDocuments(EdcrApplication edcrApplication, FileStoreMapper dxfFile, FileStoreMapper reportOutput, PlanDetail planDetail) {

        if (dxfFile != null) {
            EdcrApplicationDetail edcrApplicationDetail = new EdcrApplicationDetail();

            edcrApplicationDetail.setDxfFileId(dxfFile);
            edcrApplicationDetail.setApplication(edcrApplication);

            List<EdcrApplicationDetail> edcrApplicationDetails = new ArrayList<>();
            edcrApplicationDetails.add(edcrApplicationDetail);
            edcrApplication.setSavedEdcrApplicationDetail(edcrApplicationDetail);
            edcrApplication.setEdcrApplicationDetails(edcrApplicationDetails);
        }

        if (reportOutput != null) {
            EdcrApplicationDetail edcrApplicationDetail = edcrApplication.getEdcrApplicationDetails().get(0);

            if (planDetail.getEdcrPassed()) {
                edcrApplicationDetail.setStatus("Accepted");
            } else {
                edcrApplicationDetail.setStatus("Not Accepted");
            }
            edcrApplicationDetail.setCreatedDate(new Date());
            edcrApplicationDetail.setReportOutputId(reportOutput);
            List<EdcrApplicationDetail> edcrApplicationDetails = new ArrayList<>();
            edcrApplicationDetails.add(edcrApplicationDetail);
            edcrApplication.setEdcrApplicationDetails(edcrApplicationDetails);
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
        if (reportStatus) {
            valuesMap.put("qrCode", generatePDF417Code(buildQRCodeDetails(dcrApplication, reportStatus)));
        }
        valuesMap.put("reportStatus", (reportStatus ? "Accepted" : "NotAccepted"));
        drb.setTemplateFile("/reports/templates/edcr_report.jrxml");
        drb.setMargins(5, 0, 33, 20);
        if (planDetail.getEdcrPassed()) {
            String dcrApplicationNumber = dcrApplicationNumberGenerator.generateEdcrApplicationNumber(dcrApplication);
            EdcrApplicationDetail edcrApplicationDetail = dcrApplication.getEdcrApplicationDetails().get(0);
            edcrApplicationDetail.setDcrNumber(dcrApplicationNumber);
        }

        final DynamicReport dr = drb.build();
        planDetail2.setEdcrPassed(reportStatus);
        return DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds, valuesMap);

    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

    private String buildQRCodeDetails(final EdcrApplication dcrApplication, boolean reportStatus) {
        StringBuilder qrCodeValue = new StringBuilder();
        qrCodeValue = !org.apache.commons.lang.StringUtils.isEmpty(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber()) ? qrCodeValue.append("DCR Number : ").append(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber()).append("\n") : qrCodeValue.append("DCR Number : ").append("N/A").append("\n");
        qrCodeValue = !org.apache.commons.lang.StringUtils.isEmpty(dcrApplication.getApplicationNumber()) ? qrCodeValue.append("Applicstion Number : ").append(dcrApplication.getApplicationNumber()).append("\n") : qrCodeValue.append("Application Number : ").append("N/A").append("\n");
        qrCodeValue = dcrApplication.getApplicationDate() != null ? qrCodeValue.append("Application Date : ").append(dcrApplication.getApplicationDate()).append("\n") : qrCodeValue.append("Application Date : ").append("N/A").append("\n");
        qrCodeValue = qrCodeValue.append("Report Status :").append(reportStatus ? "Accepted" : "NotAccepted").append("\n");
        return qrCodeValue.toString();
    }
}
