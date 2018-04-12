package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.kabeja.dxf.DXFDocument;

public interface RuleService {

    public PlanDetail extract(PlanDetail pl, DXFDocument doc);

    public PlanDetail validate(PlanDetail pl);

    public PlanDetail process(PlanDetail pl);

}
