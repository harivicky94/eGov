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
			<form:form role="form" action="/bpa/application/citizen/update-submit/${bpaApplication.applicationNumber}" method="post" modelAttribute="bpaApplication"
			id="citizenViewApplicationForm"
			cssClass="form-horizontal form-groups-bordered"
			enctype="multipart/form-data">
				<form:hidden path="" id="workFlowAction" name="workFlowAction"/>
				<form:hidden path="" id="wfstateDesc" value="${bpaApplication.state.value}" />
				<input type="hidden" id="appointmentDateRes" value="${appointmentDateRes}" />
				<input type="hidden" id="appointmentTimeRes" value="${appointmentTimeRes}" />
				<input type="hidden" id="appointmentTitle" value="${appointmentTitle}" />
                <input type="hidden" id="appmntInspnRemarks" value="${appmntInspnRemarks}" />
				<input type="hidden" id="collectFeeValidate" value="${collectFeeValidate}" />
				<input type="hidden" name="citizenOrBusinessUser" value="${citizenOrBusinessUser}">
				<input type="hidden" id="applicationStatus" value="${bpaApplication.status.code}">
			<ul class="nav nav-tabs" id="settingstab">
				<li class="active"><a data-toggle="tab"
					href="#appliccation-info" data-tabidx=0><spring:message
							code='lbl.appln.details' /></a></li>
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
				<c:if test="${not empty bpaApplication.applicationNOCDocument}">
						<li><a data-toggle="tab" href="#noc-info" data-tabidx=4><spring:message
									code='lbl.noc.details' /></a></li>
				</c:if>
				<c:if test="${not empty bpaApplication.applicationFee}">
					<li><a data-toggle="tab" href="#view-fee" data-tabidx=5><spring:message
								code='lbl.fee.details' /></a></li>
				</c:if>
				<c:if test="${not empty lettertopartylist && mode eq 'showLPDetails'}">
					<li><a data-toggle="tab" href="#view-lp" data-tabidx=6><spring:message
								code='lbl.lp.details' /></a></li>
				</c:if>
			</ul>
			<div class="tab-content">
				<div id="document-info" class="tab-pane fade">
					<div class="panel panel-primary" data-collapsed="0">
						<c:if test="${bpaApplication.serviceType.code eq '01'}">
							<div class="dcrDocuments">
								<jsp:include page="view-dcr-documentdetails.jsp"></jsp:include>
							</div>
						</c:if>
						<jsp:include page="view-bpaDocumentdetails.jsp"></jsp:include>
					</div>
				</div>
				<div id="appliccation-info" class="tab-pane fade in active">
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
				<c:if test="${not empty bpaApplication.applicationNOCDocument}">
						<div id="noc-info" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="view-noc-document.jsp"></jsp:include>
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
				<c:if test="${not empty lettertopartylist && mode eq 'showLPDetails'}">
						<div id="view-lp" class="tab-pane fade">
							<div class="panel panel-primary" data-collapsed="0">
								<jsp:include page="../lettertoparty/lettertoparty-details-citizen.jsp"></jsp:include> 
							</div>
						</div>
				</c:if>
			</div>
			<div class="buttonbottom" align="center">
				<table>
					<tr>
						<td>
							<c:if test="${citizenOrBusinessUser && bpaApplication.id != null && bpaApplication.state == null && bpaApplication.status.code ne 'Cancelled'}">
							<form:button type="button" id="buttonCancel" class="btn btn-danger"
										 value="Cancel Application"> Cancel Application </form:button>
							</c:if>
						</td>
						<c:if test="${ mode eq 'showRescheduleToCitizen'}">
							<td> <a
									href="/bpa/application/scrutiny/reschedule/${bpaApplication.applicationNumber}"
									class="btn btn-primary"> Reschedule Appointment </a>
							</td>
						</c:if>
						<c:if test="${ bpaApplication.status.code eq 'Scheduled For Document Scrutiny'
								|| bpaApplication.status.code eq 'Pending For Rescheduling For Document Scrutiny'
								|| bpaApplication.status.code eq 'Rescheduled For Document Scrutiny'}">
							<td>
								<a href="/bpa/application/scrutiny/view/${bpaApplication.applicationNumber}"
								   target="popup" class="btn btn-primary"
								   onclick="window.open('/bpa/application/scrutiny/view/${bpaApplication.applicationNumber}','popup','width=1100,height=700'); return false;">
									View Scheduled Appointment Details
								</a>
							</td>
						</c:if>

						<c:if test="${bpaApplication.status.code eq  'Letter To Party Created' && mode eq 'showLPDetails' }">
							<td> <a	href="/bpa/lettertoparty/lettertopartyreply/${lettertopartylist.get(0).id}" class="btn btn-primary">
										 Reply Letter To Party
								    </a>
							</td>
						</c:if>
						<input type="hidden" id="onlinePaymentEnable" value="${onlinePaymentEnable}">
						<c:if test="${onlinePaymentEnable && isFeeCollected && (bpaApplication.status.code eq 'Registered' || bpaApplication.status.code eq 'Scheduled For Document Scrutiny' || bpaApplication.status.code eq 'Approved') }">
							<td> <a	href="/bpa/application/bpageneratebill/${bpaApplication.applicationNumber}" class="btn btn-primary">
								Pay Fee Online
							</a>
							</td>
						</c:if>
						<c:if test="${bpaApplication.status.code eq 'Approved' && isFeeCollected }">
							<td>
								<a href="/bpa/application/demandnotice/${bpaApplication.applicationNumber}"
								   target="popup" class="btn btn-primary"
								   onclick="window.open('/bpa/application/demandnotice/${bpaApplication.applicationNumber}','popup','width=1100,height=700'); return false;">
									Print Demand Notice
								</a>
							</td> 
						</c:if>
						<c:if test="${bpaApplication.status.code eq 'Order Issued to Applicant' }">
							<td>
								<a href="/bpa/application/generatepermitorder/${bpaApplication.applicationNumber}"
								   target="popup" class="btn btn-primary"
								   onclick="window.open('/bpa/application/generatepermitorder/${bpaApplication.applicationNumber}','popup','width=1100,height=700'); return false;">
									Print Permit Order
								</a>
							</td> 
						</c:if>
						
						<c:if test="${bpaApplication.status.code eq 'Cancelled' && bpaApplication.state ne null}">
							<td>
								<a href="/bpa/application/rejectionnotice/${bpaApplication.applicationNumber}"
								   target="popup" class="btn btn-primary"
								   onclick="window.open('/bpa/application/rejectionnotice/${bpaApplication.applicationNumber}','popup','width=1100,height=700'); return false;">
									Print Rejection Notice
								</a>
							</td>
						</c:if>
						
						<td>&nbsp;<input type="button" name="button2" id="button2" value="Close"
							class="btn btn-default" onclick="window.close();" />
						</td>
					</tr>
				</table>
			</div>
		</form:form>
	</div>
</div>

<div class="modal fade bs-example-modal-lg" tabindex="-1" role="dialog" aria-labelledby="myLargeModalLabel" aria-hidden="true"  id="myModal">
	<div class="modal-dialog">
		<!-- Modal content-->
		<div class="modal-content">
			<div class="modal-header">
				<button type="button" class="close" data-dismiss="modal">×</button>
				<h4 class="modal-title"><div id="appointmentTitleModal"/> </h4>
			</div>
			<div class="modal-body">
				<div class="row add-border">
					<div class="col-sm-5 add-margin">
						<spring:message code="lbl.appmnt.date"></spring:message>
					</div>
					<div class="col-sm-7 add-margin view-content" id="appointmentDateModal">
					</div>
				</div>
				<div class="row add-border">
					<div class="col-sm-5 add-margin">
						<spring:message code="lbl.appmnt.time"></spring:message>
					</div>
					<div class="col-sm-7 add-margin view-content" id="appointmentTimeModal">
					</div>
				</div>
                <c:if test="${appmntInspnRemarks ne null && appmntInspnRemarks ne ''}">
                    <div class="row add-border">
                        <div class="col-sm-5 add-margin">
                            <spring:message code="lbl.remarks"></spring:message>
                        </div>
                        <div class="col-sm-7 add-margin view-content" id="appmntInspnRemarksModal">
                        </div>
                    </div>
                </c:if>
			</div>
			<div class="modal-footer">
				<button type="button" class="btn btn-default" data-dismiss="modal">Close</button>
			</div>
		</div>
	</div>
</div>

<script
	src="<cdn:url value='/resources/global/js/egov/inbox.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script
	src="<cdn:url value='/resources/js/app/application-view.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/edcr-helper.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/citizen-view-helper.js?rnd=${app_release_no}'/>"></script>