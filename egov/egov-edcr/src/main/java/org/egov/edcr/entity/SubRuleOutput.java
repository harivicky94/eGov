package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

import org.egov.edcr.entity.utility.RuleReportOutput;

public class SubRuleOutput {
    public String key;
    public List<RuleReportOutput> ruleReportOutputs;
    public String message;
    public Result result;
    public String ruleDescription;

    @Override
    public String toString() {
        return "SubRuleOutput [message=" + message + "RuleReportOutput" + ruleReportOutputs + "]";
    }

    public void add(RuleReportOutput ruleReportOutput) {
        if (ruleReportOutputs == null) {
            ruleReportOutputs = new ArrayList<>();
            ruleReportOutputs.add(ruleReportOutput);
        } else
            ruleReportOutputs.add(ruleReportOutput);

    }

}
