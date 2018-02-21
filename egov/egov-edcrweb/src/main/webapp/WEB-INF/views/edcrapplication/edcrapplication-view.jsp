<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://www.springframework.org/tags/form" prefix="form"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn"%>

<div class="main-content">
    <div class="row">
        <div class="col-md-12">
            <div class="panel panel-primary" data-collapsed="0">
                <div class="panel-heading">
                    <div class="panel-title">EdcrApplication</div>
                </div>
                <div class="panel-body custom">
                    <div class="row add-border">
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.applicationnumber"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <c:out value="${edcrApplication.applicationNumber}" default="N/A"></c:out>
                        </div>
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.dcrnumber"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <c:out value="${edcrApplication.dcrNumber}" default="N/A"></c:out>
                        </div>
                    </div>
                    <div class="row add-border">
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.applicationdate"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <fmt:formatDate pattern="MM/dd/yyyyy" value="${edcrApplication.applicationDate}"/>
                        </div>
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.dxffile"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <c:out value="${edcrApplication.dxfFile}" default="N/A"></c:out>
                        </div>
                    </div>
                    <div class="row add-border">
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.filehistory"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <c:out value="${edcrApplication.fileHistory}" default="N/A"></c:out>
                        </div>
                        <div class="col-xs-3 add-margin"><spring:message code="lbl.planinformation"/>
                        </div>
                        <div class="col-sm-3 add-margin view-content">
                            <c:out value="${edcrApplication.planInformation.name}" default="N/A"></c:out>
                        </div>
                    </div>

                    <div class="row text-center">
                        <div class="add-margin"><a href="javascript:void(0)" class="btn
                            btn-default" onclick="self.close()">Close</a></div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>