<%--
  ~ eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~ accountability and the service delivery of the government  organizations.
  ~
  ~  Copyright (C) <2017>  eGovernments Foundation
  ~
  ~  The updated version of eGov suite of products as by eGovernments Foundation
  ~  is available at http://www.egovernments.org
  ~
  ~  This program is free software: you can redistribute it and/or modify
  ~  it under the terms of the GNU General Public License as published by
  ~  the Free Software Foundation, either version 3 of the License, or
  ~  any later version.
  ~
  ~  This program is distributed in the hope that it will be useful,
  ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~  GNU General Public License for more details.
  ~
  ~  You should have received a copy of the GNU General Public License
  ~  along with this program. If not, see http://www.gnu.org/licenses/ or
  ~  http://www.gnu.org/licenses/gpl.html .
  ~
  ~  In addition to the terms of the GPL license to be adhered to in using this
  ~  program, the following additional terms are to be complied with:
  ~
  ~      1) All versions of this program, verbatim or modified must carry this
  ~         Legal Notice.
  ~ 	Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~         derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~ 	For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~ 	For any further queries on attribution, including queries on brand guidelines,
  ~         please contact contact@egovernments.org
  ~
  ~      2) Any misrepresentation of the origin of the material is prohibited. It
  ~         is required that all modified versions of this material be marked in
  ~         reasonable ways as different from the original version.
  ~
  ~      3) This license does not grant any rights to any user of the program
  ~         with regards to rights under trademark law for use of the trade names
  ~         or trademarks of eGovernments Foundation.
  ~
  ~  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  --%>
  
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn"%>
<div class="row">
	<div class="col-md-12">
		<form:form role="form" action="/bpa/application/update-submit/${bpaApplication.applicationNumber}" method="post" modelAttribute="bpaApplication"
			id="viewBpaApplicationForm"
			cssClass="form-horizontal form-groups-bordered"
			enctype="multipart/form-data">
			<c:if
				test="${isFeeCollected && bpaApplication.status.code eq 'Approved'}">
				<div data-collapsed="0">
					<div class="panel-heading">
						<div style="color: red; font-size: 16px;" align="center">
							<spring:message code="lbl.collect.bpaFee" />
						</div>
					</div>
				</div>
			</c:if>
			<input type="hidden" id="applicationId" name="bpaApplication"
				value="${bpaApplication.id}">
				<input type="hidden" name="citizenOrBusinessUser"
				value="${citizenOrBusinessUser}"/>
			<form:hidden path="" id="wfstate" value="${bpaApplication.state.id}" />
			<form:hidden path="" id="workFlowAction" name="workFlowAction" />
			<form:hidden path="" id="wfstateDesc"
				value="${bpaApplication.state.value}" />
			<form:hidden path="" id="collectFeeValidate" value="${collectFeeValidate}" />
			<form:hidden path="" id="mode" name="mode" value="${mode}" />
			<form:hidden  path="" id="amountRule" name="amountRule" value="${amountRule}"/>
			<form:hidden path="" id="scheduleType" name="scheduleType"
				value="${scheduleType}" />
			<form:hidden path="" id="showPermitConditions" value="${showpermitconditions}" />
			<form:hidden path="sentToPreviousOwner" id="sentToPreviousOwner" />
			<form:hidden path="LPRequestInitiated" id="lpRequestInitiated" value="${bpaApplication.LPRequestInitiated}" />
			<input type="hidden" id="isOneDayPermitApplication" value="${bpaApplication.isOneDayPermitApplication}" />
			<ul class="nav nav-tabs" id="settingstab">
				<li class="active"><a data-toggle="tab" href="#applicant-info"
					data-tabidx=0><spring:message code='lbl.appln.details' /></a></li>
				<li><a data-toggle="tab" href="#document-info" data-tabidx=1><spring:message
							code='title.documentdetail' /></a></li>
				<c:if test="${not empty bpaApplication.documentScrutiny}">
						<li><a data-toggle="tab" href="#doc-scrnty" data-tabidx=2><spring:message
									code='lbl.document.scrutiny' /></a></li>
				</c:if>
				<c:if test="${not empty bpaApplication.inspections}">
					<li><a data-toggle="tab" href="#view-inspection" data-tabidx=3><spring:message
								code='lbl.inspection.appln' /></a></li>
				</c:if>
				<c:if test="${captureTSRemarks}">
					<li><a data-toggle="tab" href="#ts-remarks"
						   data-tabidx=4><spring:message code='lbl.town.surveyor.remarks' /></a></li>
				</c:if>
				<c:if test="${(null eq captureTSRemarks || !captureTSRemarks) && null ne bpaApplication.townSurveyorRemarks}">
					<li><a data-toggle="tab" href="#view-ts-remarks"
						   data-tabidx=4><spring:message code='lbl.town.surveyor.remarks' /></a></li>
				</c:if>
				<c:if test="${showUpdateNoc}">
					<li><a data-toggle="tab" href="#checklist-info" data-tabidx=5><spring:message
								code='lbl.noc.doc.details' /></a></li>
				</c:if>
				<c:if test="${!showUpdateNoc && not empty bpaApplication.applicationNOCDocument}">
						<li><a data-toggle="tab" href="#noc-info" data-tabidx=5><spring:message
									code='lbl.noc.details' /></a></li>
				</c:if>
				<c:if test="${not empty bpaApplication.applicationFee}">
					<li><a data-toggle="tab" href="#view-fee" data-tabidx=6><spring:message
								code='lbl.fee.details' /></a></li>
				</c:if>
				<c:if test="${not empty lettertopartylist}">
					<li><a data-toggle="tab" href="#view-lp" data-tabidx=7><spring:message
								code='lbl.lp.details' /></a></li>
				</c:if>
				<c:if test="${showpermitconditions}">
					<li><a data-toggle="tab" href="#permit-conditions"
						data-tabidx=8><spring:message code='lbl.permit.conditions' /></a></li>
				</c:if>
			</ul>
			<div class="tab-content">
				<div id="applicant-info" class="tab-pane fade in active">
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="viewapplication-details.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary edcrApplnDetails" data-collapsed="0">
						<jsp:include page="view-edcr-application-details.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="view-applicantdetails.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="view-sitedetail.jsp"></jsp:include>
					</div>
					<c:if test="${not empty  bpaApplication.existingBuildingDetails}">
						<div class="panel panel-primary buildingdetails" data-collapsed="0">
							<jsp:include page="view-existing-building-details.jsp" />
						</div>
					</c:if>
					<div class="panel panel-primary buildingdetails" data-collapsed="0">
						<jsp:include page="view-building-details.jsp" />
					</div>
					<c:if test="${not empty  bpaApplication.receipts}">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="view-bpa-receipt-details.jsp"></jsp:include>
						</div>
					</c:if>
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="applicationhistory-view.jsp"></jsp:include>
					</div>
					<c:if test="${showRejectionReasons}">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="rejection-reasons.jsp"></jsp:include>
						</div>
					</c:if>
				</div>
				<div id="document-info" class="tab-pane fade">
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="view-bpaDocumentdetails.jsp"></jsp:include>
					</div>
				</div>
				<c:if test="${not empty bpaApplication.documentScrutiny}">
						<div id="doc-scrnty" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="view-documentscrutiny.jsp"></jsp:include>
							</div>
						</div>
				</c:if>
				<c:if test="${not empty bpaApplication.inspections}">
						<div id="view-inspection" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="view-inspection-details.jsp"></jsp:include>
							</div>
						</div>
				</c:if>
				<c:if test="${captureTSRemarks}">
					<input type="hidden" id="captureTSRemarks" value="${captureTSRemarks}">
					<div id="ts-remarks" class="tab-pane fade">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="town-surveyor-remarks.jsp"></jsp:include>
						</div>
					</div>
				</c:if>
				<c:if test="${(null eq captureTSRemarks || !captureTSRemarks) && null ne bpaApplication.townSurveyorRemarks}">
				<c:if test="${'Town Surveyor Inspected' eq bpaApplication.status.code}">
					<input type="hidden" id="viewTsRemarks" value="true">
				</c:if>
					<div id="view-ts-remarks" class="tab-pane fade">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="view-town-surveyor-remarks.jsp"></jsp:include>
						</div>
					</div>
				</c:if>
				<c:if test="${!showUpdateNoc && not empty bpaApplication.applicationNOCDocument}">
						<div id="noc-info" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="view-noc-document.jsp"></jsp:include>
							</div>
						</div>
				</c:if>
				<c:if test="${showUpdateNoc}">
					<input type="hidden" id="showUpdateNoc" value="${showUpdateNoc}">
					<div id="checklist-info" class="tab-pane fade">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="noc-document-updation.jsp"></jsp:include>
						</div>
					</div>
				</c:if>
				<c:if test="${not empty bpaApplication.applicationFee}">
						<div id="view-fee" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="view-bpa-fee-details.jsp"></jsp:include>
							</div>
						</div>
				</c:if>
				<c:if test="${not empty lettertopartylist}">
						<div id="view-lp" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="../lettertoparty/lettertoparty-details.jsp"></jsp:include> 
							</div>
						</div>
				</c:if>
				<c:if test="${showpermitconditions}">
					<input type="hidden" id="showPermitConditions" value="${showpermitconditions}">
					<div id="permit-conditions" class="tab-pane fade">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="permit-conditions.jsp"></jsp:include>
						</div>
					</div>
				</c:if>
			</div>

			<div class="text-center">

				<c:if test="${mode eq 'captureInspection'}">
					<a
						href="/bpa/application/createinspectiondetails/${bpaApplication.applicationNumber}"
						class="btn btn-primary">Capture Inspection Details </a>
				<c:if test="${!bpaApplication.isOneDayPermitApplication}">
					<a
						href="/bpa/application/postponeappointment/${scheduleType}/${bpaApplication.applicationNumber}"
						class="btn btn-primary"> Reschedule Appointment </a>
				</c:if>
				</c:if>
				<c:if test="${mode eq 'modifyInspection'}">
					<a
						href="/bpa/application/modify-inspection/${bpaApplication.applicationNumber}"
						class="btn btn-primary">Add/Edit Inspection Details </a>
				</c:if>
				<c:if test="${mode eq 'newappointment' && !bpaApplication.isOneDayPermitApplication}">
					<a
						href="/bpa/application/scheduleappointment/${bpaApplication.applicationNumber}"
						class="btn btn-primary"> New Appointment </a>
				</c:if>
				<c:if test="${mode eq 'initiatedForApproval'}">
					<a
						href="/bpa/application/calculateFee/${bpaApplication.applicationNumber}"
						class="btn btn-primary">Modify Fee </a>

				</c:if>
				<c:if test="${bpaApplication.state.value ne 'Field Inspection completed' && bpaApplication.status.code eq 'Field Inspected'}">
					<input type="button" name="save" id="btnSave" value="Save"
						   class="btn btn-primary"/>
				</c:if>
				<c:if test="${createlettertoparty}">
				<a
						href="/bpa/lettertoparty/create/${bpaApplication.applicationNumber}"
						target="_self" class="btn btn-primary"> Create Letter to Party </a>	 
				</c:if>
			</div>
			<br>
			<c:if test="${(not empty bpaApplication.inspections && bpaApplication.status.code eq 'Document Verified') 
				|| (bpaApplication.state.value eq 'Field Inspection completed' && bpaApplication.status.code eq 'Field Inspected')}">
				<c:if test="${!bpaApplication.isOneDayPermitApplication}">
				<div class="panel panel-primary" data-collapsed="0" id="townSurveyorInspectionDiv">
					<div class="panel-heading toggle-header custom_form_panel_heading">
						<div class="panel-title">
						</div>
					</div>
					<div class="panel-body">
						<label class="view-content">
							&nbsp;&nbsp;&nbsp;<form:checkbox path="townSurveyorInspectionRequire"
															 id="townSurveyorInspectionRequire"/>
							&nbsp;&nbsp;&nbsp;<spring:message
								code="lbl.ts.inspn.requr"/>
						</label>
					</div>
				</div>
				</c:if>
			</c:if> 
			<c:choose>
				<c:when
					test="${isFeeCollected && bpaApplication.status.code eq 'Approved'}">
					<div class="buttonbottom" align="center">
						<input type="button" name="button2" value="Close"
							class="btn btn-default" onclick="window.close();" />
					</div>
				</c:when> 
				<c:otherwise>
					<c:choose>
						<c:when
							test="${ (citizenOrBusinessUser && bpaApplication.id !=null) || bpaApplication.state.value eq 'LP Created' || bpaApplication.state.value eq 'LP Reply Received'}">
							<div class="buttonbottom" align="center">
								<form:button type="submit" id="buttonSubmit"
									class="btn btn-primary" value="Forward">Forward</form:button>
								<input type="button" name="button2" value="Close"
									class="btn btn-default" onclick="window.close();" />
							</div>
						</c:when>
						<c:otherwise>
							<c:if test="${bpaApplication.status.code ne 'Digitally signed'}">
								<jsp:include page="../common/commonWorkflowMatrix.jsp" />
							</c:if>
							<div class="buttonbottom" align="center">
								<jsp:include page="../common/commonWorkflowMatrix-button.jsp" />
							</div>
						</c:otherwise>
					</c:choose>
				</c:otherwise>
			</c:choose>
		</form:form>
	</div>
</div>

<script
	src="<cdn:url value='/resources/global/js/egov/inbox.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script
	src="<cdn:url value='/resources/js/app/application-edit.js?rnd=${app_release_no}'/>"></script>
<script
	src="<cdn:url value='/resources/js/app/documentsuploadvalidation.js?rnd=${app_release_no}'/>"></script>
<script
	src="<cdn:url value='/resources/js/app/application-view.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/edcr-helper.js?rnd=${app_release_no}'/>"></script>