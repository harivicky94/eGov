<div class="main-content"><div class="row"><div class="col-md-12"><div class="panel panel-primary" data-collapsed="0"><div class="panel-heading"><div class="panel-title">EdcrApplication</div></div><div class="panel-body"><div class="form-group">
<label class="col-sm-3 control-label text-right"><spring:message code="lbl.applicationnumber" />
</label><div class="col-sm-3 add-margin">
<form:input  path="applicationNumber" class="form-control text-left patternvalidation" data-pattern="alphanumeric" maxlength="0"  />
<form:errors path="applicationNumber" cssClass="error-msg" /></div><label class="col-sm-3 control-label text-right"><spring:message code="lbl.dcrnumber" />
</label><div class="col-sm-3 add-margin">
<form:input  path="dcrNumber" class="form-control text-left patternvalidation" data-pattern="alphanumeric" maxlength="0"  />
<form:errors path="dcrNumber" cssClass="error-msg" /></div></div>
<div class="form-group">
<label class="col-sm-3 control-label text-right"><spring:message code="lbl.applicationdate" />
</label><div class="col-sm-3 add-margin">
 <form:input path="applicationDate" class="form-control datepicker" data-date-end-date="0d"  data-inputmask="'mask': 'd/m/y'" />
<form:errors path="applicationDate" cssClass="error-msg" /></div><label class="col-sm-3 control-label text-right"><spring:message code="lbl.dxffile" />
</label><div class="col-sm-3 add-margin">
 <form:input path="dxfFile" class="form-control text-right patternvalidation" data-pattern="number"  />
<form:errors path="dxfFile" cssClass="error-msg" /></div></div>
<div class="form-group">
<label class="col-sm-3 control-label text-right"><spring:message code="lbl.planinformation" />
</label><div class="col-sm-3 add-margin">
<form:select path="planInformation" id="planInformation" cssClass="form-control"  cssErrorClass="form-control error" >
<form:option value=""> <spring:message code="lbl.select"/> </form:option>
<form:options items="${planInformations}" itemValue="id" itemLabel="name" />
</form:select>
 <form:input path="planInformation" class="form-control text-right patternvalidation" data-pattern="number"  />
<form:errors path="planInformation" cssClass="error-msg" /></div> <input type="hidden" name="edcrApplication" value="${edcrApplication.id}" />