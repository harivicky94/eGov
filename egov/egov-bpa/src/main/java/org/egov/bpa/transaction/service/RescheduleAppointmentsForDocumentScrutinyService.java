package org.egov.bpa.transaction.service;

import java.util.Date;
import java.util.List;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.Slot;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.bpa.transaction.repository.ApplicationBpaRepository;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.repository.SlotDetailRepository;
import org.egov.bpa.transaction.repository.SlotRepository;
import org.egov.infra.admin.master.entity.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class RescheduleAppointmentsForDocumentScrutinyService {

	@Autowired
	private ApplicationBpaRepository applicationBpaRepository;

	@Autowired
	private SlotApplicationRepository slotApplicationRepository;

	@Autowired
	private SlotDetailRepository slotDetailRepository;

	@Autowired
	private SlotRepository slotRepository;

	@Autowired
	private BpaStatusRepository bpaStatusRepository;

	@Transactional
	public SlotApplication rescheduleAppointmentsForDocumentScrutinyByCitizen(Long applicationId,
			Date rescheduleAppointmentDate, String appointmentTime) {
		BpaApplication application = applicationBpaRepository.findById(applicationId);
		List<SlotApplication> slotApplication = slotApplicationRepository.findByApplicationOrderByIdDesc(application);
		// free up previous slot
		SlotDetail slotDetail = slotApplication.get(0).getSlotDetail();
		if (slotApplication.get(0).getScheduleAppointmentType().toString()
				.equals(ScheduleAppointmentType.SCHEDULE.toString()))
			slotDetail.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots() - 1);
		else
			slotDetail.setUtilizedRescheduledSlots(slotDetail.getUtilizedRescheduledSlots() - 1);
		slotDetailRepository.save(slotDetail);
		// build new slot application and reschedule appointment
		SlotApplication slotApp = new SlotApplication();
		slotApp.setApplication(application);
		slotApp.setIsRescheduledByCitizen(true);
		slotApp.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);
		SlotDetail slotDet = slotDetailRepository.findByAppointmentDateTimeAndZone(rescheduleAppointmentDate,
				appointmentTime, application.getSiteDetail().get(0).getAdminBoundary());
		if (slotDet.getMaxRescheduledSlots() - slotDet.getUtilizedRescheduledSlots() > 0)
			slotDet.setUtilizedRescheduledSlots(slotDet.getUtilizedRescheduledSlots() + 1);
		else
			slotDet.setUtilizedScheduledSlots(slotDet.getUtilizedScheduledSlots() + 1);
		slotApp.setSlotDetail(slotDet);
		BpaStatus bpaStatus = bpaStatusRepository.findByCode("Rescheduled For Document Scrutiny");
		application.setStatus(bpaStatus);
		applicationBpaRepository.save(application);
		slotApplicationRepository.save(slotApp);
		return slotApp;
	}

	@Transactional
	public SlotApplication rescheduleAppointmentsForDocumentScrutinyByEmployee(Long applicationId,
			Date rescheduleAppointmentDate, String appointmentTime) {
		BpaApplication bpaApplication = applicationBpaRepository.findById(applicationId);
		List<SlotApplication> slotApplication = slotApplicationRepository
				.findByApplicationOrderByIdDesc(bpaApplication);
		// free up previous slot
		SlotDetail slotDetail = slotApplication.get(0).getSlotDetail();
		if (slotApplication.get(0).getScheduleAppointmentType().toString()
				.equals(ScheduleAppointmentType.SCHEDULE.toString()))
			slotDetail.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots() - 1);
		else
			slotDetail.setUtilizedRescheduledSlots(slotDetail.getUtilizedRescheduledSlots() - 1);
		slotDetailRepository.save(slotDetail);
		// build new slot application and reschedule appointment
		SlotApplication slotApp = new SlotApplication();
		slotApp.setApplication(bpaApplication);
		slotApp.setIsRescheduledByEmployee(true);
		slotApp.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);
		SlotDetail slotDet = slotDetailRepository.findByAppointmentDateTimeAndZone(rescheduleAppointmentDate,
				appointmentTime, bpaApplication.getSiteDetail().get(0).getAdminBoundary());
		if (slotDet.getMaxRescheduledSlots() - slotDet.getUtilizedRescheduledSlots() > 0)
			slotDet.setUtilizedRescheduledSlots(slotDet.getUtilizedRescheduledSlots() + 1);
		else
			slotDet.setUtilizedScheduledSlots(slotDet.getUtilizedScheduledSlots() + 1);
		slotApp.setSlotDetail(slotDet);
		BpaStatus bpaStatus = bpaStatusRepository.findByCode("Rescheduled For Document Scrutiny");
		bpaApplication.setStatus(bpaStatus);
		applicationBpaRepository.save(bpaApplication);
		slotApplicationRepository.save(slotApp);
		return slotApp;
	}

	public List<Slot> searchAvailableSlotsForReschedule(Long applicationId) {
		BpaApplication bpaApplication = applicationBpaRepository.findById(applicationId);
		Boundary zone = bpaApplication.getSiteDetail().get(0).getAdminBoundary();
		List<SlotApplication> slotApplication = slotApplicationRepository
				.findByApplicationOrderByIdDesc(bpaApplication);
		Date appointmentDate = slotApplication.get(0).getSlotDetail().getSlot().getAppointmentDate();
		List<Slot> slotsList = slotRepository.findSlotsByAppointmentDate(appointmentDate);
		return slotsList;
	}

	@Transactional
	public void rescheduleAppointmentWhenSlotsNotAvailable(Long id) {
		BpaApplication bpaApplication = applicationBpaRepository.findById(id);
		List<SlotApplication> slotApplication = slotApplicationRepository
				.findByApplicationOrderByIdDesc(bpaApplication);
		SlotDetail slotDetail = slotApplication.get(0).getSlotDetail();
		if (slotApplication.get(0).getScheduleAppointmentType().toString()
				.equals(ScheduleAppointmentType.SCHEDULE.toString()))
			slotDetail.setUtilizedScheduledSlots(slotDetail.getUtilizedScheduledSlots() - 1);
		else
			slotDetail.setUtilizedRescheduledSlots(slotDetail.getUtilizedRescheduledSlots() - 1);
		slotDetailRepository.save(slotDetail);
		BpaStatus bpaStatus = bpaStatusRepository.findByCode("Pending For Rescheduling For Document Scrutiny");
		bpaApplication.setStatus(bpaStatus);
		applicationBpaRepository.save(bpaApplication);
	}

}
