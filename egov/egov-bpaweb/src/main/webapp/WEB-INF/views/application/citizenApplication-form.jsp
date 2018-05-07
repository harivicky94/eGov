<%--
  ~ eGov suite of products aim to improve the internal efficiency,transparency,
  ~      accountability and the service delivery of the government  organizations.
  ~
  ~       Copyright (C) <2017>  eGovernments Foundation
  ~
  ~       The updated version of eGov suite of products as by eGovernments Foundation
  ~       is available at http://www.egovernments.org
  ~
  ~       This program is free software: you can redistribute it and/or modify
  ~       it under the terms of the GNU General Public License as published by
  ~       the Free Software Foundation, either version 3 of the License, or
  ~       any later version.
  ~
  ~       This program is distributed in the hope that it will be useful,
  ~       but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~       MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~       GNU General Public License for more details.
  ~
  ~       You should have received a copy of the GNU General Public License
  ~       along with this program. If not, see http://www.gnu.org/licenses/ or
  ~       http://www.gnu.org/licenses/gpl.html .
  ~
  ~       In addition to the terms of the GPL license to be adhered to in using this
  ~       program, the following additional terms are to be complied with:
  ~
  ~           1) All versions of this program, verbatim or modified must carry this
  ~              Legal Notice.
  ~
  ~           2) Any misrepresentation of the origin of the material is prohibited. It
  ~              is required that all modified versions of this material be marked in
  ~              reasonable ways as different from the original version.
  ~
  ~           3) This license does not grant any rights to any user of the program
  ~              with regards to rights under trademark law for use of the trade names
  ~              or trademarks of eGovernments Foundation.
  ~
  ~     In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn"%>
<div class="row">
	<div class="col-md-12">
		<div class="text-left error-msg col-sm-12" style="font-size: 14px;">
			<span class="applicantname"> <spring:message
					code="lbl.applicant.name" /> : <span id="applicantName"></span>
			</span>
		</div>
		<%-- <div class="text-right error-msg col-sm-6" style="font-size: 14px;">
			<spring:message code="lbl.application.date" />
			:
			<fmt:formatDate pattern="dd/MM/yyyy"
				value="${bpaApplication.applicationDate}" />
		</div> --%>
		<form:form role="form" action="application-create" method="post"
				   modelAttribute="bpaApplication" id="newCitizenApplicationform"
				   cssClass="form-horizontal form-groups-bordered"
				   enctype="multipart/form-data">
			<input type="hidden" id="bpaApplication" name="bpaApplication"
				   value="${bpaApplication.id}" />
			<input type="hidden" id="noJAORSAMessage" name="noJAORSAMessage"
				   value="${noJAORSAMessage}" />
			<input type="hidden" id="invalidStakeholder" name="invalidStakeholder"
				   value="${invalidStakeholder}" />
			<form:hidden path="" id="onlinePaymentEnable" name="onlinePaymentEnable" value="${onlinePaymentEnable}" />
			<input type="hidden" id="mode" name="mode" value="${mode}" />
			<input type="hidden"  id="citizenOrBusinessUser" name="citizenOrBusinessUser" value="${citizenOrBusinessUser}"/>
			<input type="hidden"  id="validateCitizenAcceptance" name="validateCitizenAcceptance" value="${validateCitizenAcceptance}"/>
			<input type="hidden"  id="citizenDisclaimerAccepted" name="citizenDisclaimerAccepted" value="${citizenDisclaimerAccepted}"/>
			<input type="hidden"  id="isCitizen" name="isCitizen" value="${isCitizen}"/>
			<input type="hidden"  id="cityName" name="cityName" value="${cityName}"/>
			<form:hidden path="" id="workFlowAction" name="workFlowAction" />
			<ul class="nav nav-tabs" id="settingstab">
				<li class="active"><a data-toggle="tab"
									  href="#appliccation-info" data-tabidx=0><spring:message
						code='lbl.appln.details' /></a></li>
				<li><a data-toggle="tab" href="#document-info" data-tabidx=1><spring:message
						code='title.documentdetail' /></a></li>
				<li><a data-toggle="tab" href="#noc-document-info" data-tabidx=2><spring:message
						code='lbl.noc.doc.details' /></a></li>
			</ul>
			<div class="tab-content">
				<div id="appliccation-info" class="tab-pane fade in active">
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="applicationDetails.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary edcrApplnDetails" data-collapsed="0">
						<jsp:include page="edcr-application-details-form.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary" data-collapsed="0" id="applicantDiv">
						<jsp:include page="applicantDetailForm.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="siteDetail.jsp"></jsp:include>
					</div>
					<div class="panel panel-primary existingbuildingdetails"
						 data-collapsed="0">
						<jsp:include page="existing-buildingdetails.jsp" />
					</div>

					<div class="panel panel-primary buildingdetails" data-collapsed="0">
						<jsp:include page="buildingDetails.jsp" />
					</div>
					<c:if test="${(isCitizen && validateCitizenAcceptance) || (!isCitizen)}">
						<div class="panel panel-primary" data-collapsed="0">
							<jsp:include page="disclaimer.jsp" />
						</div>
					</c:if>
				</div>
				<div id="document-info" class="tab-pane fade">
					<div class="panel panel-primary" data-collapsed="0">
                        <c:if test="${bpaApplication.serviceType.code eq '01'}">
                            <div class="dcrDocuments">
								<jsp:include page="bpa-dcr-documents.jsp"></jsp:include>
                            </div>
                        </c:if>
						<jsp:include page="bpaDocumentDetails.jsp"></jsp:include>
					</div>
				</div>
				<div id="noc-document-info" class="tab-pane fade">
					<div class="panel panel-primary" data-collapsed="0">
						<jsp:include page="noc-document-updation.jsp"></jsp:include>
					</div>
				</div>
			</div>

			<div align="center">
				<form:button type="submit" id="bpaSave" class="btn btn-primary"
							 value="Save">Save</form:button>
				<form:button type="submit" id="bpaCreate" class="btn btn-primary"
							 value="Submit">Submit</form:button>
				<input type="button" name="button2" id="button2" value="Close"
					   class="btn btn-default" onclick="window.close();" />
			</div>
		</form:form>
	</div>
</div>

<link rel="stylesheet"
	  href="<c:url value='/resources/global/css/bootstrap/bootstrap-tagsinput.css?rnd=${app_release_no}' context='/egi'/>">
<script
		src="<c:url value='/resources/global/js/bootstrap/bootstrap-tagsinput.min.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script
		src="<c:url value='/resources/global/js/handlebars/handlebars.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script
		src="<cdn:url value='/resources/global/js/egov/inbox.js?rnd=${app_release_no}' context='/egi'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/bpa-ajax-helper.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/documentsuploadvalidation.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/buildingarea-details.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/bpa-application-validations.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/citizen-helper.js?rnd=${app_release_no}'/>"></script>
<script
		src="<cdn:url value='/resources/js/app/edcr-helper.js?rnd=${app_release_no}'/>"></script>