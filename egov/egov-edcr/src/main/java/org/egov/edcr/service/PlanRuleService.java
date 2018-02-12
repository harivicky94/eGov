package org.egov.edcr.service;

import java.util.List;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.springframework.stereotype.Service;

@Service
public class PlanRuleService {
    
    public List<PlanRule> findRulesByPlanDetail(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        
        //BASED ON PLAN DETAIL, GET DIFFERENT RULES.
        return null;
    }
}
