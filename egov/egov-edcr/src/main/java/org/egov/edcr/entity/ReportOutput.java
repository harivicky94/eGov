package org.egov.edcr.entity;

import java.util.ArrayList;
import java.util.List;

public class ReportOutput {

    public List<RuleOutput> ruleOutPuts = new ArrayList<>();

    public void add(RuleOutput ruleOut) {
        if (ruleOutPuts == null) {
            ruleOutPuts = new ArrayList<>();
            ruleOutPuts.add(ruleOut);
        } else{
            if(ruleOutPuts.contains(ruleOut)) 
            {
                ruleOutPuts.get(ruleOutPuts.indexOf(ruleOut)).addAll(ruleOut.getSubRuleOutputs());
            }else            
            ruleOutPuts.add(ruleOut);
        }

    }

    public List<RuleOutput> getRuleOutPuts() {
        return ruleOutPuts;
    }

    public void setRuleOutPuts(List<RuleOutput> ruleOutPuts) {
        this.ruleOutPuts = ruleOutPuts;
    }

    @Override
    public String toString() {
        return "ReportOutput [ruleOutPuts=" + ruleOutPuts + "]";
    }



}
