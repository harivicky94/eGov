package org.egov.edcr.rule;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
@Service
public class GeneralRule {
	
    @Autowired
    @Qualifier("parentMessageSource")
    protected MessageSource edcrMessageSource;

    public PlanDetail validate(PlanDetail planDetail){
        System.out.println("validate Generalrule");
    	return planDetail;
    }
    public PlanDetail process(PlanDetail planDetail){
    	return planDetail;
    	
    }
    protected RuleOutput buildRuleOutput(String rule26, String fieldVerified, String expectedResult,
            String actualResult, Result status, String messageKey) {
        RuleOutput ruleOutput = new RuleOutput();
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        ruleOutput.key = rule26;
        subRuleOutput.message =/* edcrMessageSource.getMessage(messageKey,
                new String[] { fieldVerified, expectedResult, actualResult,status.toString() },
                LocaleContextHolder.getLocale());*/
                messageKey +" "+fieldVerified +" Expected Result " + expectedResult + " Actual result " + actualResult +" \n Result : "+ status;
        subRuleOutput.result = status;
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;
    }
    protected RuleOutput buildRuleOutput(String ruleName, String fieldVerified, Result result, String messageKey) {
        RuleOutput ruleOutput = new RuleOutput();
        SubRuleOutput subRuleOutput = new SubRuleOutput();
        ruleOutput.key = ruleName;
        subRuleOutput.message = /*edcrMessageSource.getMessage(messageKey,
                new String[] { fieldVerified }, LocaleContextHolder.getLocale());*/
                messageKey +" "+fieldVerified;
        subRuleOutput.result = result;
        ruleOutput.subRuleOutputs.add(subRuleOutput);
        return ruleOutput;
    }
}
