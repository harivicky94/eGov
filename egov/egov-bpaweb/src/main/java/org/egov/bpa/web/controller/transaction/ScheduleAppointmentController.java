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
package org.egov.bpa.web.controller.transaction;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.egov.bpa.master.service.AppointmentLocationsService;
import org.egov.bpa.transaction.entity.*;

import org.egov.bpa.transaction.entity.dto.ScheduleScrutiny;
import org.egov.bpa.transaction.entity.enums.AppointmentSchedulePurpose;
import org.egov.bpa.transaction.service.BpaAppointmentScheduleService;
import org.egov.bpa.transaction.service.RescheduleAppointmentsForDocumentScrutinyService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.commons.entity.Source;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_DOC_VERIFIED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_SCHEDULED;

@Controller
@RequestMapping(value = "/application")
public class ScheduleAppointmentController extends BpaGenericApplicationController {

    private static final String APPOINTMENT_LOCATIONS_LIST = "appointmentLocationsList";

    private static final String APPOINTMENT_SCHEDULED_LIST = "appointmentScheduledList";

    private static final String SCHEDULE_APPIONTMENT_RESULT = "schedule-appiontment-result";

    private static final String MESSAGE = "message";

    private static final String APPLICATION_NUMBER = "applicationNumber";

    private static final String REDIRECT_APPLICATION_VIEW_APPOINTMENT = "redirect:/application/view-appointment/";

    private static final String RESCHEDULE_APPIONTMENT = "reschedule-appiontment";

    private static final String BPA_APPOINTMENT_SCHEDULE = "bpaAppointmentSchedule";

    private static final String SCHEDULE_APPIONTMENT_NEW = "schedule-appiontment-new";

    public static final String SCHEDULED_SCRUTINY_DETAILS_RESULT = "view-scheduled-scrutiny-details-result";
    public static final String CITIZEN_SUCEESS = "citizen_suceess";
    private static final String RESCHEDULE_DOC_SCRUTINY = "reschedule-document-scrutiny";
    public static final String COMMON_ERROR = "common-error";

    @Autowired
    private BpaAppointmentScheduleService bpaAppointmentScheduleService;
    @Autowired
    private AppointmentLocationsService appointmentLocationsService;
    @Autowired
    private RescheduleAppointmentsForDocumentScrutinyService rescheduleAppnmtsForDocScrutinyService;

    @GetMapping("/scheduleappointment/{applicationNumber}")
    public String newScheduleAppointment(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        BpaAppointmentSchedule appointmentSchedule = new BpaAppointmentSchedule();
        if (BpaConstants.APPLICATION_STATUS_REGISTERED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
            appointmentSchedule.setPurpose(AppointmentSchedulePurpose.DOCUMENTSCRUTINY);
        } else if (APPLICATION_STATUS_DOC_VERIFIED.equalsIgnoreCase(bpaApplication.getStatus().getCode())) {
            appointmentSchedule.setPurpose(AppointmentSchedulePurpose.INSPECTION);
        }
        model.addAttribute(APPOINTMENT_LOCATIONS_LIST, appointmentLocationsService.findAllOrderByOrderNumber());
        model.addAttribute(BPA_APPOINTMENT_SCHEDULE, appointmentSchedule);
        model.addAttribute(APPLICATION_NUMBER, applicationNumber);
        return SCHEDULE_APPIONTMENT_NEW;
    }

    @PostMapping("/scheduleappointment/{applicationNumber}")
    public String createScheduleAppointment(@Valid @ModelAttribute final BpaAppointmentSchedule appointmentSchedule,
            @PathVariable final String applicationNumber, final Model model, final RedirectAttributes redirectAttributes) {
        BpaAppointmentSchedule schedule = buildAndSaveNewAppointment(appointmentSchedule, applicationNumber);
        if (AppointmentSchedulePurpose.DOCUMENTSCRUTINY.equals(appointmentSchedule.getPurpose())) {
            redirectAttributes.addFlashAttribute(MESSAGE,
                    messageSource.getMessage("msg.new.appoimt", null, null));
        } else if (AppointmentSchedulePurpose.INSPECTION.equals(appointmentSchedule.getPurpose())) {
            redirectAttributes.addFlashAttribute(MESSAGE,
                    messageSource.getMessage("msg.new.appoimt.fieldins", null, null));
        }
        return REDIRECT_APPLICATION_VIEW_APPOINTMENT + schedule.getId();
    }

    private BpaAppointmentSchedule buildAndSaveNewAppointment(final BpaAppointmentSchedule appointmentSchedule,
            final String applicationNumber) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        appointmentSchedule.setApplication(bpaApplication);
        BpaAppointmentSchedule schedule = bpaAppointmentScheduleService.save(appointmentSchedule);
        bpaSmsAndEmailService.sendSMSAndEmailToscheduleAppointment(schedule, bpaApplication);
        return schedule;
    }

    @GetMapping("/postponeappointment/{scheduleType}/{applicationNumber}")
    public String editScheduleAppointment(@PathVariable final AppointmentSchedulePurpose scheduleType,
            @PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        List<BpaAppointmentSchedule> appointmentScheduledList = bpaAppointmentScheduleService.findByApplication(bpaApplication,
                scheduleType);
        BpaAppointmentSchedule appointmentSchedule = new BpaAppointmentSchedule();
        appointmentSchedule.setPurpose(appointmentScheduledList.get(0).getPurpose());
        model.addAttribute(APPOINTMENT_LOCATIONS_LIST, appointmentLocationsService.findAllOrderByOrderNumber());
        model.addAttribute(BPA_APPOINTMENT_SCHEDULE, appointmentSchedule);
        model.addAttribute(APPLICATION_NUMBER, applicationNumber);
        model.addAttribute(APPOINTMENT_SCHEDULED_LIST, appointmentScheduledList);
        model.addAttribute("mode", "postponeappointment");
        return RESCHEDULE_APPIONTMENT;
    }

    @PostMapping("/postponeappointment/{scheduleType}/{applicationNumber}")
    public String updateScheduleAppointment(@Valid @ModelAttribute final BpaAppointmentSchedule appointmentSchedule,
            @PathVariable final AppointmentSchedulePurpose scheduleType, @PathVariable final String applicationNumber,
            @RequestParam Long bpaAppointmentScheduleId, final Model model, final RedirectAttributes redirectAttributes) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        BpaAppointmentSchedule parent = bpaAppointmentScheduleService.findById(bpaAppointmentScheduleId);
        appointmentSchedule.setApplication(bpaApplication);
        appointmentSchedule.setPostponed(true);
        appointmentSchedule.setParent(parent);
        BpaAppointmentSchedule schedule = bpaAppointmentScheduleService.save(appointmentSchedule);
        bpaSmsAndEmailService.sendSMSAndEmailToscheduleAppointment(schedule, bpaApplication);
        if (AppointmentSchedulePurpose.DOCUMENTSCRUTINY.equals(schedule.getPurpose())) {
            redirectAttributes.addFlashAttribute(MESSAGE,
                    messageSource.getMessage("msg.rescheduled.appoimt", null, null));
        } else if (AppointmentSchedulePurpose.INSPECTION.equals(schedule.getPurpose())) {
            redirectAttributes.addFlashAttribute(MESSAGE,
                    messageSource.getMessage("msg.update.appoimt.fieldins", null, null));
        }
        return REDIRECT_APPLICATION_VIEW_APPOINTMENT + schedule.getId();
    }

    @GetMapping("/scrutiny/schedule/{applicationNumber}")
    public String showScheduleAppointmentForDocScrutiny(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        BpaAppointmentSchedule appointmentSchedule = new BpaAppointmentSchedule();
        appointmentSchedule.setPurpose(AppointmentSchedulePurpose.DOCUMENTSCRUTINY);
        model.addAttribute(APPOINTMENT_LOCATIONS_LIST, appointmentLocationsService.findAllOrderByOrderNumber());
        model.addAttribute(BPA_APPOINTMENT_SCHEDULE, appointmentSchedule);
        model.addAttribute(APPLICATION_NUMBER, applicationNumber);
        return SCHEDULE_APPIONTMENT_NEW;
    }

    @PostMapping("/scrutiny/schedule/{applicationNumber}")
    public String scheduleAppointmentForDocScrutiny(@Valid @ModelAttribute final BpaAppointmentSchedule appointmentSchedule,
                                                    @PathVariable final String applicationNumber, final Model model, final RedirectAttributes redirectAttributes) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        appointmentSchedule.setApplication(bpaApplication);
        bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(), bpaApplication, null, BpaConstants.APPLICATION_STATUS_SCHEDULED, "Forward", null);
        BpaAppointmentSchedule schedule = bpaAppointmentScheduleService.save(appointmentSchedule);
        bpaSmsAndEmailService.sendSMSAndEmailToscheduleAppointment(schedule, bpaApplication);
        redirectAttributes.addFlashAttribute(MESSAGE,
                messageSource.getMessage("msg.new.appoimt", null, null));

        return REDIRECT_APPLICATION_VIEW_APPOINTMENT + schedule.getId();
    }
   
    @GetMapping("/scrutiny/reschedule/{applicationNumber}")
    public String showReScheduleDcoumentScrutiny(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication application = applicationBpaService.findByApplicationNumber(applicationNumber);
        if (validateOnDocumentScrutiny(model, application)) return COMMON_ERROR;
        if(application.getIsRescheduledByEmployee()) {
            model.addAttribute(MESSAGE, "Reschedule appointment by employee already completed, employee can reschedule appointment only once.");
            return COMMON_ERROR;
        }
		List<SlotDetail> slotDetails = rescheduleAppnmtsForDocScrutinyService
				.searchAvailableSlotsForReschedule(application.getId());
		Set<Date> appointmentDates = new LinkedHashSet<>();
        Optional<SlotApplication> activeSlotApplication = application.getSlotApplications().stream().reduce((slotApp1, slotApp2) -> slotApp2);
		for (SlotDetail slotDetail : slotDetails) {
		        if(activeSlotApplication.isPresent()
                   && slotDetail.getSlot().getAppointmentDate().after(activeSlotApplication.get().getSlotDetail().getSlot().getAppointmentDate()))
				appointmentDates.add(slotDetail.getSlot().getAppointmentDate());
		}
		if (slotDetails.isEmpty() || appointmentDates.isEmpty()) {
			model.addAttribute("slotsNotAvailableMsg", messageSource.getMessage("msg.slot.not.available", null, null));
		}
		ScheduleScrutiny scheduleScrutiny = new ScheduleScrutiny();
		scheduleScrutiny.setApplicationId(application.getId());
		if (bpaUtils.logedInuseCitizenOrBusinessUser()) {
			scheduleScrutiny.setReScheduledBy(Source.CITIZENPORTAL.name());
		} else {
			scheduleScrutiny.setReScheduledBy(Source.SYSTEM.name());
        }
        model.addAttribute("appointmentDates", appointmentDates);
        model.addAttribute("bpaApplication", application);
        model.addAttribute("scheduleScrutiny", scheduleScrutiny);
        model.addAttribute("slotDetailList", slotDetails);

        return RESCHEDULE_DOC_SCRUTINY;
    }

    @RequestMapping(value = "/scrutiny/reschedule/{applicationNumber}", method = RequestMethod.POST)
    public String rescheduleDocumentScrutiny(@Valid @ModelAttribute final ScheduleScrutiny scheduleScrutiny, @PathVariable final String applicationNumber, final Model model, final HttpServletRequest request, final RedirectAttributes redirectAttributes) {
        String reScheduleAction = request.getParameter("reScheduleAction");
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        if ("Re-Schedule".equals(reScheduleAction)) {
            if (APPLICATION_STATUS_SCHEDULED.equals(bpaApplication.getStatus().getCode()))
                bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(), bpaApplication, null, "document scrutiny re-scheduled", BpaConstants.WF_RESCHDLE_APPMNT_BUTTON, null);
            if (Source.SYSTEM.name().equals(scheduleScrutiny.getReScheduledBy())) {
                rescheduleAppnmtsForDocScrutinyService.rescheduleAppointmentsForDocumentScrutinyByEmployee(scheduleScrutiny.getApplicationId(), scheduleScrutiny.getAppointmentDate(), scheduleScrutiny.getAppointmentTime());
            } else if (Source.CITIZENPORTAL.name().equals(scheduleScrutiny.getReScheduledBy())) {
                rescheduleAppnmtsForDocScrutinyService.rescheduleAppointmentsForDocumentScrutinyByCitizen(scheduleScrutiny.getApplicationId(), scheduleScrutiny.getAppointmentDate(), scheduleScrutiny.getAppointmentTime());
            }
            model.addAttribute("bpaApplication", bpaApplication);
        } else if ("Auto Re-Schedule".equals(reScheduleAction)) {
            bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(), bpaApplication, null, BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING, BpaConstants.WF_AUTO_RESCHDLE_APPMNT_BUTTON, null);
            String scheduleBy = StringUtils.EMPTY;
            if (Source.SYSTEM.name().equals(scheduleScrutiny.getReScheduledBy())) {
                scheduleBy = "EMPLOYEE";
            } else if (Source.CITIZENPORTAL.name().equals(scheduleScrutiny.getReScheduledBy())) {
                scheduleBy = "CITIZENPORTAL";
            }
            rescheduleAppnmtsForDocScrutinyService.rescheduleAppointmentWhenSlotsNotAvailable(bpaApplication.getId(), scheduleBy);
            model.addAttribute("message", messageSource.getMessage("msg.auto.schedule", new String[]{bpaApplication.getApplicationNumber()}, null));
            return CITIZEN_SUCEESS;
        } else if ("Cancel Application".equals(reScheduleAction)) {
            bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(), bpaApplication, null, "Application cancelled by citizen", BpaConstants.WF_CANCELAPPLICATION_BUTTON, null);
            model.addAttribute("message", messageSource.getMessage("msg.cancel.appln", new String[]{bpaApplication.getApplicationNumber()}, null));
            return CITIZEN_SUCEESS;
        }
        return SCHEDULED_SCRUTINY_DETAILS_RESULT;
    }

    @GetMapping("/scrutiny/view/{applicationNumber}")
    public String viewDocumentScrutinyDetails(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        model.addAttribute("bpaApplication", bpaApplication);
        return SCHEDULED_SCRUTINY_DETAILS_RESULT;
    }

    @GetMapping("/view-appointment/{id}")
    public String viewScheduledAppointment(@PathVariable final Long id, final Model model) {
        List<BpaAppointmentSchedule> appointmentScheduledList = bpaAppointmentScheduleService
                .findByIdAsList(id);
        model.addAttribute(APPOINTMENT_SCHEDULED_LIST, appointmentScheduledList);
        return SCHEDULE_APPIONTMENT_RESULT;
    }

    @RequestMapping(value = {"/scrutiny/slotdetails"}, method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public void registrarOfficeVillageMapping(@RequestParam Date appointmentDate, @RequestParam Long zoneId, HttpServletResponse response) throws IOException {
        List<SlotDetail> slotDetails = rescheduleAppnmtsForDocScrutinyService.getOneSlotDetailsByAppointmentDateAndZoneId(appointmentDate, zoneId);
        final List<JSONObject> jsonObjects = new ArrayList<>();
        if (!slotDetails.isEmpty()) {
            for (final SlotDetail slotDetail : slotDetails) {
                final JSONObject jsonObj = new JSONObject();
                jsonObj.put("appointmentTime", slotDetail.getAppointmentTime());
                jsonObjects.add(jsonObj);
            }
        }
        IOUtils.write(jsonObjects.toString(), response.getWriter());
    }
}

