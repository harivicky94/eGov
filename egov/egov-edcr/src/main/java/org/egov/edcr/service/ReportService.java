package org.egov.edcr.service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.egov.edcr.entity.EdcrApplication;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.PlanRule;
import org.egov.edcr.entity.RuleOutput;
import org.egov.edcr.entity.SubRuleOutput;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.egov.edcr.rule.GeneralRule;
import org.egov.edcr.rule.Rule23;
import org.egov.edcr.rule.Rule26;
import org.egov.edcr.rule.Rule30;
import org.egov.edcr.rule.Rule60;
import org.egov.edcr.rule.Rule62;
import org.egov.edcr.utility.DcrConstants;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportOutput;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.core.DJConstants;
import ar.com.fdvs.dj.core.DynamicJasperHelper;
import ar.com.fdvs.dj.core.layout.ClassicLayoutManager;
import ar.com.fdvs.dj.domain.DJDataSource;
import ar.com.fdvs.dj.domain.DynamicReport;
import ar.com.fdvs.dj.domain.Style;
import ar.com.fdvs.dj.domain.builders.FastReportBuilder;
import ar.com.fdvs.dj.domain.constants.Border;
import ar.com.fdvs.dj.domain.constants.Font;
import ar.com.fdvs.dj.domain.constants.HorizontalAlign;
import ar.com.fdvs.dj.domain.constants.Page;
import ar.com.fdvs.dj.domain.constants.Transparency;
import ar.com.fdvs.dj.domain.entities.Subreport;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

@Service
public class ReportService {
	

    @Autowired
    private ReportService reportService;
    
    @Autowired
    private FileStoreService fileStoreService;
    
    @Autowired
    private PlanRuleService planRuleService;

    
    public InputStream exportPdf(final JasperPrint jasperPrint) throws JRException, IOException {
    	ByteArrayOutputStream outputBytes = new ByteArrayOutputStream(1 * 1024 * 1024);
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputBytes);
        InputStream inputStream = new ByteArrayInputStream(outputBytes.toByteArray());
        //closeStream(reportStream);
        return inputStream;
    }

    public byte[] generatePlanScrutinyReport(PlanDetail planDetail) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    
    public Style getConcurrenceColumnStyle() {
        final Style columnStyle = new Style("ColumnCss");
        columnStyle.setBorderLeft(Border.THIN());
        columnStyle.setBorderRight(Border.THIN());
        columnStyle.setTextColor(Color.black);
        // columnStyle.
        columnStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnStyle.setFont(new Font(5, Font._FONT_ARIAL, false));
        columnStyle.setTransparency(Transparency.OPAQUE);
        columnStyle.setBorderBottom(Border.THIN());
        // detailAmountStyle.s
        return columnStyle;
    }
    
    public Style getColumnStyle() {
    	final Style columnStyle = new Style("ColumnCss");
        columnStyle.setBorderLeft(Border.THIN());
        columnStyle.setBorderRight(Border.THIN());
        columnStyle.setTextColor(Color.black);
        // columnStyle.
        columnStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnStyle.setFont(new Font(8, Font.TIMES_NEW_ROMAN_MEDIUM_BOLD._FONT_TIMES_NEW_ROMAN, false));
       // columnStyle.setTransparency(Transparency.OPAQUE);
        columnStyle.setBorderBottom(Border.THIN());
        columnStyle.setPadding(5);
        return columnStyle;
    }
    
    public Style getColumnHeaderStyle() {
    	   final Style columnheaderStyle = new Style("ColumnHeaderCss");
    	    columnheaderStyle.setBorderLeft(Border.THIN());
    	    columnheaderStyle.setBorderRight(Border.THIN());
    	    columnheaderStyle.setBorderTop(Border.THIN());
    	    columnheaderStyle.setTextColor(Color.black);
    	    // columnStyle.
    	    columnheaderStyle.setHorizontalAlign(HorizontalAlign.CENTER);
    	    columnheaderStyle.setFont(new Font(8, Font.TIMES_NEW_ROMAN_MEDIUM_BOLD._FONT_TIMES_NEW_ROMAN, false));
    	   // columnStyle.setTransparency(Transparency.OPAQUE);
    	    columnheaderStyle.setBorderBottom(Border.THIN());
    	    return columnheaderStyle;
    }
    
    public Style getTitleStyle() {
    	   final Style titleStyle = new Style("titleStyle");
    	    titleStyle.setFont(new Font(8, Font._FONT_TIMES_NEW_ROMAN, true));
    	    titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
    	    return titleStyle;
    }
    
    public Style getSubTitleStyle() {
        final Style subTitleStyle = new Style("titleStyle");
        subTitleStyle.setFont(new Font(8, Font._FONT_TIMES_NEW_ROMAN, true));
        subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
 	    return subTitleStyle;
    }
    
    
    public Style getResultColumnStyle(List<RuleReportOutput> ruleReportOutputs) {
    	Style columnResultStyle = null;
    	for(RuleReportOutput ruleReportOutput : ruleReportOutputs) {
    		if(ruleReportOutput.getStatus().equals("NotAccepted")) {
    			columnResultStyle = getNotAcceptedResultStyle();
        	}
        	if(ruleReportOutput.getStatus().equals("Accepted")) { 
        		columnResultStyle = getAcceptedResultStyle();
        	}
        	if(ruleReportOutput.getStatus().equals("Verify")) { 
        		columnResultStyle = getVerifyResultStyle();
        	}
    	}
    	return columnResultStyle;
    }
    
    public Style getAcceptedResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.GREEN);
        // columnStyle.
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font.TIMES_NEW_ROMAN_MEDIUM_BOLD._FONT_TIMES_NEW_ROMAN, false));
        
       // columnStyle.setTransparency(Transparency.OPAQUE);
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }
    
    public Style getNotAcceptedResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.RED);
        // columnStyle.
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font.TIMES_NEW_ROMAN_MEDIUM_BOLD._FONT_TIMES_NEW_ROMAN, false));
        
       // columnStyle.setTransparency(Transparency.OPAQUE);
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }
    
    public Style getVerifyResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.RED);
        // columnStyle.
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font.TIMES_NEW_ROMAN_MEDIUM_BOLD._FONT_TIMES_NEW_ROMAN, false));
        
       // columnStyle.setTransparency(Transparency.OPAQUE);
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }
    
 
   

}
