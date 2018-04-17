/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2017>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 *      Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *      For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *      For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */
package org.egov.bpa.transaction.entity;

import org.egov.bpa.master.entity.ServiceType;
import org.egov.bpa.transaction.entity.dto.BpaStateInfo;
import org.egov.bpa.transaction.entity.enums.ApplicantMode;
import org.egov.bpa.transaction.entity.enums.GovernmentType;
import org.egov.bpa.transaction.entity.enums.OneDayPermitLandType;
import org.egov.common.entity.Occupancy;
import org.egov.commons.entity.Source;
import org.egov.dcb.bean.Receipt;
import org.egov.demand.model.EgDemand;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.pims.commons.Position;
import org.hibernate.validator.constraints.Length;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "EGBPA_APPLICATION")
@SequenceGenerator(name = BpaApplication.SEQ_APPLICATION, sequenceName = BpaApplication.SEQ_APPLICATION, allocationSize = 1)
public class BpaApplication extends StateAware<Position> {

	public static final String SEQ_APPLICATION = "SEQ_EGBPA_APPLICATION";
	public static final String ORDER_BY_ID_ASC = "id ASC";
	private static final long serialVersionUID = -361205348191992865L;
	@Id
	@GeneratedValue(generator = SEQ_APPLICATION, strategy = GenerationType.SEQUENCE)
	private Long id;
	@Length(min = 1, max = 128)
	private String buildingplanapprovalnumber;
	@Temporal(value = TemporalType.DATE)
	private Date buildingPlanApprovalDate;
	@Length(min = 1, max = 128)
	private String applicationNumber;
	@NotNull
	@Temporal(value = TemporalType.DATE)
	private Date applicationDate;
	@Temporal(value = TemporalType.DATE)
	private Date approvalDate;
	@Length(min = 1, max = 128)
	private String assessmentNumber;
	@NotNull
	@Enumerated(EnumType.ORDINAL)
	private Source source;
	@Length(min = 1, max = 128)
	private String applicantType;
	// same as source
	@NotNull
	@Enumerated(EnumType.STRING)
	@Column(name = "applicantmode")
	private ApplicantMode applicantMode;
	@NotNull
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "serviceType")
	private ServiceType serviceType;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "status")
	private BpaStatus status;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	private Applicant owner;
	@Length(min = 1, max = 128)
	private String planPermissionNumber;
	@Temporal(value = TemporalType.DATE)
	private Date planPermissionDate;
	@Length(min = 1, max = 128)
	private String oldApplicationNumber;
	@Length(min = 1, max = 128)
	private String tapalNumber;
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "occupancy")
	private Occupancy occupancy;
	@Enumerated(EnumType.STRING)
	@Column(name = "governmentType")
	private GovernmentType governmentType;// Government or Quasi Govt
	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "demand")
	private EgDemand demand;
	@Length(min = 1, max = 128)
	private String remarks;
	@Length(min = 1, max = 128)
	private String projectName;
	@Length(min = 1, max = 128)
	private String groupDevelopment;
	private BigDecimal admissionfeeAmount;
	@Length(min = 1, max = 128)
	private String feeAmountRecieptNo;
	private BigDecimal approvedFeeAmount;
	private Date approvedReceiptDate;
	@Length(min = 1, max = 128)
	private String revisedApplicationNumber;
	@Length(min = 1, max = 128)
	private String revisedPermitNumber;
	private Boolean isExistingApprovedPlan = false;
	private boolean citizenAccepted;
	private boolean architectAccepted;
	private Boolean isEconomicallyWeakerSection;
	private String additionalRejectionReasons;
	private Boolean isSentToPreviousOwner = false;
	@Length(min = 1, max = 5000)
	private String townSurveyorRemarks;
	private Boolean isTownSurveyorInspectionRequire = false;
	private Boolean isRescheduledByCitizen = false;
	private Boolean isRescheduledByEmployee = false;
	private Boolean isOneDayPermitApplication = false;
	private Boolean isLPRequestInitiated;
	private Boolean failureInScheduler = false;
	private String schedulerFailedRemarks;

	@Enumerated(EnumType.STRING)
	@Column(name = "typeOfLand")
	private OneDayPermitLandType typeOfLand;// Garden Land or Wet Land
	@Length(min = 1, max = 20)
	private String eDcrNumber;

	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SiteDetail> siteDetail = new ArrayList<>(0);
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "egbpa_ApplicationAmenity", joinColumns = @JoinColumn(name = "application"), inverseJoinColumns = @JoinColumn(name = "amenityId"))
	private List<ServiceType> applicationAmenity = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BuildingDetail> buildingDetail = new ArrayList<>(0);
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "application")
	private List<DocumentHistory> documentHistory = new ArrayList<>();
	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "application")
	private List<PermittedFloorDetail> permittedFloorDetail = new ArrayList<>();
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<AutoDcrMap> autoDcr = new ArrayList<>();
	@OrderBy(ORDER_BY_ID_ASC)
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationDocument> applicationDocument = new ArrayList<>(0);
	@OrderBy(ORDER_BY_ID_ASC)
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationNocDocument> applicationNOCDocument = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@OrderBy("id DESC ")
	private List<Inspection> inspections = new ArrayList<>();
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<LettertoParty> lettertoParty = new ArrayList<>();
	@OrderBy(ORDER_BY_ID_ASC)
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationFee> applicationFee = new ArrayList<>();
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BpaDocumentScrutiny> documentScrutiny = new ArrayList<>();
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BpaAppointmentSchedule> appointmentSchedule = new ArrayList<>();
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationStakeHolder> stakeHolder = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ExistingBuildingDetail> existingBuildingDetails = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationPermitConditions> dynamicPermitConditions = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationPermitConditions> staticPermitConditions = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationPermitConditions> additionalPermitConditions = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<ApplicationPermitConditions> rejectionReasons = new ArrayList<>(0);
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<BpaNotice> bpaNotice = new ArrayList<>(0);
	@OrderBy(ORDER_BY_ID_ASC)
	@OneToMany(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<SlotApplication> slotApplications = new ArrayList<>();
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "egbpa_ts_inspn_documents", joinColumns = @JoinColumn(name = "application"), inverseJoinColumns = @JoinColumn(name = "fileStoreId"))
	private Set<FileStoreMapper> tsInspnSupportDocs = Collections.emptySet();

	private transient MultipartFile[] files;
	private transient Long approvalDepartment;
	private transient Long zoneId;
	private transient Long wardId;
	private transient String approvalComent;
	private transient Set<Receipt> receipts = new HashSet<>();
	private transient boolean mailPwdRequired;
	private transient List<ApplicationPermitConditions> dynamicPermitConditionsTemp = new ArrayList<>(0);
	private transient List<ApplicationPermitConditions> staticPermitConditionsTemp = new ArrayList<>(0);
	private transient List<ApplicationPermitConditions> rejectionReasonsTemp = new ArrayList<>(0);
	private transient List<ApplicationPermitConditions> additionalPermitConditionsTemp = new ArrayList<>(0);
	private transient List<ServiceType> applicationAmenityTemp = new ArrayList<>(0);

	@Override
	public Long getId() {
		return id;
	}

	@Override
	public void setId(final Long id) {
		this.id = id;
	}

	@Override
	public String myLinkId() {
		return applicationNumber == null ? planPermissionNumber : applicationNumber;
	}

	public String getAmenityName() {
		return applicationAmenity.stream().map(ServiceType::getDescription).collect(Collectors.joining(","));
	}

	public String getBuildingplanapprovalnumber() {
		return buildingplanapprovalnumber;
	}

	public void setBuildingplanapprovalnumber(final String buildingplanapprovalnumber) {
		this.buildingplanapprovalnumber = buildingplanapprovalnumber;
	}

	public String getApplicationNumber() {
		return applicationNumber;
	}

	public void setApplicationNumber(final String applicationNumber) {
		this.applicationNumber = applicationNumber;
	}

	public Date getApplicationDate() {
		return applicationDate;
	}

	public void setApplicationDate(final Date applicationDate) {
		this.applicationDate = applicationDate;
	}

	public String getAssessmentNumber() {
		return assessmentNumber;
	}

	public void setAssessmentNumber(final String assessmentNumber) {
		this.assessmentNumber = assessmentNumber;
	}

	public String getApplicantType() {
		return applicantType;
	}

	public void setApplicantType(final String applicantType) {
		this.applicantType = applicantType;
	}

	public ApplicantMode getApplicantMode() {
		return applicantMode;
	}

	public void setApplicantMode(final ApplicantMode applicantMode) {
		this.applicantMode = applicantMode;
	}

	public BpaStatus getStatus() {
		return status;
	}

	public void setStatus(final BpaStatus status) {
		this.status = status;
	}

	public String getPlanPermissionNumber() {
		return planPermissionNumber;
	}

	public void setPlanPermissionNumber(final String planPermissionNumber) {
		this.planPermissionNumber = planPermissionNumber;
	}

	public Date getPlanPermissionDate() {
		return planPermissionDate;
	}

	public void setPlanPermissionDate(final Date planPermissionDate) {
		this.planPermissionDate = planPermissionDate;
	}

	public String getOldApplicationNumber() {
		return oldApplicationNumber;
	}

	public void setOldApplicationNumber(final String oldApplicationNumber) {
		this.oldApplicationNumber = oldApplicationNumber;
	}

	public String getTapalNumber() {
		return tapalNumber;
	}

	public void setTapalNumber(final String tapalNumber) {
		this.tapalNumber = tapalNumber;
	}

	public MultipartFile[] getFiles() {
		return files;
	}

	public void setFiles(MultipartFile[] files) {
		this.files = files;
	}

	public Long getApprovalDepartment() {
		return approvalDepartment;
	}

	public void setApprovalDepartment(final Long approvalDepartment) {
		this.approvalDepartment = approvalDepartment;
	}

	public String getApprovalComent() {
		return approvalComent;
	}

	public void setApprovalComent(final String approvalComent) {
		this.approvalComent = approvalComent;
	}

	public Occupancy getOccupancy() {
		return occupancy;
	}

	public void setOccupancy(final Occupancy occupancy) {
		this.occupancy = occupancy;
	}

	public GovernmentType getGovernmentType() {
		return governmentType;
	}

	public void setGovernmentType(GovernmentType governmentType) {
		this.governmentType = governmentType;
	}

	public EgDemand getDemand() {
		return demand;
	}

	public void setDemand(final EgDemand demand) {
		this.demand = demand;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(final String remarks) {
		this.remarks = remarks;
	}

	public String getProjectName() {
		return projectName;
	}

	public void setProjectName(final String projectName) {
		this.projectName = projectName;
	}

	public String getGroupDevelopment() {
		return groupDevelopment;
	}

	public void setGroupDevelopment(final String groupDevelopment) {
		this.groupDevelopment = groupDevelopment;
	}

	public List<AutoDcrMap> getAutoDcr() {
		return autoDcr;
	}

	public void setAutoDcr(final List<AutoDcrMap> autoDcr) {
		this.autoDcr = autoDcr;
	}

	public List<Inspection> getInspections() {
		return inspections;
	}

	public void setInspections(final List<Inspection> inspections) {
		this.inspections = inspections;
	}

	public List<DocumentHistory> getDocumentHistory() {
		return documentHistory;
	}

	public void setDocumentHistory(final List<DocumentHistory> documentHistory) {
		this.documentHistory = documentHistory;
	}

	public List<PermittedFloorDetail> getPermittedFloorDetail() {
		return permittedFloorDetail;
	}

	public void setPermittedFloorDetail(final List<PermittedFloorDetail> permittedFloorDetail) {
		this.permittedFloorDetail = permittedFloorDetail;
	}

	public List<LettertoParty> getLettertoParty() {
		return lettertoParty;
	}

	public void setLettertoParty(final List<LettertoParty> lettertoParty) {
		this.lettertoParty = lettertoParty;
	}

	public List<ApplicationFee> getApplicationFee() {
		return applicationFee;
	}

	public void setApplicationFee(final List<ApplicationFee> applicationFee) {
		this.applicationFee = applicationFee;
	}

	public List<ApplicationStakeHolder> getStakeHolder() {
		return stakeHolder;
	}

	public void setStakeHolder(final List<ApplicationStakeHolder> stakeHolder) {
		this.stakeHolder = stakeHolder;
	}

	public Source getSource() {
		return source;
	}

	public void setSource(final Source source) {
		this.source = source;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	public void setServiceType(final ServiceType serviceType) {
		this.serviceType = serviceType;
	}

	public Applicant getOwner() {
		return owner;
	}

	public void setOwner(final Applicant owner) {
		this.owner = owner;
	}

	public List<SiteDetail> getSiteDetail() {
		return siteDetail;
	}

	public void setSiteDetail(final List<SiteDetail> siteDetail) {
		this.siteDetail = siteDetail;
	}

	public List<BuildingDetail> getBuildingDetail() {
		return buildingDetail;
	}

	public void setBuildingDetail(final List<BuildingDetail> buildingDetail) {
		this.buildingDetail = buildingDetail;
	}

	public List<ApplicationDocument> getApplicationDocument() {
		return applicationDocument;
	}

	public void setApplicationDocument(final List<ApplicationDocument> applicationDocument) {
		this.applicationDocument = applicationDocument;
	}

	public void addApplicationDocument(final ApplicationDocument nocDocument) {
		nocDocument.setApplication(this);
		getApplicationDocument().add(nocDocument);
	}

	public List<ApplicationNocDocument> getApplicationNOCDocument() {
		return applicationNOCDocument;
	}

	public void setApplicationNOCDocument(final List<ApplicationNocDocument> applicationNOCDocument) {
		this.applicationNOCDocument = applicationNOCDocument;
	}

	public void addApplicationNocDocument(final ApplicationNocDocument nocDocument) {
		nocDocument.setApplication(this);
		getApplicationNOCDocument().add(nocDocument);
	}

	public Date getBuildingPlanApprovalDate() {
		return buildingPlanApprovalDate;
	}

	public void setBuildingPlanApprovalDate(final Date buildingPlanApprovalDate) {
		this.buildingPlanApprovalDate = buildingPlanApprovalDate;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(final Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	@Override
	public String getStateDetails() {
		return String.format("Applicant Name: %s Application Number %s Dated %s For the service type - %s.",
				owner == null ? "Not Specified" : owner.getName(),
				applicationNumber == null ? planPermissionNumber : applicationNumber,
				applicationDate == null ? DateUtils.toDefaultDateFormat(new Date()) : DateUtils.toDefaultDateFormat(applicationDate),
				serviceType.getDescription() == null ? "" : serviceType.getDescription());
	}

	public BigDecimal getAdmissionfeeAmount() {
		return admissionfeeAmount;
	}

	public void setAdmissionfeeAmount(final BigDecimal admissionfeeAmount) {
		this.admissionfeeAmount = admissionfeeAmount;
	}

	public List<BpaDocumentScrutiny> getDocumentScrutiny() {
		return documentScrutiny;
	}

	public void setDocumentScrutiny(final List<BpaDocumentScrutiny> documentScrutiny) {
		this.documentScrutiny = documentScrutiny;
	}

	public List<BpaAppointmentSchedule> getAppointmentSchedule() {
		return appointmentSchedule;
	}

	public void setAppointmentSchedule(final List<BpaAppointmentSchedule> appointmentSchedule) {
		this.appointmentSchedule = appointmentSchedule;
	}

	public List<ExistingBuildingDetail> getExistingBuildingDetails() {
		return existingBuildingDetails;
	}

	public void setExistingBuildingDetails(List<ExistingBuildingDetail> existingBuildingDetails) {
		this.existingBuildingDetails = existingBuildingDetails;
	}

	public Long getZoneId() {
		return zoneId;
	}

	public void setZoneId(final Long zoneId) {
		this.zoneId = zoneId;
	}

	public Long getWardId() {
		return wardId;
	}

	public void setWardId(final Long wardId) {
		this.wardId = wardId;
	}

	public boolean isFeeCollected() {
		return demand == null ? false : demand.getBaseDemand().compareTo(demand.getAmtCollected()) <= 0;
	}

	public List<ServiceType> getApplicationAmenity() {
		return applicationAmenity;
	}

	public void setApplicationAmenity(List<ServiceType> applicationAmenity) {
		this.applicationAmenity = applicationAmenity;
	}

	public String getFeeAmountRecieptNo() {
		return feeAmountRecieptNo;
	}

	public void setFeeAmountRecieptNo(String feeAmountRecieptNo) {
		this.feeAmountRecieptNo = feeAmountRecieptNo;
	}

	public Date getApprovedReceiptDate() {
		return approvedReceiptDate;
	}

	public void setApprovedReceiptDate(Date approvedReceiptDate) {
		this.approvedReceiptDate = approvedReceiptDate;
	}

	public String getRevisedApplicationNumber() {
		return revisedApplicationNumber;
	}

	public void setRevisedApplicationNumber(String revisedApplicationNumber) {
		this.revisedApplicationNumber = revisedApplicationNumber;
	}

	public String getRevisedPermitNumber() {
		return revisedPermitNumber;
	}

	public void setRevisedPermitNumber(String revisedPermitNumber) {
		this.revisedPermitNumber = revisedPermitNumber;
	}

	public Boolean getIsExistingApprovedPlan() {
		return isExistingApprovedPlan;
	}

	public void setIsExistingApprovedPlan(Boolean isExistingApprovedPlan) {
		this.isExistingApprovedPlan = isExistingApprovedPlan;
	}

	public boolean isCitizenAccepted() {
		return citizenAccepted;
	}

	public void setCitizenAccepted(boolean citizenAccepted) {
		this.citizenAccepted = citizenAccepted;
	}

	public boolean isArchitectAccepted() {
		return architectAccepted;
	}

	public void setArchitectAccepted(boolean architectAccepted) {
		this.architectAccepted = architectAccepted;
	}

	public Set<Receipt> getReceipts() {
		return receipts;
	}

	public void setReceipts(Set<Receipt> receipts) {
		this.receipts = receipts;
	}

	public BigDecimal getApprovedFeeAmount() {
		return approvedFeeAmount;
	}

	public void setApprovedFeeAmount(BigDecimal approvedFeeAmount) {
		this.approvedFeeAmount = approvedFeeAmount;
	}

	public Boolean getIsEconomicallyWeakerSection() {
		return isEconomicallyWeakerSection;
	}

	public void setIsEconomicallyWeakerSection(Boolean isEconomicallyWeakerSection) {
		this.isEconomicallyWeakerSection = isEconomicallyWeakerSection;
	}

	public Boolean getTownSurveyorInspectionRequire() {
		return isTownSurveyorInspectionRequire;
	}

	public void setTownSurveyorInspectionRequire(Boolean townSurveyorInspectionRequire) {
		isTownSurveyorInspectionRequire = townSurveyorInspectionRequire;
	}

	public Boolean getIsRescheduledByCitizen() {
		return isRescheduledByCitizen;
	}

	public void setIsRescheduledByCitizen(Boolean isRescheduledByCitizen) {
		this.isRescheduledByCitizen = isRescheduledByCitizen;
	}

	public Boolean getIsRescheduledByEmployee() {
		return isRescheduledByEmployee;
	}

	public void setIsRescheduledByEmployee(Boolean isRescheduledByEmployee) {
		this.isRescheduledByEmployee = isRescheduledByEmployee;
	}

	public void deleteBuildingDetails(final BuildingDetail buildingDetail) {
		if (buildingDetail != null)
			this.buildingDetail.remove(buildingDetail);
	}

	public List<ApplicationPermitConditions> getDynamicPermitConditions() {
		return dynamicPermitConditions;
	}

	public void setDynamicPermitConditions(List<ApplicationPermitConditions> dynamicPermitConditions) {
		this.dynamicPermitConditions = dynamicPermitConditions;
	}

	public List<ApplicationPermitConditions> getStaticPermitConditions() {
		return staticPermitConditions;
	}

	public void setStaticPermitConditions(List<ApplicationPermitConditions> staticPermitConditions) {
		this.staticPermitConditions = staticPermitConditions;
	}

	public boolean isMailPwdRequired() {
		return mailPwdRequired;
	}

	public void setMailPwdRequired(boolean mailPwdRequired) {
		this.mailPwdRequired = mailPwdRequired;
	}

	public List<BpaNotice> getBpaNotice() {
		return bpaNotice;
	}

	public void setBpaNotice(List<BpaNotice> bpaNotice) {
		this.bpaNotice = bpaNotice;
	}

	public void addNotice(final BpaNotice bpaNotice) {
		getBpaNotice().add(bpaNotice);
	}

	public List<ApplicationPermitConditions> getRejectionReasons() {
		return rejectionReasons;
	}

	public void setRejectionReasons(List<ApplicationPermitConditions> rejectionReasons) {
		this.rejectionReasons = rejectionReasons;
	}

	public List<SlotApplication> getSlotApplications() {
		return slotApplications;
	}

	public void setSlotApplications(List<SlotApplication> slotApplications) {
		this.slotApplications = slotApplications;
	}

	public Set<FileStoreMapper> getTsInspnSupportDocs() {
		return this.tsInspnSupportDocs.stream()
									  .sorted(Comparator.comparing(FileStoreMapper::getId))
									  .collect(Collectors.toSet());
	}

	public void setTsInspnSupportDocs(Set<FileStoreMapper> tsInspnSupportDocs) {
		this.tsInspnSupportDocs = tsInspnSupportDocs;
	}

	public String getAdditionalRejectionReasons() {
		return additionalRejectionReasons;
	}

	public void setAdditionalRejectionReasons(String additionalRejectionReasons) {
		this.additionalRejectionReasons = additionalRejectionReasons;
	}

	public Boolean getSentToPreviousOwner() {
		return isSentToPreviousOwner;
	}

	public void setSentToPreviousOwner(Boolean sentToPreviousOwner) {
		isSentToPreviousOwner = sentToPreviousOwner;
	}

	public String getTownSurveyorRemarks() {
		return townSurveyorRemarks;
	}

	public void setTownSurveyorRemarks(String townSurveyorRemarks) {
		this.townSurveyorRemarks = townSurveyorRemarks;
	}

	public List<ApplicationPermitConditions> getAdditionalPermitConditions() {
		return additionalPermitConditions;
	}

	public void setAdditionalPermitConditions(List<ApplicationPermitConditions> additionalPermitConditions) {
		this.additionalPermitConditions = additionalPermitConditions;
	}

	public List<ApplicationPermitConditions> getDynamicPermitConditionsTemp() {
		return dynamicPermitConditionsTemp;
	}

	public void setDynamicPermitConditionsTemp(List<ApplicationPermitConditions> dynamicPermitConditionsTemp) {
		this.dynamicPermitConditionsTemp = dynamicPermitConditionsTemp;
	}

	public List<ApplicationPermitConditions> getStaticPermitConditionsTemp() {
		return staticPermitConditionsTemp;
	}

	public void setStaticPermitConditionsTemp(List<ApplicationPermitConditions> staticPermitConditionsTemp) {
		this.staticPermitConditionsTemp = staticPermitConditionsTemp;
	}

	public List<ApplicationPermitConditions> getRejectionReasonsTemp() {
		return rejectionReasonsTemp;
	}

	public void setRejectionReasonsTemp(List<ApplicationPermitConditions> rejectionReasonsTemp) {
		this.rejectionReasonsTemp = rejectionReasonsTemp;
	}

	public List<ApplicationPermitConditions> getAdditionalPermitConditionsTemp() {
		return additionalPermitConditionsTemp;
	}

	public void setAdditionalPermitConditionsTemp(List<ApplicationPermitConditions> additionalPermitConditionsTemp) {
		this.additionalPermitConditionsTemp = additionalPermitConditionsTemp;
	}

	public List<ServiceType> getApplicationAmenityTemp() {
		return applicationAmenityTemp;
	}

	public void setApplicationAmenityTemp(List<ServiceType> applicationAmenityTemp) {
		this.applicationAmenityTemp = applicationAmenityTemp;
	}

	public BpaStateInfo extraInfo() {
		return super.extraInfoAs(BpaStateInfo.class);
	}

	public Boolean getIsOneDayPermitApplication() {
		return isOneDayPermitApplication;
	}

	public void setIsOneDayPermitApplication(Boolean isOneDayPermitApplication) {
		this.isOneDayPermitApplication = isOneDayPermitApplication;
	}

	public Boolean getLPRequestInitiated() {
		return isLPRequestInitiated;
	}

	public void setLPRequestInitiated(Boolean LPRequestInitiated) {
		isLPRequestInitiated = LPRequestInitiated;
	}

	public OneDayPermitLandType getTypeOfLand() {
		return typeOfLand;
	}

	public void setTypeOfLand(OneDayPermitLandType typeOfLand) {
		this.typeOfLand = typeOfLand;
	}

	public Boolean getFailureInScheduler() {
		return failureInScheduler;
	}

	public void setFailureInScheduler(Boolean failureInScheduler) {
		this.failureInScheduler = failureInScheduler;
	}

	public String getSchedulerFailedRemarks() {
		return schedulerFailedRemarks;
	}

	public void setSchedulerFailedRemarks(String schedulerFailedRemarks) {
		this.schedulerFailedRemarks = schedulerFailedRemarks;
	}

	public String geteDcrNumber() {
		return eDcrNumber;
	}

	public void seteDcrNumber(String eDcrNumber) {
		this.eDcrNumber = eDcrNumber;
	}

}