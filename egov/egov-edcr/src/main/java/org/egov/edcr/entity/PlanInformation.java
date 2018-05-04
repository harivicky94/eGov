/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2015>  eGovernments Foundation
 *
 *     The updated version of eGov suite of products as by eGovernments Foundation
 *     is available at http://www.egovernments.org
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see http://www.gnu.org/licenses/ or
 *     http://www.gnu.org/licenses/gpl.html .
 *
 *     In addition to the terms of the GPL license to be adhered to in using this
 *     program, the following additional terms are to be complied with:
 *
 *         1) All versions of this program, verbatim or modified must carry this
 *            Legal Notice.
 *
 *         2) Any misrepresentation of the origin of the material is prohibited. It
 *            is required that all modified versions of this material be marked in
 *            reasonable ways as different from the original version.
 *
 *         3) This license does not grant any rights to any user of the program
 *            with regards to rights under trademark law for use of the trade names
 *            or trademarks of eGovernments Foundation.
 *
 *   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.edcr.entity;

import org.egov.infra.persistence.entity.AbstractAuditable;

import java.math.BigDecimal;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "EDCR_PLANINFO")
@SequenceGenerator(name = PlanInformation.SEQ_EDCR_PLANINFO, sequenceName = PlanInformation.SEQ_EDCR_PLANINFO, allocationSize = 1)
public class PlanInformation extends AbstractAuditable{

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
    private Boolean openingOnRear=false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planDetail")
    private PlanDetail planDetail;

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

    public PlanDetail getPlanDetail() {
        return planDetail;
    }

    public void setPlanDetail(PlanDetail planDetail) {
        this.planDetail = planDetail;
    }

}
