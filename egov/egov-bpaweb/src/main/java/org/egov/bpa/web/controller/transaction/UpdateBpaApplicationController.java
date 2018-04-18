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
package org.egov.bpa.web.controller.transaction;

import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.master.service.PermitConditionsService;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaAppointmentSchedule;
import org.egov.bpa.transaction.entity.LettertoParty;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.enums.AppointmentSchedulePurpose;
import org.egov.bpa.transaction.entity.enums.PermitConditionType;
import org.egov.bpa.transaction.service.BpaApplicationPermitConditionsService;
import org.egov.bpa.transaction.service.InspectionService;
import org.egov.bpa.transaction.service.LettertoPartyService;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.PositionMasterService;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.utils.DateUtils;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.pims.commons.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_APPROVED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_CANCELLED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_CREATED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_DIGI_SIGNED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_DOC_VERIFIED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_FIELD_INS;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_NOCUPDATED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_RECORD_APPROVED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_REGISTERED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_REJECTED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_RESCHEDULED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_SCHEDULED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_TS_INS;
import static org.egov.bpa.utils.BpaConstants.BPA_APPLICATION;
import static org.egov.bpa.utils.BpaConstants.CHECKLIST_TYPE;
import static org.egov.bpa.utils.BpaConstants.CREATE_ADDITIONAL_RULE_CREATE;
import static org.egov.bpa.utils.BpaConstants.CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT;
import static org.egov.bpa.utils.BpaConstants.DESIGNATION_AE;
import static org.egov.bpa.utils.BpaConstants.DESIGNATION_OVERSEER;
import static org.egov.bpa.utils.BpaConstants.FORWARDED_TO_NOC_UPDATE;
import static org.egov.bpa.utils.BpaConstants.FWDINGTOLPINITIATORPENDING;
import static org.egov.bpa.utils.BpaConstants.FWD_TO_AE_AFTER_TS_INSP;
import static org.egov.bpa.utils.BpaConstants.FWD_TO_AE_FOR_APPROVAL;
import static org.egov.bpa.utils.BpaConstants.FWD_TO_AE_FOR_FIELD_ISPECTION;
import static org.egov.bpa.utils.BpaConstants.FWD_TO_OVERSEER_AFTER_TS_INSPN;
import static org.egov.bpa.utils.BpaConstants.FWD_TO_OVRSR_FOR_FIELD_INS;
import static org.egov.bpa.utils.BpaConstants.GENERATEPERMITORDER;
import static org.egov.bpa.utils.BpaConstants.GENERATEREJECTNOTICE;
import static org.egov.bpa.utils.BpaConstants.MESSAGE;
import static org.egov.bpa.utils.BpaConstants.WF_CANCELAPPLICATION_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_CREATED_STATE;
import static org.egov.bpa.utils.BpaConstants.WF_INITIATE_REJECTION_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_NEW_STATE;
import static org.egov.bpa.utils.BpaConstants.WF_REJECT_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_REJECT_STATE;
import static org.egov.bpa.utils.BpaConstants.WF_REVERT_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_SAVE_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_TS_APPROVAL_PENDING;
import static org.egov.bpa.utils.BpaConstants.WF_TS_INSPECTION_INITIATED;

@Controller
@RequestMapping(value = "/application")
public class UpdateBpaApplicationController extends BpaGenericApplicationController {

	public static final String COMMON_ERROR = "common-error";
	private static final String COLLECT_FEE_VALIDATE = "collectFeeValidate";
	private static final String WORK_FLOW_ACTION = "workFlowAction";
	private static final String AMOUNT_RULE = "amountRule";
	private static final String APPRIVALPOSITION = "approvalPosition";
	private static final String APPLICATION_HISTORY = "applicationHistory";
	private static final String ADDITIONALRULE = "additionalRule";
	private static final String APPROVAL_COMENT = "approvalComent";
	private static final String MSG_REJECT_FORWARD_REGISTRATION = "msg.reject.forward.registration";
	private static final String MSG_INITIATE_REJECTION = "msg.initiate.reject";
	private static final String MSG_UPDATE_FORWARD_REGISTRATION = "msg.update.forward.registration";
	private static final String APPLICATION_VIEW = "application-view";
	private static final String CREATEDOCUMENTSCRUTINY_FORM = "createdocumentscrutiny-form";
	private static final String DOCUMENTSCRUTINY_FORM = "documentscrutiny-form";
	private static final String BPAAPPLICATION_FORM = "bpaapplication-Form";
	private static final String BPA_APPLICATION_RESULT = "bpa-application-result";
	@Autowired
	private InspectionService inspectionService;
	@Autowired
	private PositionMasterService positionMasterService;
	@Autowired
	private LettertoPartyService lettertoPartyService;
	@Autowired
	private PermitConditionsService permitConditionsService;
	@Autowired
	private BpaApplicationPermitConditionsService bpaApplicationPermitConditionsService;

	@ModelAttribute
	public BpaApplication getBpaApplication(@PathVariable final String applicationNumber) {
		return applicationBpaService.findByApplicationNumber(applicationNumber);
	}

	@RequestMapping(value = "/update/{applicationNumber}", method = RequestMethod.GET)
	public String updateApplicationForm(final Model model, @PathVariable final String applicationNumber) {
		final BpaApplication application = getBpaApplication(applicationNumber);
		getModeForUpdateApplication(model, application);
		model.addAttribute("inspectionList", inspectionService.findByBpaApplicationOrderByIdAsc(application));
		model.addAttribute("lettertopartylist", lettertoPartyService.findByBpaApplicationOrderByIdDesc(application));
		if (!application.getIsOneDayPermitApplication()
			&& (FWD_TO_AE_FOR_FIELD_ISPECTION.equals(application.getState().getNextAction())
				|| APPLICATION_STATUS_FIELD_INS.equals(application.getStatus().getCode())
				|| APPLICATION_STATUS_NOCUPDATED.equalsIgnoreCase(application.getStatus().getCode()))) {
			model.addAttribute("createlettertoparty", true);
		}

		if (!bpaUtils.checkAnyTaxIsPendingToCollect(application)
			&& (APPLICATION_STATUS_APPROVED.equals(application.getStatus().getCode())
				|| APPLICATION_STATUS_DIGI_SIGNED.equalsIgnoreCase(application.getStatus().getCode()))) {
			model.addAttribute("showpermitconditions", true);
			model.addAttribute("permitConditions", permitConditionsService
					.findByConditionTypeOrderByOrderNumberAsc(PermitConditionType.STATIC_PERMITCONDITION.name()));
			model.addAttribute("modifiablePermitConditions", permitConditionsService
					.findByConditionTypeOrderByOrderNumberAsc(PermitConditionType.DYNAMIC_PERMITCONDITION.name()));
			model.addAttribute("additionalPermitCondition", permitConditionsService
					.findByConditionTypeOrderByOrderNumberAsc(PermitConditionType.ADDITIONAL_PERMITCONDITION.name()).get(0));
			buildApplicationPermitConditions(application);
		}
		buildRejectionReasons(model, application);
		model.addAttribute("workFlowByNonEmp", applicationBpaService.applicationinitiatedByNonEmployee(application));
		model.addAttribute(APPLICATION_HISTORY,
				bpaThirdPartyService.getHistory(application));

		if (APPLICATION_STATUS_CREATED.equals(application.getStatus().getCode())
			|| APPLICATION_STATUS_REGISTERED.equals(application.getStatus().getCode())
			|| APPLICATION_STATUS_SCHEDULED.equals(application.getStatus().getCode())
			|| APPLICATION_STATUS_RESCHEDULED.equals(application.getStatus().getCode())) {
			if (applicationBpaService.applicationinitiatedByNonEmployee(application)
				&& applicationBpaService.checkAnyTaxIsPendingToCollect(application)) {
				model.addAttribute(COLLECT_FEE_VALIDATE, "Please Collect Application Fees to Process Application");
			} else
				model.addAttribute(COLLECT_FEE_VALIDATE, "");
		}

		if (application != null) {
			loadViewdata(model, application);
			if (application.getState() != null
				&& application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_REGISTERED) ||
				application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_SCHEDULED)
				|| application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_RESCHEDULED)) {
				return DOCUMENTSCRUTINY_FORM;
			}
		}
		return APPLICATION_VIEW;
	}

	private void buildRejectionReasons(Model model, BpaApplication application) {
		if (application.getIsOneDayPermitApplication()
			&& APPLICATION_STATUS_FIELD_INS.equalsIgnoreCase(application.getStatus().getCode())
			|| APPLICATION_STATUS_NOCUPDATED.equals(application.getStatus().getCode())
			|| APPLICATION_STATUS_REJECTED.equalsIgnoreCase(application.getStatus().getCode())
			|| APPLICATION_STATUS_SCHEDULED.equalsIgnoreCase(application.getStatus().getCode())
			|| APPLICATION_STATUS_RESCHEDULED.equalsIgnoreCase(application.getStatus().getCode())) {
			model.addAttribute("showRejectionReasons", true);
			model.addAttribute("additionalPermitCondition", permitConditionsService
					.findByConditionTypeOrderByOrderNumberAsc(PermitConditionType.ADDITIONAL_PERMITCONDITION.name()).get(0));
			model.addAttribute("rejectionReasons", permitConditionsService.findByConditionTypeOrderByOrderNumberAsc("Rejection"));
			application.setRejectionReasonsTemp(bpaApplicationPermitConditionsService
					.findAllByApplicationAndPermitConditionType(application, PermitConditionType.REJECTION_REASON));
			application.setAdditionalPermitConditionsTemp(bpaApplicationPermitConditionsService
					.findAllByApplicationAndPermitConditionType(application, PermitConditionType.ADDITIONAL_PERMITCONDITION));
		}
	}

	private void buildApplicationPermitConditions(final BpaApplication application) {
		application.setDynamicPermitConditionsTemp(bpaApplicationPermitConditionsService
				.findAllByApplicationAndPermitConditionType(application, PermitConditionType.DYNAMIC_PERMITCONDITION));
		application.setStaticPermitConditionsTemp(bpaApplicationPermitConditionsService
				.findAllByApplicationAndPermitConditionType(application, PermitConditionType.STATIC_PERMITCONDITION));
		application.setAdditionalPermitConditionsTemp(bpaApplicationPermitConditionsService
				.findAllByApplicationAndPermitConditionType(application, PermitConditionType.ADDITIONAL_PERMITCONDITION));
	}

	private void getModeForUpdateApplication(final Model model, final BpaApplication application) {
		String mode = null;
		AppointmentSchedulePurpose scheduleType = null;
		List<String> purposeInsList = new ArrayList<>();
		for (BpaAppointmentSchedule schedule : application.getAppointmentSchedule()) {
			if (AppointmentSchedulePurpose.INSPECTION.equals(schedule.getPurpose())) {
				purposeInsList.add(schedule.getPurpose().name());
			}
		}
		Assignment appvrAssignment = bpaWorkFlowService
				.getApproverAssignment(application.getCurrentState().getOwnerPosition());
		// To show reschedule scrutiny button to employee
		if ((APPLICATION_STATUS_SCHEDULED.equals(application.getStatus().getCode()) ||
			 APPLICATION_STATUS_RESCHEDULED.equals(application.getStatus().getCode()) ||
			 APPLICATION_STATUS_PENDING_FOR_RESCHEDULING.equals(application.getStatus().getCode()))
			&& !application.getIsRescheduledByEmployee()) {
			mode = "showRescheduleToEmployee";
		} else if (WF_CREATED_STATE.equalsIgnoreCase(application.getStatus().getCode())) {
			mode = "view";
		} else if (!application.getIsOneDayPermitApplication() &&
				   APPLICATION_STATUS_DOC_VERIFIED.equalsIgnoreCase(application.getStatus().getCode()) &&
				   FWD_TO_OVRSR_FOR_FIELD_INS.equalsIgnoreCase(application.getState().getNextAction()) &&
				   purposeInsList.isEmpty()) {
			mode = "newappointment";
		} else if (FWD_TO_OVRSR_FOR_FIELD_INS.equalsIgnoreCase(application.getState().getNextAction())
				   && APPLICATION_STATUS_DOC_VERIFIED.equalsIgnoreCase(application.getStatus().getCode())
				   && application.getInspections().isEmpty()) {
			mode = "captureInspection";
			scheduleType = AppointmentSchedulePurpose.INSPECTION;
		} else if ((FWD_TO_OVRSR_FOR_FIELD_INS.equalsIgnoreCase(application.getState().getNextAction())
					&& APPLICATION_STATUS_DOC_VERIFIED.equalsIgnoreCase(application.getStatus().getCode()) ||
					(DESIGNATION_OVERSEER.equals(appvrAssignment.getDesignation().getName()) &&
					 APPLICATION_STATUS_TS_INS.equalsIgnoreCase(application.getStatus().getCode())))
				   && !application.getInspections().isEmpty()) {
			mode = "modifyInspection";
			scheduleType = AppointmentSchedulePurpose.INSPECTION;
		} else if (FORWARDED_TO_NOC_UPDATE.equalsIgnoreCase(application.getState().getNextAction())
				   && APPLICATION_STATUS_FIELD_INS.equalsIgnoreCase(application.getStatus().getCode())) {
			model.addAttribute("showUpdateNoc", true);
		} else if (FWD_TO_AE_FOR_APPROVAL.equalsIgnoreCase(application.getState().getNextAction())
				   && !application.getInspections().isEmpty()) {
			mode = "initiatedForApproval";
		}

		if (mode == null) {
			mode = "edit";
		}
		model.addAttribute("scheduleType", scheduleType);
		model.addAttribute("mode", mode);
	}

	@RequestMapping(value = "/documentscrutiny/{applicationNumber}", method = RequestMethod.GET)
	public String documentScrutinyForm(final Model model, @PathVariable final String applicationNumber) {
		final BpaApplication application = getBpaApplication(applicationNumber);
		if (validateOnDocumentScrutiny(model, application) || checkIsRescheduledOnScrutiny(model, application)) {
			return COMMON_ERROR;
		}
		buildRejectionReasons(model, application);
		loadViewdata(model, application);
		model.addAttribute("loginUser", securityUtils.getCurrentUser());
		model.addAttribute(APPLICATION_HISTORY,
				bpaThirdPartyService.getHistory(application));
		return CREATEDOCUMENTSCRUTINY_FORM;
	}

	private Boolean checkIsRescheduledOnScrutiny(final Model model, final BpaApplication application) {
		Optional<SlotApplication> actvSltApp = application.getSlotApplications().stream()
														  .reduce((slotApp1, slotApp2) -> slotApp2);
		if (actvSltApp.isPresent()
			&& actvSltApp.get().getSlotDetail().getSlot().getAppointmentDate().after(new Date())) {
			model.addAttribute(MESSAGE, messageSource.getMessage("msg.validate.doc.scrutiny", new String[]{
							application.getApplicationNumber(), DateUtils.getDefaultFormattedDate(
					actvSltApp.get().getSlotDetail().getSlot().getAppointmentDate())},
					LocaleContextHolder.getLocale()));
			return true;
		} else {
			return false;
		}
	}

	@RequestMapping(value = "/documentscrutiny/{applicationNumber}", method = RequestMethod.POST)
	public String documentScrutinyForm(@Valid @ModelAttribute(BPA_APPLICATION) BpaApplication bpaApplication,
									   @PathVariable final String applicationNumber, final BindingResult resultBinder,
									   final HttpServletRequest request, @RequestParam final BigDecimal amountRule,
									   final Model model) {
		if (resultBinder.hasErrors()) {
			loadViewdata(model, bpaApplication);
			return CREATEDOCUMENTSCRUTINY_FORM;
		}
		Long approvalPosition;
		String workFlowAction = request.getParameter(WORK_FLOW_ACTION);
		String approvalComent = request.getParameter(APPROVAL_COMENT);
		if (request.getParameter(APPRIVALPOSITION) != null) {
			// In case of one day permit, on reject from clerk should be sent to AE
			if (workFlowAction.equalsIgnoreCase(WF_INITIATE_REJECTION_BUTTON) && bpaApplication.getIsOneDayPermitApplication()
				&& bpaApplication.getCurrentState().getValue().equalsIgnoreCase(APPLICATION_STATUS_SCHEDULED)) {
				approvalPosition = bpaUtils.getUserPositionIdByZone(DESIGNATION_AE, bpaApplication.getSiteDetail().get(0).getElectionBoundary().getId());
			} else
				approvalPosition = Long.valueOf(request.getParameter(APPRIVALPOSITION));
			Position pos = positionMasterService.getPositionById(approvalPosition);
			User user = bpaThirdPartyService.getUserPositionByPassingPosition(approvalPosition);
			if (!bpaApplication.getApplicationDocument().isEmpty())
				applicationBpaService.persistOrUpdateApplicationDocument(bpaApplication);
			BpaApplication bpaAppln = applicationBpaService.updateApplication(bpaApplication, approvalPosition, workFlowAction,
					amountRule);
			String message;
			if (WF_INITIATE_REJECTION_BUTTON.equalsIgnoreCase(workFlowAction) && !bpaApplication.getIsOneDayPermitApplication()) {
				User userObj = bpaThirdPartyService
						.getUserPositionByPassingPosition(bpaAppln.getCurrentState().getOwnerPosition().getId());
				message = getMessageOnRejectionInitiation(approvalComent, bpaAppln, userObj, MSG_INITIATE_REJECTION,
						bpaAppln.getCurrentState().getOwnerPosition());
			} else if (WF_INITIATE_REJECTION_BUTTON.equalsIgnoreCase(workFlowAction)
					   && bpaApplication.getIsOneDayPermitApplication()) {
				message = getMessageOnRejectionInitiation(approvalComent, bpaAppln, user, MSG_INITIATE_REJECTION, pos);
			} else
				message = messageSource.getMessage("msg.update.forward.documentscrutiny", new String[]{
						user == null ? ""
									 : user.getUsername().concat("~")
										   .concat(getDesinationNameByPosition(pos)),
						bpaAppln.getApplicationNumber()}, LocaleContextHolder.getLocale());
			model.addAttribute(MESSAGE, message);
		}
		return BPA_APPLICATION_RESULT;
	}

	private String getMessageOnRejectionInitiation(String approvalComent, BpaApplication bpaAppln, User userObj,
												   String msgInitiateRjctn, Position ownerPosition) {
		return messageSource.getMessage(msgInitiateRjctn, new String[]{
				userObj == null ? ""
								: userObj.getUsername().concat("~")
										 .concat(getDesinationNameByPosition(ownerPosition)),
				bpaAppln.getApplicationNumber(), approvalComent}, LocaleContextHolder.getLocale());
	}

	private void loadViewdata(final Model model, final BpaApplication application) {
		applicationBpaService.buildExistingAndProposedBuildingDetails(application);
		model.addAttribute("stateType", application.getClass().getSimpleName());
		final WorkflowContainer workflowContainer = new WorkflowContainer();
		// added for one day permit. amount rule and pending action needs to be set only for other services.
		if (application.getIsOneDayPermitApplication()) {
			model.addAttribute(ADDITIONALRULE, CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT);
			workflowContainer.setAdditionalRule(CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT);

			if (application.getState() != null
				&& application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_SCHEDULED)) {
				workflowContainer.setPendingActions(application.getState().getNextAction());
			}
		} else {
			model.addAttribute(ADDITIONALRULE, CREATE_ADDITIONAL_RULE_CREATE);
			workflowContainer.setAdditionalRule(CREATE_ADDITIONAL_RULE_CREATE);
			List<LettertoParty> lettertoParties = lettertoPartyService.findByBpaApplicationOrderByIdDesc(application);

			if (application.getState() != null
				&& application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_REGISTERED) ||
				application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_SCHEDULED)
				|| application.getState().getValue().equalsIgnoreCase(APPLICATION_STATUS_RESCHEDULED)) {
				workflowContainer.setPendingActions(application.getState().getNextAction());
			}

			// Setting AmountRule to decide no. of level approval cycle
			if (APPLICATION_STATUS_NOCUPDATED.equals(application.getStatus().getCode())
				|| !APPLICATION_STATUS_DIGI_SIGNED.equals(application.getStatus().getCode())
				   && !APPLICATION_STATUS_APPROVED.equals(application.getStatus().getCode())
				   && !lettertoParties.isEmpty()
				   && APPLICATION_STATUS_NOCUPDATED
						   .equals(lettertoParties.get(0).getCurrentApplnStatus().getCode())) {
				workflowContainer.setAmountRule(bpaWorkFlowService.getAmountRuleByServiceType(application));
				workflowContainer.setPendingActions(application.getState().getNextAction());
			} else if (APPLICATION_STATUS_APPROVED.equals(application.getStatus().getCode())
					   && !APPLICATION_STATUS_RECORD_APPROVED.equalsIgnoreCase(application.getState().getValue())) {
				workflowContainer.setAmountRule(bpaWorkFlowService.getAmountRuleByServiceType(application));
			}
			// Town surveyor workflow
			if (WF_TS_INSPECTION_INITIATED.equalsIgnoreCase(application.getStatus().getCode())) {
				workflowContainer.setPendingActions(WF_TS_APPROVAL_PENDING);
				model.addAttribute("captureTSRemarks", true);
			} else if (APPLICATION_STATUS_TS_INS.equalsIgnoreCase(application.getStatus().getCode())) {
				Assignment approverAssignment = bpaWorkFlowService
						.getApproverAssignment(application.getCurrentState().getOwnerPosition());
				if (DESIGNATION_AE.equals(approverAssignment.getDesignation().getName())) {
					workflowContainer.setPendingActions(FWD_TO_AE_AFTER_TS_INSP);
				} else if (DESIGNATION_OVERSEER.equals(approverAssignment.getDesignation().getName())) {
					workflowContainer.setPendingActions(FWD_TO_OVERSEER_AFTER_TS_INSPN);
				}
			}
			// workflowContainer.setAdditionalRule(CREATE_ADDITIONAL_RULE_CREATE);
		}
		prepareWorkflow(model, application, workflowContainer);
		model.addAttribute("pendingActions", workflowContainer.getPendingActions());
		model.addAttribute(AMOUNT_RULE, workflowContainer.getAmountRule());
		model.addAttribute("currentState", application.getCurrentState().getValue());
		model.addAttribute(BPA_APPLICATION, application);
		model.addAttribute("electionBoundary", application.getSiteDetail().get(0).getElectionBoundary().getId());
		model.addAttribute("electionBoundaryName", application.getSiteDetail().get(0).getElectionBoundary().getName());
		model.addAttribute("revenueBoundaryName", application.getSiteDetail().get(0).getAdminBoundary().getName());
		model.addAttribute("bpaPrimaryDept", bpaUtils.getAppconfigValueByKeyNameForDefaultDept());
		model.addAttribute("checkListDetailList", checkListDetailService
				.findActiveCheckListByServiceType(application.getServiceType().getId(), CHECKLIST_TYPE));
		model.addAttribute("applicationDocumentList", application.getApplicationDocument());
		model.addAttribute("isFeeCollected", bpaDemandService.checkAnyTaxIsPendingToCollect(application));
		model.addAttribute("admissionFee", applicationBpaService.setAdmissionFeeAmountForRegistrationWithAmenities(
				application.getServiceType().getId(), application.getApplicationAmenity()));
		buildReceiptDetails(application);
	}

	@RequestMapping(value = "/update-submit/{applicationNumber}", method = RequestMethod.POST)
	public String updateApplication(@Valid @ModelAttribute(BPA_APPLICATION) BpaApplication bpaApplication,
									@PathVariable final String applicationNumber,
									final BindingResult resultBinder,
									final HttpServletRequest request,
									final Model model,
									@RequestParam final BigDecimal amountRule) {
		proposedBuildingFloorDetailsService.removeDuplicateProposedBuildFloorDetails(bpaApplication);
		existingBuildingFloorDetailsService.removeDuplicateExistingBuildFloorDetails(bpaApplication);
		if (resultBinder.hasErrors()) {
			loadViewdata(model, bpaApplication);
			return BPAAPPLICATION_FORM;
		}

		if (bpaApplicationValidationService.validateBuildingDetails(bpaApplication, model)) {
			loadViewdata(model, bpaApplication);
			return BPAAPPLICATION_FORM;
		}

		String workFlowAction = request.getParameter(WORK_FLOW_ACTION);
		String approvalComent = request.getParameter(APPROVAL_COMENT);
		if (WF_CANCELAPPLICATION_BUTTON.equalsIgnoreCase(workFlowAction)) {
			StateHistory stateHistory = bpaApplication.getStateHistory().stream()
													  .filter(history -> history.getValue().equalsIgnoreCase(APPLICATION_STATUS_REJECTED))
													  .findAny().orElse(null);
			if (stateHistory != null)
				approvalComent = stateHistory.getComments();
		}

		String message;
		Long approvalPosition = null;
		Position pos = null;
		if (WF_TS_INSPECTION_INITIATED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
			pos = positionMasterService.getPositionById(bpaWorkFlowService.getTownSurveyorInspnInitiator(bpaApplication));
			approvalPosition = positionMasterService
					.getPositionById(bpaWorkFlowService.getTownSurveyorInspnInitiator(bpaApplication)).getId();
		} else if (WF_REVERT_BUTTON.equalsIgnoreCase(workFlowAction)) {
			pos = bpaApplication.getCurrentState().getPreviousOwner();
			approvalPosition = bpaApplication.getCurrentState().getPreviousOwner().getId();
		} else if (FWDINGTOLPINITIATORPENDING.equalsIgnoreCase(bpaApplication.getState().getNextAction())) {
			List<LettertoParty> lettertoParties = lettertoPartyService.findByBpaApplicationOrderByIdDesc(bpaApplication);
			StateHistory stateHistory = bpaWorkFlowService.getStateHistoryToGetLPInitiator(bpaApplication, lettertoParties);
			approvalPosition = stateHistory.getOwnerPosition().getId();
		} else if (StringUtils.isNotBlank(request.getParameter(APPRIVALPOSITION))
				   && !WF_REJECT_BUTTON.equalsIgnoreCase(workFlowAction)
				   && !GENERATEREJECTNOTICE.equalsIgnoreCase(workFlowAction)) {
			approvalPosition = Long.valueOf(request.getParameter(APPRIVALPOSITION));
		} // For one day permit, on reject from AE it's forwarded to SUP (workflow user)
		else if (WF_REJECT_BUTTON.equalsIgnoreCase(workFlowAction)) {
			if (bpaApplication.getIsOneDayPermitApplication() && null != request.getParameter(APPRIVALPOSITION)
				&& !"".equals(request.getParameter(APPRIVALPOSITION))) {
				approvalPosition = Long.valueOf(request.getParameter(APPRIVALPOSITION));
			} else if (!bpaApplication.getIsOneDayPermitApplication()) {
				pos = bpaWorkFlowService.getApproverPositionOfElectionWardByCurrentState(bpaApplication, WF_REJECT_STATE);
				approvalPosition = pos.getId();
			}
		}
		buildReceiptDetails(bpaApplication);
		if (!bpaApplication.getApplicationDocument().isEmpty())
			applicationBpaService.persistOrUpdateApplicationDocument(bpaApplication);
		if (bpaApplication.getCurrentState().getValue().equals(WF_NEW_STATE)) {
			return applicationBpaService.redirectToCollectionOnForward(bpaApplication, model);
		}
		BpaApplication bpaAppln = applicationBpaService.updateApplication(bpaApplication, approvalPosition, workFlowAction,
				amountRule);
		bpaUtils.updatePortalUserinbox(bpaAppln, null);
		if (null != approvalPosition) {
			pos = positionMasterService.getPositionById(approvalPosition);
		}
		if (null == approvalPosition) {
			pos = positionMasterService.getPositionById(bpaAppln.getCurrentState().getOwnerPosition().getId());
		}
		User user = bpaThirdPartyService.getUserPositionByPassingPosition(approvalPosition == null ? pos.getId() : approvalPosition);
		if (WF_REJECT_BUTTON.equalsIgnoreCase(workFlowAction)) {
			message = getMessageOnRejectionInitiation(approvalComent, bpaAppln, user, MSG_REJECT_FORWARD_REGISTRATION, pos);
		} else if (WF_SAVE_BUTTON.equalsIgnoreCase(workFlowAction)) {
			message = messageSource.getMessage("msg.noc.update.success", new String[]{}, LocaleContextHolder.getLocale());
		} else {
			message = messageSource.getMessage(MSG_UPDATE_FORWARD_REGISTRATION, new String[]{
					user == null ? ""
								 : user.getUsername().concat("~")
									   .concat(getDesinationNameByPosition(pos)),
					bpaAppln.getApplicationNumber()}, LocaleContextHolder.getLocale());
		}
		model.addAttribute(MESSAGE, message);
		if (APPLICATION_STATUS_CANCELLED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
			bpaSmsAndEmailService.sendSMSAndEmail(bpaAppln);
		}
		if (isNotBlank(workFlowAction) && GENERATEPERMITORDER.equalsIgnoreCase(workFlowAction)) {
			return "redirect:/application/generatepermitorder/" + bpaAppln.getApplicationNumber();
		} else if (isNotBlank(workFlowAction) && GENERATEREJECTNOTICE.equalsIgnoreCase(workFlowAction)) {
			return "redirect:/application/rejectionnotice/" + bpaAppln.getApplicationNumber();
		}
		return BPA_APPLICATION_RESULT;
	}

}