package org.egov.edcr.rule;

import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.domain.builders.FastReportBuilder;

@Service
public class GeneralRule {

    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;

    public PlanDetail validate(PlanDetail planDetail) {
       
        
        
        return planDetail;
    }

    public PlanDetail process(PlanDetail planDetail) {
        return planDetail;

    }
    
    public boolean generateRuleReport(PlanDetail planDetail, FastReportBuilder drb, Map map, boolean status) {
        return true;
    }

    protected RuleOutput buildRuleOutputWithSubRule(String mainRule, String subRule, String ruleDescription, String fieldVerified,
            String expectedResult,
            String actualResult, Result status, String message) {
        RuleOutput ruleOutput = new RuleOutput();

        if (mainRule != null) {
            ruleOutput.key = mainRule;
            // ruleOutput.result = status;

            if (subRule != null || fieldVerified != null) {
                SubRuleOutput subRuleOutput = new SubRuleOutput();
                subRuleOutput.key = subRule != null ? subRule : fieldVerified;
                subRuleOutput.result = status;
                subRuleOutput.message = message;
                subRuleOutput.ruleDescription = ruleDescription;

                if (expectedResult != null) {
                    RuleReportOutput ruleReportOutput = new RuleReportOutput();
                    ruleReportOutput.setActualResult(actualResult);
                    ruleReportOutput.setExpectedResult(expectedResult);
                    ruleReportOutput.setFieldVerified(fieldVerified);
                    ruleReportOutput.setStatus(status.toString());
                    subRuleOutput.add(ruleReportOutput);
                }
                ruleOutput.subRuleOutputs.add(subRuleOutput);
            }
        }

        return ruleOutput;
    }

    protected RuleOutput buildRuleOutputWithMainRule(String mainRule, String ruleDescription, Result status, String message) {
        RuleOutput ruleOutput = new RuleOutput();
        ruleOutput.key = mainRule;
        ruleOutput.result = status;
        ruleOutput.setMessage(message);
        ruleOutput.ruleDescription = ruleDescription;

        return ruleOutput;
    }

    /*
     * protected RuleOutput buildRuleOutput(String ruleName, String fieldVerified, Result result, String messageKey) { RuleOutput
     * ruleOutput = new RuleOutput(); SubRuleOutput subRuleOutput = new SubRuleOutput(); ruleOutput.key = ruleName;
     * subRuleOutput.message = edcrMessageSource.getMessage(messageKey, new String[] { fieldVerified },
     * LocaleContextHolder.getLocale()); messageKey +" "+fieldVerified; subRuleOutput.result = result;
     * ruleOutput.subRuleOutputs.add(subRuleOutput); return ruleOutput; }
     */
}
