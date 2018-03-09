package org.egov.edcr.service;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import ar.com.fdvs.dj.domain.constants.*;
import org.egov.edcr.entity.PlanDetail;
import org.egov.edcr.entity.utility.RuleReportOutput;
import org.springframework.stereotype.Service;

import ar.com.fdvs.dj.domain.Style;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperPrint;

@Service
public class ReportService {

    public InputStream exportPdf(final JasperPrint jasperPrint) throws JRException, IOException {
        ByteArrayOutputStream outputBytes = new ByteArrayOutputStream(1 * 1024 * 1024);
        JasperExportManager.exportReportToPdfStream(jasperPrint, outputBytes);
        InputStream inputStream = new ByteArrayInputStream(outputBytes.toByteArray());
        // closeStream(reportStream);
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
        columnStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnStyle.setFont(new Font(5, Font._FONT_ARIAL, false));
        columnStyle.setTransparency(Transparency.OPAQUE);
        columnStyle.setBorderBottom(Border.THIN());
        columnStyle.setTransparent(false);
        return columnStyle;
    }

    public Style getColumnStyle() {
        final Style columnStyle = new Style("ColumnCss");
        columnStyle.setBorderLeft(Border.THIN());
        columnStyle.setBorderRight(Border.THIN());
        columnStyle.setBorderBottom(Border.THIN());
        columnStyle.setBorderTop(Border.THIN());
        columnStyle.setTextColor(Color.black);
        columnStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        columnStyle.setFont(new Font(9, Font._FONT_TIMES_NEW_ROMAN, false));
        columnStyle.setBorderBottom(Border.THIN());
        columnStyle.setTransparent(false);
        return columnStyle;
    }

    public Style getVerifiedColumnStyle() {
        final Style columnStyle = new Style("ColumnCss");
        columnStyle.setBorderLeft(Border.THIN());
        columnStyle.setBorderRight(Border.THIN());
        columnStyle.setTextColor(Color.black);
        columnStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnStyle.setFont(new Font(10, Font._FONT_COMIC_SANS, false));
        columnStyle.setBorderBottom(Border.THIN());
        return columnStyle;
    }

    public Style getColumnHeaderStyle() {
        final Style columnheaderStyle = new Style("ColumnHeaderCss");
        columnheaderStyle.setBorderLeft(Border.THIN());
        columnheaderStyle.setBorderRight(Border.THIN());
        columnheaderStyle.setBorderTop(Border.THIN());
        columnheaderStyle.setBorderBottom(Border.THIN());
        columnheaderStyle.setTextColor(Color.black);
        columnheaderStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnheaderStyle.setVerticalAlign(VerticalAlign.BOTTOM);
        columnheaderStyle.setFont(new Font(10, Font._FONT_ARIAL, true));
        return columnheaderStyle;
    }

    public Style getTitleStyle() {
        final Style titleStyle = new Style("titleStyle");
        titleStyle.setFont(new Font(10, Font._FONT_ARIAL, true));
        titleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        return titleStyle;
    }

    public Style getSubTitleStyle() {
        final Style subTitleStyle = new Style("titleStyle");
        subTitleStyle.setFont(new Font(10, Font._FONT_COMIC_SANS, true));
        subTitleStyle.setHorizontalAlign(HorizontalAlign.LEFT);
        return subTitleStyle;
    }

    public Style getResultColumnStyle(List<RuleReportOutput> ruleReportOutputs) {
        Style columnResultStyle = null;
        for (RuleReportOutput ruleReportOutput : ruleReportOutputs) {
            if (ruleReportOutput.getStatus().equals("Not Accepted"))
                columnResultStyle = getNotAcceptedResultStyle();
            if (ruleReportOutput.getStatus().equals("Accepted"))
                columnResultStyle = getAcceptedResultStyle();
            if (ruleReportOutput.getStatus().equals("Verify"))
                columnResultStyle = getVerifyResultStyle();
        }
        return columnResultStyle;
    }

    public Style getAcceptedResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.GREEN);
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font._FONT_TIMES_NEW_ROMAN, false));
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }

    public Style getNotAcceptedResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.RED);
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font._FONT_TIMES_NEW_ROMAN, false));
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }

    public Style getVerifyResultStyle() {

        final Style columnResultStyle = new Style("ColumnResultCss");
        columnResultStyle.setBorderLeft(Border.THIN());
        columnResultStyle.setBorderRight(Border.THIN());
        columnResultStyle.setTextColor(Color.RED);
        columnResultStyle.setHorizontalAlign(HorizontalAlign.CENTER);
        columnResultStyle.setFont(new Font(8, Font._FONT_TIMES_NEW_ROMAN, false));
        columnResultStyle.setBorderBottom(Border.THIN());
        return columnResultStyle;
    }

    public boolean getReportStatus(List<RuleReportOutput> ruleReportOutputs, boolean status) {
        if (status)
            for (RuleReportOutput ruleReportOutput : ruleReportOutputs) {
                if (ruleReportOutput.getStatus().equals("NotAccepted"))
                    status = false;
                break;
            }
        return status;
    }

}