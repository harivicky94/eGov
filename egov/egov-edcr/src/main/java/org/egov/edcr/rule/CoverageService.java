package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.kabeja.dxf.DXFDocument;
import org.springframework.stereotype.Service;
@Service
public class CoverageService  extends GeneralRule implements RuleService {

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return super.validate(planDetail);
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return super.process(planDetail);
    }

    @Override
    public PlanDetail extract(PlanDetail pl, DXFDocument doc) {
        // TODO Auto-generated method stub
        return super.extract(pl, doc);
    }  

}
