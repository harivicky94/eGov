package org.egov.edcr.service;

import static org.egov.infra.security.utils.SecureCodeUtils.generatePDF417Code;

import java.io.File;
import java.io.InputStream;

import java.math.BigDecimal;
import java.math.RoundingMode;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.egov.edcr.autonumber.DcrApplicationNumberGenerator;
import org.egov.edcr.entity.DcrReportOutput;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.EdcrApplicationDetail;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.rule.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.admin.master.service.CityService;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.util.ReportUtil;
import org.joda.time.LocalDate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.VerticalAlign;
import ar.com.fdvs.dj.domain.entities.Subreport;
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
    private DcrApplicationNumberGenerator dcrApplicationNumberGenerator;

    @Autowired
    private EdcrApplicationDetailService edcrApplicationDetailService;

    @Value("${edcr.client.subreport}")
    private boolean clientSpecificSubReport;

    @Autowired
    private CityService cityService;

    private ReportUtil reportUtil;

    @Autowired
    private PlanDetailService planDetailService;

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    public PlanDetail process(File dxf1File, EdcrApplication dcrApplication) {

        if(LOG.isDebugEnabled()) LOG.debug("hello ");
        // TODO:
        // BASIC VALIDATION

        // File dxfFile = new File("/home/mani/Desktop/BPA/kozhi/SAMPLE 4.dxf");

        generalRule.validate(planDetail);

        // dxfFile.getf
        // EXTRACT DATA FROM DXFFILE TO planDetail;

        // planDetail= generalRule.validate(planDetail);
        // EXTRACT DATA FROM DXFFILE TO planDetail;
        planDetail = extractService.extract(dxf1File, dcrApplication);
        //planDetailService.save(planDetail);
        if (planDetail.getBuilding().getBuildingHeight().compareTo(BigDecimal.valueOf(10)) > 0)
            planDetail.addError("Cannot Process",
                    " This report is incomplete. All rules are not processed. Only Building up to 10 Mtr height is considered for processing.");
        // return planDetail;

        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);

        for (PlanRule pl : planRules) {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                if(LOG.isDebugEnabled()) LOG.debug(s);
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
       // jasper.setProperty("SplitType", SplitTypeEnum.STRETCH.toString());
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

        List<EdcrApplicationDetail> edcrApplicationDetails = edcrApplicationDetailService.fingByDcrApplicationId(edcrApplication.getId());
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
                edcrApplication.setStatus("Accepted");
            } else {
                edcrApplicationDetail.setStatus("Not Accepted");
                edcrApplication.setStatus("Not Accepted");
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

        if (dcrApplication.getPlanInformation() != null && dcrApplication.getPlanInformation().getApplicantName() != null)
            planDetail2.getPlanInformation().setApplicantName(dcrApplication.getPlanInformation().getApplicantName());

        boolean reportStatus = false;
        boolean finalReportStatus = true;
        StringBuilder errors = new StringBuilder();
        StringBuilder nocs = new StringBuilder();
        if (planDetail.getNoObjectionCertificates() != null && planDetail.getNoObjectionCertificates().size() > 0) {
            int i = 1;
            for (Map.Entry<String, String> entry : planDetail.getNoObjectionCertificates().entrySet()) {
                nocs.append(String.valueOf(i)).append(". ");
                nocs.append(entry.getValue());
                nocs.append("\n"); 
                i++;
            }
        }
        
        if (planDetail.getErrors() != null && planDetail.getErrors().size() > 0) {
            int i = 1;
            for (Map.Entry<String, String> entry : planDetail.getErrors().entrySet()) {
                errors.append(String.valueOf(i)).append(". "); 
                errors.append(entry.getValue());
                errors.append("\n"); 
                i++;
                finalReportStatus = false;
            }
        }

        drb.setPageSizeAndOrientation(new Page(842, 595, true));
        final JRDataSource ds = new JRBeanCollectionDataSource(Collections.singletonList(planDetail2));
        final JRDataSource ds1 = new JRBeanCollectionDataSource(planDetail.getReportOutput().getRuleOutPuts());

        final Map valuesMap = new HashMap();
        valuesMap.put("buildUpArea", planDetail.getBuilding().getTotalBuitUpArea() != null
                ? planDetail.getBuilding().getTotalBuitUpArea().setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0));
        valuesMap.put("floorArea", planDetail.getBuilding().getTotalFloorArea() != null
                ? planDetail.getBuilding().getTotalFloorArea().setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0));
        valuesMap.put("carpetArea",
                planDetail.getBuilding().getTotalFloorArea().multiply(BigDecimal.valueOf(0.8)).setScale(2, RoundingMode.HALF_UP));
        valuesMap.put("plotArea", planDetail.getPlot().getArea() != null
                ? planDetail.getPlot().getArea().setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0));
        valuesMap.put("far", planDetail.getBuilding().getFar() != null
                ? planDetail.getBuilding().getFar().setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0));
        valuesMap.put("coverage", planDetail.getBuilding().getCoverage() != null
                ? planDetail.getBuilding().getCoverage().setScale(2, RoundingMode.HALF_UP) : BigDecimal.valueOf(0));

        valuesMap.put("applicationNumber", applicationNumber);
        valuesMap.put("applicationDate", applicationDate);
        valuesMap.put("errors", planDetail.getErrors());
        valuesMap.put("errorString", errors.toString());
        valuesMap.put("nocString", nocs.toString());
        valuesMap.put("nocs", planDetail.getNoObjectionCertificates());
        valuesMap.put("cityLogo", cityService.getCityLogoURL());
        valuesMap.put("currentYear", new LocalDate().getYear());

        String imageURL = reportUtil.getImageURL("/egi/resources/global/images/egov_logo_brown.png");
        valuesMap.put("egovLogo", imageURL);

        if (clientSpecificSubReport) {
            List<DcrReportOutput> list = new ArrayList<>();
            for (RuleOutput ruleOutput : planDetail2.getReportOutput().getRuleOutPuts()) {
                DcrReportOutput dcrReportOutput;
                if (ruleOutput.getSubRuleOutputs() != null && ruleOutput.getSubRuleOutputs().size() > 0) {
                    for (SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs()) {
                        if (subRuleOutput.getRuleReportOutputs().size() > 0) {
                            dcrReportOutput = new DcrReportOutput();
                            for (RuleReportOutput ruleReportOutput : subRuleOutput.getRuleReportOutputs()) {
                                dcrReportOutput.setKey(subRuleOutput.getKey());
                                dcrReportOutput.setDescription(subRuleOutput.getRuleDescription()+"\n");
                                dcrReportOutput.setExpectedResult(ruleReportOutput.getExpectedResult()+"\n");
                                dcrReportOutput.setActualResult(ruleReportOutput.getActualResult()+"\n");
                                dcrReportOutput.setStatus(ruleReportOutput.getStatus());
                                
                                if(ruleReportOutput.getStatus()!=null && ruleReportOutput.getStatus().equalsIgnoreCase("NotAccepted")) {
                                    finalReportStatus=false; 
                                }
                            }
                            list.add(dcrReportOutput);
                        } else {
                            dcrReportOutput = new DcrReportOutput();
                            dcrReportOutput.setKey(subRuleOutput.getKey());
                            dcrReportOutput.setDescription(subRuleOutput.getRuleDescription()+"\n");
                            dcrReportOutput.setExpectedResult(null);
                            dcrReportOutput.setActualResult(subRuleOutput.getMessage()+"\n");
                            dcrReportOutput.setStatus(subRuleOutput.getResult().toString());
                            list.add(dcrReportOutput);
                        }
                    }
                }
                JRDataSource dataSource = new JRBeanCollectionDataSource(list);
                valuesMap.put("subreportds", dataSource);
                try {
                    drb.addConcatenatedReport(generateDcrSubReport(list));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);

            for (PlanRule pl : planRules) {
                String rules = pl.getRules();
                String[] ruleSet = rules.split(","); 
                for (String s : ruleSet) {
                    String ruleName = "rule" + s;
                    if(LOG.isDebugEnabled()) LOG.debug(s);
                    Object ruleBean = getRuleBean(ruleName);
                    if (ruleBean != null) {
                        GeneralRule bean = (GeneralRule) ruleBean;
                        if (bean != null){
                            reportStatus = bean.generateRuleReport(planDetail, drb, valuesMap, reportStatus);
                            if(!reportStatus) {
                                finalReportStatus=false;
                            }
                            
                        }
                    } else
                        LOG.error("Skipping rule " + ruleName + "Since rule cannot be injected");
                }
            }
        }

        reportBuilder.append("Report Status : " + (finalReportStatus ? "Accepted" : "Not Accepted")).append("\\n").append("\\n");
        reportBuilder.append("Rules Verified : ").append("\\n");
        valuesMap.put("reportStatus", (finalReportStatus ? "Accepted" : "Not Accepted"));
        drb.setTemplateFile("/reports/templates/edcr_report.jrxml");
        drb.setMargins(5, 0, 33, 20);
        if (finalReportStatus) {
            String dcrApplicationNumber = dcrApplicationNumberGenerator.generateEdcrApplicationNumber(dcrApplication);
            EdcrApplicationDetail edcrApplicationDetail = dcrApplication.getEdcrApplicationDetails().get(0);
            edcrApplicationDetail.setDcrNumber(dcrApplicationNumber);
        }
        if (finalReportStatus) {
            valuesMap.put("qrCode", generatePDF417Code(buildQRCodeDetails(dcrApplication, finalReportStatus)));
        }
        final DynamicReport dr = drb.build();
        planDetail2.setEdcrPassed(finalReportStatus);
        return DynamicJasperHelper.generateJasperPrint(dr, new ClassicLayoutManager(), ds, valuesMap);

    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

    private String buildQRCodeDetails(final EdcrApplication dcrApplication, boolean reportStatus) {
        StringBuilder qrCodeValue = new StringBuilder();
        qrCodeValue = !org.apache.commons.lang.StringUtils.isEmpty(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber()) ? qrCodeValue.append("DCR Number : ").append(dcrApplication.getEdcrApplicationDetails().get(0).getDcrNumber()).append("\n") : qrCodeValue.append("DCR Number : ").append("N/A").append("\n");
        qrCodeValue = !org.apache.commons.lang.StringUtils.isEmpty(dcrApplication.getApplicationNumber()) ? qrCodeValue.append("Application Number : ").append(dcrApplication.getApplicationNumber()).append("\n") : qrCodeValue.append("Application Number : ").append("N/A").append("\n");
        qrCodeValue = dcrApplication.getApplicationDate() != null ? qrCodeValue.append("Application Date : ").append(dcrApplication.getApplicationDate()).append("\n") : qrCodeValue.append("Application Date : ").append("N/A").append("\n");
        qrCodeValue = qrCodeValue.append("Report Status :").append(reportStatus ? "Accepted" : "Not Accepted").append("\n");
        return qrCodeValue.toString();
    }

    public Subreport generateDcrSubReport(final List<DcrReportOutput> dcrReportOutputs) throws Exception {
        FastReportBuilder drb = new FastReportBuilder();

        final Style titleStyle = new Style("titleStyle");
        titleStyle.setFont(Font.ARIAL_MEDIUM_BOLD);
        titleStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        titleStyle.setVerticalAlign(VerticalAlign.BOTTOM);


        final Style columnStyle = reportService.getColumnStyle();
        final Style columnHeaderStyle = reportService.getColumnHeaderStyle();
        drb.setTitle("Building Rule Scrutiny");
        drb.setTitleStyle(titleStyle);
        drb.addColumn("KMBR Rule No.", "key", String.class.getName(), 60, columnStyle, columnHeaderStyle);
        drb.addColumn("Rule description", "description", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Required by Rule", "expectedResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Provided as per drawings", "actualResult", String.class.getName(), 125, columnStyle, columnHeaderStyle);
        drb.addColumn("Accepted / Not Accepted", "status", String.class.getName(), 90, columnStyle, columnHeaderStyle); 
        drb.setUseFullPageWidth(true);
        drb.setPageSizeAndOrientation(Page.Page_Legal_Landscape());
        new JRBeanCollectionDataSource(dcrReportOutputs);
        final DJDataSource djds = new DJDataSource("subreportds", DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);

        final Subreport subRep = new Subreport();
        subRep.setLayoutManager(new ClassicLayoutManager());
        subRep.setDynamicReport(drb.build());
        subRep.setDatasource(djds);
        subRep.setUseParentReportParameters(true);

        return subRep;
    }
}
