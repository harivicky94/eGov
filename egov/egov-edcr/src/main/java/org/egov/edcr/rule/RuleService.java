package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.utility.math.Ray;
import org.kabeja.dxf.DXFDocument;
import org.kabeja.dxf.helpers.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;

public interface RuleService {

    final Ray RAY_CASTING = new Ray(new Point(-1.123456789, -1.987654321, 0d));
    
    public PlanDetail extract(PlanDetail pl, DXFDocument doc);

    public PlanDetail validate(PlanDetail pl);

    public PlanDetail process(PlanDetail pl);

}
