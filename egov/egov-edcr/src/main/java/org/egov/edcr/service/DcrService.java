package org.egov.edcr.service;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.rule.GeneralRule;
import org.egov.edcr.rule.Rule23;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import net.sf.jasperreports.engine.JasperPrint;

/*General rule class contains validations which are required for all types of building plans*/
@Service
public class DcrService {

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

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }


    public PlanDetail process(MultipartFile dxf1File, EdcrApplication dcrApplication) {

        System.out.println("hello ");
        //TODO:
        //BASIC VALIDATION
        
        File dxfFile=new File("/home/mani/Desktop/BPA/kozhi/SAMPLE 3.dxf");
       
 
        generalRule.validate(planDetail);

       // dxfFile.getf
        // EXTRACT DATA FROM DXFFILE TO planDetail;   
 
      //  planDetail=    generalRule.validate(planDetail);
     // EXTRACT DATA FROM DXFFILE TO planDetail;   
        planDetail=extractService.extract(dxfFile,dcrApplication);
        
       
     // USING PLANDETAIL OBJECT, FINDOUT RULES.
        // ITERATE EACH RULE.CHECK CONDITIONS.
 
        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);
 
        for(PlanRule pl:planRules)
        {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for(String s:ruleSet)
            {
                String ruleName="rule"+s;
                System.out.println(s);
                GeneralRule bean =(GeneralRule) applicationContext.getBean(ruleName);
                planDetail=   bean.validate(planDetail);
                
                
            }
        }
        
        for(PlanRule pl:planRules)
        {
 
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
 
            for(String s:ruleSet)
            {
                String ruleName="rule"+s;
                
                GeneralRule bean =(GeneralRule) applicationContext.getBean(ruleName);
                planDetail=   bean.process(planDetail);
 
            }
        }
        
        System.err.println(planDetail.getErrors());
        System.err.println(planDetail.getReportOutput());
        
        
        // GENERATE OUTPUT USING PLANDETAIL.


        return null;
    }

    public ReportOutput generateDCRReport(PlanDetail planDetail) {
    	Map<String, Object> params = new HashMap<>();
    	Rule23 rule23 = new Rule23();
    	params.put("rule23" ,rule23.generateReport(planDetail));
        final ReportRequest reportInput = new ReportRequest("edcr_report", planDetail,
        		params);
        final ReportOutput reportOutput = reportService.createReport(reportInput);
        return reportOutput;
      
    }
 
    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}
