package org.egov.edcr.service;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.rule.GeneralRule;
import org.egov.edcr.utility.DcrConstants;
import org.egov.edcr.utility.Util;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

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
        

        // USING PLANDETAIL OBJECT, FINDOUT RULES.
        // ITERATE EACH RULE.CHECK CONDITIONS.

        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);

        for (PlanRule pl : planRules) {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                LOG.info(s);
                Object ruleBean = getRuleBean(ruleName);
                if(ruleBean!=null)
                {
                GeneralRule bean = (GeneralRule) ruleBean;
                if(bean!=null)
                {
                    planDetail = bean.validate(planDetail);
                }
                }else
                {
                    LOG.error("Skipping rule "+ruleName+ "Since rule cannot be injected");
                }
              

            }
        }

        for (PlanRule pl : planRules) {

            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");

            for (String s : ruleSet) {
                String ruleName = "rule" + s;
                Object ruleBean = getRuleBean(ruleName);
                if(ruleBean!=null)
                {
                GeneralRule bean = (GeneralRule) ruleBean;
                if(bean!=null){
                planDetail = bean.process(planDetail);
                }
                }else
                {
                    LOG.error("Skipping rule "+ruleName+ "Since rule cannot be injected");
                } 

            }
        }
        generateDCRReport(planDetail,dcrApplication);

        Util.print(planDetail);
        Util.print(planDetail.getErrors());
        Util.print(planDetail.getGeneralInformation()) ;
        Util.print(planDetail.getReportOutput());
        return planDetail;
    }

    private Object getRuleBean(String ruleName) {
        Object bean= null;
        try {
            bean=  applicationContext.getBean(ruleName);
        } catch (BeansException e) {
            LOG.error("No Bean Defined for the Rule"+ruleName); 
        }
        return bean;
    }

    public ReportOutput generateDCRReport(PlanDetail planDetail, EdcrApplication dcrApplication) {
    	
        Map<String, Object> params = new HashMap<>();
        org.egov.edcr.entity.ReportOutput planReportOutput =  planDetail.getReportOutput();
        StringBuffer resultOutput= new StringBuffer();
        StringBuffer errorOutput= new StringBuffer();
        boolean reportStatus=true;
        if (dcrApplication != null) {
            if (dcrApplication.getApplicationNumber() != null)
                params.put("applicationNumber", dcrApplication.getApplicationNumber());
            if (dcrApplication.getApplicationDate() != null)
                params.put("applicationDate", dcrApplication.getApplicationDate().toString());
            if (dcrApplication.getPlanInformation() != null && dcrApplication.getPlanInformation().getOccupancy() != null)
                params.put("occupancy", dcrApplication.getPlanInformation().getOccupancy());
            if (dcrApplication.getPlanInformation() != null
                    && dcrApplication.getPlanInformation().getArchitectInformation() != null)
                params.put("architect", dcrApplication.getPlanInformation().getArchitectInformation());

        }
        
        if (planDetail.getErrors() != null) {

            for (Map.Entry<String, String> entry : planDetail.getErrors().entrySet()) {
                errorOutput.append(entry.getValue());
                errorOutput.append("\n");

            }
            params.put("errorOutput", errorOutput.toString());
            reportStatus = false;
        }
        
        resultOutput.append("Rules Verified : \n\n");
        
    for(RuleOutput ruleOutput: planReportOutput.getRuleOutPuts())
        {
            resultOutput.append("\n");
            resultOutput.append(ruleOutput.key);

            if (ruleOutput.getSubRuleOutputs() != null && ruleOutput.getSubRuleOutputs().size() > 0) {
                for (SubRuleOutput subRuleOutputs : ruleOutput.getSubRuleOutputs()) {
                    if (subRuleOutputs.ruleReportOutputs != null && subRuleOutputs.ruleReportOutputs.size()>0) {
                        resultOutput.append("\n\b");
                        resultOutput.append(subRuleOutputs.key);
                        if (subRuleOutputs.ruleDescription != null) {
                            resultOutput.append(" :: ");
                            resultOutput.append("\b");
                            resultOutput.append(subRuleOutputs.ruleDescription);
                        }
                        resultOutput.append("\b\n");

                        for (RuleReportOutput ruleReportOutput : subRuleOutputs.ruleReportOutputs) {
                            resultOutput.append("\b \n");
                            resultOutput.append(ruleReportOutput.fieldVerified);
                            if (ruleReportOutput.expectedResult != null) {
                                resultOutput.append("\nExpected : ");
                                resultOutput.append("\b");
                                resultOutput.append(ruleReportOutput.expectedResult);
                            }
                            if (ruleReportOutput.actualResult != null) {
                                resultOutput.append("\nActual Result : ");
                                resultOutput.append("\b");
                                resultOutput.append(ruleReportOutput.actualResult);
                            }
                            if (ruleReportOutput.status != null) {
                                resultOutput.append("\nResult : ");
                                resultOutput.append("\b");
                                resultOutput.append(ruleReportOutput.status);
                                if(ruleReportOutput.status.equals("NotAccepted"))  reportStatus=false;    

                            }
                            resultOutput.append("\b\n");
                        }
                    } else {
                        resultOutput.append("\n\b");
                        resultOutput.append(subRuleOutputs.key);
                        if (subRuleOutputs.ruleDescription != null) {
                            resultOutput.append(" :: ");
                            resultOutput.append("\b");
                            resultOutput.append(subRuleOutputs.ruleDescription);
                        }
                        resultOutput.append("\b\n");

                        if (subRuleOutputs.message != null) {
                            resultOutput.append("\tMessage: ");
                            resultOutput.append("\b");
                            resultOutput.append(subRuleOutputs.message);
                        }
                        if (subRuleOutputs.result != null) {
                            resultOutput.append("\n");
                            resultOutput.append("\tResult : ");
                            resultOutput.append("\b");
                            resultOutput.append(subRuleOutputs.result);
                            resultOutput.append("\n");
                            if(subRuleOutputs.result.equals("NotAccepted"))  reportStatus=false;    

                        }
                    }
                }
           }else
           {
               resultOutput.append("\n\b");
               if (ruleOutput.ruleDescription != null && !ruleOutput.ruleDescription.equals("")) {
                   resultOutput.append(ruleOutput.ruleDescription);
               }
               
               if (ruleOutput.message != null) {
                   resultOutput.append("\b\n");
                   resultOutput.append("\tMessage: ");
                   resultOutput.append("\b");
                   resultOutput.append(ruleOutput.message);
               }
               if (ruleOutput.result != null) {
                   resultOutput.append("\n");
                   resultOutput.append("\tResult : ");
                   resultOutput.append("\b");
                   resultOutput.append(ruleOutput.result);
                   resultOutput.append("\n");
               }
               
           }
        }
    
        params.put("resultOutput",resultOutput.toString());  
        final ReportRequest reportInput = new ReportRequest("edcr_report", planDetail,
                params);
        
        if(reportStatus)   params.put("reportStatus",Result.Accepted.toString());    
        else
            params.put("reportStatus",Result.Not_Accepted.toString());    
            
        final ReportOutput reportOutput = reportService.createReport(reportInput);
        FileStoreMapper fileStoreId = fileStoreService.store(new ByteArrayInputStream(reportOutput.getReportOutputData()), "EDCR", "application/pdf",DcrConstants.FILESTORE_MODULECODE);
        
        if(fileStoreId!=null)
            dcrApplication.getSavedDcrDocument().setReportOutputId(fileStoreId);
        
        return reportOutput;

    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}
