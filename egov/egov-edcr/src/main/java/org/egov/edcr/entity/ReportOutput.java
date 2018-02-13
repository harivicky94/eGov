package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

public class ReportOutput {

    public List<RuleOutput> ruleOutPuts;

    public void add(RuleOutput ruleOut) {
        if (ruleOutPuts == null) {
            ruleOutPuts = new ArrayList<>();
            ruleOutPuts.add(ruleOut);
        } else
            ruleOutPuts.add(ruleOut);

    }
}
