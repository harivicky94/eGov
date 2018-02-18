package org.egov.edcr.entity;

import java.io.File;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.ja.annotation.Ignore;
@Entity
@Table(name = "edcredcrapplication")
//@SequenceGenerator(name = PlanRule.SEQ_EDCR_PLANRULE, sequenceName = PlanRule.SEQ_EDCR_PLANRULE, allocationSize = 1)
public class EdcrApplication extends AbstractAuditable {
    /*
     * Application number and date.Owner name, contact info,email id, address,
     * Architect name, emailid,contract info.
     * 
     */
 
    @Id
    private String applicationNumber;
   
    private String dcrNumber;
 
    private Date applicationDate;
    @Transient
    private File dxfFile; //File to be process.
    @Ignore
    @Transient
    private List <File> fileHistory;  
    @Transient
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
    public File getDxfFile() {
        return dxfFile;
    }
    public void setDxfFile(File dxfFile) {
        this.dxfFile = dxfFile;
    }
    public List<File> getFileHistory() {
        return fileHistory;
    }
    public void setFileHistory(List<File> fileHistory) {
        this.fileHistory = fileHistory;
    }
    public PlanInformation getPlanInformation() {
        return planInformation;
    }
    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }
    @Override
    public Long getId() {
      return null;
    }
    @Override
    protected void setId(Long id) {
        // TODO Auto-generated method stub
        
    }
    

}
