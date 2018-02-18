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
package org.egov.bpa.transaction.service;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.bpa.transaction.repository.ApplicationBpaRepository;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.repository.SlotDetailRepository;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ScheduleAppointmentForDocumentScrutinyService {
	@Autowired
	private BpaStatusRepository bpaStatusRepository;

	@Autowired
	private ApplicationBpaRepository applicationBpaRepository;

	@Autowired
	private SlotDetailRepository slotDetailRepository;

	@Autowired
	private SlotApplicationRepository slotApplicationRepository;
	
	@Autowired
	private BPASmsAndEmailService bpaSmsAndEmailService;

	@Autowired
	private BpaUtils bpaUtils;

	@Transactional
	public void scheduleAppointmentsForDocumentScrutiny() {
		List<BpaApplication> bpaApplicationList = applicationBpaRepository.findByStatusOrderByCreatedDateAsc(
				bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING));
		List<BpaApplication> bpaApplications = applicationBpaRepository.findByStatusOrderByCreatedDateAsc(
				bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_REGISTERED));
		bpaApplicationList.addAll(bpaApplications);
		List<SlotDetail> slotDetailList = slotDetailRepository.findSlotDetailOrderByAppointmentDate();
		List<SlotApplication> slotApplicationList = new ArrayList<>();
		if(!bpaApplicationList.isEmpty()) {
			for (BpaApplication application : bpaApplicationList) {
				Long applnZoneId = application.getSiteDetail().get(0).getAdminBoundary().getParent().getId();
				SlotApplication slotApplication = new SlotApplication();
				slotApplication.setApplication(application);
				if (application.getStatus().getCode()
							   .equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING))
					slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);
				else
					slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.SCHEDULE);
				for (SlotDetail slotDetail : slotDetailList) {
					if (slotDetail.getSlot().getZone().getId().equals(applnZoneId)) {
						if (application.getStatus().getCode()
									   .equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
							if (slotDetail.getMaxRescheduledSlots() - slotDetail.getUtilizedRescheduledSlots() > 0) {
								slotDetail.setUtilizedRescheduledSlots(slotDetail.getUtilizedRescheduledSlots() + 1);
								slotApplication.setSlotDetail(slotDetail);
								break;
							} else if (slotDetail.getMaxScheduledSlots() - slotDetail.getUtilizedScheduledSlots() > 0) {
								slotDetail.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots() + 1);
								slotApplication.setSlotDetail(slotDetail);
								break;
							} else
								break;
						} else {
							if (slotDetail.getMaxScheduledSlots() - slotDetail.getUtilizedScheduledSlots() > 0) {
								slotDetail.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots() + 1);
								slotApplication.setSlotDetail(slotDetail);
								break;
							}
						}
					}
				}
				if (slotApplication.getSlotDetail() != null) {
					slotApplicationList.add(slotApplication);
					if (slotApplication.getScheduleAppointmentType().name()
									   .equals(ScheduleAppointmentType.SCHEDULE.name())) {
						bpaUtils.redirectToBpaWorkFlow(application.getCurrentState().getOwnerPosition().getId(), application, null, BpaConstants.APPLICATION_STATUS_SCHEDULED, "Forward", null);
						applicationBpaRepository.save(application);
						bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication,
								application);
					} else if (slotApplication.getScheduleAppointmentType().name()
											  .equals(ScheduleAppointmentType.RESCHEDULE.name())) {
						bpaUtils.redirectToBpaWorkFlow(application.getCurrentState().getOwnerPosition().getId(), application, null, "document scrutiny re-scheduled", BpaConstants.WF_RESCHDLE_APPMNT_BUTTON, null);
						applicationBpaRepository.save(application);
						bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication,
								application);
					}

				}
			}
			slotApplicationRepository.save(slotApplicationList);
		}
	}
}
