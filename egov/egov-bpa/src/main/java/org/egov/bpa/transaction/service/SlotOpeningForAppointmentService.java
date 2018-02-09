package org.egov.bpa.transaction.service;

import java.text.SimpleDateFormat;
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
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SlotOpeningForAppointmentService {

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

	@Transactional
	public void openSlots() {
		List<SlotMapping> slotZoneList = zoneSlotRepository.findByApplicationType(ApplicationType.ALL_OTHER_SERVICES);
		List<AppConfigValues> appConfigValue = appConfigValuesService.getConfigValuesByModuleAndKey("BPA",
				"NOOFDAYSFORASSIGNINGSLOTS");
		String noOfDays = appConfigValue.get(0).getValue();
		Integer scheduledSlotsAllowedForMorning;
		Integer scheduledSlotsAllowedForEvening;
		Integer rescheduledSlotsAllowedForMorning;
		Integer rescheduledSlotsAllowedForEvening;
		Calendar calender = Calendar.getInstance();
		int year = calender.get(Calendar.YEAR);
		final User user = securityUtils.getCurrentUser();
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
		List<Holiday> holidayList = holidayListRepository.findByYearOrderByHolidayDateAsc(String.valueOf(year));
		List<Slot> slots = new ArrayList<>();
		for (SlotMapping slotZone : slotZoneList) {
			calender = Calendar.getInstance();
			if (slotZone.getMaxSlotsAllowed() % 2 == 0) {
				scheduledSlotsAllowedForMorning = slotZone.getMaxSlotsAllowed() / 2;
				scheduledSlotsAllowedForEvening = slotZone.getMaxSlotsAllowed() / 2;
			} else {
				scheduledSlotsAllowedForMorning = (slotZone.getMaxSlotsAllowed() / 2) + 1;
				scheduledSlotsAllowedForEvening = slotZone.getMaxSlotsAllowed() / 2;
			}
			if (slotZone.getMaxRescheduledSlotsAllowed() % 2 == 0) {
				rescheduledSlotsAllowedForMorning = slotZone.getMaxRescheduledSlotsAllowed() / 2;
				rescheduledSlotsAllowedForEvening = slotZone.getMaxRescheduledSlotsAllowed() / 2;
			} else {
				rescheduledSlotsAllowedForMorning = (slotZone.getMaxRescheduledSlotsAllowed() / 2) + 1;
				rescheduledSlotsAllowedForEvening = slotZone.getMaxRescheduledSlotsAllowed() / 2;
			}
			for (int i = 1; i <= Integer.valueOf(noOfDays); i++) {
				Slot slot = new Slot();
				calender.add(Calendar.DAY_OF_YEAR, 1);
				slot.setZone(slotZone.getZone());
				slot.setCreatedBy(user);
				slot.setCreatedDate(new Date());
				for (Holiday holiday : holidayList) {
					if (simpleFormat.format(holiday.getHolidayDate()).equals(simpleFormat.format(calender.getTime()))) {
						calender.add(Calendar.DAY_OF_YEAR, 1);
					}
				}
				slot.setAppointmentDate(calender.getTime());
				List<SlotDetail> slotDetailList = new ArrayList<>();
				for (int j = 1; j <= 2; j++) {
					SlotDetail slotDetail = new SlotDetail();
					if (j == 1) {
						slotDetail.setAppointmentTime("Morning");
						slotDetail.setMaxScheduledSlots(scheduledSlotsAllowedForMorning);
						slotDetail.setMaxRescheduledSlots(rescheduledSlotsAllowedForMorning);
					} else {
						slotDetail.setAppointmentTime("Evening");
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
		slotRepository.save(slots);
	}
}
