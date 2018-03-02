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

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaStatus;
import org.egov.bpa.transaction.entity.SlotApplication;
import org.egov.bpa.transaction.repository.BpaStatusRepository;
import org.egov.bpa.transaction.repository.SlotApplicationRepository;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.utils.BpaConstants;
import org.egov.bpa.utils.BpaUtils;
import org.egov.infra.validation.exception.ValidationException;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@Transactional(readOnly = true)
public class BpaApplicationCancellationService {
	@Autowired
	private ApplicationBpaService applicationBpaService;

	@Autowired
	private BpaStatusRepository bpaStatusRepository;

	@Autowired
	private SlotApplicationRepository slotApplicationRepository;

	@Autowired
	private BPASmsAndEmailService bpaSmsAndEmailService;
	
	@Autowired
	private BpaUtils bpaUtils;
	
	@Autowired
	private TransactionTemplate transactionTemplate;

	@Transactional
	public void cancelNonverifiedApplications() {
		Calendar calender = Calendar.getInstance();
		Date todayDate = calender.getTime();
		BpaStatus bpaStatusScheduled = bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_SCHEDULED);
		BpaStatus bpaStatusRescheduled = bpaStatusRepository.findByCode(BpaConstants.APPLICATION_STATUS_RESCHEDULED);
		List<BpaStatus> listOfBpaStatus = new ArrayList<>();
		listOfBpaStatus.add(bpaStatusScheduled);
		listOfBpaStatus.add(bpaStatusRescheduled);
		List<BpaApplication> bpaApplicationList = applicationBpaService
				.findByStatusListOrderByCreatedDate(listOfBpaStatus);
		for (BpaApplication bpaApplication : bpaApplicationList) {
			try {
				TransactionTemplate template = new TransactionTemplate(transactionTemplate.getTransactionManager());
				template.execute(result -> {
			List<SlotApplication> slotApplicationList = slotApplicationRepository
					.findByApplicationOrderByIdDesc(bpaApplication);
			if (slotApplicationList.size() > 0) {
				Date appointmentDate = slotApplicationList.get(0).getSlotDetail().getSlot().getAppointmentDate();
				if (todayDate.compareTo(appointmentDate) > 0) {
					bpaUtils.redirectToBpaWorkFlow(bpaApplication.getCurrentState().getOwnerPosition().getId(),
							bpaApplication, null, "Application is cancelled because citizen not attended for document scrutiny",
							BpaConstants.WF_CANCELAPPLICATION_BUTTON, null);
					applicationBpaService.saveBpaApplication(bpaApplication);
					bpaSmsAndEmailService.sendSMSAndEmailForDocumentScrutiny(slotApplicationList.get(0),
							bpaApplication);
				}
			}
					return true;
				});
			} catch (Exception e) {
				getErrorMessage(e);
			}
			}
	}

	LocalDate convertToLocalDate(Date date) {
		if (date == null)
			return null;
		return new LocalDate(date);
	}
	
	private String getErrorMessage(final Exception exception) {
		String error;
		if (exception instanceof ValidationException)
			error = ((ValidationException) exception).getErrors().get(0).getMessage();
		else
			error = "Error : " + exception;
		return error;
	}
	
	}