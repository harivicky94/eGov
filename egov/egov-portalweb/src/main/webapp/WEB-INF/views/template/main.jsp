<%--
  ~    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
  ~    accountability and the service delivery of the government  organizations.
  ~
  ~     Copyright (C) 2017  eGovernments Foundation
  ~
  ~     The updated version of eGov suite of products as by eGovernments Foundation
  ~     is available at http://www.egovernments.org
  ~
  ~     This program is free software: you can redistribute it and/or modify
  ~     it under the terms of the GNU General Public License as published by
  ~     the Free Software Foundation, either version 3 of the License, or
  ~     any later version.
  ~
  ~     This program is distributed in the hope that it will be useful,
  ~     but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  ~     GNU General Public License for more details.
  ~
  ~     You should have received a copy of the GNU General Public License
  ~     along with this program. If not, see http://www.gnu.org/licenses/ or
  ~     http://www.gnu.org/licenses/gpl.html .
  ~
  ~     In addition to the terms of the GPL license to be adhered to in using this
  ~     program, the following additional terms are to be complied with:
  ~
  ~         1) All versions of this program, verbatim or modified must carry this
  ~            Legal Notice.
  ~            Further, all user interfaces, including but not limited to citizen facing interfaces,
  ~            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
  ~            derived works should carry eGovernments Foundation logo on the top right corner.
  ~
  ~            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
  ~            For any further queries on attribution, including queries on brand guidelines,
  ~            please contact contact@egovernments.org
  ~
  ~         2) Any misrepresentation of the origin of the material is prohibited. It
  ~            is required that all modified versions of this material be marked in
  ~            reasonable ways as different from the original version.
  ~
  ~         3) This license does not grant any rights to any user of the program
  ~            with regards to rights under trademark law for use of the trade names
  ~            or trademarks of eGovernments Foundation.
  ~
  ~   In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
  ~
  --%>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>
<%@ taglib uri="/WEB-INF/taglib/cdn.tld" prefix="cdn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
    <head>
        <meta charset="utf-8">
        <meta http-equiv="X-UA-Compatible" content="IE=edge">
        <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, user-scalable=no">
        <meta name="description" content="eGov ERP System"/>
        <meta name="author" content="eGovernments Foundation"/>

        <title><tiles:insertAttribute name="title"/></title>

        <spring:eval expression="@environment.getProperty('user.pwd.strength')" var="pwdstrengthmsg"/>
        <spring:message code="usr.pwd.strength.msg.${pwdstrengthmsg}" var="pwdmsg" htmlEscape="true"/>

        <link rel="stylesheet" href="<cdn:url value='/resources/global/css/bootstrap/bootstrap.css' context='/egi'/>">
        <link rel="stylesheet"
              href="<cdn:url value='/resources/global/css/font-icons/font-awesome/css/font-awesome.min.css?rnd=${app_release_no}' context='/egi'/>">
        <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
        <link href="<cdn:url value='/resources/css/citizen.css?rnd=${app_release_no}'/>" rel="stylesheet">
        <link href="<cdn:url value='/resources/css/scrollbar.css'/>" rel="stylesheet">
        <link rel="icon" href="<cdn:url value='/resources/global/images/favicon.png' context='/egi'/>" sizes="32x32">

        <script src="<cdn:url value='/resources/global/js/jquery/jquery.js' context='/egi'/>"></script>
        <script src="<cdn:url value='/resources/global/js/bootstrap/bootstrap.js' context='/egi'/>"></script>
        <script src="<cdn:url value='/resources/global/js/bootstrap/bootbox.min.js' context='/egi'/>"></script>
        <script src="<cdn:url value='/resources/global/js/jquery/plugins/jquery.validate.min.js' context='/egi'/>"></script>
        <script src="//cdnjs.cloudflare.com/ajax/libs/jquery.matchHeight/0.7.0/jquery.matchHeight-min.js"></script>
        <script src="<cdn:url value='/resources/js/citizen.js?rnd=${app_release_no}'/>"></script>

        <%-- <script src="<cdn:url value='/resources/global/js/egov/custom.js?rnd=${app_release_no}' context='/egi'/>"></script> --%>
        <%-- <script src="<cdn:url value='/resources/js/app/homepagecitizen.js?rnd=${app_release_no}' context='/egi'/>"></script> --%>


        <!-- HTML5 shim and Respond.js IE8 support of HTML5 elements and media queries -->
        <!--[if lt IE 9]>
        <script src="<cdn:url value='/resources/global/js/ie8/html5shiv.min.js' context='/egi'/>"></script>
        <script src="<cdn:url value='/resources/global/js/ie8/respond.min.js' context='/egi'/>"></script>
        <![endif]-->
        <script>
            var googleapikey = '${sessionScope.googleApiKey}';
            var citylat = ${sessionScope.citylat};
            var citylng = ${sessionScope.citylng};
        </script>
    </head>
    <body>
    <spring:htmlEscape defaultHtmlEscape="true"/>
    <tiles:insertAttribute name="body"/>

    <div class="modal fade change-password" data-backdrop="static" data-keyboard="false">
        <div class="modal-dialog">
            <div class="modal-content">

                <div class="modal-header">
                    <button type="button" class="close pass-cancel" data-dismiss="modal" aria-hidden="true">&times;</button>
                    <h4 class="modal-title">Change Password</h4>
                </div>

                <div class="modal-body">

                    <c:if test="${dflt_pwd_reset_req}">
                        <div class="alert alert-warning" role="alert" id="pass-alert">
                            <i class="fa fa-exclamation-triangle"></i> Security alert...! You are using default password,
                            please reset your password.
                        </div>
                    </c:if>
                    <form id="passwordForm" class="form-horizontal form-groups-bordered">
                        <div class="form-group">
                            <div class="col-md-4">
                                <label class="control-label">Old Password</label>
                            </div>
                            <div class="col-md-8 add-margin">
                                <input type="password" autocomplete="new-password" class="form-control" id="old-pass"
                                       required="required">
                            </div>
                        </div>
                        <div class="form-group" id="wrap">
                            <div class="col-md-4">
                                <label class="control-label">New Password</label>
                            </div>
                            <div class="col-md-8 add-margin">
                                <input type="password" class="form-control checkpassword" id="new-pass" maxlength="32"
                                       required="required" data-container="#wrap" data-toggle="popover"
                                       data-content="${pwdmsg}">
                            </div>
                        </div>
                        <div class="form-group">
                            <div class="col-md-4">
                                <label class="control-label">Re-type Password</label>
                            </div>
                            <div class="col-md-8 add-margin">
                                <input type="password" class="form-control checkpassword" autocomplete="new-password"
                                       id="retype-pass">
                                <div id="pwd-incorrt-match"
                                     class="password-error error-msg alert alert-danger display-hide">Password is not matching
                                </div>
                                <div class="password-error-msg display-hide">${pwdmsg}</div>
                            </div>
                        </div>
                        <div class="form-group text-right">
                            <div class="col-md-12 add-margin">
                                <button type="submit" class="btn btn-primary" id="btnChangePwd">Change Password</button>
                                <button type="button" class="btn btn-default pass-cancel" data-dismiss="modal">Cancel
                                </button>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </div>
    <c:if test="${dflt_pwd_reset_req}">
        <script>
            $('.change-password').modal('show');
            $('.pass-cancel').attr('disabled', 'disabled');
        </script>
    </c:if>
    <c:if test="${warn_pwd_expire}">
        <script>
            var pwdExpireInDays = ${pwd_expire_in_days};
            bootbox.alert("Your password will expire in " + pwdExpireInDays + " day(s), please update your password.");
        </script>
    </c:if>
    </body>
</html>