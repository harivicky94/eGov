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

import org.egov.bpa.master.service.AppointmentLocationsService;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaAppointmentSchedule;
import org.egov.bpa.transaction.entity.enums.AppointmentSchedulePurpose;
import org.egov.bpa.transaction.service.BpaAppointmentScheduleService;
import org.egov.bpa.utils.BpaConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;

import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_DOC_VERIFIED;

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

    @Autowired
    private BpaAppointmentScheduleService bpaAppointmentScheduleService;
    @Autowired
    private AppointmentLocationsService appointmentLocationsService;

    @RequestMapping(value = "/scheduleappointment/{applicationNumber}", method = RequestMethod.GET)
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

    @RequestMapping(value = "/scheduleappointment/{applicationNumber}", method = RequestMethod.POST)
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

    @RequestMapping(value = "/postponeappointment/{scheduleType}/{applicationNumber}", method = RequestMethod.GET)
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

    @RequestMapping(value = "/postponeappointment/{scheduleType}/{applicationNumber}", method = RequestMethod.POST)
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

    @RequestMapping(value = "/scrutiny/schedule/{applicationNumber}", method = RequestMethod.GET)
    public String showScheduleAppointmentForDocScrutiny(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        BpaAppointmentSchedule appointmentSchedule = new BpaAppointmentSchedule();
        appointmentSchedule.setPurpose(AppointmentSchedulePurpose.DOCUMENTSCRUTINY);
        model.addAttribute(APPOINTMENT_LOCATIONS_LIST, appointmentLocationsService.findAllOrderByOrderNumber());
        model.addAttribute(BPA_APPOINTMENT_SCHEDULE, appointmentSchedule);
        model.addAttribute(APPLICATION_NUMBER, applicationNumber);
        return SCHEDULE_APPIONTMENT_NEW;
    }

    @RequestMapping(value = "/scrutiny/schedule/{applicationNumber}", method = RequestMethod.POST)
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

    @RequestMapping(value = "/scrutiny/reschedule/{applicationNumber}", method = RequestMethod.GET)
    public String showReScheduleDcoumentScrutiny(@PathVariable final String applicationNumber, final Model model) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        List<BpaAppointmentSchedule> appointmentScheduledList = bpaAppointmentScheduleService.findByApplication(bpaApplication,
                AppointmentSchedulePurpose.DOCUMENTSCRUTINY);
        BpaAppointmentSchedule appointmentSchedule = new BpaAppointmentSchedule();
        appointmentSchedule.setPurpose(appointmentScheduledList.get(0).getPurpose());
        model.addAttribute(APPOINTMENT_LOCATIONS_LIST, appointmentLocationsService.findAllOrderByOrderNumber());
        model.addAttribute(BPA_APPOINTMENT_SCHEDULE, appointmentSchedule);
        model.addAttribute(APPLICATION_NUMBER, applicationNumber);
        model.addAttribute(APPOINTMENT_SCHEDULED_LIST, appointmentScheduledList);
        model.addAttribute("mode", "postponeappointment");
        return RESCHEDULE_APPIONTMENT;
    }

    @RequestMapping(value = "/scrutiny/reschedule/{applicationNumber}", method = RequestMethod.POST)
    public String rescheduleDocumentScrutiny(@Valid @ModelAttribute final BpaAppointmentSchedule appointmentSchedule,
                                            @PathVariable final String applicationNumber,
                                            @RequestParam Long bpaAppointmentScheduleId, final Model model, final RedirectAttributes redirectAttributes) {
        BpaApplication bpaApplication = applicationBpaService.findByApplicationNumber(applicationNumber);
        BpaAppointmentSchedule parent = bpaAppointmentScheduleService.findById(bpaAppointmentScheduleId);
        appointmentSchedule.setApplication(bpaApplication);
        appointmentSchedule.setPostponed(true);
        appointmentSchedule.setParent(parent);
        bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(), bpaApplication, null, "document scrutiny re-scheduled", BpaConstants.WF_RESCHDLE_APPMNT_BUTTON, null);
        BpaAppointmentSchedule schedule = bpaAppointmentScheduleService.save(appointmentSchedule);
        bpaSmsAndEmailService.sendSMSAndEmailToscheduleAppointment(schedule, bpaApplication);
        redirectAttributes.addFlashAttribute(MESSAGE,
                    messageSource.getMessage("msg.rescheduled.appoimt", null, null));
        return REDIRECT_APPLICATION_VIEW_APPOINTMENT + schedule.getId();
    }

    @RequestMapping(value = "/view-appointment/{id}", method = RequestMethod.GET)
    public String viewScheduledAppointment(@PathVariable final Long id, final Model model) {
        List<BpaAppointmentSchedule> appointmentScheduledList = bpaAppointmentScheduleService
                .findByIdAsList(id);
        model.addAttribute(APPOINTMENT_SCHEDULED_LIST, appointmentScheduledList);
        return SCHEDULE_APPIONTMENT_RESULT;
    }
}
