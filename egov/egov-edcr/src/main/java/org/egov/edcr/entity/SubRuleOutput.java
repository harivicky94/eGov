package org.egov.edcr.entity;

import java.util.List;

import org.egov.edcr.entity.utility.RuleReportOutput;

public class SubRuleOutput {
	public String key;
	public List<RuleReportOutput> ruleReportOutputs;
    public String message;
    public Result result;
    @Override
    public String toString() {
        return "SubRuleOutput [message=" + message + "]";
    }

}
