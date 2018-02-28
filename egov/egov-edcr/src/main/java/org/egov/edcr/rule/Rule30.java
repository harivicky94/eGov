package org.egov.edcr.rule;

import java.awt.Color;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.Result;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.service.ReportService;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.Subreport;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class Rule30 extends GeneralRule {
	
	@Autowired
    private ReportService reportService;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        HashMap<String, String> errors = new HashMap<>();
        System.out.println("validate 30");
        if (planDetail != null && planDetail.getPlanInformation() != null
                && StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy())) {
            errors.put(DcrConstants.OCCUPANCY,
                    edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                            new String[] { DcrConstants.OCCUPANCY }, LocaleContextHolder.getLocale()));
            planDetail.addErrors(errors);
        }
        return planDetail;
    }

    @Override
    public PlanDetail process(PlanDetail planDetail) {

        rule30(planDetail);
        return planDetail;

    }

    private void rule30(PlanDetail planDetail) {
        if (planDetail != null && planDetail.getPlanInformation() != null
                && !StringUtils.isEmpty(planDetail.getPlanInformation().getOccupancy()))
            planDetail.reportOutput
                    .add(buildRuleOutputWithMainRule(DcrConstants.RULE30, DcrConstants.OCCUPANCY,
                            Result.Verify, DcrConstants.OCCUPANCY + DcrConstants.OBJECTDEFINED_DESC));
    }
    
    public void generateRuleReport(PlanDetail planDetail, FastReportBuilder drb2, Map valuesMap) {
    	List<RuleOutput> rules = planDetail.getReportOutput().getRuleOutPuts();
    	for(RuleOutput ruleOutput : rules)
    	if(ruleOutput.getKey().equalsIgnoreCase(DcrConstants.RULE30)) {
    	 FastReportBuilder drb = new FastReportBuilder();
         
         StringBuilder stringBuilder = new StringBuilder();
         if(ruleOutput.getMessage() != null)
             stringBuilder.append("Message : ").append(ruleOutput.getMessage()).append("\\n");
         if(ruleOutput.getRuleDescription() != null)
             stringBuilder.append("Description : ").append(ruleOutput.getRuleDescription()).append("\\n");
         drb.setMargins(5, 0, 10, 10);
         drb.setTitle("Rule : " + ruleOutput.getKey() + "\\n")
         .setSubtitle(stringBuilder.toString())
                         .setPrintBackgroundOnOddRows(false).setWhenNoData("", null)
                         .setTitleStyle(reportService.getTitleStyle())
                         .setSubtitleStyle(reportService.getSubTitleStyle())
                         /*.setDefaultStyles(getBudgetTitleStyle(), getDepartmentwiseSubTitleStyle(), getHeaderStyle(), getDetailStyle())
                         .setOddRowBackgroundStyle(getOddRowStyle()).setDetailHeight(10)
                         .setHeaderHeight(35).setUseFullPageWidth(true).setSubtitleStyle(getDepartmentwiseSubTitleStyle())*/
                         .setSubtitleHeight(30);
         //drb.setFooterHeight(1000);

         
     		
         final JRDataSource ds1 = new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs());
         new JRBeanCollectionDataSource(ruleOutput.getSubRuleOutputs()); 
         final DJDataSource djds = new DJDataSource("ruleOutput", DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                 DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);
       
         final Subreport subRep = new Subreport();
         subRep.setLayoutManager(new ClassicLayoutManager());
         subRep.setDynamicReport(drb.build());
         subRep.setDatasource(djds);
         subRep.setUseParentReportParameters(true);
         
         drb2.addConcatenatedReport(subRep);
         //return subRep;
         if(ruleOutput != null && !ruleOutput.getSubRuleOutputs().isEmpty()) {
      		for(SubRuleOutput subRuleOutput : ruleOutput.getSubRuleOutputs())  {
      		try {
      			valuesMap.put(subRuleOutput.getKey()+"DataSource", new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs()));
 				drb2.addConcatenatedReport(generateSubRuleReport(subRuleOutput, drb2, valuesMap));
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
      		}
          }
         break;
    	}
    
    }
    
    public Subreport generateSubRuleReport(final SubRuleOutput subRuleOutput, FastReportBuilder drb2, Map valuesMap)
            throws JRException, IOException, Exception {
       // final Style detailAmountStyle = getConcurrenceAmountStyle();
        FastReportBuilder drb = new FastReportBuilder();
        final Style columnStyle = reportService.getColumnStyle();
        final Style columnHeaderStyle = reportService.getColumnHeaderStyle();
        StringBuilder stringBuilder = new StringBuilder();
        if(subRuleOutput.getMessage() != null)
            stringBuilder.append("Message : ").append(subRuleOutput.getMessage()).append("\\n");
        if(subRuleOutput.getRuleDescription() != null)
            stringBuilder.append("Description : ").append(subRuleOutput.getRuleDescription()).append("\\n");
        
        drb.setMargins(0, 10, 10, 10);
        drb.setTitle("SubRule : " + subRuleOutput.getKey())
        .setSubtitle(stringBuilder.toString())
                        .setPrintBackgroundOnOddRows(false).setWhenNoData("", null)
                        .setTitleStyle(reportService.getTitleStyle())
                        .setSubtitleStyle(reportService.getSubTitleStyle())
                        /*.setDefaultStyles(getBudgetTitleStyle(), getDepartmentwiseSubTitleStyle(), getHeaderStyle(), getDetailStyle())
                        .setOddRowBackgroundStyle(getOddRowStyle()).setDetailHeight(10)
                        .setHeaderHeight(35).setUseFullPageWidth(true).setSubtitleStyle(getDepartmentwiseSubTitleStyle())*/
                        .setSubtitleHeight(30).setTitleHeight(40);
       //.drb.setFooterHeight(1000);
       
        
        if(subRuleOutput.getRuleReportOutputs() != null && !subRuleOutput.getRuleReportOutputs().isEmpty()) {
        	
        drb.addColumn("Field Verified", "fieldVerified", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Expected Result", "expectedResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Actual Result", "actualResult", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        drb.addColumn("Status", "status", String.class.getName(), 120, columnStyle, columnHeaderStyle);
        }
       
      
        new JRBeanCollectionDataSource(subRuleOutput.getRuleReportOutputs());
        final DJDataSource djds = new DJDataSource(subRuleOutput.getKey() +"DataSource", DJConstants.DATA_SOURCE_ORIGIN_PARAMETER,
                DJConstants.DATA_SOURCE_TYPE_JRDATASOURCE);

        final Subreport subRep = new Subreport();
        subRep.setLayoutManager(new ClassicLayoutManager());
        subRep.setDynamicReport(drb.build());
        subRep.setDatasource(djds);
        subRep.setUseParentReportParameters(true);
        subRep.setSplitAllowed(true);

        return subRep;
    }


}