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
package org.egov.bpa.transaction.service.notice;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.egov.bpa.master.entity.ServiceType;
import org.egov.bpa.transaction.entity.ApplicationPermitConditions;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaNotice;
import org.egov.bpa.transaction.entity.BuildingDetail;
import org.egov.bpa.transaction.entity.Response;
import org.egov.bpa.transaction.entity.dto.PermitFeeHelper;
import org.egov.bpa.transaction.entity.enums.PermitConditionType;
import org.egov.bpa.transaction.repository.BpaNoticeRepository;
import org.egov.bpa.transaction.service.ApplicationBpaService;
import org.egov.bpa.transaction.service.BpaApplicationPermitConditionsService;
import org.egov.bpa.transaction.workflow.BpaWorkFlowService;
import org.egov.bpa.utils.BpaUtils;
import org.egov.commons.Installment;
import org.egov.demand.model.EgDemandDetails;
import org.egov.infra.filestore.entity.FileStoreMapper;
import org.egov.infra.filestore.service.FileStoreService;
import org.egov.infra.reporting.engine.ReportFormat;
import org.egov.infra.reporting.engine.ReportOutput;
import org.egov.infra.reporting.engine.ReportRequest;
import org.egov.infra.reporting.engine.ReportService;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.pims.commons.Position;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_MODULE_TYPE;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_CANCELLED;
import static org.egov.bpa.utils.BpaConstants.BPADEMANDNOTICETITLE;
import static org.egov.bpa.utils.BpaConstants.BPAREJECTIONFILENAME;
import static org.egov.bpa.utils.BpaConstants.BPA_ADM_FEE;
import static org.egov.bpa.utils.BpaConstants.BPA_DEMAND_NOTICE_TYPE;
import static org.egov.bpa.utils.BpaConstants.BPA_REJECTION_NOTICE_TYPE;
import static org.egov.bpa.utils.BpaConstants.BUILDINGDEVELOPPERMITFILENAME;
import static org.egov.bpa.utils.BpaConstants.BUILDINGPERMITFILENAME;
import static org.egov.bpa.utils.BpaConstants.DEMANDNOCFILENAME;
import static org.egov.bpa.utils.BpaConstants.PERMIT_ORDER_NOTICE_TYPE;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_14;
import static org.egov.bpa.utils.BpaConstants.ST_CODE_15;
import static org.egov.bpa.utils.BpaConstants.getServicesForBuildPermit;
import static org.egov.bpa.utils.BpaConstants.getServicesForDevelopPermit;
import static org.egov.infra.security.utils.SecureCodeUtils.generatePDF417Code;
import static org.egov.infra.utils.DateUtils.currentDateToDefaultDateFormat;

@Service
@Transactional(readOnly = true)
public class BpaNoticeService {

	public static final String TWO_NEW_LINE = "\n\n";
	public static final String N_A = "N/A";
	public static final String ONE_NEW_LINE = "\n";
	private static final String APPLICATION_PDF = "application/pdf";
	private static final String APPLICATION_REJECTION_REASON = "applctn.reject.reason";
	private static final String APPLICATION_AUTO_REJECTION_REASON = "applctn.auto.reject.reason";
	@Autowired
	@Qualifier("fileStoreService")
	protected FileStoreService fileStoreService;
	@Autowired
	private ReportService reportService;
	@Autowired
	private BpaUtils bpaUtils;
	@Autowired
	@Qualifier("parentMessageSource")
	private MessageSource bpaMessageSource;
	@Autowired
	private BpaNoticeRepository bpaNoticeRepository;
	@Autowired
	private ApplicationBpaService applicationBpaService;
	@Autowired
	private BpaApplicationPermitConditionsService bpaApplicationPermitConditionsService;
	@Autowired
	private BpaWorkFlowService bpaWorkFlowService;

	public BpaNotice findByApplicationAndNoticeType(final BpaApplication application, final String noticeType) {
		return bpaNoticeRepository.findByApplicationAndNoticeType(application, noticeType);
	}

	public ReportOutput generateDemandNotice(final BpaApplication bpaApplication,
											 final Map<String, Object> ulbDetailsReportParams)
			throws IOException {
		String fileName = "bpa_demand_notice_" + bpaApplication.getApplicationNumber();
		BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, BPA_DEMAND_NOTICE_TYPE);
		ReportOutput reportOutput = getReportOutput(bpaApplication, ulbDetailsReportParams, fileName, bpaNotice, DEMANDNOCFILENAME, BPA_DEMAND_NOTICE_TYPE);
		reportOutput.setReportFormat(ReportFormat.PDF);
		return reportOutput;
	}

	private Map<String, Object> buildParametersForDemandDetails(final BpaApplication bpaApplication) {
		List<Response> demandResponseList = new ArrayList<>();
		BigDecimal totalPendingAmt = BigDecimal.ZERO;
		final Map<String, Object> reportParams = new HashMap<>();
		Installment currentInstallemnt = bpaApplication.getDemand().getEgInstallmentMaster();
		for (final EgDemandDetails demandDtl : bpaApplication.getDemand().getEgDemandDetails()) {
			Response response = new Response();
			if (!BPA_ADM_FEE.equalsIgnoreCase(demandDtl.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster())
				&& demandDtl.getBalance().compareTo(BigDecimal.ZERO) > 0) {
				response.setDemandDescription(
						demandDtl.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster());
				response.setDemandAmount(demandDtl.getBalance().setScale(2, BigDecimal.ROUND_HALF_EVEN));
				totalPendingAmt = totalPendingAmt.add(demandDtl.getBalance());
				demandResponseList.add(response);
			}
		}
		reportParams.put("installmentDesc",
				currentInstallemnt.getDescription() == null ? EMPTY : currentInstallemnt.getDescription());
		reportParams.put("demandResponseList", demandResponseList);
		reportParams.put("totalPendingAmt", totalPendingAmt.setScale(2, BigDecimal.ROUND_HALF_EVEN));
		return reportParams;
	}

	public ReportOutput generatePermitOrder(final BpaApplication bpaApplication, final Map<String, Object> ulbDetailsReportParams)
			throws IOException {
		ReportOutput reportOutput = new ReportOutput();
		ReportRequest reportInput = null;
		BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, PERMIT_ORDER_NOTICE_TYPE);
		if (bpaNotice != null && bpaNotice.getNoticeFileStore() == null) {
			String reportFileName;
			if (getServicesForBuildPermit().contains(bpaApplication.getServiceType().getCode())) {
				reportFileName = BUILDINGPERMITFILENAME;
			} else if (getServicesForDevelopPermit().contains(bpaApplication.getServiceType().getCode())) {
				reportFileName = BUILDINGDEVELOPPERMITFILENAME;
			} else {
				reportFileName = BUILDINGPERMITFILENAME;
			}
			final Map<String, Object> reportParams = buildParametersForReport(bpaApplication);
			reportParams.putAll(ulbDetailsReportParams);
			reportInput = new ReportRequest(reportFileName, !bpaApplication.getBuildingDetail().isEmpty()
															? bpaApplication.getBuildingDetail().get(0) : new BuildingDetail(), reportParams);
			reportOutput = reportService.createReport(reportInput);
			saveBpaNotices(bpaApplication, reportOutput, bpaApplication.getPlanPermissionNumber(),
					PERMIT_ORDER_NOTICE_TYPE);
		} else {
			final FileStoreMapper fmp = bpaNotice.getNoticeFileStore();
			final File file = fileStoreService.fetch(fmp, APPLICATION_MODULE_TYPE);
			reportOutput.setReportOutputData(FileUtils.readFileToByteArray(file));
		}
		reportOutput.setReportFormat(ReportFormat.PDF);
		return reportOutput;
	}

	public ReportOutput generateRejectionNotice(final BpaApplication bpaApplication,
												final Map<String, Object> ulbDetailsReportParams)
			throws IOException {
		String fileName = "bpa_rejection_notice_" + bpaApplication.getApplicationNumber();
		BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, BPA_REJECTION_NOTICE_TYPE);
		ReportOutput reportOutput = getReportOutput(bpaApplication, ulbDetailsReportParams, fileName, bpaNotice, BPAREJECTIONFILENAME, BPA_REJECTION_NOTICE_TYPE);
		reportOutput.setReportFormat(ReportFormat.PDF);
		return reportOutput;
	}

	private ReportOutput getReportOutput(BpaApplication bpaApplication, Map<String, Object> ulbDetailsReportParams, String fileName, BpaNotice bpaNotice, String bparejectionfilename, String bpaRejectionNoticeType) throws IOException {
		ReportOutput reportOutput = new ReportOutput();
		if (bpaNotice == null || bpaNotice.getNoticeFileStore() == null) {
			final Map<String, Object> reportParams = buildParametersForReport(bpaApplication);
			reportParams.putAll(ulbDetailsReportParams);
			reportParams.putAll(buildParametersForDemandDetails(bpaApplication));
			ReportRequest reportInput = new ReportRequest(bparejectionfilename, bpaApplication, reportParams);
			reportOutput = reportService.createReport(reportInput);
			saveBpaNotices(bpaApplication, reportOutput, fileName, bpaRejectionNoticeType);
		} else {
			final FileStoreMapper fmp = bpaNotice.getNoticeFileStore();
			final File file = fileStoreService.fetch(fmp, APPLICATION_MODULE_TYPE);
			reportOutput.setReportOutputData(FileUtils.readFileToByteArray(file));
		}
		return reportOutput;
	}

	private BpaNotice saveBpaNotices(final BpaApplication application, ReportOutput reportOutput, String fileName,
									 String noticeType) {
		BpaNotice bpaNotice = new BpaNotice();
		bpaNotice.setApplication(application);
		bpaNotice.setNoticeFileStore(
				fileStoreService.store(new ByteArrayInputStream(reportOutput.getReportOutputData()), fileName, APPLICATION_PDF,
						APPLICATION_MODULE_TYPE));
		bpaNotice.setNoticeGeneratedDate(new Date());
		bpaNotice.setNoticeType(noticeType);
		application.addNotice(bpaNotice);
		applicationBpaService.saveBpaApplication(application);
		return bpaNotice;
	}

	private Map<String, Object> buildParametersForReport(final BpaApplication bpaApplication) {
		StringBuilder serviceTypeDesc = new StringBuilder();
		final Map<String, Object> reportParams = new HashMap<>();
		reportParams.put("bpademandtitle", WordUtils.capitalize(BPADEMANDNOTICETITLE));
		reportParams.put("currentDate", currentDateToDefaultDateFormat());
		reportParams.put("lawAct", "[See Rule 11 (3)]");
		reportParams.put("applicationNumber", bpaApplication.getApplicationNumber());
		reportParams.put("buildingPermitNumber",
				bpaApplication.getPlanPermissionNumber() == null ? EMPTY : bpaApplication.getPlanPermissionNumber());
		reportParams.put("applicantName", bpaApplication.getOwner().getName());
		reportParams.put("applicantAddress",
				bpaApplication.getOwner() == null ? "Not Mentioned" : bpaApplication.getOwner().getAddress());
		reportParams.put("applicationDate", DateUtils.getDefaultFormattedDate(bpaApplication.getApplicationDate()));
		if (APPLICATION_STATUS_CANCELLED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
			reportParams.put("rejectionReasons", buildRejectionReasons(bpaApplication));
		} else {
			reportParams.put("permitConditions", buildPermitConditions(bpaApplication));
		}
		reportParams.put("additionalNotes", getBuildingCommonPermitNotes());
		String amenities = bpaApplication.getApplicationAmenity().stream().map(ServiceType::getDescription)
										 .collect(Collectors.joining(", "));
		if (bpaApplication.getApplicationAmenity().isEmpty()) {
			serviceTypeDesc.append(bpaApplication.getServiceType().getDescription());
		} else {
			serviceTypeDesc.append(bpaApplication.getServiceType().getDescription()).append(" with amenities ")
						   .append(amenities);
		}
		reportParams.put("serviceTypeDesc", serviceTypeDesc.toString());
		reportParams.put("serviceTypeForDmd", bpaApplication.getServiceType().getDescription());
		reportParams.put("amenities", amenities == null ? EMPTY : amenities);
		reportParams.put("occupancy", bpaApplication.getOccupancy());
		if (!bpaApplication.getSiteDetail().isEmpty()) {
			reportParams.put("electionWard", bpaApplication.getSiteDetail().get(0).getElectionBoundary().getName());
			reportParams.put("revenueWard", bpaApplication.getSiteDetail().get(0).getAdminBoundary());
			reportParams.put("landExtent", bpaApplication.getSiteDetail().get(0).getExtentinsqmts());
			reportParams.put("buildingNo", bpaApplication.getSiteDetail().get(0).getPlotnumber() == null
										   ? EMPTY : bpaApplication.getSiteDetail().get(0).getPlotnumber());
			reportParams.put("nearestBuildingNo",
					bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() == null
					? EMPTY : bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber());
			reportParams.put("surveyNo", bpaApplication.getSiteDetail().get(0).getReSurveyNumber() == null
										 ? EMPTY : bpaApplication.getSiteDetail().get(0).getReSurveyNumber());
			reportParams.put("village", bpaApplication.getSiteDetail().get(0).getLocationBoundary() == null
										? EMPTY : bpaApplication.getSiteDetail().get(0).getLocationBoundary().getName());
			reportParams.put("taluk", bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() == null
									  ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk());
			reportParams.put("district", bpaApplication.getSiteDetail().get(0).getPostalAddress() == null
										 ? EMPTY : bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict());
		}
		reportParams.put("certificateValidity",
				getValidityDescription(bpaApplication.getServiceType().getCode(), bpaApplication.getPlanPermissionDate()));
		reportParams.put("isBusinessUser", bpaUtils.logedInuseCitizenOrBusinessUser());
		reportParams.put("designation", getApproverDesignation(bpaWorkFlowService.getAmountRuleByServiceType(bpaApplication).intValue()));
		reportParams.put("qrCode", generatePDF417Code(buildQRCodeDetails(bpaApplication)));
		if (bpaApplication.getIsOneDayPermitApplication())
			reportParams.put("permitOrderTitle", "ONE DAY BUILDING PERMIT");
		else
			reportParams.put("permitOrderTitle", "BUILDING PERMIT");
		if (!bpaApplication.getApplicationFee().isEmpty())
			reportParams.put("permitFeeDetails", getPermitFeeDetails(bpaApplication));

		return reportParams;
	}

	private List<PermitFeeHelper> getPermitFeeDetails(final BpaApplication application) {
		List<PermitFeeHelper> permitFeeDetails = new ArrayList<>();
		for (EgDemandDetails demandDetails : application.getDemand().getEgDemandDetails()) {
			if (demandDetails.getAmount().compareTo(BigDecimal.ZERO) > 0) {
				PermitFeeHelper feeHelper = new PermitFeeHelper();
				feeHelper.setFeeDescription(demandDetails.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster());
				feeHelper.setAmount(demandDetails.getAmount());
				permitFeeDetails.add(feeHelper);
			}
		}
		return permitFeeDetails;
	}

	private String buildQRCodeDetails(final BpaApplication bpaApplication) {
		StringBuilder qrCodeValue = new StringBuilder();
		qrCodeValue = StringUtils.isBlank(bpaApplication.getPlanPermissionNumber()) ? qrCodeValue.append("Permit number : ").append(N_A).append(ONE_NEW_LINE) : qrCodeValue.append("Permit number : ").append(bpaApplication.getPlanPermissionNumber()).append(ONE_NEW_LINE);
		qrCodeValue = bpaWorkFlowService.getAmountRuleByServiceType(bpaApplication) == null ? qrCodeValue.append("Approved by : ").append(N_A).append(ONE_NEW_LINE) : qrCodeValue.append("Approved by : ").append(getApproverDesignation(bpaWorkFlowService.getAmountRuleByServiceType(bpaApplication).intValue())).append(ONE_NEW_LINE);
		qrCodeValue = bpaApplication.getPlanPermissionDate() == null ? qrCodeValue.append("Date of issue of permit : ").append(N_A).append(ONE_NEW_LINE) : qrCodeValue.append("Date of issue of permit : ").append(DateUtils.getDefaultFormattedDate(bpaApplication.getPlanPermissionDate())).append(ONE_NEW_LINE);
		qrCodeValue = StringUtils.isBlank(getApproverName(bpaApplication)) ? qrCodeValue.append("Name of approver : ").append(N_A).append(ONE_NEW_LINE) : qrCodeValue.append("Name of approver : ").append(getApproverName(bpaApplication)).append(ONE_NEW_LINE);
		return qrCodeValue.toString();
	}

	private String getValidityDescription(final String serviceTypeCode, final Date planPermissionDate) {
		StringBuilder certificateValidatiy = new StringBuilder();
		String validityExpiryDate;
		if (serviceTypeCode.equals(ST_CODE_14) || serviceTypeCode.equals(ST_CODE_15)) {
			validityExpiryDate = calculateCertExpryDate(new DateTime(planPermissionDate),
					getMessageFromPropertyFile("tower.pole.certificate.expiry"));
		} else {
			validityExpiryDate = calculateCertExpryDate(new DateTime(planPermissionDate),
					getMessageFromPropertyFile("common.services.certificate.expiry"));
		}
		certificateValidatiy.append("\n\nValidity : This certificate is valid upto ").append(validityExpiryDate).append(" Only.");
		return certificateValidatiy.toString();
	}

	private String getBuildingCommonPermitNotes() {
		StringBuilder permitNotes = new StringBuilder();
		permitNotes.append(getMessageFromPropertyFile("build.permit.note1"))
				   .append(getMessageFromPropertyFile("build.permit.note2")).append(getMessageFromPropertyFile("build.permit.note3"))
				   .append(getMessageFromPropertyFile("build.permit.note4"));
		return permitNotes.toString();
	}

	private String buildPermitConditions(final BpaApplication bpaApplication) {

		StringBuilder permitConditions = new StringBuilder();
		List<ApplicationPermitConditions> additionalPermitConditions = bpaApplicationPermitConditionsService
				.findAllByApplicationAndPermitConditionType(bpaApplication, PermitConditionType.ADDITIONAL_PERMITCONDITION);
		if (bpaApplication.getPlanPermissionDate() != null && bpaApplication.getServiceType().getCode().equals(ST_CODE_14)
			|| bpaApplication.getServiceType().getCode().equals(ST_CODE_15)) {
			permitConditions.append(getMessageFromPropertyFile("tower.pole.permit.condition1"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition2"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition3"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition4"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition5"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition6"))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition7"))
							.append(getMessageFromPropertyFileWithParameters("tower.pole.permit.condition8",
									bpaApplication.getOwner().getName()))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition9"))
							.append(getMessageFromPropertyFileWithParameters("tower.pole.permit.condition10",
									DateUtils.getDefaultFormattedDate(bpaApplication.getPlanPermissionDate()).toString()))
							.append(getMessageFromPropertyFile("tower.pole.permit.condition11"));
			int order = 12;
			buildAdditionalPermitConditionsOrRejectionReason(permitConditions, additionalPermitConditions, order);
		} else {
			int order = buildApplicationPermitConditions(bpaApplication, permitConditions);
			buildAdditionalPermitConditionsOrRejectionReason(permitConditions, additionalPermitConditions, order);
		}
		return permitConditions.toString();
	}

	private int buildAdditionalPermitConditionsOrRejectionReason(StringBuilder permitConditions,
																 List<ApplicationPermitConditions> additionalPermitConditions, int order) {
		int additionalOrder = order;
		if (!additionalPermitConditions.isEmpty()
			&& StringUtils.isNotBlank(additionalPermitConditions.get(0).getAdditionalPermitCondition())) {
			for (ApplicationPermitConditions addnlPermitConditions : additionalPermitConditions) {
				if (StringUtils.isNotBlank(addnlPermitConditions.getAdditionalPermitCondition())) {
					permitConditions.append(
							String.valueOf(additionalOrder) + ") " + addnlPermitConditions.getAdditionalPermitCondition() + TWO_NEW_LINE);
					additionalOrder++;
				}
			}
		}
		return additionalOrder;
	}

	private String buildRejectionReasons(final BpaApplication bpaApplication) {
		StringBuilder rejectReasons = new StringBuilder();
		if (!bpaApplication.getRejectionReasons().isEmpty()) {
			List<ApplicationPermitConditions> additionalPermitConditions = bpaApplicationPermitConditionsService
					.findAllByApplicationAndPermitConditionType(bpaApplication, PermitConditionType.ADDITIONAL_PERMITCONDITION);
			int order = buildPredefinedRejectReasons(bpaApplication, rejectReasons);
			int additionalOrder = buildAdditionalPermitConditionsOrRejectionReason(rejectReasons, additionalPermitConditions, order);
			StateHistory<Position> stateHistory = bpaUtils.getRejectionComments(bpaApplication);
			rejectReasons.append(String.valueOf(additionalOrder) + ") " + (stateHistory != null && StringUtils.isNotBlank(stateHistory.getComments()) ? stateHistory.getComments() : EMPTY) + TWO_NEW_LINE);
		} else {
			rejectReasons.append(bpaApplication.getState().getComments().equalsIgnoreCase("Application cancelled by citizen") ? getMessageFromPropertyFile(APPLICATION_REJECTION_REASON) : getMessageFromPropertyFile(APPLICATION_AUTO_REJECTION_REASON));
		}
		return rejectReasons.toString();
	}

	private int buildPredefinedRejectReasons(final BpaApplication bpaApplication,
											 StringBuilder permitConditions) {
		int order = 1;
		for (ApplicationPermitConditions rejectReason : bpaApplication.getRejectionReasons()) {
			if (rejectReason.isRequired()
				&& PermitConditionType.REJECTION_REASON.equals(rejectReason.getPermitConditionType())) {
				permitConditions
						.append(String.valueOf(order) + ") " + rejectReason.getPermitCondition().getDescription() + TWO_NEW_LINE);
				order++;
			}
		}
		return order;
	}

	private int buildApplicationPermitConditions(final BpaApplication bpaApplication,
												 StringBuilder permitConditions) {
		int order = 1;
		for (ApplicationPermitConditions applnPermit : bpaApplication.getDynamicPermitConditions()) {
			if (applnPermit.isRequired()
				&& PermitConditionType.DYNAMIC_PERMITCONDITION.equals(applnPermit.getPermitConditionType())) {
				permitConditions
						.append(String.valueOf(order) + ") " + applnPermit.getPermitCondition().getDescription()
								+ applnPermit.getPermitConditionNumber() + " Dtd "
								+ DateUtils.toDefaultDateFormat(applnPermit.getPermitConditiondDate()) + "." + TWO_NEW_LINE);
				order++;
			}
		}

		for (ApplicationPermitConditions applnPermit : bpaApplication.getDynamicPermitConditions()) {
			if (applnPermit.isRequired()
				&& PermitConditionType.STATIC_PERMITCONDITION.equals(applnPermit.getPermitConditionType())) {
				permitConditions
						.append(String.valueOf(order) + ") " + applnPermit.getPermitCondition().getDescription() + TWO_NEW_LINE);
				order++;
			}
		}
		return order;
	}

	private String getMessageFromPropertyFile(String key) {
		return bpaMessageSource.getMessage(key, null, null);
	}

	private String getMessageFromPropertyFileWithParameters(String key, String value) {
		return bpaMessageSource.getMessage(key, new String[]{value}, null);
	}

	private String calculateCertExpryDate(DateTime permissionDate, String noOfYears) {
		DateTimeFormatter fmt = DateUtils.defaultDateFormatter();
		return fmt.print(permissionDate.plusYears(Integer.valueOf(noOfYears)));
	}

	private String getApproverDesignation(final Integer amountRule) {
		String designation = EMPTY;
		if (amountRule >= 0 && amountRule <= 300) {
			designation = "Assistant Engineer";
		} else if (amountRule > 300 && amountRule <= 750) {
			designation = "Assistant Executive Engineer";
		} else if (amountRule > 750 && amountRule <= 1500) {
			designation = "Executive Engineer";
		} else if (amountRule > 1500 && amountRule <= 2500) {
			designation = "Corporation Engineer";
		} else if (amountRule > 2500 && amountRule <= 1000000) {
			designation = "Secretary";
		}
		return designation;
	}

	private String getApproverName(final BpaApplication application) {
		StateHistory<Position> stateHistory = application.getStateHistory().stream()
														 .filter(history -> history.getOwnerPosition().getDeptDesig().getDesignation().getName().equalsIgnoreCase(getApproverDesignation(bpaWorkFlowService.getAmountRuleByServiceType(application).intValue())))
														 .findAny().orElse(null);
		return stateHistory == null ? N_A : bpaWorkFlowService.getApproverAssignment(stateHistory.getOwnerPosition()).getEmployee().getName();
	}
}
