/*
 * eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 * accountability and the service delivery of the government  organizations.
 *
 *  Copyright (C) <2017>  eGovernments Foundation
 *
 *  The updated version of eGov suite of products as by eGovernments Foundation
 *  is available at http://www.egovernments.org
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see http://www.gnu.org/licenses/ or
 *  http://www.gnu.org/licenses/gpl.html .
 *
 *  In addition to the terms of the GPL license to be adhered to in using this
 *  program, the following additional terms are to be complied with:
 *
 *      1) All versions of this program, verbatim or modified must carry this
 *         Legal Notice.
 * 	Further, all user interfaces, including but not limited to citizen facing interfaces,
 *         Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *         derived works should carry eGovernments Foundation logo on the top right corner.
 *
 * 	For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 * 	For any further queries on attribution, including queries on brand guidelines,
 *         please contact contact@egovernments.org
 *
 *      2) Any misrepresentation of the origin of the material is prohibited. It
 *         is required that all modified versions of this material be marked in
 *         reasonable ways as different from the original version.
 *
 *      3) This license does not grant any rights to any user of the program
 *         with regards to rights under trademark law for use of the trade names
 *         or trademarks of eGovernments Foundation.
 *
 *  In case of any queries, you can reach eGovernments Foundation at contact@egovernments.org.
 */

$(document).ready(function() {

    var tbody = $('#bpaAdditionalPermitConditions').children('tbody');
    var table = tbody.length ? tbody : $('#bpaAdditionalPermitConditions');
    var row = '<tr>'+
        '<td class="text-center"><span class="serialNo text-center" id="slNoInsp">{{sno}}</span><input type="hidden" name="additionalPermitConditionsTemp[{{idx}}].application" value="{{applicationId}}" /><input type="hidden" class="additionalPermitCondition" name="additionalPermitConditionsTemp[{{idx}}].permitConditionType" value="ADDITIONAL_PERMITCONDITION"/><input type="hidden" class="additionalPermitCondition" name="additionalPermitConditionsTemp[{{idx}}].permitCondition" value="{{permitConditionId}}"/><input type="hidden" class="serialNo" data-sno name="additionalPermitConditionsTemp[{{idx}}].orderNumber"/></td>'+
        '<td><textarea class="form-control patternvalidation additionalPermitCondition" rows="2" maxlength="500" name="additionalPermitConditionsTemp[{{idx}}].additionalPermitCondition"/></td>';

    $('#addAddnlPermitRow').click(function(){
        var idx=$(tbody).find('tr').length;
        //Add row
        var row={
            'sno' : idx+1,
            'idx': idx,
            'permitConditionId':$('#additionalPermitCondition').val(),
            'applicationId':$('#scrutinyapplicationid').val()
        };
        addRowFromObject(row);
        patternvalidation();
    });

    function addRowFromObject(rowJsonObj)
    {
        table.append(row.compose(rowJsonObj));
    }

    String.prototype.compose = (function (){
        var re = /\{{(.+?)\}}/g;
        return function (o){
            return this.replace(re, function (_, k){
                return typeof o[k] != 'undefined' ? o[k] : '';
            });
        }
    }());

	roundOfDecimalPlaces();
	function roundOfDecimalPlaces() {
		// Set decimal places of 2
		$('.decimalfixed').each(function(){
			if($(this).val()){
				$(this).val(parseFloat($(this).val()).toFixed(2));
			}
		});
	}
	var seviceTypeName = $("#serviceType").val();
	$('.extentOfLand').show();
	if('Tower Construction'.localeCompare(seviceTypeName) == 0 || 'Pole Structures'.localeCompare(seviceTypeName) == 0){
		$('.extentOfLand').hide();
		$('.areaOfBase').show();
		hideNewAndExistingBuildingDetails();
	}
	
	var validator = $("#documentscrutinyform").validate({
		highlight : function(element, errorClass) {
			$(element).fadeOut(function() {
				$(element).fadeIn();
			});
		}
	});

	$(".workAction").click(function(e) {
        var action = document
            .getElementById("workFlowAction").value;
        if (action == 'Initiate Rejection') {
            bootbox
                .confirm({
                    message : 'Please confirm, do you really want to initiate rejection for this the application ?',
                    buttons : {
                        'cancel' : {
                            label : 'No',
                            className : 'btn-danger'
                        },
                        'confirm' : {
                            label : 'Yes',
                            className : 'btn-primary'
                        }
                    },
                    callback : function(result) {
                        if (result) {
                            var approvalComent = $('#approvalComent').val();
                            var rejectionReasonsLength = $('.rejectionReasons:checked').length;
                            if(rejectionReasonsLength <= 0){
                                $('.rejectionReason').show();
                                bootbox.alert('Please select atleast one rejection reason is mandatory');
                                return true;
                            } else if (approvalComent == "") {
                                bootbox.alert("Please enter rejection comments!");
                                $('#approvalComent').focus();
                                return true;
                            } else {
                                return validateDocScrutinyForm(validator);
                            }
                        } else {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    }
                });
            return false;
        } else if (action == 'Forward') {
            bootbox
                .confirm({
                    message : 'Please confirm, do you really want to forward this application ?',
                    buttons : {
                        'cancel' : {
                            label : 'No',
                            className : 'btn-danger'
                        },
                        'confirm' : {
                            label : 'Yes',
                            className : 'btn-primary'
                        }
                    },
                    callback: function (result) {
                        if (result) {
                            return validateDocScrutinyForm(validator);
                        } else {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    }
                });
            return false;
        }
	});

});

function validateDocScrutinyForm(validator) {
	if ($('#documentscrutinyform').valid()) {
        $('.loader-class').modal('show', {
            backdrop: 'static'
        });
        document.forms[0].submit();
	} else {
		$errorInput = undefined;

		$.each(validator.invalidElements(),
				function(index, elem) {

					if (!$(elem).is(":visible")
							&& !$(elem).val()
							&& index == 0
							&& $(elem).closest('div').find(
									'.bootstrap-tagsinput').length > 0) {
						$errorInput = $(elem);
					}

					if (!$(elem).is(":visible")
							&& !$(elem).closest('div.panel-body')
									.is(":visible")) {
						$(elem).closest('div.panel-body').show();
						console.log("elem", $(elem));
					}
				});

		if ($errorInput)
			$errorInput.tagsinput('focus');

		validator.focusInvalid();
		return false;
	}
}