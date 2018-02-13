package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

public class RuleOutput {

    public String key;
    public List<SubRuleOutput> subRuleOutputs;
    public String message;
    public Result result;
    
    
    public void add(SubRuleOutput subruleOut) {
        if (subRuleOutputs == null) {
            subRuleOutputs = new ArrayList<>();
            subRuleOutputs.add(subruleOut);
        } else
            subRuleOutputs.add(subruleOut);

    }

    @Override
    public String toString() {
        return "RuleOutput [key=" + key + ", subRuleOutputs=" + subRuleOutputs + ", message=" + message + "]";
    }
}
