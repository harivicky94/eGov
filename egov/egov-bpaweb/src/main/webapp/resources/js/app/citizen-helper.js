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


jQuery(document).ready(function($) {
	
	// toggle between multiple tab
	//toggle between multiple tab
	jQuery('form').validate({
		ignore: ".ignore",
		invalidHandler: function(e, validator){
		if(validator.errorList.length)
		$('#settingstab a[href="#' + jQuery(validator.errorList[0].element).closest(".tab-pane").attr('id') + '"]').tab('show');
		}
		});
	
	
	var validator=$("#newCitizenApplicationform").validate({
		  highlight: function(element, errorClass) {
		    $(element).fadeOut(function() {
		      $(element).fadeIn();
		    });
		  }
		});
	
	if ($('#noJAORSAMessage') && $('#noJAORSAMessage').val())
		bootbox.alert($('#noJAORSAMessage').val());

	if ($('#invalidStakeholder').val())
		bootbox.alert($('#invalidStakeholder').val());
	
	function validateForm1(button, validator) {
        $('#newCitizenApplicationform').find(':input',':select',':textarea').each(function(){
            $(this).removeAttr("disabled");
        });
		if ($('#newCitizenApplicationform').valid() && validateUploadFilesMandatory()) {
			$('#serviceType').prop("disabled", false);
			document.getElementById("workFlowAction").value = button;
			if($('#citizenOrBusinessUser').val())
			{
				if($('#isCitizen').val() == 'true'){
					if($('#validateCitizenAcceptance').val() == 'true' && !$('#citizenAccepted').prop('checked')){
						bootbox.alert("Please accept disclaimer to continue...");
						return false;
					}
				}else{
					if(!$('#architectAccepted').prop('checked')){
						bootbox.alert("Please accept disclaimer to continue...");
						return false;
					}
				}
			}
            var seviceTypeName = $( "#serviceType option:selected" ).text();
            if('Amenities' == seviceTypeName && !$( "#applicationAmenity option:selected" ).val()){
                bootbox.alert("Please select atleast one amenity.");
                return false;
            }
            if($('#isOneDayPermitApplication').is(':checked')){
                var inputArea;
                if('Amenities' == seviceTypeName){
                    inputArea = $('#roofConversion').val();
                } else
                    inputArea = $('#totalPlintArea').val();
                if(inputArea > 300){
                    bootbox.alert("For one day permit application maximum permissible area allowed is less than or equal to 300 Sq.Mtrs. Beyond of maximum permissible area not allowed to submit application.");
                    return false;
                }
            }
            $('.loader-class').modal('show', {
                backdrop : 'static'
            });
            document.forms[0].submit();
		} else {
			
			$errorInput=undefined;
			
			$.each(validator.invalidElements(), function(index, elem){
				
				if(!$(elem).is(":visible") && !$(elem).val() && index==0 
						&& $(elem).closest('div').find('.bootstrap-tagsinput').length > 0){
					$errorInput=$(elem);
				}
				
				if(!$(elem).is(":visible") && !$(elem).closest('div.panel-body').is(":visible")){
					$(elem).closest('div.panel-body').show();
					console.log("elem", $(elem));
				}
			});
			
			if($errorInput)
				$errorInput.tagsinput('focus');
			
			validator.focusInvalid();
			
			return false;
		}
	}

    $('#bpaSave').click(function(e) {
        bootbox
            .confirm({
                message : 'Do you really want to save the application, once the application is saved you are not allowed to modify applicant details. Please make sure entered applicant details are valid before save.',
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
                        var button = $('#bpaSave').val();
                        validateForm1(button, validator);
                    } else {
                        e.stopPropagation();
                        e.preventDefault();
                    }
                }
            });
        return false;
    });


    $('#bpaCreate').click(function(e) {
            bootbox
                .confirm({
                    message : 'Do you really want to submit the application, once application is submitted you are not allowed to modify application details and please make sure entered details are valid before submit.',
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
                            var button = $('#bpaCreate').val();
                            validateForm1(button, validator)
                        } else {
                            e.stopPropagation();
                            e.preventDefault();
                        }
                    }
                });
        return false;
    });
	
	$('.applicantname').hide();
	$('#name').change(function() {
		$('.applicantname').show();
		$("span#applicantName").html($(this).val());
	});
	
});
	