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
package org.egov.bpa.web.controller.transaction.citizen;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.service.InspectionService;
import org.egov.bpa.transaction.service.LettertoPartyService;
import org.egov.bpa.transaction.service.ScheduleAppointmentForDocumentScrutinyService;
import org.egov.bpa.transaction.service.SlotOpeningForAppointmentService;
import org.egov.bpa.transaction.service.collection.ApplicationBpaBillService;
import org.egov.bpa.transaction.service.collection.GenericBillGeneratorService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.web.controller.transaction.BpaGenericApplicationController;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.persistence.entity.PermanentAddress;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.pims.commons.Position;
import org.python.icu.text.SimpleDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import static org.egov.bpa.utils.BpaConstants.*;

import java.math.BigDecimal;

@Controller
@RequestMapping(value = "/application")
public class CitizenUpdateApplicationController extends BpaGenericApplicationController {
    private static final String COLLECT_FEE_VALIDATE = "collectFeeValidate";
    private static final String IS_CITIZEN = "isCitizen";
    private static final String CITIZEN_VIEW = "citizen-view";
    private static final String BPAAPP_CITIZEN_FORM = "bpaapp-citizenForm";
    private static final String MESSAGE = "message";
    private static final String BPA_APPLICATION = "bpaApplication";
    private static final String APPLICATION_HISTORY = "applicationHistory";
    private static final String ADDITIONALRULE = "additionalRule";
    private static final String BPAAPPLICATION_CITIZEN = "citizen_suceess";
    @Autowired
    private GenericBillGeneratorService genericBillGeneratorService;
    @Autowired
    LettertoPartyService lettertoPartyService;
    @Autowired
    private InspectionService inspectionService;
    @Autowired
    private PositionMasterService positionMasterService;
    @Autowired
    private ApplicationBpaBillService applicationBpaBillService;
    @Autowired
    private SlotOpeningForAppointmentService slotOpeningForAppointmentService;
    @Autowired
    private ScheduleAppointmentForDocumentScrutinyService scheduleAppointmentForDocumentScrutinyService;

    @ModelAttribute
    public BpaApplication getBpaApplication(@PathVariable final String applicationNumber) {
        return applicationBpaService.findByApplicationNumber(applicationNumber);
    } 

    @RequestMapping(value = "/citizen/update/{applicationNumber}", method = RequestMethod.GET)
    public String updateApplicationForm(final Model model, @PathVariable final String applicationNumber,
            final HttpServletRequest request) {
        final BpaApplication application = getBpaApplication(applicationNumber);
        model.addAttribute("mode", "newappointment");

        if (!application.getIsOneDayPermitApplication() && (APPLICATION_STATUS_SCHEDULED.equals(application.getStatus().getCode()) ||
             APPLICATION_STATUS_RESCHEDULED.equals(application.getStatus().getCode()) ||
             APPLICATION_STATUS_PENDING_FOR_RESCHEDULING.equals(application.getStatus().getCode()))
            && !application.getIsRescheduledByCitizen()) {
            model.addAttribute("mode", "showRescheduleToCitizen");
        }
        model.addAttribute(APPLICATION_HISTORY, bpaThirdPartyService.getHistory(application));
        prepareCommonModelAttribute(model, application);
        return loadViewdata(model, application);
    }

    private String loadViewdata(final Model model, final BpaApplication application) {
        prepareFormData(model);
        buildReceiptDetails(application);
        application.setApplicationAmenityTemp(application.getApplicationAmenity());
        applicationBpaService.buildExistingAndProposedBuildingDetails(application);
        model.addAttribute("stateType", application.getClass().getSimpleName());
        if(application.getIsOneDayPermitApplication()){
        	model.addAttribute(ADDITIONALRULE, CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT);
        }else
        	model.addAttribute(ADDITIONALRULE, CREATE_ADDITIONAL_RULE_CREATE);
        model.addAttribute(BPA_APPLICATION, application);
        model.addAttribute("currentState",
                application.getCurrentState() != null ? application.getCurrentState().getValue() : "");
        model.addAttribute("nocCheckListDetails", checkListDetailService
                .findActiveCheckListByServiceType(application.getServiceType().getId(), CHECKLIST_TYPE_NOC));
        model.addAttribute("checkListDetailList", checkListDetailService
                .findActiveCheckListByServiceType(application.getServiceType().getId(), BpaConstants.CHECKLIST_TYPE));
        model.addAttribute("applicationDocumentList", application.getApplicationDocument());
        model.addAttribute("isFeeCollected", bpaDemandService.checkAnyTaxIsPendingToCollect(application));
        model.addAttribute("lettertopartylist", lettertoPartyService.findByBpaApplicationOrderByIdDesc(application));
        model.addAttribute("inspectionList", inspectionService.findByBpaApplicationOrderByIdAsc(application));
        application.getOwner().setPermanentAddress((PermanentAddress) application.getOwner().getUser().getAddress().get(0));
        model.addAttribute("admissionFee", applicationBpaService.setAdmissionFeeAmountForRegistrationWithAmenities(
                application.getServiceType().getId(), application.getApplicationAmenity()));
        Boolean isCitizen = (Boolean) model.asMap().get(IS_CITIZEN);
        Boolean validateCitizenAcceptance = (Boolean) model.asMap().get("validateCitizenAcceptance");
        if (APPLICATION_STATUS_REGISTERED.equals(application.getStatus().getCode())
            || APPLICATION_STATUS_SCHEDULED.equals(application.getStatus().getCode())
            || APPLICATION_STATUS_RESCHEDULED.equals(application.getStatus().getCode())
            || APPLICATION_STATUS_APPROVED.equals(application.getStatus().getCode())) {
            if (applicationBpaService.applicationinitiatedByNonEmployee(application)
                && applicationBpaService.checkAnyTaxIsPendingToCollect(application)) {
                model.addAttribute(COLLECT_FEE_VALIDATE, "Please Pay Fees to Process Application");
                String enableOrDisablePayOnline=bpaUtils.getAppconfigValueByKeyName(BpaConstants.ENABLEONLINEPAYMENT);
                model.addAttribute("onlinePaymentEnable", (enableOrDisablePayOnline.equalsIgnoreCase("YES") ? Boolean.TRUE : Boolean.FALSE));
            } else
                model.addAttribute(COLLECT_FEE_VALIDATE, "");
        }

        if (application.getStatus() != null
                && application.getStatus().getCode().equals(BpaConstants.APPLICATION_STATUS_CREATED) &&
                (!isCitizen || (isCitizen && (validateCitizenAcceptance && !application.isCitizenAccepted()))))
            return BPAAPP_CITIZEN_FORM;
        else
            return CITIZEN_VIEW;
    }

    @RequestMapping(value = "/citizen/update-submit/{applicationNumber}", method = RequestMethod.POST)
    public String updateApplication(@Valid @ModelAttribute("") BpaApplication bpaApplication,
            @PathVariable final String applicationNumber, final BindingResult resultBinder,
            final HttpServletRequest request, final Model model,
            @RequestParam("files") final MultipartFile[] files) {
        proposedBuildingFloorDetailsService.removeDuplicateProposedBuildFloorDetails(bpaApplication);
        existingBuildingFloorDetailsService.removeDuplicateExistingBuildFloorDetails(bpaApplication);
        if (resultBinder.hasErrors()) {
            prepareCommonModelAttribute(model, bpaApplication);
            return loadViewdata(model, bpaApplication);
        }
        if (bpaApplicationValidationService.validateBuildingDetails(bpaApplication, model)) {
            prepareCommonModelAttribute(model, bpaApplication);
            return loadViewdata(model, bpaApplication);
        }
        String workFlowAction = request.getParameter("workFlowAction");
        Long approvalPosition = null;
        if (!bpaApplication.getApplicationDocument().isEmpty())
            applicationBpaService.persistOrUpdateApplicationDocument(bpaApplication);
        applicationBpaService.buildExistingAndProposedBuildingDetails(bpaApplication);
        bpaApplication.getApplicationAmenity().clear();
        bpaApplication.setApplicationAmenity(bpaApplication.getApplicationAmenityTemp());
        bpaApplication.setDemand(applicationBpaBillService.createDemand(bpaApplication));
        String enableOrDisablePayOnline = bpaUtils.getAppconfigValueByKeyName(ENABLEONLINEPAYMENT);
        if (workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON)) {
            final WorkFlowMatrix wfMatrix = bpaUtils.getWfMatrixByCurrentState(bpaApplication,
                    WF_NEW_STATE);
            if (wfMatrix != null)
                approvalPosition = bpaUtils.getUserPositionIdByZone(wfMatrix.getNextDesignation(),
                        bpaApplication.getSiteDetail().get(0) != null
                                && bpaApplication.getSiteDetail().get(0).getElectionBoundary() != null
                                        ? bpaApplication.getSiteDetail().get(0).getElectionBoundary().getId()
                                        : null);
            bpaUtils.redirectToBpaWorkFlow(approvalPosition, bpaApplication, WF_NEW_STATE, null, null,
                    null);
        } else if (workFlowAction != null && WF_CANCELAPPLICATION_BUTTON.equalsIgnoreCase(workFlowAction)) {
            bpaApplication.setStatus(
                    applicationBpaService.getStatusByCodeAndModuleType(APPLICATION_STATUS_CANCELLED));
        }

        if (bpaApplication.getOwner().getUser() != null && bpaApplication.getOwner().getUser().getId() == null)
            buildOwnerDetails(bpaApplication);
        // To allot slot for one day permit applications
        applicationBpaService.scheduleAppointmentForOneDayPermit(bpaApplication);
        applicationBpaService.saveAndFlushApplication(bpaApplication);
        bpaUtils.updatePortalUserinbox(bpaApplication, null);
        if (workFlowAction != null
                && workFlowAction
                        .equals(WF_LBE_SUBMIT_BUTTON)
                && !bpaUtils.logedInuserIsCitizen()) {
            Position pos = positionMasterService.getPositionById(bpaApplication.getCurrentState().getOwnerPosition().getId());
            User wfUser = bpaThirdPartyService.getUserPositionByPassingPosition(pos.getId());
            String message = messageSource.getMessage("msg.portal.forward.registration", new String[] {
                    wfUser != null ? wfUser.getUsername().concat("~")
                            .concat(getDesinationNameByPosition(pos))
                            : "",
                    bpaApplication.getApplicationNumber() }, LocaleContextHolder.getLocale());
            if(bpaApplication.getIsOneDayPermitApplication()) {
            	message = message.concat(DISCLIMER_MESSAGE_ONEDAYPERMIT_ONSAVE);
                getAppointmentMsgForOnedayPermit(bpaApplication, model);
            } else {
            	message = message.concat(DISCLIMER_MESSAGE_ONSAVE);
            }
            model.addAttribute(MESSAGE, message);
        } else if (workFlowAction != null && workFlowAction.equals(WF_CANCELAPPLICATION_BUTTON)) {
            model.addAttribute(MESSAGE, " Application is cancelled by applicant itself successfully with application number "+bpaApplication.getApplicationNumber());
        } else
            model.addAttribute(MESSAGE,
                    "Application is successfully saved with ApplicationNumber " + bpaApplication.getApplicationNumber());
        if (workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON))
            bpaUtils.sendSmsEmailOnCitizenSubmit(bpaApplication);

        if (workFlowAction != null && workFlowAction.equals(WF_LBE_SUBMIT_BUTTON)
            && enableOrDisablePayOnline.equalsIgnoreCase("YES") && bpaUtils.checkAnyTaxIsPendingToCollect(bpaApplication)) {
            return genericBillGeneratorService
                    .generateBillAndRedirectToCollection(bpaApplication, model);
        }
        return BPAAPPLICATION_CITIZEN;
    }

}
