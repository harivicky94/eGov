package org.egov.edcr.entity;

import java.io.File;
import java.util.Date;
import java.util.List;

public class EdcrApplication {
    /*
     * Application number and date.Owner name, contact info,email id, address,
     * Architect name, emailid,contract info.
     * 
     */
    private String applicationNumber;
    private String dcrNumber;
    private Date applicationDate;
    private File dxfFile; //File to be process.
    private List <File> fileHistory;  
    private PlanInformation planInformation;
    
    
    
    public String getApplicationNumber() {
        return applicationNumber;
    }
    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }
    public String getDcrNumber() {
        return dcrNumber;
    }
    public void setDcrNumber(String dcrNumber) {
        this.dcrNumber = dcrNumber;
    }
    public Date getApplicationDate() {
        return applicationDate;
    }
    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }
    

}
