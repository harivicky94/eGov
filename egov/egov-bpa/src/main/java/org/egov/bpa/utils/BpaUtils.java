package org.egov.bpa.utils;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.RandomStringGenerator;
import org.egov.bpa.transaction.entity.BpaApplication;
import org.egov.bpa.transaction.service.messaging.BPASmsAndEmailService;
import org.egov.bpa.transaction.workflow.BpaApplicationWorkflowCustomDefaultImpl;
import org.egov.demand.model.EgDemandDetails;
import org.egov.eis.entity.Assignment;
import org.egov.eis.service.AssignmentService;
import org.egov.eis.service.DesignationService;
import org.egov.infra.admin.master.entity.AppConfigValues;
import org.egov.infra.admin.master.entity.Boundary;
import org.egov.infra.admin.master.entity.Module;
import org.egov.infra.admin.master.entity.User;
import org.egov.infra.admin.master.service.AppConfigValueService;
import org.egov.infra.admin.master.service.BoundaryService;
import org.egov.infra.admin.master.service.ModuleService;
import org.egov.infra.admin.master.service.UserService;
import org.egov.infra.persistence.entity.enums.UserType;
import org.egov.infra.security.utils.SecurityUtils;
import org.egov.infra.workflow.entity.StateHistory;
import org.egov.infra.workflow.matrix.entity.WorkFlowMatrix;
import org.egov.infra.workflow.service.SimpleWorkflowService;
import org.egov.pims.commons.Position;
import org.egov.portal.entity.PortalInbox;
import org.egov.portal.entity.PortalInboxBuilder;
import org.egov.portal.service.PortalInboxService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.egov.bpa.utils.BpaConstants.APPLICATION_MODULE_TYPE;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_CANCELLED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_REJECTED;
import static org.egov.bpa.utils.BpaConstants.APPLICATION_STATUS_REJECT_CLERK;
import static org.egov.bpa.utils.BpaConstants.BOUNDARY_TYPE_CITY;
import static org.egov.bpa.utils.BpaConstants.BOUNDARY_TYPE_ZONE;
import static org.egov.bpa.utils.BpaConstants.CREATE_ADDITIONAL_RULE_CREATE;
import static org.egov.bpa.utils.BpaConstants.CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT;
import static org.egov.bpa.utils.BpaConstants.EGMODULE_NAME;
import static org.egov.bpa.utils.BpaConstants.LETTERTOPARTYINITIATE;
import static org.egov.bpa.utils.BpaConstants.LPCREATED;
import static org.egov.bpa.utils.BpaConstants.LPREPLIED;
import static org.egov.bpa.utils.BpaConstants.LPREPLYRECEIVED;
import static org.egov.bpa.utils.BpaConstants.WF_LBE_SUBMIT_BUTTON;
import static org.egov.bpa.utils.BpaConstants.WF_PERMIT_FEE_COLL_PENDING;

@Service
@Transactional(readOnly = true)
public class BpaUtils {

	private static final String CLOSED = "Closed";
	private static final String WF_END_ACTION = "END";
	@Autowired
	private ApplicationContext context;

	@Autowired
	private SecurityUtils securityUtils;

	@Autowired
	private ModuleService moduleService;

	@Autowired
	private AssignmentService assignmentService;

	@Autowired
	private PortalInboxService portalInboxService;
	@Autowired
	private BPASmsAndEmailService bpaSmsAndEmailService;

	@Autowired
	private BoundaryService boundaryService;

	@Autowired
	@Qualifier("workflowService")
	private SimpleWorkflowService<BpaApplication> bpaApplicationWorkflowService;

	@Autowired
	private DesignationService designationService;

	@Autowired
	private AppConfigValueService appConfigValueService;

	@Autowired
	private UserService userService;

	public String getAppconfigValueByKeyName(String code) {
		List<AppConfigValues> appConfigValueList = appConfigValueService
				.getConfigValuesByModuleAndKey(APPLICATION_MODULE_TYPE, code);
		return appConfigValueList.isEmpty() ? "" : appConfigValueList.get(0).getValue();
	}

	public Boolean checkAnyTaxIsPendingToCollect(final BpaApplication bpaApplication) {
		Boolean pendingTaxCollection = false;

		if (bpaApplication != null && bpaApplication.getDemand() != null)
			for (final EgDemandDetails demandDtl : bpaApplication.getDemand().getEgDemandDetails())
				if (demandDtl.getAmount().subtract(demandDtl.getAmtCollected()).compareTo(BigDecimal.ZERO) > 0) {
					pendingTaxCollection = true;
					break;
				}
		return pendingTaxCollection;
	}

	public Boolean applicationinitiatedByNonEmployee(BpaApplication application) {
		Boolean initiatedByNonEmployee = false;
		User applicationInitiator;
		if (application.getCreatedBy() != null)
			applicationInitiator = userService.getUserById(application.getCreatedBy().getId());
		else
			applicationInitiator = getCurrentUser();
		if (applicationInitiator != null && !applicationInitiator.getType().equals(UserType.EMPLOYEE)) {
			initiatedByNonEmployee = Boolean.TRUE;
		}

		return initiatedByNonEmployee;
	}

	public User getCurrentUser() {
		return securityUtils.getCurrentUser();
	}

	public BpaApplicationWorkflowCustomDefaultImpl getInitialisedWorkFlowBean() {
		BpaApplicationWorkflowCustomDefaultImpl applicationWorkflowCustomDefaultImpl = null;
		if (null != context)
			applicationWorkflowCustomDefaultImpl = (BpaApplicationWorkflowCustomDefaultImpl) context
					.getBean("bpaApplicationWorkflowCustomDefaultImpl");
		return applicationWorkflowCustomDefaultImpl;
	}

	public WorkFlowMatrix getWfMatrixByCurrentState(final BpaApplication application, final String currentState) {
		if (application.getIsOneDayPermitApplication()) {
			return bpaApplicationWorkflowService.getWfMatrix(application.getStateType(), null, null,
					CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT, currentState, null);
		} else
			return bpaApplicationWorkflowService.getWfMatrix(application.getStateType(), null, null,
					CREATE_ADDITIONAL_RULE_CREATE, currentState, null);
	}

	@Transactional
	public void updatePortalUserinbox(final BpaApplication application, final User additionalPortalInboxUser) {
		Module module = moduleService.getModuleByName(EGMODULE_NAME);
		boolean isResolved = false;
		if ((application.getState() != null && (CLOSED.equals(application.getState().getValue())
												|| WF_END_ACTION.equals(application.getState().getValue())))
			|| (application.getStatus() != null
				&& application.getStatus().getCode().equals(APPLICATION_STATUS_CANCELLED)))
			isResolved = true;
		String url = "/bpa/application/citizen/update/" + application.getApplicationNumber();
		if (application.getStatus() != null)
			portalInboxService.updateInboxMessage(application.getApplicationNumber(), module.getId(),
					application.getStatus().getDescription(), isResolved, new Date(), application.getState(),
					additionalPortalInboxUser, application.getPlanPermissionNumber(), url);
	}

	@Transactional
	public void createPortalUserinbox(final BpaApplication application, final List<User> portalInboxUser,
									  final String workFlowAction) {
		String status = StringUtils.EMPTY;
		if ("Save".equalsIgnoreCase(workFlowAction)) {
			status = "To be submitted";
		} else if (null != application.getStatus().getDescription()
				   && WF_LBE_SUBMIT_BUTTON.equalsIgnoreCase(workFlowAction)) {
			status = application.getStatus().getDescription();
		}
		Module module = moduleService.getModuleByName(EGMODULE_NAME);
		boolean isResolved = false;
		String url = "/bpa/application/citizen/update/" + application.getApplicationNumber();
		final PortalInboxBuilder portalInboxBuilder = new PortalInboxBuilder(module, application.getOwner().getName(),
				application.getServiceType().getDescription(), application.getApplicationNumber(),
				application.getPlanPermissionNumber(), application.getId(), "Success", "Success", url, isResolved,
				status, new Date(), application.getState(), portalInboxUser);

		final PortalInbox portalInbox = portalInboxBuilder.build();
		portalInboxService.pushInboxMessage(portalInbox);
	}

	@Transactional(readOnly = true)
	public Long getUserPositionIdByZone(final String designation, final Long boundary) {
		List<Assignment> assignment = getAssignmentsByDesigAndBndryId(designation, boundary);
		return assignment.isEmpty() ? 0 : assignment.get(0).getPosition().getId();
	}

	@Transactional(readOnly = true)
	public Position getUserPositionByZone(final String designation, final Long boundary) {
		List<Assignment> assignment = getAssignmentsByDesigAndBndryId(designation, boundary);
		return assignment.isEmpty() ? null : assignment.get(0).getPosition();
	}

	private List<Assignment> getAssignmentsByDesigAndBndryId(String designation, Long boundary) {
		final Boundary boundaryObj = getBoundaryById(boundary);
		final String[] designationarr = designation.split(",");
		List<Assignment> assignment = new ArrayList<>();
		for (final String desg : designationarr) {
			assignment = assignmentService.findAssignmentByDepartmentDesignationAndBoundary(null,
					designationService.getDesignationByName(desg).getId(), boundaryObj.getId());
			if (assignment.isEmpty()) {
				// Ward->Zone
				if (boundaryObj.getParent() != null && boundaryObj.getParent().getBoundaryType() != null && boundaryObj
						.getParent().getBoundaryType().getName().equals(BOUNDARY_TYPE_ZONE)) {
					assignment = assignmentService.findByDeptDesgnAndParentAndActiveChildBoundaries(null,
							designationService.getDesignationByName(desg).getId(), boundaryObj.getParent().getId());
					if (assignment.isEmpty() && boundaryObj.getParent() != null
						&& boundaryObj.getParent().getParent() != null && boundaryObj.getParent().getParent()
																					 .getBoundaryType().getName().equals(BOUNDARY_TYPE_CITY))
						assignment = assignmentService.findByDeptDesgnAndParentAndActiveChildBoundaries(null,
								designationService.getDesignationByName(desg).getId(),
								boundaryObj.getParent().getParent().getId());
				}
				// ward->City mapp
				if (assignment.isEmpty() && boundaryObj.getParent() != null
					&& boundaryObj.getParent().getBoundaryType().getName().equals(BOUNDARY_TYPE_CITY))
					assignment = assignmentService.findByDeptDesgnAndParentAndActiveChildBoundaries(null,
							designationService.getDesignationByName(desg).getId(), boundaryObj.getParent().getId());
			}
			if (!assignment.isEmpty())
				break;
		}
		return assignment;
	}

	public Boundary getBoundaryById(final Long boundary) {
		return boundaryService.getBoundaryById(boundary);
	}

	public Boolean logedInuseCitizenOrBusinessUser() {
		Boolean citizenOrbusiness = Boolean.FALSE;
		User applicationInitiator = getCurrentUser();
		if (applicationInitiator != null && (applicationInitiator.getType().equals(UserType.CITIZEN)
											 || applicationInitiator.getType().equals(UserType.BUSINESS))) {
			citizenOrbusiness = Boolean.TRUE;
		}
		return citizenOrbusiness;
	}

	public Boolean logedInuserIsCitizen() {
		return getCurrentUser() != null && getCurrentUser().getType().equals(UserType.CITIZEN) ? Boolean.TRUE : Boolean.FALSE;
	}

	@Transactional
	public void redirectToBpaWorkFlow(Long approvalPosition, final BpaApplication application,
									  final String currentState, final String remarks, final String workFlowAction, final BigDecimal amountRule) {

		buildWorkFlow(approvalPosition, application, currentState, remarks, workFlowAction, amountRule);
	}

	public void redirectToBpaWorkFlowForScheduler(Long approvalPosition, final BpaApplication application,
												  final String currentState, final String remarks, final String workFlowAction, final BigDecimal amountRule) {

		buildWorkFlow(approvalPosition, application, currentState, remarks, workFlowAction, amountRule);
	}

	private void buildWorkFlow(Long approvalPosition, final BpaApplication application, final String currentState,
							   final String remarks, final String workFlowAction, final BigDecimal amountRule) {
		final WorkFlowMatrix wfmatrix = getWfMatrixByCurrentState(application, currentState);
		final BpaApplicationWorkflowCustomDefaultImpl applicationWorkflowCustomDefaultImpl = getInitialisedWorkFlowBean();
		Long approvalPositionId = approvalPosition;
		if (approvalPosition == null) {
			approvalPositionId = getUserPositionIdByZone(wfmatrix.getNextDesignation(),
					application.getSiteDetail().get(0).getElectionBoundary().getId());
		}
		if (LETTERTOPARTYINITIATE.equals(currentState))
			applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
					CREATE_ADDITIONAL_RULE_CREATE, LETTERTOPARTYINITIATE, amountRule);
		else if (LPCREATED.equals(currentState))
			applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
					CREATE_ADDITIONAL_RULE_CREATE, LPCREATED, amountRule);
		else if (LPREPLIED.equals(currentState))
			applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
					CREATE_ADDITIONAL_RULE_CREATE, LPREPLYRECEIVED, amountRule);
		else if (WF_PERMIT_FEE_COLL_PENDING.equals(currentState)) {
			if (application.getIsOneDayPermitApplication()) {
				applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
						CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT, WF_PERMIT_FEE_COLL_PENDING, amountRule);
			} else
				applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
						CREATE_ADDITIONAL_RULE_CREATE, WF_PERMIT_FEE_COLL_PENDING, amountRule);
		} else {
			if (application.getIsOneDayPermitApplication()) {
				applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
						CREATE_ADDITIONAL_RULE_CREATE_ONEDAYPERMIT, workFlowAction, amountRule);
			} else
				applicationWorkflowCustomDefaultImpl.createCommonWorkflowTransition(application, approvalPositionId, remarks,
						CREATE_ADDITIONAL_RULE_CREATE, workFlowAction, amountRule);
		}
	}

	public void sendSmsEmailOnCitizenSubmit(BpaApplication bpaApplication) {
		bpaSmsAndEmailService.sendSMSAndEmail(bpaApplication);
	}

	public String generateUserName(final String name) {
		final StringBuilder userNameBuilder = new StringBuilder();
		String userName;
		if (name.length() < 6)
			userName = String.format("%-6s", name).replace(' ', '0').replace(',', '0');
		else
			userName = name.substring(0, 6).replace(' ', '0').replace(',', '0');
		RandomStringGenerator generator = new RandomStringGenerator.Builder().withinRange('0', '9').build();
		userNameBuilder.append(userName).append(generator.generate(4));
		return userNameBuilder.toString();
	}

	public String getAppconfigValueByKeyNameForDefaultDept() {
		List<AppConfigValues> appConfigValueList = appConfigValueService
				.getConfigValuesByModuleAndKey(APPLICATION_MODULE_TYPE, "BPAPRIMARYDEPARTMENT");
		return !appConfigValueList.isEmpty() ? appConfigValueList.get(0).getValue() : "";
	}

	public StateHistory<Position> getRejectionComments(BpaApplication bpaApplication) {
		StateHistory<Position> stateHistory = bpaApplication.getStateHistory().stream()
															.filter(history -> history.getValue().equalsIgnoreCase(APPLICATION_STATUS_REJECTED))
															.findAny().orElse(null);
		if (stateHistory == null)
			stateHistory = bpaApplication.getStateHistory().stream()
										 .filter(history -> history.getValue().equalsIgnoreCase(APPLICATION_STATUS_REJECT_CLERK))
										 .findAny().orElse(null);
		return stateHistory;
	}
}