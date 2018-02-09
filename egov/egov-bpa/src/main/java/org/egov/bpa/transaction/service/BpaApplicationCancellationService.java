package org.egov.bpa.transaction.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.repository.ApplicationBpaRepository;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class BpaApplicationCancellationService {
	@Autowired
	private ApplicationBpaRepository applicationBpaRepository;

	@Autowired
	private BpaStatusRepository bpaStatusRepository;

	@Autowired
	private SlotApplicationRepository slotApplicationRepository;

	@Transactional
	public void cancelNonverifiedApplications() {
		Calendar calender = Calendar.getInstance();
		Date todayDate = calender.getTime();
		SimpleDateFormat simpleFormat = new SimpleDateFormat("yyyy-MM-dd");
		List<BpaApplication> bpaApplicationList = applicationBpaRepository
				.findByStatusOrderByCreatedDateAsc(bpaStatusRepository.findByCode("Scheduled For Document Scrutiny"));
		List<BpaApplication> bpaApplications = applicationBpaRepository
				.findByStatusOrderByCreatedDateAsc(bpaStatusRepository.findByCode("Rescheduled For Document Scrutiny"));
		bpaApplicationList.addAll(bpaApplications);
		List<BpaApplication> cancelledBpaApplicationList = new ArrayList<>();
		for (BpaApplication bpaApplication : bpaApplicationList) {
			List<SlotApplication> slotApplicationList = slotApplicationRepository
					.findByApplicationOrderByIdDesc(bpaApplication);
			if (slotApplicationList.size() > 0) {
				Date appointmentDate = slotApplicationList.get(0).getSlotDetail().getSlot().getAppointmentDate();
				if (simpleFormat.format(todayDate).equals(simpleFormat.format(appointmentDate))) {
					BpaStatus status = bpaStatusRepository.findByCodeAndModuleType("Cancelled", "REGISTRATION");
					bpaApplication.setStatus(status);
					cancelledBpaApplicationList.add(bpaApplication);
				}
			}
		}
		applicationBpaRepository.save(cancelledBpaApplicationList);
	}
}
