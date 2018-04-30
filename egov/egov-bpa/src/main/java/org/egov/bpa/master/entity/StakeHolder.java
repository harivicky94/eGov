/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2017>  eGovernments Foundation
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

package org.egov.bpa.master.entity;

import org.egov.bpa.transaction.entity.StakeHolderDocument;
import org.egov.bpa.transaction.entity.enums.StakeHolderType;
import org.egov.commons.entity.Source;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.persistence.entity.CorrespondenceAddress;
import org.egov.infra.persistence.entity.PermanentAddress;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.persistence.validator.annotation.Unique;
import org.hibernate.validator.constraints.Length;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "EGBPA_MSTR_STAKEHOLDER")
@Unique(fields = {"code", "coaEnrolmentNumber", "tinNumber"}, enableDfltMsg = true)
public class StakeHolder extends User {

	private static final long serialVersionUID = 3078684328383202788L;
	@OneToMany(mappedBy = "stakeHolder", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<StakeHolderDocument> stakeHolderDocument = new ArrayList<>(0);
	@Enumerated(EnumType.ORDINAL)
	@NotNull
	private StakeHolderType stakeHolderType;
	@NotNull
	@Length(min = 1, max = 128)
	@Column(name = "code", unique = true)
	private String code;
	@NotNull
	@Length(min = 1, max = 64)
	private String licenceNumber;
	@NotNull
	@Temporal(value = TemporalType.DATE)
	private Date buildingLicenceIssueDate;
	@Enumerated(EnumType.ORDINAL)
	private Source source;
	@Temporal(value = TemporalType.DATE)
	private Date buildingLicenceExpiryDate;
	@Length(min = 1, max = 64)
	private String coaEnrolmentNumber;
	@Temporal(value = TemporalType.DATE)
	private Date coaEnrolmentDueDate;
	private Boolean isEnrolWithLocalBody;
	@Length(min = 1, max = 128)
	private String organizationName;
	@Length(min = 1, max = 128)
	private String organizationAddress;
	@Length(min = 1, max = 64)
	private String organizationUrl;
	@Length(min = 1, max = 15)
	private String organizationMobNo;
	private Boolean isOnbehalfOfOrganization;
	@NotNull
	private Boolean isActive;
	@Length(max = 11)
	private String tinNumber;
	@Length(max = 50)
	private String contactPerson;
	@Length(max = 50)
	private String designation;
	private transient CorrespondenceAddress correspondenceAddress = new CorrespondenceAddress();
	private transient PermanentAddress permanentAddress = new PermanentAddress();
	private transient List<CheckListDetail> checkListDocuments = new ArrayList<>(0);
	private transient String activationCode;
	private String comments;

	public StakeHolder() {
		setType(UserType.BUSINESS);
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(final Boolean isActive) {
		this.isActive = isActive;
	}

	public String getCode() {
		return code;
	}

	public void setCode(final String code) {
		this.code = code;
	}

	public StakeHolderType getStakeHolderType() {
		return stakeHolderType;
	}

	public void setStakeHolderType(final StakeHolderType stakeHolderType) {
		this.stakeHolderType = stakeHolderType;
	}

	public String getLicenceNumber() {
		return licenceNumber;
	}

	public void setLicenceNumber(String licenceNumber) {
		this.licenceNumber = licenceNumber;
	}

	public Date getBuildingLicenceIssueDate() {
		return buildingLicenceIssueDate;
	}

	public void setBuildingLicenceIssueDate(Date buildingLicenceIssueDate) {
		this.buildingLicenceIssueDate = buildingLicenceIssueDate;
	}

	public Date getBuildingLicenceExpiryDate() {
		return buildingLicenceExpiryDate;
	}

	public void setBuildingLicenceExpiryDate(Date buildingLicenceExpiryDate) {
		this.buildingLicenceExpiryDate = buildingLicenceExpiryDate;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public String getCoaEnrolmentNumber() {
		return coaEnrolmentNumber;
	}

	public void setCoaEnrolmentNumber(final String coaEnrolmentNumber) {
		this.coaEnrolmentNumber = coaEnrolmentNumber;
	}

	public Boolean getIsEnrolWithLocalBody() {
		return isEnrolWithLocalBody;
	}

	public void setIsEnrolWithLocalBody(final Boolean isEnrolWithLocalBody) {
		this.isEnrolWithLocalBody = isEnrolWithLocalBody;
	}

	public String getOrganizationName() {
		return organizationName;
	}

	public void setOrganizationName(final String organizationName) {
		this.organizationName = organizationName;
	}

	public String getOrganizationAddress() {
		return organizationAddress;
	}

	public void setOrganizationAddress(final String organizationAddress) {
		this.organizationAddress = organizationAddress;
	}

	public String getOrganizationUrl() {
		return organizationUrl;
	}

	public void setOrganizationUrl(final String organizationUrl) {
		this.organizationUrl = organizationUrl;
	}

	public String getOrganizationMobNo() {
		return organizationMobNo;
	}

	public void setOrganizationMobNo(final String organizationMobNo) {
		this.organizationMobNo = organizationMobNo;
	}

	public Boolean getIsOnbehalfOfOrganization() {
		return isOnbehalfOfOrganization;
	}

	public void setIsOnbehalfOfOrganization(final Boolean isOnbehalfOfOrganization) {
		this.isOnbehalfOfOrganization = isOnbehalfOfOrganization;
	}

	public String getTinNumber() {
		return tinNumber;
	}

	public void setTinNumber(final String tinNumber) {
		this.tinNumber = tinNumber;
	}

	public List<StakeHolderDocument> getStakeHolderDocument() {
		return stakeHolderDocument;
	}

	public Date getCoaEnrolmentDueDate() {
		return coaEnrolmentDueDate;
	}

	public void setCoaEnrolmentDueDate(final Date coaEnrolmentDueDate) {
		this.coaEnrolmentDueDate = coaEnrolmentDueDate;
	}

	public CorrespondenceAddress getCorrespondenceAddress() {
		return correspondenceAddress;
	}

	public void setCorrespondenceAddress(final CorrespondenceAddress correspondenceAddress) {
		this.correspondenceAddress = correspondenceAddress;
	}

	public PermanentAddress getPermanentAddress() {
		return permanentAddress;
	}

	public void setPermanentAddress(final PermanentAddress permanentAddress) {
		this.permanentAddress = permanentAddress;
	}

	public void addStakeHolderDocument(final StakeHolderDocument stakeHolderDocument) {
		stakeHolderDocument.setStakeHolder(this);
		getStakeHolderDocument().add(stakeHolderDocument);
	}

	public List<CheckListDetail> getCheckListDocuments() {
		return checkListDocuments;
	}

	public void setCheckListDocuments(final List<CheckListDetail> checkListDocuments) {
		this.checkListDocuments = checkListDocuments;
	}

	public String getActivationCode() {
		return activationCode;
	}

	public void setActivationCode(String activationCode) {
		this.activationCode = activationCode;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(Source source) {
		this.source = source;
	}

}