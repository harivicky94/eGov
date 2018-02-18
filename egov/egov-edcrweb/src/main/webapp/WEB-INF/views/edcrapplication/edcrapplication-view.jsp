<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ include file="/includes/taglibs.jsp"%>
<div class="main-content"><div class="row"><div class="col-md-12"><div class="panel panel-primary" data-collapsed="0"><div class="panel-heading"><div class="panel-title">EdcrApplication</div></div><div class="panel-body custom"><div class="row add-border"><div class="col-xs-3 add-margin"><spring:message code="lbl.applicationnumber" />
</div><div class="col-sm-3 add-margin view-content">
${edcrApplication.applicationNumber}
</div><div class="col-xs-3 add-margin"><spring:message code="lbl.dcrnumber" />
</div><div class="col-sm-3 add-margin view-content">
${edcrApplication.dcrNumber}
</div></div>
<div class="row add-border"><div class="col-xs-3 add-margin"><spring:message code="lbl.applicationdate" />
</div><div class="col-sm-3 add-margin view-content">
<fmt:formatDate pattern="MM/dd/yyyyy" value="${edcrApplication.applicationDate} />
</div><div class="col-xs-3 add-margin"><spring:message code="lbl.dxffile" />
</div><div class="col-sm-3 add-margin view-content">
${edcrApplication.dxfFile}
</div></div>
<div class="row add-border"><div class="col-xs-3 add-margin"><spring:message code="lbl.filehistory" />
</div><div class="col-sm-3 add-margin view-content">
${edcrApplication.fileHistory}
</div><div class="col-xs-3 add-margin"><spring:message code="lbl.planinformation" />
</div><div class="col-sm-3 add-margin view-content">
${edcrApplication.planInformation.name}
</div></div>
</div></div></div></div><div class="row text-center"><div class="add-margin"><a href="javascript:void(0)" class="btn btn-default" onclick="self.close()">Close</a></div></div>