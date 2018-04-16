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
package org.egov.bpa.transaction.entity.dto;

import java.math.BigDecimal;
import java.util.Date;

public class SearchBpaApplicationForm {
    private Long id;
    private String applicationNumber;
    private String buildingplanapprovalnumber;
    private Date applicationDate;
    private String applicantType;
    private String serviceType;
    private String occupancy;
    private Long serviceTypeId;
    private String serviceCode;
    private String status;
    private Long statusId;
    private String planPermissionNumber;
    private BigDecimal admissionfeeAmount;
    private String applicantName;
    private String stakeHolderName;
    private String currentOwner;
    private String pendingAction;
    private String ward;
    private Long wardId;
    private String electionWard;
    private Long electionWardId;
    private String zone;
    private Long zoneId;
    private boolean isFeeCollected;
    private Date fromDate;
    private Date toDate;
    private String address;
    private String locality;
    private String reSurveyNumber;
    private String serviceTypeEnum;
    private Date appointmentDate;
    private String appointmentTime;
    private Boolean isRescheduledByEmployee;
    private Boolean isOnePermitApplication;
    private String applicationType;
    private String scheduleType;
    private String failureRemarks;

    public Boolean getIsRescheduledByEmployee() {
        return isRescheduledByEmployee;
    }

    public void setIsRescheduledByEmployee(Boolean isRescheduledByEmployee) {
        this.isRescheduledByEmployee = isRescheduledByEmployee;
    }

    public Boolean getIsOnePermitApplication() {
        return isOnePermitApplication;
    }

    public void setIsOnePermitApplication(Boolean isOnePermitApplication) {
        this.isOnePermitApplication = isOnePermitApplication;
    }

    public String getFailureRemarks() {
        return failureRemarks;
    }

    public void setFailureRemarks(String failureRemarks) {
        this.failureRemarks = failureRemarks;
    }

    public String getServiceTypeEnum() {
		return serviceTypeEnum;
	}

	public void setServiceTypeEnum(String serviceTypeEnum) {
		this.serviceTypeEnum = serviceTypeEnum;
	}

	public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getApplicationNumber() {
        return applicationNumber;
    }

    public void setApplicationNumber(String applicationNumber) {
        this.applicationNumber = applicationNumber;
    }

    public String getBuildingplanapprovalnumber() {
        return buildingplanapprovalnumber;
    }

    public void setBuildingplanapprovalnumber(String buildingplanapprovalnumber) {
        this.buildingplanapprovalnumber = buildingplanapprovalnumber;
    }

    public Date getApplicationDate() {
        return applicationDate;
    }

    public void setApplicationDate(Date applicationDate) {
        this.applicationDate = applicationDate;
    }

    public String getOccupancy() {
        return occupancy;
    }

    public void setOccupancy(String occupancy) {
        this.occupancy = occupancy;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getApplicantType() {
        return applicantType;
    }

    public void setApplicantType(String applicantType) {
        this.applicantType = applicantType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPlanPermissionNumber() {
        return planPermissionNumber;
    }

    public void setPlanPermissionNumber(String planPermissionNumber) {
        this.planPermissionNumber = planPermissionNumber;
    }

    public BigDecimal getAdmissionfeeAmount() {
        return admissionfeeAmount;
    }

    public void setAdmissionfeeAmount(BigDecimal admissionfeeAmount) {
        this.admissionfeeAmount = admissionfeeAmount;
    }

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getStakeHolderName() {
        return stakeHolderName;
    }

    public void setStakeHolderName(String stakeHolderName) {
        this.stakeHolderName = stakeHolderName;
    }

    public String getCurrentOwner() {
        return currentOwner;
    }

    public void setCurrentOwner(String currentOwner) {
        this.currentOwner = currentOwner;
    }

    public String getPendingAction() {
        return pendingAction;
    }

    public void setPendingAction(String pendingAction) {
        this.pendingAction = pendingAction;
    }

    public String getWard() {
        return ward;
    }

    public void setWard(String ward) {
        this.ward = ward;
    }

    public String getElectionWard() {
        return electionWard;
    }

    public void setElectionWard(String electionWard) {
        this.electionWard = electionWard;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Long getWardId() {
        return wardId;
    }

    public void setWardId(Long wardId) {
        this.wardId = wardId;
    }

    public Long getElectionWardId() {
        return electionWardId;
    }

    public void setElectionWardId(Long electionWardId) {
        this.electionWardId = electionWardId;
    }

    public Long getZoneId() {
        return zoneId;
    }

    public void setZoneId(Long zoneId) {
        this.zoneId = zoneId;
    }

    public boolean isFeeCollected() {
        return isFeeCollected;
    }

    public void setFeeCollected(boolean isFeeCollected) {
        this.isFeeCollected = isFeeCollected;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public Long getServiceTypeId() {
        return serviceTypeId;
    }

    public void setServiceTypeId(Long serviceTypeId) {
        this.serviceTypeId = serviceTypeId;
    }

    public Long getStatusId() {
        return statusId;
    }

    public void setStatusId(Long statusId) {
        this.statusId = statusId;
    }

    public Date getFromDate() {
        return fromDate;
    }

    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    public Date getToDate() {
        return toDate;
    }

    public void setToDate(Date toDate) {
        this.toDate = toDate;
    }

    public String getLocality() {
        return locality;
    }

    public void setLocality(String locality) {
        this.locality = locality;
    }

    public String getReSurveyNumber() {
        return reSurveyNumber;
    }

    public void setReSurveyNumber(String reSurveyNumber) {
        this.reSurveyNumber = reSurveyNumber;
    }

    public Date getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(Date appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Boolean getRescheduledByEmployee() {
        return isRescheduledByEmployee;
    }

    public void setRescheduledByEmployee(Boolean rescheduledByEmployee) {
        isRescheduledByEmployee = rescheduledByEmployee;
    }

    public Boolean getOnePermitApplication() {
        return isOnePermitApplication;
    }

    public void setOnePermitApplication(Boolean onePermitApplication) {
        isOnePermitApplication = onePermitApplication;
    }

    public String getApplicationType() {
        return applicationType;
    }

    public void setApplicationType(String applicationType) {
        this.applicationType = applicationType;
    }

    public String getScheduleType() {
        return scheduleType;
    }

    public void setScheduleType(String scheduleType) {
        this.scheduleType = scheduleType;
    }
}