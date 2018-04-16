/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2016>  eGovernments Foundation
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

import org.apache.commons.lang.StringUtils;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.BpaAppointmentSchedule;
import org.egov.bpa.utils.BpaConstants;
import org.egov.eis.service.EisCommonService;
import org.egov.eis.service.PositionMasterService;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.workflow.entity.State;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.pims.commons.Position;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class BpaThirdPartyService {
	private static final String APPOINTMENT_FOR_INSPECTION_RESCHEDULED = "Appointment for inspection rescheduled";
	private static final String APPOINTMENT_FOR_INSPECTION_SCHEDULED = "Appointment for inspection scheduled";
	private static final String USER = "user";
	private static final String DATE = "date";
	private static final String STATUS = "status";
	private static final String UPDATED_BY = "updatedBy";
	private static final String COMMENTS = "comments";
	private static final String INSPECTION = "INSPECTION";
	private static final String DEPARTMENT = "department";
	@Autowired
	private EisCommonService eisCommonService;
	@Autowired
	private PositionMasterService positionMasterService;
	@Autowired
	private ApplicationBpaService applicationBpaService;
	@Autowired
	@Qualifier("parentMessageSource")
	private MessageSource bpaMessageSource;
	@Autowired
	private SecurityUtils securityUtils;

	public List<HashMap<String, Object>> getHistory(final BpaApplication application) {
		User userObject;
		final List<HashMap<String, Object>> historyTable = new ArrayList<>();
		final State<Position> workflowState = application.getState();
		final HashMap<String, Object> workFlowHistory = new HashMap<>(0);
		if (null != workflowState) {
			if (null != application.getStateHistory() && !application.getStateHistory().isEmpty())
				Collections.reverse(application.getStateHistory());

			for (final StateHistory<Position> stateHistory : application.getStateHistory()) {
				final HashMap<String, Object> historyMap = new HashMap<>(0);
				historyMap.put(DATE, stateHistory.getDateInfo());
				historyMap.put(COMMENTS, stateHistory.getComments() == null ? "" : stateHistory.getComments());
				historyMap.put(UPDATED_BY, stateHistory.getLastModifiedBy().getUsername() + "::"
										   + stateHistory.getLastModifiedBy().getName());
				historyMap.put(STATUS, stateHistory.getValue());
				Position owner = stateHistory.getOwnerPosition();
				userObject = stateHistory.getOwnerUser();

				setEmployeeDetails(userObject, historyMap, owner);
				historyTable.add(historyMap);
			}
			buildApplicationHistoryForSchedulingAppointments(application, historyTable);
			workFlowHistory.put(DATE, workflowState.getDateInfo());
			workFlowHistory.put(COMMENTS, workflowState.getComments() == null ? "" : workflowState.getComments());
			workFlowHistory.put(UPDATED_BY,
					workflowState.getLastModifiedBy().getUsername() + "::" + workflowState.getLastModifiedBy().getName());
			workFlowHistory.put(STATUS, workflowState.getValue());
			final Position ownerPosition = workflowState.getOwnerPosition();
			userObject = workflowState.getOwnerUser();
			setEmployeeDetails(userObject, workFlowHistory, ownerPosition);
			historyTable.add(workFlowHistory);
		}
		historyTable.sort((applnHsty1, applnHsty2) -> applnHsty1.get(DATE).toString().compareTo(applnHsty2.get(DATE).toString()));
		return historyTable;
	}

	private void setEmployeeDetails(User userObject, HashMap<String, Object> historyMap, Position owner) {
		if (null != userObject) {
			historyMap.put(USER, userObject.getUsername() + "::" + userObject.getName());
			historyMap.put(DEPARTMENT,
					eisCommonService.getDepartmentForUser(userObject.getId()) == null ? "" : eisCommonService
							.getDepartmentForUser(userObject.getId()).getName());
		} else if (null != owner && null != owner.getDeptDesig()) {
			userObject = getUserPositionByPassingPosition(owner.getId());
			historyMap
					.put(USER, userObject.getUsername() == null ? ""
																: userObject.getUsername() + "::" + userObject.getName());
			historyMap.put(DEPARTMENT, owner.getDeptDesig().getDepartment() == null ? "" : owner.getDeptDesig()
																								.getDepartment().getName());
		}
	}

	private void buildApplicationHistoryForSchedulingAppointments(final BpaApplication application,
																  final List<HashMap<String, Object>> historyTable) {
		if (!application.getAppointmentSchedule().isEmpty()) {
			for (BpaAppointmentSchedule appointmentSchedule : application.getAppointmentSchedule()) {
				if (INSPECTION.equals(appointmentSchedule.getPurpose().name())) {
					final HashMap<String, Object> appointmentHistory = new HashMap<>();
					appointmentHistory.put(DATE, appointmentSchedule.getCreatedDate());
					String comments = StringUtils.EMPTY;
					String status = StringUtils.EMPTY;
					if (INSPECTION.equals(appointmentSchedule.getPurpose().name()) && appointmentSchedule.isPostponed()) {
						status = APPOINTMENT_FOR_INSPECTION_RESCHEDULED;
						comments = APPOINTMENT_FOR_INSPECTION_RESCHEDULED + " for " + appointmentSchedule.getPostponementReason();
					} else if (INSPECTION.equals(appointmentSchedule.getPurpose().name()) && !appointmentSchedule.isPostponed()) {
						status = APPOINTMENT_FOR_INSPECTION_SCHEDULED;
						comments = APPOINTMENT_FOR_INSPECTION_SCHEDULED;
					}
					appointmentHistory.put(COMMENTS, comments);
					appointmentHistory.put(UPDATED_BY, appointmentSchedule.getLastModifiedBy().getUsername() + "::"
													   + appointmentSchedule.getLastModifiedBy().getName());
					appointmentHistory.put(STATUS, status);

					final Position owner = positionMasterService
							.getCurrentPositionForUser(appointmentSchedule.getCreatedBy().getId());
					User user = appointmentSchedule.getCreatedBy();

					setEmployeeDetails(user, appointmentHistory, owner);
					historyTable.add(appointmentHistory);
				}
			}
		}
	}

	public User getUserPositionByPassingPosition(Long ownerPosition) {
		return eisCommonService.getUserForPosition(ownerPosition, new Date());
	}

	public Map<String, String> checkIsEdcrUsedInBpaApplication(final String eDcrNumber) {
		Map<String, String> eDcrApplicationDetails = new HashMap<>();
		BpaApplication bpaApplication = applicationBpaService.findApplicationByEDCRNumber(eDcrNumber);
		if (null == bpaApplication || bpaApplication != null && null == bpaApplication.getState() && BpaConstants.APPLICATION_STATUS_CANCELLED.equals(bpaApplication.getStatus())) {
			eDcrApplicationDetails.put("isExists", "false");
			eDcrApplicationDetails.put(BpaConstants.MESSAGE, "Not used");
		} else {
			String message = bpaMessageSource.getMessage("msg.dcr.exist.with.appln",
					new String[]{securityUtils.getCurrentUser().getName(), bpaApplication.geteDcrNumber(), bpaApplication.getApplicationNumber()},
					null);
			eDcrApplicationDetails.put("isExists", "true");
			eDcrApplicationDetails.put("applnNoUsedEdcr", bpaApplication.getApplicationNumber());
			eDcrApplicationDetails.put(BpaConstants.MESSAGE, message);
		}
		return eDcrApplicationDetails;
	}

}
