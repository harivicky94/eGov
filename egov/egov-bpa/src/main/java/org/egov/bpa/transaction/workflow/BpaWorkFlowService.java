/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *     accountability and the service delivery of the government  organizations.
 *
 *      Copyright (C) <2017>  eGovernments Foundation
 *
 *      The updated version of eGov suite of products as by eGovernments Foundation
 *      is available at http://www.egovernments.org
 *
 *      This program is free software: you can redistribute it and/or modify
 *      it under the terms of the GNU General Public License as published by
 *      the Free Software Foundation, either version 3 of the License, or
 *      any later version.
 *
 *      This program is distributed in the hope that it will be useful,
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *      GNU General Public License for more details.
 *
 *      You should have received a copy of the GNU General Public License
 *      along with this program. If not, see http://www.gnu.org/licenses/ or
 *      http://www.gnu.org/licenses/gpl.html .
 *
 *      In addition to the terms of the GPL license to be adhered to in using this
 *      program, the following additional terms are to be complied with:
 *
 *          1) All versions of this program, verbatim or modified must carry this
 *             Legal Notice.
 *
 *          2) Any misrepresentation of the origin of the material is prohibited. It
 *             is required that all modified versions of this material be marked in
 *             reasonable ways as different from the original version.
 *
 *          3) This license does not grant any rights to any user of the program
 *             with regards to rights under trademark law for use of the trade names
 *             or trademarks of eGovernments Foundation.
 *
 *    In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

package org.egov.bpa.transaction.workflow;

import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.entity.LettertoParty;
import org.egov.bpa.transaction.entity.dto.BpaStateInfo;
import org.egov.bpa.utils.BpaUtils;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.web.contract.WorkflowContainer;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.utils.StringUtils;
import org.egov.infra.workflow.entity.State;
import org.egov.infra.workflow.entity.StateAware;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.infra.workflow.matrix.service.CustomizedWorkFlowService;
import org.egov.pims.commons.Position;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class BpaWorkFlowService {

    @Autowired
    protected AssignmentService assignmentService;
    @Autowired
    protected CustomizedWorkFlowService customizedWorkFlowService;
    @Autowired
    private BpaUtils bpaUtils;

    public Assignment getWorkFlowInitiator(final BpaApplication application) {
        Assignment wfInitiator = null;
        List<Assignment> assignment;
        if (application != null)
            if (application.getState() != null
                    && application.getState().getInitiatorPosition() != null) {
                wfInitiator = getUserAssignmentByPassingPositionAndUser(application
                        .getCreatedBy(), application.getState().getInitiatorPosition());

                if (wfInitiator == null) {
                    assignment = assignmentService
                            .getAssignmentsForPosition(application.getState().getInitiatorPosition().getId(),
                                    new Date());
                    wfInitiator = getActiveAssignment(assignment);
                }
            } else
                wfInitiator = assignmentService.getPrimaryAssignmentForUser(application
                        .getCreatedBy().getId());
        return wfInitiator;
    }

    private Assignment getActiveAssignment(final List<Assignment> assignment) {
        Assignment wfInitiator = null;
        for (final Assignment assign : assignment)
            if (assign.getEmployee().isActive()) {
                wfInitiator = assign;
                break;
            }
        return wfInitiator;
    }

    public boolean validateUserHasSamePositionAsInitiator(final Long userId, final Position position) {

        Boolean userHasSamePosition = false;

        if (userId != null && position != null) {
            final List<Assignment> assignmentList = assignmentService.findByEmployeeAndGivenDate(userId, new Date());
            for (final Assignment assignment : assignmentList)
                if (position.getId() == assignment.getPosition().getId())
                    userHasSamePosition = true;
        }
        return userHasSamePosition;
    }

    private Assignment getUserAssignmentByPassingPositionAndUser(final User user, final Position position) {

        Assignment wfInitiatorAssignment = null;

        if (user != null && position != null) {
            final List<Assignment> assignmentList = assignmentService.findByEmployeeAndGivenDate(user.getId(), new Date());
            for (final Assignment assignment : assignmentList)
                if (position.getId() == assignment.getPosition().getId())
                    wfInitiatorAssignment = assignment;
        }

        return wfInitiatorAssignment;
    }

    /**
     * @param model
     * @param container
     * @return NextAction From Matrix With Parameters Type,CurrentState,CreatedDate
     */
    public String getNextAction(final StateAware<Position> model, final WorkflowContainer container) {

        WorkFlowMatrix wfMatrix = null;
        if (null != model && null != model.getId())
            if (null != model.getCurrentState())
                wfMatrix = customizedWorkFlowService.getWfMatrix(model.getStateType(),
                        container.getWorkFlowDepartment(), container.getAmountRule(), container.getAdditionalRule(),
                        model.getCurrentState().getValue(), container.getPendingActions(), model.getCreatedDate());
            else
                wfMatrix = customizedWorkFlowService.getWfMatrix(model.getStateType(),
                        container.getWorkFlowDepartment(), container.getAmountRule(), container.getAdditionalRule(),
                        State.DEFAULT_STATE_VALUE_CREATED, container.getPendingActions(), model.getCreatedDate());
        return wfMatrix == null ? "" : wfMatrix.getNextAction();
    }

    /**
     * @param model
     * @param container
     * @return List of WorkFlow Buttons From Matrix By Passing parametres Type,CurrentState,CreatedDate
     */
    public List<String> getValidActions(final StateAware<Position> model, final WorkflowContainer container) {
        List<String> validActions;
        if (null == model
                || null == model.getId() || (model.getCurrentState() == null)
                || ((model != null && model.getCurrentState() != null) && (model.getCurrentState().getValue()
																				.equals("Closed")
																		   || model.getCurrentState().getValue().equals("END"))))
            validActions = Arrays.asList("Forward");
        else if (null != model.getCurrentState())
            validActions = customizedWorkFlowService.getNextValidActions(model.getStateType(), container
                    .getWorkFlowDepartment(), container.getAmountRule(), container.getAdditionalRule(), model
                            .getCurrentState().getValue(),
                    container.getPendingActions(), model.getCreatedDate());
        else
            validActions = customizedWorkFlowService.getNextValidActions(model.getStateType(),
                    container.getWorkFlowDepartment(), container.getAmountRule(), container.getAdditionalRule(),
                    State.DEFAULT_STATE_VALUE_CREATED, container.getPendingActions(), model.getCreatedDate());
        return validActions;
    }

    public StateHistory<Position> getStateHistoryToGetLPInitiator(BpaApplication bpaApplication, List<LettertoParty> lettertoParties) {
        return bpaApplication.getStateHistory().stream()
                .filter(history -> history.getValue().equalsIgnoreCase(lettertoParties.get(0).getStateForOwnerPosition()))
                .findAny().orElse(null);
    }

    public Assignment getApproverAssignment(final Position position) {
        return assignmentService.getPrimaryAssignmentForPositon(position.getId());
    }

    public Optional<StateHistory<Position>> getLastStateHstryObj(final BpaApplication bpaApplication) {
        return bpaApplication.getStateHistory().stream().reduce((sh1, sh2) -> sh2);
    }

    public BpaStateInfo getBpaStateinfo(final BpaApplication application, final BpaStateInfo bpaStateInfo, final WorkFlowMatrix wfmatrix) {
        bpaStateInfo.setWfMatrixRef(wfmatrix.getId());
        if (application.getTownSurveyorInspectionRequire() && getTownSurveyorInspnInitiator(application) != 0)
            bpaStateInfo.setTsInitiatorPos(getTownSurveyorInspnInitiator(application));
        else if (application.getTownSurveyorInspectionRequire())
            bpaStateInfo.setTsInitiatorPos(application.getCurrentState().getOwnerPosition().getId());

        return bpaStateInfo;
    }

    public Long getPreviousWfMatrixId(final BpaApplication application) {
        Optional<StateHistory<Position>> stateHistory = getLastStateHstryObj(application);
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            if (stateHistory.isPresent() && StringUtils.isNotEmpty(stateHistory.get().getExtraInfo()))
                json = (JSONObject) parser.parse(stateHistory.get().getExtraInfo());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return Long.valueOf(json.get("wfMatrixRef").toString());
    }

    public Long getTownSurveyorInspnInitiator(final BpaApplication application) {
        State currentState = application.getCurrentState();
        JSONParser parser = new JSONParser();
        JSONObject json = null;
        try {
            if (StringUtils.isNotEmpty(currentState.getExtraInfo()))
                json = (JSONObject) parser.parse(currentState.getExtraInfo());
            if (json == null || json.get("tsInitiatorPos") == null) {
                Optional<StateHistory<Position>> stateHistory = getLastStateHstryObj(application);
                if (stateHistory.isPresent() && StringUtils.isNotEmpty(stateHistory.get().getExtraInfo()))
                    json = (JSONObject) parser.parse(stateHistory.get().getExtraInfo());
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }
        return json == null || json.get("tsInitiatorPos") == null ? 0 : Long.valueOf(json.get("tsInitiatorPos").toString());
    }

    public Position getApproverPositionOfElectionWardByCurrentState(final BpaApplication application, final String currentState) {
        WorkFlowMatrix wfMatrix = bpaUtils.getWfMatrixByCurrentState(application, currentState);
        return bpaUtils.getUserPositionByZone(wfMatrix.getNextDesignation(),
                application.getSiteDetail().get(0) != null
                && application.getSiteDetail().get(0).getElectionBoundary() != null
                ? application.getSiteDetail().get(0).getElectionBoundary().getId() : null);
    }

}
