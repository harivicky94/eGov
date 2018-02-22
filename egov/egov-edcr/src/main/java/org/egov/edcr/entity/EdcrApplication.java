package org.egov.edcr.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

@Entity
@Table(name = "EDCR_APPLICATION")
@SequenceGenerator(name = EdcrApplication.SEQ_EDCR_APPLICATION, sequenceName = EdcrApplication.SEQ_EDCR_APPLICATION, allocationSize = 1)
public class EdcrApplication extends AbstractAuditable {
    /*
     * Application number and date.Owner name, contact info,email id, address, Architect name, emailid,contract info.
     */
    public static final String SEQ_EDCR_APPLICATION = "SEQ_EDCR_APPLICATION";
    private static final long serialVersionUID = 3624499612401625081L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_APPLICATION, strategy = GenerationType.SEQUENCE)
    private Long id;

    @NotNull
    @Length(min = 1, max = 128)
    private String applicationNumber;

    @Length(min = 1, max = 128)
    private String dcrNumber;

    @Temporal(value = TemporalType.DATE)
    private Date applicationDate;

    @Transient
    private MultipartFile dxfFile; // File to be process.

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private List<DcrDocument> dcrDocuments;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "planinfoid")
    private PlanInformation planInformation;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

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

    public MultipartFile getDxfFile() {
        return dxfFile;
    }

    public void setDxfFile(MultipartFile dxfFile) {
        this.dxfFile = dxfFile;
    }

    public List<DcrDocument> getDcrDocuments() {
        return dcrDocuments;
    }

    public void setDcrDocuments(List<DcrDocument> dcrDocuments) {
        this.dcrDocuments = dcrDocuments;
    }

    public PlanInformation getPlanInformation() {
        return planInformation;
    }

    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }

}
