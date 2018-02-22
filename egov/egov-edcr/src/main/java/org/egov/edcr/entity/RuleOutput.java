package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

public class RuleOutput {

    public String key;
    public List<SubRuleOutput> subRuleOutputs = new ArrayList<>();
    public String message;
    public Result result;
    public String ruleDescription;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public List<SubRuleOutput> getSubRuleOutputs() {
        return subRuleOutputs;
    }

    public void setSubRuleOutputs(List<SubRuleOutput> subRuleOutputs) {
        this.subRuleOutputs = subRuleOutputs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Result getResult() {
        return result;
    }

    public void setResult(Result result) {
        this.result = result;
    }

}
