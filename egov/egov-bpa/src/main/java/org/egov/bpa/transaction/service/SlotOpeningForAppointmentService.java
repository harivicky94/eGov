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
import java.util.Date;
import java.util.List;

import org.egov.bpa.master.entity.Holiday;
import org.egov.bpa.master.entity.SlotMapping;
import org.egov.bpa.master.entity.enums.ApplicationType;
import org.egov.bpa.master.repository.HolidayListRepository;
import org.egov.bpa.master.repository.SlotMappingRepository;
import org.egov.bpa.transaction.entity.Slot;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.repository.SlotRepository;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.utils.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SlotOpeningForAppointmentService {

	private static final String MODULE_NAME = "BPA";
	private static final String APP_CONFIG_KEY = "NOOFDAYSFORASSIGNINGSLOTS";
	private static final String APP_CONFIG_KEY_ONEDAYPERMIT = "NOOFDAYSFORASSIGNING_ONEDAYPERMIT_SLOTS";
	private static final String APP_TIME_MORNING = "Morning";
	private static final String APP_TIME_EVENING = "Evening";

	@Autowired
	private SlotMappingRepository zoneSlotRepository;

	@Autowired
	private SecurityUtils securityUtils;

	@Autowired
	private HolidayListRepository holidayListRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private AppConfigValueService appConfigValuesService;

	/*
	 * public void ddd(){ LocalDateTime yourDate = LocalDateTime.now();
	 * System.out.println(yourDate.getWeekOfWeekyear()); int weekOfyear =
	 * yourDate.getWeekOfWeekyear(); //Fetch Week Start Date for Given Week
	 * Number DateTime weekStartDate = new
	 * DateTime().withWeekOfWeekyear(weekOfyear);
	 * System.out.println(weekStartDate.toString()); //Fetch Specific Days for
	 * given week DateTime wedDateTime =
	 * weekStartDate.withDayOfWeek(DateTimeConstants.WEDNESDAY); }
	 */

	@Transactional
	public void openSlots() {
		List<SlotMapping> slotZoneList = zoneSlotRepository.findByApplType(ApplicationType.ALL_OTHER_SERVICES);
		List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey(MODULE_NAME,
				APP_CONFIG_KEY);
		String noOfDays = appConfigValue.get(0).getValue();
		Integer scheduledSlotsAllowedForMorning = 0;
		Integer scheduledSlotsAllowedForEvening = 0;
		Integer rescheduledSlotsAllowedForMorning = 0;
		Integer rescheduledSlotsAllowedForEvening = 0;
		Calendar calender = Calendar.getInstance();
		int year = calender.get(Calendar.YEAR);
		final User user = securityUtils.getCurrentUser();
		List<Holiday> holidayList = holidayListRepository.findByYearOrderByHolidayDateAsc(String.valueOf(year));
		List<Slot> slots = new ArrayList<>();
		for (SlotMapping slotZone : slotZoneList) {
			calender = Calendar.getInstance();
			if (slotZone.getMaxSlotsAllowed() != null && slotZone.getMaxSlotsAllowed() > 0) {
				if (slotZone.getMaxSlotsAllowed() % 2 == 0) {
					scheduledSlotsAllowedForMorning = slotZone.getMaxSlotsAllowed() / 2;
					scheduledSlotsAllowedForEvening = slotZone.getMaxSlotsAllowed() / 2;
				} else {
					scheduledSlotsAllowedForMorning = (slotZone.getMaxSlotsAllowed() / 2) + 1;
					scheduledSlotsAllowedForEvening = slotZone.getMaxSlotsAllowed() / 2;
				}
			}
			if (slotZone.getMaxRescheduledSlotsAllowed() != null && slotZone.getMaxRescheduledSlotsAllowed() > 0) {
				if (slotZone.getMaxRescheduledSlotsAllowed() % 2 == 0) {
					rescheduledSlotsAllowedForMorning = slotZone.getMaxRescheduledSlotsAllowed() / 2;
					rescheduledSlotsAllowedForEvening = slotZone.getMaxRescheduledSlotsAllowed() / 2;
				} else {
					rescheduledSlotsAllowedForMorning = (slotZone.getMaxRescheduledSlotsAllowed() / 2) + 1;
					rescheduledSlotsAllowedForEvening = slotZone.getMaxRescheduledSlotsAllowed() / 2;
				}
			}
			for (int i = 1; i <= Integer.valueOf(noOfDays); i++) {
				calender.add(Calendar.DAY_OF_YEAR, 1);
				for (Holiday holiday : holidayList) {
					if (DateUtils.toDefaultDateFormat(convertToLocalDate(holiday.getHolidayDate()))
							.equals(DateUtils.toDefaultDateFormat(convertToLocalDate(calender.getTime())))) {
						calender.add(Calendar.DAY_OF_YEAR, 1);
					}
				}
				if (slotRepository.findByZoneAndAppointmentDate(slotZone.getZone(), calender.getTime()).isEmpty()) {
					Slot slot = new Slot();
					slot.setZone(slotZone.getZone());
					slot.setCreatedBy(user);
					slot.setCreatedDate(new Date());
					slot.setAppointmentDate(calender.getTime());
					List<SlotDetail> slotDetailList = new ArrayList<>();
					for (int j = 1; j <= 2; j++) {
						SlotDetail slotDetail = new SlotDetail();
						if (j == 1) {
							slotDetail.setAppointmentTime(APP_TIME_MORNING);
							slotDetail.setMaxScheduledSlots(scheduledSlotsAllowedForMorning);
							slotDetail.setMaxRescheduledSlots(rescheduledSlotsAllowedForMorning);
						} else {
							slotDetail.setAppointmentTime(APP_TIME_EVENING);
							slotDetail.setMaxScheduledSlots(scheduledSlotsAllowedForEvening);
							slotDetail.setMaxRescheduledSlots(rescheduledSlotsAllowedForEvening);
						}
						slotDetail.setUtilizedScheduledSlots(0);
						slotDetail.setUtilizedRescheduledSlots(0);
						slotDetail.setCreatedDate(new Date());
						slotDetail.setCreatedBy(user);
						slotDetail.setSlot(slot);
						slotDetailList.add(slotDetail);
					}
					slot.setSlotDetail(slotDetailList);

					slots.add(slot);
				}
			}
		}
		slotRepository.save(slots);
	}

	LocalDate convertToLocalDate(Date date) {
		if (date == null)
			return null;
		return new LocalDate(date);
	}

	@Transactional
	public SlotDetail openSlotsForDocumentScrutiny(Boundary zone, Boundary revWard, Boundary elecWard) {
		SlotDetail slotDetailToBeUsed = null;
		Slot slot = new Slot();
		Integer scheduledSlotsAllowedForMorning = 0;
		Integer scheduledSlotsAllowedForEvening = 0;
		Calendar calendar = Calendar.getInstance();
		Integer weeksOfYear = calendar.getActualMaximum(Calendar.WEEK_OF_YEAR);
		LocalDateTime yourDate = LocalDateTime.now();
		final User user = securityUtils.getCurrentUser();
		List<SlotMapping> slotMapping = zoneSlotRepository.findByZoneRevenueWardElectionWardAndAppType(zone, revWard, elecWard, ApplicationType.ONE_DAY_PERMIT);
		// TODO : if no slotmapping data found then need to make new insertion
		// TODO : consider cross year no of weeks ie week falling in end of
		// current and start of next year
		if (slotMapping != null && !slotMapping.isEmpty()) {
			int weekOfyear = yourDate.getWeekOfWeekyear();
			// Fetch Week Start Date for Given Week Number
			DateTime weekStartDate = new DateTime().withWeekOfWeekyear(weekOfyear);
			// Fetch Specific Days for given week
			DateTime weekDateTime = weekStartDate.withDayOfWeek(Integer.parseInt(slotMapping.get(0).getDay().toString()));
			// Day falling in same week
			if(!(Integer.parseInt(slotMapping.get(0).getDay().toString()) > yourDate.getDayOfWeek())) {
				weekOfyear = weekOfyear + 1;
				weekStartDate = new DateTime().withWeekOfWeekyear(weekOfyear);
				// Fetch Specific Days for given week
				weekDateTime = weekStartDate.withDayOfWeek(Integer.parseInt(slotMapping.get(0).getDay().toString()));
			} 
			Boolean flag = true;
			while (flag) {
				// Not a holiday
				if (holidayListRepository.findByHolidayDate(weekDateTime.toDate()) == null) {
					Slot dbSlot = slotRepository.findByZoneAndElectionWardAndAppointmentDate(zone, elecWard,
							weekDateTime.toDate());
					if (dbSlot != null) {
						if (!dbSlot.getSlotDetail().isEmpty()) {
							for (SlotDetail sd : dbSlot.getSlotDetail()) {
								if (sd.getMaxScheduledSlots() > sd.getUtilizedScheduledSlots()) {
									slotDetailToBeUsed = sd;
									break;
								}
							}
						}
						// If current week slots are full. Pick next week.
						if(slotDetailToBeUsed == null) {
							flag = true;
							weekOfyear = weekOfyear + 1;
							weekStartDate = new DateTime().withWeekOfWeekyear(weekOfyear);
							// Fetch Specific Days for given week
							weekDateTime = weekStartDate.withDayOfWeek(Integer.parseInt(slotMapping.get(0).getDay().toString()));
							continue;
						}
					}
					// Create New Slot
					if (slotDetailToBeUsed == null) {
						if (slotMapping.get(0).getMaxSlotsAllowed() != null && slotMapping.get(0).getMaxSlotsAllowed() > 0) {
							if (slotMapping.get(0).getMaxSlotsAllowed() % 2 == 0) {
								scheduledSlotsAllowedForMorning = slotMapping.get(0).getMaxSlotsAllowed() / 2;
								scheduledSlotsAllowedForEvening = slotMapping.get(0).getMaxSlotsAllowed() / 2;
							} else {
								scheduledSlotsAllowedForMorning = (slotMapping.get(0).getMaxSlotsAllowed() / 2) + 1;
								scheduledSlotsAllowedForEvening = slotMapping.get(0).getMaxSlotsAllowed() / 2;
							}
						}
						slot.setZone(slotMapping.get(0).getZone());
						slot.setElectionWard(slotMapping.get(0).getElectionWard());
						slot.setCreatedBy(user);
						slot.setCreatedDate(new Date());
						slot.setAppointmentDate(weekDateTime.toDate());
						List<SlotDetail> slotDetailList = new ArrayList<>();
						for (int j = 1; j <= 2; j++) {
							SlotDetail slotDetail = new SlotDetail();
							if (j == 1) {
								slotDetail.setAppointmentTime(APP_TIME_MORNING);
								slotDetail.setMaxScheduledSlots(scheduledSlotsAllowedForMorning);

							} else {
								slotDetail.setAppointmentTime(APP_TIME_EVENING);
								slotDetail.setMaxScheduledSlots(scheduledSlotsAllowedForEvening);

							}
							slotDetail.setUtilizedScheduledSlots(0);
							slotDetail.setMaxRescheduledSlots(0);
							slotDetail.setUtilizedRescheduledSlots(0);
							slotDetail.setCreatedDate(new Date());
							slotDetail.setCreatedBy(user);
							slotDetail.setSlot(slot);
							slotDetailList.add(slotDetail);
						}
						slot.setSlotDetail(slotDetailList);
						slot = slotRepository.save(slot);
						slotDetailToBeUsed = slot.getSlotDetail().get(0);
					}
					flag = false;
				} // if its holiday pick other date
				else {
					flag = true;
					weekOfyear = weekOfyear + 1;
					weekStartDate = new DateTime().withWeekOfWeekyear(weekOfyear);
					// Fetch Specific Days for given week
					weekDateTime = weekStartDate.withDayOfWeek(Integer.parseInt(slotMapping.get(0).getDay().toString()));

				}
			}
		} 
		return slotDetailToBeUsed;
	}
}