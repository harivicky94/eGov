package org.egov.edcr.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.egov.infra.persistence.entity.AbstractAuditable;

@Entity
@Table(name = "EDCR_PLANINFO")
@SequenceGenerator(name = PlanInformation.SEQ_EDCR_PLANINFO, sequenceName = PlanInformation.SEQ_EDCR_PLANINFO, allocationSize = 1)
public class PlanInformation extends AbstractAuditable {

    public static final String SEQ_EDCR_PLANINFO = "SEQ_EDCR_PLANINFO";
    private static final long serialVersionUID = -8471202461472480934L;

    @Id
    @GeneratedValue(generator = SEQ_EDCR_PLANINFO, strategy = GenerationType.SEQUENCE)
    private Long id;

    private BigDecimal plotArea;

    private String ownerName;

    private String occupancy;

    private String serviceType;

    private String amenities;

    private String architectInformation;

    private Long acchitectId;

    private String applicantName;

    private Boolean crzZoneArea = false;

    @Transient
    private Boolean securityZone = false;

    @Transient
    private BigDecimal accessWidth;

    @Transient
    private Boolean nocToAbutSide=false;

    @Transient
    private Boolean nocToAbutRear=false;

    @Transient
    private Boolean openingOnSide=false;
    @Transient
    private Boolean openingBelow2mts=false;
    @Transient
    private Boolean openingAbove2mts=false;
    @Transient
    private Boolean nocToAbutAdjascentSide=false;
    
    @Transient
    private Boolean openingOnRear=false;
    
    @Transient
    private List<Occupancy> occupancies=new ArrayList<>();
    @Transient
    private Boolean parkingToMainBuilding=false;
    
    public Boolean getParkingToMainBuilding() {
        return parkingToMainBuilding;
    }

    public void setParkingToMainBuilding(Boolean parkingToMainBuilding) {
        this.parkingToMainBuilding = parkingToMainBuilding;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getCrzZoneArea() {
        return crzZoneArea;
    }

    public void setCrzZoneArea(Boolean crzZoneArea) {
        this.crzZoneArea = crzZoneArea;
    }

    public BigDecimal getPlotArea() {
        return plotArea;
    }

    public void setPlotArea(BigDecimal plotArea) {
        this.plotArea = plotArea;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getAmenities() {
        return amenities;
    }

    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }

    public String getArchitectInformation() {
        return architectInformation;
    }

    public void setArchitectInformation(String architectInformation) {
        this.architectInformation = architectInformation;
    }

    public Long getAcchitectId() {
        return acchitectId;
    }

    public void setAcchitectId(Long acchitectId) {
        this.acchitectId = acchitectId;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public Boolean getSecurityZone() {
        return securityZone;
    }

    public void setSecurityZone(Boolean securityZone) {
        this.securityZone = securityZone;
    }

    public BigDecimal getAccessWidth() {
        return accessWidth;
    }

    public void setAccessWidth(BigDecimal accessWidth) {
        this.accessWidth = accessWidth;
    }


    public Boolean getNocToAbutSide() {
        return nocToAbutSide;
    }

    public void setNocToAbutSide(Boolean nocToAbutSide) {
        this.nocToAbutSide = nocToAbutSide;
    }

    public Boolean getNocToAbutRear() {
        return nocToAbutRear;
    }

    public void setNocToAbutRear(Boolean nocToAbutRear) {
        this.nocToAbutRear = nocToAbutRear;
    }

    public Boolean getOpeningOnSide() {
        return openingOnSide;
    }

    public void setOpeningOnSide(Boolean openingOnSide) {
        this.openingOnSide = openingOnSide;
    }

    public Boolean getOpeningBelow2mts() {
        return openingBelow2mts;
    }

    public void setOpeningBelow2mts(Boolean openingBelow2mts) {
        this.openingBelow2mts = openingBelow2mts;
    }

    public Boolean getOpeningAbove2mts() {
        return openingAbove2mts;
    }

    public void setOpeningAbove2mts(Boolean openingAbove2mts) {
        this.openingAbove2mts = openingAbove2mts;
    }

    public Boolean getNocToAbutAdjascentSide() {
        return nocToAbutAdjascentSide;
    }

    public void setNocToAbutAdjascentSide(Boolean nocToAbutAdjascentSide) {
        this.nocToAbutAdjascentSide = nocToAbutAdjascentSide;
    }

    public Boolean getOpeningOnRear() {
        return openingOnRear;
    }

    public void setOpeningOnRear(Boolean openingOnRear) {
        this.openingOnRear = openingOnRear;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }
}
