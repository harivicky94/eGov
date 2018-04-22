/*
 *    eGov  SmartCity eGovernance suite aims to improve the internal efficiency,transparency,
 *    accountability and the service delivery of the government  organizations.
 *
 *     Copyright (C) 2017  eGovernments Foundation
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
 *            Further, all user interfaces, including but not limited to citizen facing interfaces,
 *            Urban Local Bodies interfaces, dashboards, mobile applications, of the program and any
 *            derived works should carry eGovernments Foundation logo on the top right corner.
 *
 *            For the logo, please refer http://egovernments.org/html/logo/egov_logo.png.
 *            For any further queries on attribution, including queries on brand guidelines,
 *            please contact contact@egovernments.org
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
 *
 */

$(document).ready( function () {
    $('#otp-section,#create-section').hide();

    //validate form while toggle between multiple tab
    jQuery('form').validate({
        ignore: ".ignore",
        invalidHandler: function(e, validator){
            if(validator.errorList.length)
                $('#settingstab a[href="#' + jQuery(validator.errorList[0].element).closest(".tab-pane").attr('id') + '"]').tab('show');
        }
    });

    var validator=$('form').validate({
        highlight: function(element, errorClass) {
            $(element).fadeOut(function() {
                $(element).fadeIn();
            });
        }
    });

    function validateForm(validator) {
        if ($('form').valid() && validateUploadFilesMandatory()) {
            return true;
        } else {
            $.each(validator.invalidElements(), function(index, elem){
                if(!$(elem).is(":visible") && !$(elem).closest('div.panel-body').is(":visible")){
                    $(elem).closest('div.panel-body').show();
                    console.log("elem", $(elem));
                }
            });
            validator.focusInvalid();
            return false;
        }
    }

    // On edit form load checking is both addresses same then adding/removing check box value
	if( $('input[id="correspondenceAddress.houseNoBldgApt"]').val() != '' && $('input[id="correspondenceAddress.streetRoadLine"]').val() != ''
		&& $('input[id="permanentAddress.houseNoBldgApt"]').val() != '' && $('input[id="permanentAddress.streetRoadLine"]').val() != ''){
	if ($('input[id="permanentAddress.houseNoBldgApt"]').val() === $('input[id="correspondenceAddress.houseNoBldgApt"]').val()
		&& $('input[id="permanentAddress.streetRoadLine"]').val() === $('input[id="correspondenceAddress.streetRoadLine"]').val() 
		&& $('input[id="permanentAddress.areaLocalitySector"]').val() === $('input[id="correspondenceAddress.areaLocalitySector"]').val()) {
		$('#isAddressSame').attr( "checked", "checked" );
	} else {
		$('#isAddressSame').removeAttr( "checked" );
	}
	}

    $('#buttonSubmit').click(function(e){
        if(validateForm(validator)){
            $('#mobileNumber').val();
            $('#stakeHolderByCitizenform').trigger('submit');
        }else{
            e.preventDefault();
        }
    });

    $('#otpbtn').click(function (e) {
        if (validateForm(validator)) {
            $.ajax({
                url: "/portal/citizen/signup/otp/" + $('#mobileNumber').val(),
                dataType: "json",
                success: function (data) {
                    if (data) {
                        console.log('OTP sent');
                        $('#activationcode').val('');
                        $('#create-section,#otp-section').show();
                        $('#otpbtn-section').hide();
                    }
                },
                error: function () {
                    console.error('Error while sending otp');
                    $(this).show();
                }

            });
        } else {
            e.preventDefault();
        }
    });

    $(document).on('click','.otp-view',function(){
        if($(this).hasClass('otp-view')){
            if($(this).data('view') == 'show'){
                $(this).closest('.form-group').find('input').attr({type : 'text', autocomplete : 'off'});
                $(this).parent().empty().html('<i class="fa fa-eye-slash otp-view" data-view="hide" aria-hidden="true"></i>');
            }else{
                $(this).closest('.form-group').find('input').attr({type : 'password', autocomplete : 'new-password'});
                $(this).parent().empty().html('<i class="fa fa-eye otp-view" data-view="show" aria-hidden="true"></i>');
            }
        }
    });
	
// mobile number validation
$('.mobileno-field').blur( function () {
	 var mobileno = $(this).val();
		if (mobileno.length < 10) {
			bootbox.alert("Please enter 10 digit mobile number");
			$(this).val('');
		}
	});
// To coping present address to permanent address if both same
$('#isAddressSame').click(function(e) {
	if($('#isAddressSame').is(':checked')){
		$('input[id="permanentAddress.houseNoBldgApt"]').val($('input[id="correspondenceAddress.houseNoBldgApt"]').val());
		$('input[id="permanentAddress.streetRoadLine"]').val($('input[id="correspondenceAddress.streetRoadLine"]').val());
		$('input[id="permanentAddress.areaLocalitySector"]').val($('input[id="correspondenceAddress.areaLocalitySector"]').val());
		$('input[id="permanentAddress.cityTownVillage"]').val($('input[id="correspondenceAddress.cityTownVillage"]').val());
		$('input[id="permanentAddress.district"]').val($('input[id="correspondenceAddress.district"]').val());
		$('input[id="permanentAddress.state"]').val($('input[id="correspondenceAddress.state"]').val());
		$('input[id="permanentAddress.pinCode"]').val($('input[id="correspondenceAddress.pinCode"]').val());
		$('input[id="permanentAddress.postOffice"]').val($('input[id="correspondenceAddress.postOffice"]').val());
	}
});

// To show/hide Organization details based on user selection
$("input[name='isOnbehalfOfOrganization']").change(function(){
    var isOnbehalfOfOrganization = $(this).val();
if(isOnbehalfOfOrganization === 'true') {
	$('#showhide').removeClass('hide');
	$('.addremoverequired').attr( "required", "true" );
	$('.toggle-madatory').find("span").addClass( "mandatory" );
} else {
	$('#showhide').addClass('hide');
	$('.addremoverequired').removeAttr( "required" );
	$('.toggle-madatory').find("span").removeClass( "mandatory" );
}
});
if ($('#invalidBuildingLicensee').val())
	bootbox.alert($('#invalidBuildingLicensee').val());

// email validation
$('input[id$="emailId"]').blur(function() {
		var pattern = new RegExp("^[\\w!#$%&'*+/=?`{|}~^-]+(?:\\.[\\w!#$%&'*+/=?`{|}~^-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,6}$");
		var email = $(this).val();
		if (!pattern.test(email) && $(this).val().length > 0) {
			var span = $(this).siblings('span'); 
	    	$(span).addClass('error-msg');
	    	$(span).text('Please enter valid email..!');
			$(this).show();
			$(this).val("");
		} else {
			var span1 = $(this).siblings('span'); 
			$(span1).removeClass('error-msg');
	    	$(span1).text('');
		}
	});

});
	
var stakeHolderId = $('#stakeHolderId').val();
if(stakeHolderId){
	$('#emailId').prop("disabled", true);
	$('#mobileNumber').prop("disabled", true);
}
