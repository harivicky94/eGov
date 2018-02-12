package org.egov.edcr.service;

import java.io.File;

import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.springframework.stereotype.Service;

/*General rule class contains validations which are required for all types of building plans*/
@Service
public class DcrService  {

    private PlanDetail planDetail;

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

    
    public PlanDetail process(File dxfFile, EdcrApplication dcrApplication) {
      
        //TODO:
        //BASIC VALIDATION
        // EXTRACT DATA FROM DXFFILE TO planDetail;   
        // USING PLANDETAIL OBJECT, FINDOUT RULES.
        // ITERATE EACH RULE.CHECK CONDITIONS.
        // GENERATE OUTPUT USING PLANDETAIL.
        
        
        return null;
    }

    
    
    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }

}
