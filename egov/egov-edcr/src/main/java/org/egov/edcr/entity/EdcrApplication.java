package org.egov.edcr.entity;

import org.egov.infra.persistence.entity.AbstractAuditable;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.Date;
import java.util.List;

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

    @Temporal(value = TemporalType.DATE)
    private Date applicationDate;

    @Transient
    private MultipartFile dxfFile; // File to be process.

    @Transient
    private File savedDxfFile;

    public File getSavedDxfFile() {
        return savedDxfFile;
    }

    public void setSavedDxfFile(File savedDxfFile) {
        this.savedDxfFile = savedDxfFile;
    }

    @OneToMany(mappedBy = "application", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy("id DESC ")
    private List<EdcrApplicationDetail> edcrApplicationDetails;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "planinfoid")
    private PlanInformation planInformation;

    @Transient
    private EdcrApplicationDetail savedEdcrApplicationDetail;

    public EdcrApplicationDetail getSavedEdcrApplicationDetail() {
        return savedEdcrApplicationDetail;
    }

    public void setSavedEdcrApplicationDetail(EdcrApplicationDetail savedEdcrApplicationDetail) {
        this.savedEdcrApplicationDetail = savedEdcrApplicationDetail;
    }

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

    public List<EdcrApplicationDetail> getEdcrApplicationDetails() {
        return edcrApplicationDetails;
    }

    public void setEdcrApplicationDetails(List<EdcrApplicationDetail> edcrApplicationDetails) {
        this.edcrApplicationDetails = edcrApplicationDetails;
    }

    public PlanInformation getPlanInformation() {
        return planInformation;
    }

    public void setPlanInformation(PlanInformation planInformation) {
        this.planInformation = planInformation;
    }

}
