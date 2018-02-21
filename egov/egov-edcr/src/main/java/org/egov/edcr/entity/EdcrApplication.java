package org.egov.edcr.entity;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "EDCR_APPLICATION")
@SequenceGenerator(name = EdcrApplication.SEQ_EDCR_APPLICATION, sequenceName = EdcrApplication.SEQ_EDCR_APPLICATION, allocationSize = 1)
public class EdcrApplication extends AbstractAuditable {
    /*
     * Application number and date.Owner name, contact info,email id, address,
     * Architect name, emailid,contract info.
     * 
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
    private MultipartFile dxfFile; //File to be process.

    @OneToMany(mappedBy = "application" , fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = false)
    private List<DxfDocument> dxfDocuments;

    @ManyToOne
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

    public List<DxfDocument> getDxfDocuments() {
        return dxfDocuments;
    }

    public void setDxfDocuments(List<DxfDocument> dxfDocuments) {
        this.dxfDocuments = dxfDocuments;
    }

    public PlanInformation getPlanInformation() {
        return planInformation;
    }

    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }

}
