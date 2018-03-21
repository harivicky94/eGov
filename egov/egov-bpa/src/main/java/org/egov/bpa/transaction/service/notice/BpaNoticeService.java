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
import org.egov.bpa.transaction.entity.*;
import org.egov.bpa.transaction.entity.dto.PermitFeeHelper;
import org.egov.bpa.transaction.entity.enums.PermitConditionType;
import org.egov.bpa.transaction.repository.BpaNoticeRepository;
import org.egov.bpa.transaction.service.ApplicationBpaService;
import org.egov.bpa.transaction.service.BpaApplicationPermitConditionsService;
import org.egov.bpa.transaction.workflow.BpaWorkFlowService;
import org.egov.bpa.utils.BpaConstants;
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
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.egov.bpa.utils.BpaConstants.*;
import static org.egov.infra.security.utils.SecureCodeUtils.generatePDF417Code;
import static org.egov.infra.utils.DateUtils.currentDateToDefaultDateFormat;

@Service
@Transactional(readOnly = true)
public class BpaNoticeService {

    private static final String APPLICATION_PDF = "application/pdf";
    private static final String APPLICATION_REJECTION_REASON = "applctn.reject.reason";
    private static final String APPLICATION_AUTO_REJECTION_REASON = "applctn.auto.reject.reason";

    @Autowired
    private ReportService reportService;
    @Autowired
    @Qualifier("fileStoreService")
    protected FileStoreService fileStoreService;
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
        ReportRequest reportInput = null;
        ReportOutput reportOutput = new ReportOutput();
        String fileName = "bpa_demand_notice_" + bpaApplication.getApplicationNumber();
        BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, BPA_DEMAND_NOTICE_TYPE);
        if (bpaNotice != null && bpaNotice.getNoticeFileStore() != null) {
            final FileStoreMapper fmp = bpaNotice.getNoticeFileStore();
            final File file = fileStoreService.fetch(fmp, APPLICATION_MODULE_TYPE);
            reportOutput.setReportOutputData(FileUtils.readFileToByteArray(file));
        } else {
            final Map<String, Object> reportParams = buildParametersForReport(bpaApplication);
            reportParams.putAll(ulbDetailsReportParams);
            reportParams.putAll(buildParametersForDemandDetails(bpaApplication));
            reportInput = new ReportRequest(DEMANDNOCFILENAME, bpaApplication, reportParams);
            reportOutput = reportService.createReport(reportInput);
            saveBpaNotices(bpaApplication, reportOutput, fileName, BPA_DEMAND_NOTICE_TYPE);
        }
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
                currentInstallemnt.getDescription() != null ? currentInstallemnt.getDescription() : "");
        reportParams.put("demandResponseList", demandResponseList);
        reportParams.put("totalPendingAmt", totalPendingAmt.setScale(2, BigDecimal.ROUND_HALF_EVEN));
        return reportParams;
    }

    public ReportOutput generatePermitOrder(final BpaApplication bpaApplication, final Map<String, Object> ulbDetailsReportParams)
            throws IOException {
        ReportOutput reportOutput = new ReportOutput();
        ReportRequest reportInput = null;
        BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, PERMIT_ORDER_NOTICE_TYPE);
        if (bpaNotice != null && bpaNotice.getNoticeFileStore() != null) {
            final FileStoreMapper fmp = bpaNotice.getNoticeFileStore();
            final File file = fileStoreService.fetch(fmp, APPLICATION_MODULE_TYPE);
            reportOutput.setReportOutputData(FileUtils.readFileToByteArray(file));
        } else {
            String reportFileName;
            if (BpaConstants.getServicesForBuildPermit().contains(bpaApplication.getServiceType().getCode())) {
                reportFileName = BUILDINGPERMITFILENAME;
            } else if (BpaConstants.getServicesForDevelopPermit().contains(bpaApplication.getServiceType().getCode())) {
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
        }
        reportOutput.setReportFormat(ReportFormat.PDF);
        return reportOutput;
    }

    public ReportOutput generateRejectionNotice(final BpaApplication bpaApplication,
            final Map<String, Object> ulbDetailsReportParams)
            throws IOException {
        ReportRequest reportInput = null;
        ReportOutput reportOutput = new ReportOutput();
        String fileName = "bpa_rejection_notice_" + bpaApplication.getApplicationNumber();
        BpaNotice bpaNotice = findByApplicationAndNoticeType(bpaApplication, BPA_REJECTION_NOTICE_TYPE);
        if (bpaNotice != null && bpaNotice.getNoticeFileStore() != null) {
            final FileStoreMapper fmp = bpaNotice.getNoticeFileStore();
            final File file = fileStoreService.fetch(fmp, APPLICATION_MODULE_TYPE);
            reportOutput.setReportOutputData(FileUtils.readFileToByteArray(file));
        } else {
            final Map<String, Object> reportParams = buildParametersForReport(bpaApplication);
            reportParams.putAll(ulbDetailsReportParams);
            reportParams.putAll(buildParametersForDemandDetails(bpaApplication));
            reportInput = new ReportRequest(BPAREJECTIONFILENAME, bpaApplication, reportParams);
            reportOutput = reportService.createReport(reportInput);
            saveBpaNotices(bpaApplication, reportOutput, fileName, BPA_REJECTION_NOTICE_TYPE);
        }
        reportOutput.setReportFormat(ReportFormat.PDF);
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
        applicationBpaService.saveAndFlushApplication(application);
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
                bpaApplication.getPlanPermissionNumber() != null ? bpaApplication.getPlanPermissionNumber() : "");
        reportParams.put("applicantName", bpaApplication.getOwner().getUser().getName());
        reportParams.put("applicantAddress",
                bpaApplication.getOwner() != null && !bpaApplication.getOwner().getUser().getAddress().isEmpty()
                        ? bpaApplication.getOwner().getUser().getAddress().get(0).getStreetRoadLine() : "");
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
        reportParams.put("amenities", amenities != null ? amenities : "");
        reportParams.put("occupancy",
                bpaApplication.getOccupancy() != null ? bpaApplication.getOccupancy().getDescription() : "");
        reportParams.put("electionWard", bpaApplication.getSiteDetail().get(0).getElectionBoundary().getName());
        reportParams.put("revenueWard", bpaApplication.getSiteDetail().get(0).getAdminBoundary() != null
                ? bpaApplication.getSiteDetail().get(0).getAdminBoundary().getName() : "");
        if (!bpaApplication.getSiteDetail().isEmpty()) {
            reportParams.put("landExtent", bpaApplication.getSiteDetail().get(0).getExtentinsqmts());
            reportParams.put("buildingNo", bpaApplication.getSiteDetail().get(0).getPlotnumber() != null
                    ? bpaApplication.getSiteDetail().get(0).getPlotnumber() : "");
            reportParams.put("nearestBuildingNo",
                    bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() != null
                            ? bpaApplication.getSiteDetail().get(0).getNearestbuildingnumber() : "");
            reportParams.put("surveyNo", bpaApplication.getSiteDetail().get(0).getReSurveyNumber() != null
                    ? bpaApplication.getSiteDetail().get(0).getReSurveyNumber() : "");
            reportParams.put("village", bpaApplication.getSiteDetail().get(0).getLocationBoundary() != null
                    ? bpaApplication.getSiteDetail().get(0).getLocationBoundary().getName() : "");
            reportParams.put("taluk", bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getTaluk() : "");
            reportParams.put("district", bpaApplication.getSiteDetail().get(0).getPostalAddress() != null
                    ? bpaApplication.getSiteDetail().get(0).getPostalAddress().getDistrict() : "");
        }
        reportParams.put("certificateValidity",
                getValidityDescription(bpaApplication.getServiceType().getCode(), bpaApplication.getPlanPermissionDate()));
        reportParams.put("isBusinessUser", bpaUtils.logedInuseCitizenOrBusinessUser());
        reportParams.put("designation", getApproverDesignation(getAmountRuleByServiceType(bpaApplication)));
        reportParams.put("qrCode", generatePDF417Code(buildQRCodeDetails(bpaApplication)));
        if(bpaApplication.getIsOneDayPermitApplication())
            reportParams.put("permitOrderTitle", "ONE DAY BUILDING PERMIT");
        else
            reportParams.put("permitOrderTitle", "BUILDING PERMIT");
        if(!bpaApplication.getApplicationFee().isEmpty())
            reportParams.put("permitFeeDetails", getPermitFeeDetails(bpaApplication));

        return reportParams;
    }

    private List<PermitFeeHelper> getPermitFeeDetails(final BpaApplication application) {
        List<PermitFeeHelper> permitFeeDetails = new ArrayList<>();
        if(!application.getApplicationFee().get(0).getApplicationFeeDetail().isEmpty()) {
            for(EgDemandDetails demandDetails : application.getDemand().getEgDemandDetails()) {
                if(demandDetails.getAmount().compareTo(BigDecimal.ZERO) > 0) {
                    PermitFeeHelper feeHelper = new PermitFeeHelper();
                    feeHelper.setFeeDescription(demandDetails.getEgDemandReason().getEgDemandReasonMaster().getReasonMaster());
                    feeHelper.setAmount(demandDetails.getAmount());
                    permitFeeDetails.add(feeHelper);
                }
            }
        }
        return permitFeeDetails;
    }

    private String buildQRCodeDetails(final BpaApplication bpaApplication) {
    	StringBuilder qrCodeValue = new StringBuilder();
    	qrCodeValue = !StringUtils.isEmpty(bpaApplication.getPlanPermissionNumber()) ? qrCodeValue.append("Permit number : ").append(bpaApplication.getPlanPermissionNumber()).append("\n") : qrCodeValue.append("Permit number : ").append("N/A").append("\n");
    	qrCodeValue = getAmountRuleByServiceType(bpaApplication) != null ? qrCodeValue.append("Approved by : ").append(getApproverDesignation(getAmountRuleByServiceType(bpaApplication))).append("\n") : qrCodeValue.append("Approved by : ").append("N/A").append("\n");
    	qrCodeValue = bpaApplication.getPlanPermissionDate() != null ? qrCodeValue.append("Date of issue of permit : ").append(bpaApplication.getPlanPermissionDate()).append("\n") : qrCodeValue.append("Date of issue of permit : ").append("N/A").append("\n");
    	qrCodeValue = !StringUtils.isEmpty(getApproverName(bpaApplication)) ? qrCodeValue.append("Name of approver : ").append(getApproverName(bpaApplication)).append("\n") : qrCodeValue.append("Name of approver : ").append("N/A").append("\n");
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
                            bpaApplication.getOwner().getUser().getName()))
                    .append(getMessageFromPropertyFile("tower.pole.permit.condition9"))
                    .append(getMessageFromPropertyFileWithParameters("tower.pole.permit.condition10",
                            bpaApplication.getPlanPermissionDate().toString()))
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
                permitConditions.append(
                        String.valueOf(additionalOrder) + ") " + addnlPermitConditions.getAdditionalPermitCondition() + "\n\n");
                additionalOrder++;
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
            rejectReasons.append(String.valueOf(additionalOrder) + ") " + (stateHistory != null && StringUtils.isNotBlank(stateHistory.getComments()) ? stateHistory.getComments() : EMPTY) + "\n\n");
        } else {
            if(bpaApplication.getState().getComments().equalsIgnoreCase("Application cancelled by citizen")) {
                rejectReasons.append(getMessageFromPropertyFile(APPLICATION_REJECTION_REASON));
            } else
            rejectReasons.append(getMessageFromPropertyFile(APPLICATION_AUTO_REJECTION_REASON));
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
                        .append(String.valueOf(order) + ") " + rejectReason.getPermitCondition().getDescription() + "\n\n");
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
                                + DateUtils.toDefaultDateFormat(applnPermit.getPermitConditiondDate()) + "." + "\n\n");
                order++;
            } else if (applnPermit.isRequired()
                    && PermitConditionType.STATIC_PERMITCONDITION.equals(applnPermit.getPermitConditionType())) {
                permitConditions
                        .append(String.valueOf(order) + ") " + applnPermit.getPermitCondition().getDescription() + "\n\n");
                order++;
            }
        }
        return order;
    }

    private String getMessageFromPropertyFile(String key) {
        return bpaMessageSource.getMessage(key, null, null);
    }

    private String getMessageFromPropertyFileWithParameters(String key, String value) {
        return bpaMessageSource.getMessage(key, new String[] { value }, null);
    }

    private String calculateCertExpryDate(DateTime permissionDate, String noOfYears) {
        DateTimeFormatter fmt = DateUtils.defaultDateFormatter();
        return fmt.print(permissionDate.plusYears(Integer.valueOf(noOfYears)));
    }

    private Integer getAmountRuleByServiceType(final BpaApplication application) {
        BigDecimal amountRule = BigDecimal.ONE;
        if (ST_CODE_14.equalsIgnoreCase(application.getServiceType().getCode())
                || ST_CODE_15.equalsIgnoreCase(application.getServiceType().getCode())) {
            amountRule = new BigDecimal(2501);
        } else if (ST_CODE_05.equalsIgnoreCase(application.getServiceType().getCode())) {
            amountRule = application.getDocumentScrutiny().get(0).getExtentinsqmts();
        } else if (ST_CODE_08.equalsIgnoreCase(application.getServiceType().getCode())
                || ST_CODE_09.equalsIgnoreCase(application.getServiceType().getCode())) {
            amountRule = BigDecimal.ONE;
        } else if (!application.getBuildingDetail().isEmpty()
                && application.getBuildingDetail().get(0).getTotalPlintArea() != null) {
            amountRule = application.getBuildingDetail().get(0).getTotalPlintArea();
        }
        return amountRule.setScale(0, BigDecimal.ROUND_UP).intValue();
    }

    private String getApproverDesignation(final Integer amountRule) {
        String designation = StringUtils.EMPTY;
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
                .filter(history -> history.getOwnerPosition().getDeptDesig().getDesignation().getName().equalsIgnoreCase(getApproverDesignation(getAmountRuleByServiceType(application))))
                .findAny().orElse(null);
    	return stateHistory != null ? bpaWorkFlowService.getApproverAssignment(stateHistory.getOwnerPosition()).getEmployee().getName() : null;
    }
}
