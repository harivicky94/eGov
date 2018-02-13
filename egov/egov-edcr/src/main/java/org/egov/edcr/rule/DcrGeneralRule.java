package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;

public interface DcrGeneralRule {
    public PlanDetail validate(PlanDetail planDetail);
    public PlanDetail process(PlanDetail planDetail);
}
