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
        return "SubRuleOutput [key=" + key + ", ruleReportOutputs=" + ruleReportOutputs + ", message=" + message + ", result="
                + result + ", ruleDescription=" + ruleDescription + "]";
    }

    public void add(RuleReportOutput ruleReportOutput) {
        if (ruleReportOutputs == null) {
            ruleReportOutputs = new ArrayList<>();
            ruleReportOutputs.add(ruleReportOutput);
        } else
            ruleReportOutputs.add(ruleReportOutput);

    }

    public List<RuleReportOutput> getRuleReportOutputs() {
        return ruleReportOutputs;
    }

    public void setRuleReportOutputs(List<RuleReportOutput> ruleReportOutputs) {
        this.ruleReportOutputs = ruleReportOutputs;
    }

}
