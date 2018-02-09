package org.egov.bpa.transaction.service;

import java.util.ArrayList;
import java.util.List;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.entity.SlotDetail;
import org.egov.bpa.transaction.entity.enums.ScheduleAppointmentType;
import org.egov.bpa.transaction.repository.ApplicationBpaRepository;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.repository.SlotDetailRepository;
import org.egov.infra.admin.master.entity.Boundary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

	@Transactional
	public void scheduleAppointmentsForDocumentScrutiny() {
		List<BpaApplication> bpaApplicationList = applicationBpaRepository.findByStatusOrderByCreatedDateAsc(
				bpaStatusRepository.findByCode("Pending For Rescheduling For Document Scrutiny"));
		List<BpaApplication> bpaApplications = applicationBpaRepository
				.findByStatusOrderByCreatedDateAsc(bpaStatusRepository.findByCode("Registered"));
		bpaApplicationList.addAll(bpaApplications);
		List<SlotDetail> slotDetailList = slotDetailRepository.findSlotDetailOrderByAppointmentDate();
		List<SlotApplication> slotApplicationList = new ArrayList<>();
		for (int i = 0; i < bpaApplicationList.size(); i++) {
			Boundary zone = bpaApplicationList.get(i).getSiteDetail().get(0).getAdminBoundary();
			SlotApplication slotApplication = new SlotApplication();
			slotApplication.setApplication(bpaApplicationList.get(i));
			if (bpaApplicationList.get(i).getStatus().getCode().equals("Pending For Rescheduling Of Document Scrutiny"))
				slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.RESCHEDULE);
			else
				slotApplication.setScheduleAppointmentType(ScheduleAppointmentType.SCHEDULE);
			for (SlotDetail slotDetail : slotDetailList) {
				if (slotDetail.getSlot().getZone().equals(zone)) {
					if (bpaApplicationList.get(i).getStatus().getCode()
							.equals("Pending For Rescheduling For Document Scrutiny")) {
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
				BpaStatus bpaStatus = bpaStatusRepository.findByCode("Scheduled For Document Scrutiny");
				bpaApplicationList.get(i).setStatus(bpaStatus);
				applicationBpaRepository.save(bpaApplicationList.get(i));
			}
		}
		slotApplicationRepository.save(slotApplicationList);
	}
}
