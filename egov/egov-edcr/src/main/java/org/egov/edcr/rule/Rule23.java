package org.egov.edcr.rule;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
 

@Service
public class Rule23 extends GeneralRule {

    private static final BigDecimal VERTICAL_DISTANCE_11000 = BigDecimal.valueOf(2.4);
    private static final BigDecimal VERTICAL_DISTANCE_33000 = BigDecimal.valueOf(3.7);
    private static final BigDecimal HORIZONTAL_DISTANCE_33000 = BigDecimal.valueOf(1.85);

    private static final int VOLTAGE_11000 = 11000;
    private static final int VOLTAGE_33000 = 33000;
    private static final BigDecimal HORIZONTAL_DISTANCE_11000 = BigDecimal.valueOf(1.2);

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<String, String>();
        System.out.println("validate 23");
        if (planDetail != null &&
                planDetail.getElectricLine() != null && planDetail.getElectricLine().getPresentInDxf()) {
            if (planDetail.getElectricLine().getVoltage() == null) {
                errors.put(DcrConstants.VOLTAGE,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.VOLTAGE }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
            if (planDetail.getElectricLine().getHorizontalDistance() == null
                    && planDetail.getElectricLine().getVerticalDistance() == null) {
                errors.put(DcrConstants.ELECTRICLINE_DISTANCE,
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { DcrConstants.ELECTRICLINE_DISTANCE }, LocaleContextHolder.getLocale()));
                planDetail.addErrors(errors);
            }
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

       /* rule23_4(planDetail, );
        rule23_5(planDetail);*/

        return planDetail;

    }
    
    
    private void rule23_4(PlanDetail planDetail, List<RuleReportOutput> ruleReportOutputs) {
        if (planDetail.getPlanInformation()!=null && planDetail.getPlanInformation().getCrzZoneArea()) {

            planDetail.reportOutput.add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.CRZZONE, Result.Verify,
                    DcrConstants.OBJECTDEFINED));
            
        }
    }
    
    public List<RuleReportOutput> generateReport(PlanDetail planDetail) {
    	List<RuleReportOutput> ruleReportOutputs = new ArrayList<>();
    	rule23_5(planDetail, ruleReportOutputs);
    	rule23_4(planDetail, ruleReportOutputs);
    	return ruleReportOutputs;
    	
    }

    private List<RuleReportOutput> rule23_5(PlanDetail planDetail, List<RuleReportOutput> ruleReportOutputs) {
    	RuleReportOutput ruleReportOutput = null;
        if (planDetail != null &&
                planDetail.getElectricLine() != null && planDetail.getElectricLine().getPresentInDxf()) {
            if (planDetail.getElectricLine().getVoltage() != null
                    && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.ZERO) > 0
                    && (planDetail.getElectricLine().getHorizontalDistance() != null
                            || planDetail.getElectricLine().getVerticalDistance() != null)) {

                if (planDetail.getElectricLine().getHorizontalDistance() != null) {
                    if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {
                        if (planDetail.getElectricLine().getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_11000) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        } else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }
                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >= 0
                            && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {

                        if (planDetail.getElectricLine().getHorizontalDistance().compareTo(HORIZONTAL_DISTANCE_33000) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                            // IS IT
                        }                                                                                       // MANDATORY.
                        else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            HORIZONTAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }

                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {

                        Double totalHorizontalOHE = HORIZONTAL_DISTANCE_33000.doubleValue() + 0.3 *
                                (Math.ceil(
                                        ((planDetail.getElectricLine().getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000)))
                                                .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP))
                                                        .doubleValue()));

                        if (planDetail.getElectricLine().getHorizontalDistance()
                                .compareTo(BigDecimal.valueOf(totalHorizontalOHE)) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            totalHorizontalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        } else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE,
                                            totalHorizontalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getHorizontalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.HORIZONTAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }
                    }

                } else if (planDetail.getElectricLine().getVerticalDistance() != null) {

                    if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) < 0) {
                        if (planDetail.getElectricLine().getVerticalDistance().compareTo(VERTICAL_DISTANCE_11000) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        } else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_11000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }
                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_11000)) >=0
                            && planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) <= 0) {

                        if (planDetail.getElectricLine().getVerticalDistance().compareTo(VERTICAL_DISTANCE_33000) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }                                                                                       // MANDATORY.
                        else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            VERTICAL_DISTANCE_33000.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }

                    } else if (planDetail.getElectricLine().getVoltage().compareTo(BigDecimal.valueOf(VOLTAGE_33000)) > 0) {

                        Double totalVertficalOHE = VERTICAL_DISTANCE_33000.doubleValue() + 0.3 *
                                (Math.ceil(
                                        ((planDetail.getElectricLine().getVoltage().subtract(BigDecimal.valueOf(VOLTAGE_33000)))
                                                .divide(BigDecimal.valueOf(VOLTAGE_33000), 2, RoundingMode.HALF_UP))
                                                        .doubleValue()));
                        
                        if (planDetail.getElectricLine().getVerticalDistance()
                                .compareTo(BigDecimal.valueOf(totalVertficalOHE)) >= 0) {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            totalVertficalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        } else {
                            planDetail.reportOutput
                                    .add(buildRuleOutput(DcrConstants.RULE23, DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE,
                                            totalVertficalOHE.toString() + DcrConstants.IN_METER,
                                            planDetail.getElectricLine().getVerticalDistance().toString()
                                                    + DcrConstants.IN_METER,
                                            Result.Not_Accepted,
                                            DcrConstants.EXPECTEDRESULT));
                            ruleReportOutput = new RuleReportOutput();
                            ruleReportOutput.setRuleKey(DcrConstants.RULE23);
                            ruleReportOutput.setFieldVerified(DcrConstants.VERTICAL_ELECTRICLINE_DISTANCE);
                            ruleReportOutput.setActualResult(Result.Not_Accepted.toString());
                            ruleReportOutput.setExpectedResult(DcrConstants.EXPECTEDRESULT);
                            ruleReportOutputs.add(ruleReportOutput);
                        }
                    }

                }
            }
        }
        
        return ruleReportOutputs;
    }
  

}
