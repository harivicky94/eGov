package org.egov.edcr.service;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.rule.GeneralRule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/*General rule class contains validations which are required for all types of building plans*/
@Service
public class DcrService {

    private PlanDetail planDetail;

    @Autowired
    private GeneralRule generalRule;

    @Autowired
    private ApplicationContext applicationContext;


    @Autowired
    private PlanRuleService planRuleService;

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }


    public PlanDetail process(MultipartFile dxfFile, EdcrApplication dcrApplication) {

        System.out.println("hello ");
        //TODO:
        //BASIC VALIDATION
        generalRule.validate(planDetail);
        planDetail = extract(dxfFile, dcrApplication);

        // EXTRACT DATA FROM DXFFILE TO planDetail;   
        List<PlanRule> planRules = planRuleService.findRulesByPlanDetail(planDetail);
        for (PlanRule pl : planRules) {
            String rules = pl.getRules();
            String[] ruleSet = rules.split(",");
            for (String s : ruleSet) {
                String ruleName = "rule" + s;

                GeneralRule bean = (GeneralRule) applicationContext.getBean(ruleName);
                planDetail = bean.validate(planDetail);

            }
        }
        // USING PLANDETAIL OBJECT, FINDOUT RULES.
        // ITERATE EACH RULE.CHECK CONDITIONS.
        // GENERATE OUTPUT USING PLANDETAIL.


        return null;
    }


    private PlanDetail extract(MultipartFile dxfFile, EdcrApplication dcrApplication) {
        // TODO Auto-generated method stub
        return null;
    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}
