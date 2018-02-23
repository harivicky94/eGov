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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.bpa.master.service.SlotMappingService;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.Slot;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.repository.SlotDetailRepository;
import org.egov.bpa.transaction.repository.SlotRepository;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.repository.BoundaryRepository;
import org.egov.infra.validation.exception.ValidationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
public class ScheduleAppointmentForDocumentScrutinyService {

	@Autowired
	private SlotMappingService slotMappingService;
	@Autowired
	private BpaStatusRepository bpaStatusRepository;
	@Autowired
	private ApplicationBpaService applicationBpaService;
	@Autowired
	private SlotRepository slotRepository;
	@Autowired
	private SlotApplicationService slotApplicationService;
	@Autowired
	private SlotDetailRepository slotDetailRepository;
	@Autowired
	private BPASmsAndEmailService bpaSmsAndEmailService;
	@Autowired
	private BoundaryRepository boundaryRepository;
	@Autowired
	private BpaUtils bpaUtils;
	@Autowired
	private SlotApplicationRepository slotApplicationRepository;
	@Autowired
	private TransactionTemplate transactionTemplate;

	public void scheduleAppointmentsForDocumentScrutiny() {
		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 2);
		List<Boundary> zonesList = slotMappingService.slotfindZoneByApplType(ApplicationType.ALL_OTHER_SERVICES);
		for (Boundary bndry : zonesList) {
			List<Slot> slotList = slotRepository.findByZoneAndApplicationDate(bndry, calendar.getTime());
			if (slotList.size() > 0) {
				for (Slot slot : slotList) {
					List<SlotDetail> slotDetailList = slotDetailRepository.findBySlot(slot);
					slot.setSlotDetail(slotDetailList);
					if (slot.getSlotDetail().size() > 0) {
						Integer totalAvailableSlots = 0;
						for (SlotDetail slotDetail : slot.getSlotDetail()) {
							Integer diffScheduledSlots = 0;
							Integer diffRescheduledSlots = 0;
							if (slotDetail.getMaxScheduledSlots() > slotDetail.getUtilizedScheduledSlots())
								diffScheduledSlots = slotDetail.getMaxScheduledSlots()
										- slotDetail.getUtilizedScheduledSlots();
							if (slotDetail.getMaxRescheduledSlots() > slotDetail.getUtilizedRescheduledSlots())
								diffRescheduledSlots = slotDetail.getMaxRescheduledSlots()
										- slotDetail.getUtilizedRescheduledSlots();
							totalAvailableSlots = totalAvailableSlots + diffScheduledSlots + diffRescheduledSlots;
						}
						BpaStatus bpaStatusPendingForRescheduling = bpaStatusRepository
								.findByCode(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING);
						BpaStatus bpaStatusRegistered = bpaStatusRepository
								.findByCode(BpaConstants.APPLICATION_STATUS_REGISTERED);
						List<BpaStatus> bpaStatusList = new ArrayList<>();
						bpaStatusList.add(bpaStatusRegistered);
						bpaStatusList.add(bpaStatusPendingForRescheduling);
						List<Boundary> boundaryList = boundaryRepository.findActiveImmediateChildrenWithOutParent(slot.getZone().getId());
						List<BpaApplication> bpaApplications = applicationBpaService
								.getBpaApplicationsByCriteria(bpaStatusList, boundaryList, totalAvailableSlots);
						if (bpaApplications.size() > 0) {
							for (BpaApplication bpaApp : bpaApplications) {
								try {
									TransactionTemplate template = new TransactionTemplate(
											transactionTemplate.getTransactionManager());
									template.execute(result -> {
										for (SlotDetail slotDetail : slot.getSlotDetail()) {
											if (bpaApp.getStatus().getCode().toString()
													.equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
												List<SlotApplication> slotApplications = slotApplicationRepository
														.findByApplicationOrderByIdDesc(bpaApp);
												slotApplications.get(0).setActive(false);
												slotApplicationRepository.save(slotApplications.get(0));
												if (slotDetail.getMaxRescheduledSlots()
														- slotDetail.getUtilizedRescheduledSlots() > 0) {
													slotDetail.setUtilizedRescheduledSlots(
															slotDetail.getUtilizedRescheduledSlots() + 1);
													SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
															slotDetail);
													createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
															slotApplication);
													break;
												} else if (slotDetail.getMaxScheduledSlots()
														- slotDetail.getUtilizedScheduledSlots() > 0) {
													slotDetail.setUtilizedScheduledSlots(
															slotDetail.getUtilizedScheduledSlots() + 1);
													SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
															slotDetail);
													createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
															slotApplication);
													break;
												}
											} else {
												if (slotDetail.getMaxScheduledSlots()
														- slotDetail.getUtilizedScheduledSlots() > 0) {
													slotDetail.setUtilizedScheduledSlots(
															slotDetail.getUtilizedScheduledSlots() + 1);
													SlotApplication slotApplication = buildSlotApplicationObject(bpaApp,
															slotDetail);
													createSlotApplicationAndUpdateStatus(slotDetail, bpaApp,
															slotApplication);
													break;
												}
											}
										}
										return true;
									});
								} catch (Exception e) {
									getErrorMessage(e);
								}
							}
						}
					}
				}
			}
		}
	}

	private SlotApplication buildSlotApplicationObject(BpaApplication bpaApp, SlotDetail slotDetail) {
		SlotApplication slotApplication = new SlotApplication();
		slotApplication.setActive(true);
		slotApplication.setApplication(bpaApp);
		slotApplication.setSlotDetail(slotDetail);
		if (bpaApp.getStatus().getCode().toString().equals(BpaConstants.APPLICATION_STATUS_PENDING_FOR_RESCHEDULING)) {
			slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);
			bpaApp.setStatus(bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_RESCHEDULED));
			return slotApplication;
		} else {
			slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.SCHEDULE);
			bpaApp.setStatus(bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_SCHEDULED));
			return slotApplication;
		}

	}

	private void createSlotApplicationAndUpdateStatus(SlotDetail slotDetail, BpaApplication bpaApp,
			SlotApplication slotApplication) {
		slotApplicationService.save(slotApplication);
		applicationBpaService.saveBpaApplication(bpaApp);
		if (slotApplication.getScheduleAppointmentType().toString()
				.equals(ScheduleAppointmentType.RESCHEDULE.toString())) {
			bpaUtils.redirectToBpaWorkFlow(bpaApp.getCurrentState().getOwnerPosition().getId(), bpaApp, null,
					"document scrutiny re-scheduled", BpaConstants.WF_RESCHDLE_APPMNT_BUTTON, null);
		} else if (slotApplication.getScheduleAppointmentType().toString()
				.equals(ScheduleAppointmentType.SCHEDULE.toString())) {
			bpaUtils.redirectToBpaWorkFlow(
					slotApplication.getApplication().getCurrentState().getOwnerPosition().getId(),
					slotApplication.getApplication(), null, BpaConstants.APPLICATION_STATUS_SCHEDULED, "Forward", null);
		}
		bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication, bpaApp);
	}

	private String getErrorMessage(final Exception exception) {
		String error;
		if (exception instanceof ValidationException)
			error = ((ValidationException) exception).getErrors().get(0).getMessage();
		else
			error = "Error : " + exception;
		return error;
	}
	
	public void scheduleOneDayPermitApplicationsForDocumentScrutiny(BpaApplication bpaApplication,SlotDetail slotDetail) {
		SlotApplication slotApplication = buildSlotApplicationObject(bpaApplication, slotDetail);
		slotDetail.setUtilizedScheduledSlots(
				slotDetail.getUtilizedScheduledSlots() + 1);
		slotApplicationService.save(slotApplication);
		applicationBpaService.saveBpaApplication(bpaApplication);
		bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplication, bpaApplication);
	}

}
