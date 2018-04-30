/*
 * eGov suite of products aim to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) <2017>  eGovernments Foundation
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

$(document).ready(
    function ($) {

        if($('#eDcrNumber').val()) {
            getEdcrApprovedPlanDetails();
        }

        $('#eDcrNumber').blur(function () {
        	if($('#eDcrNumber').val()) {
        	    if(!checkIsEdcrNumberUsedInAnyBpaApplication())
                    getEdcrApprovedPlanDetails();
            }

        });

        function getEdcrApprovedPlanDetails() {
            $.ajax({
                async: false,
                crossDomain: true,
                url: '/edcr/rest/approved-plan-details/by-edcr-number/' + $('#eDcrNumber').val(),
                type: "GET",
                contentType: 'application/json; charset=utf-8',
                success: function (response) {
                    if (response.errorDetail && response.errorDetail.errorCode != null && response.errorDetail.errorCode != '') {
                        bootbox.alert(response.errorDetail.errorMessage);
                        $('#eDcrNumber').val('');
                    } else {
                        $('#edcrApplicationNumber').html(response.applicationNumber);
                        $('#edcrUploadedDate').html(response.applicationDate);
                        $('#edcrDxfFile').html('<a href="/egi/downloadfile?fileStoreId=' + response.dxfFile.fileStoreId + '&moduleName=Digit DCR&toSave=true">' + response.dxfFile.fileName + '</a>');
                        $('#edcrPlanReportOutput').html('<a href="/egi/downloadfile?fileStoreId=' + response.reportOutput.fileStoreId + '&moduleName=Digit DCR&toSave=true">' + response.reportOutput.fileName + '</a>');
                    }
                },
                error: function (response) {
                    //
                }
            });
        }

        function checkIsEdcrNumberUsedInAnyBpaApplication() {
            var isExist;
            $.ajax({
                async: false,
                url: '/bpa/validate/edcr-usedinbpa',
                type: "GET",
                data : {
                    eDcrNumber : $('#eDcrNumber').val()
                },
                contentType: 'application/json; charset=utf-8',
                success: function (response) {
                    if(response.isExists == 'true' && $('#applicationNumber').val() != response.applnNoUsedEdcr) {
                        bootbox.alert(response.message);
                        $('#eDcrNumber').val('');
                        isExist = true;
                    } else {
                        isExist = false;
                    }
                },
                error: function (response) {
                    //
                }
            });
            return isExist;
        }

        //$('#eDcrNumber').trigger("blur");
    });