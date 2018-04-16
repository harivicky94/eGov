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

var reportdatatable;
var occupancyResponse;
var extentOfLand;
var totalFloorArea;
var extentInSqmts;
var mixedOccupancyResponse;
$(document).ready(function() {

    $('#oneDayPermitSec').hide();
	$('.buildingdetails').hide();
	$('.existingbuildingdetails').hide();
	removeMandatoryForExistingBuildingDetails();
	$('.show-hide').hide();
	$('.totalPlintArea').show();
	
	
	roundOfDecimalPlaces();
	function roundOfDecimalPlaces() {
		// Set decimal places of 2
		$('.decimalfixed').each(function(){
			if($(this).val()){
				$(this).val(parseFloat($(this).val()).toFixed(2));
			}
		});
	}
	
	// showing server side validation message in building details section as well as showing same in alert also.
	if($('#violationMessage').val()) {
		bootbox.alert($('#violationMessage').val()+' Please check and update building floor details to proceed further.');
		$('#showViolationMessage').focus();
		$('#showViolationMessage').html($('#violationMessage').val());
		$('#showViolationMessage').closest('div.panel-body').show();
	}
	
	// Validate input value must be greater than zero using class name
	$.validator.addMethod("nonzero", function(value, element) {
		return this.optional(element) || (parseFloat(value) > 0);
		}, '* Value must be greater than zero');
	
	// pincode validation
	$.validator.addMethod("searchpincode", function(value, element) {
		return this.optional(element) || value !== '';
		}, 'required');
	
	// To prevent multiple decimal places in single input
	$(document).on('keypress', '.decimalfixed', function(evt) {
		
		  evt = (evt) ? evt : window.event;
		  var charCode = (evt.which) ? evt.which : evt.keyCode;
		  if (charCode == 8 || charCode == 37) {
		    return true;
		  } else if (charCode == 46 && $(this).val().indexOf('.') != -1) {
		    return false;
		  } else if (charCode > 31 && charCode != 46 && (charCode < 48 || charCode > 57)) {
		    return false;
		  }
		  return true;
	});
	
	// government or quasi validation
	$('#isEconomicallyWeakerSec').hide();
	$('#governmentType').prop('checked', false);
	$('.governmentType').on('change', function() {
		 var govType = $("input[name='governmentType']:checked").val();
	        if(govType == 'NOT_APPLICABLE'){
	        	$('#governmentType').prop('checked', false);
	        	$('#isEconomicallyWeakerSec').hide();
	        } else {
	        	$('#isEconomicallyWeakerSec').show();
	        }
		});
	
	$('.governmentType').trigger("change");
	// TOWN PLANNING SCHEME VALIDATION
	$('#schemes').change(function(){
		var scheme = $( "#schemes option:selected" ).val();
		if(scheme) {
			$('#landUsage').attr('required',true);
			$('.landUsage').find("span").addClass( "mandatory" );
		} else {
			$('#landUsage').removeAttr('required');
			$('.landUsage').find("span").removeClass( "mandatory" );
		}
	});
	
	// For each main service type validations
	$('.serviceType').change(function(){
		
		loadAmenities();
		
		var seviceTypeName = $( "#serviceType option:selected" ).text();
		$('.doorNo').show();
		$('.show-hide').hide();
		$('.floor-toggle-mandatory').find("span").removeClass( "mandatory" );
		$('.floor-details-mandatory').removeAttr('required');
		$('.areaOfBase').hide();
		$('.extentOfLand').show();

		if('Sub-Division of plot/Land Development'.localeCompare(seviceTypeName) == 0 ){
			hideNewAndExistingBuildingDetails();
		} else if('Tower Construction'.localeCompare(seviceTypeName) == 0 || 'Pole Structures'.localeCompare(seviceTypeName) == 0){
			$('.extentOfLand').hide();
			$('.areaOfBase').show();
			hideNewAndExistingBuildingDetails();
		} else if('Amenities' == seviceTypeName){
			hideNewAndExistingBuildingDetails();
		} else if('Huts and Sheds' == seviceTypeName){
			hideNewAndExistingBuildingDetails();
			$('.noofhutorshed').show();
			$('.Hut').find("span").addClass( "mandatory" );
			$('.noofhutorshed').find("span").addClass( "mandatory" );
		} else if('Alteration' == seviceTypeName){
			addMandatoryForExistingBuildingDetails();
			showNewAndExistingBuildingDetails();
			addMandatoryForNewBuildingDetails();
			$('.alterationInArea').find("span").addClass( "mandatory" );
			$('#totalPlintArea').attr('required',true);
			$('#totalPlintArea').attr('readOnly',false);
			$('.alterationInArea').show();
		} else if('Addition or Extension' == seviceTypeName){
			addMandatoryForExistingBuildingDetails();
			showNewAndExistingBuildingDetails();
			addMandatoryForNewBuildingDetails();
			$('#totalPlintArea').attr('required',true);
			$('#totalPlintArea').attr('readOnly',false);
			$('.additionInArea').find("span").addClass( "mandatory" );
			$('.additionInArea').show();
		} else {
			removeMandatoryForExistingBuildingDetails();
			$('.existingbuildingdetails').hide();
			if('New Construction'.localeCompare(seviceTypeName) == 0 ){
				$('.buildingdetails').show();
				$('.totalPlintArea').show();
				$('.doorNo').hide();
			} else if('Reconstruction'.localeCompare(seviceTypeName) == 0 ){
				$('.buildingdetails').show();
				$('.totalPlintArea').show();
			}  else if('Change in occupancy' == seviceTypeName){
				addMandatoryForExistingBuildingDetails();
				showNewAndExistingBuildingDetails();
				$('.changeInOccupancyArea').show();
			} else if ('Demolition' == seviceTypeName){
				$('.buildingdetails').show();
				$('.demolition').show();
			} else {
				$('.totalPlintArea').show();
			}
			addMandatoryForNewBuildingDetails();
			$('#totalPlintArea').attr('readOnly',true);
		}
	});
	
	function showNewAndExistingBuildingDetails() {
		$('.existingbuildingdetails').show();
		$('.buildingdetails').show();
	}
	function hideNewAndExistingBuildingDetails() {
        $('#buildingAreaDetails tbody').find("tr").remove();
        $('#existingBuildingAreaDetails tbody').find("tr").remove();
		$('.handle-mandatory').removeAttr('required');
		$('.handle-mandatory').find("span").removeClass( "mandatory" );
		$('.buildingdetails').hide();
		removeMandatoryForExistingBuildingDetails();
		$('.existingbuildingdetails').hide();
	}
	
	function addMandatoryForNewBuildingDetails() {
		$('.handle-mandatory').attr('required',true);
		$('.handle-mandatory').find("span").addClass( "mandatory" );
		$('.floor-toggle-mandatory').find("span").addClass( "mandatory" );
		$('.floor-details-mandatory').attr('required',true);
	}
	
	function addMandatoryForExistingBuildingDetails() {
		$('.exist-handle-mandatory').attr('required',true);
		$('.exist-handle-mandatory').find("span").addClass( "mandatory" );
		$('.exist-floor-toggle-mandatory').find("span").addClass( "mandatory" );
		$('.exist-floor-details-mandatory').attr('required',true);
	}
	
	function removeMandatoryForExistingBuildingDetails() {
		$('.exist-handle-mandatory').removeAttr('required');
		$('.exist-handle-mandatory').find("span").removeClass( "mandatory" );
		$('.exist-floor-details-mandatory').find("span").removeClass( "mandatory" );
		$('.exist-floor-details-mandatory').removeAttr('required');
	}
	
	// Each Amenity type validations
	
	$(document).on('change',"#applicationAmenity",function () {
		loadAmenities();
	});
	
	function loadAmenities(){
		
		var amenities = [];
		
		if($( "#serviceType option:selected" ).text() == 'Huts and Sheds'){
			amenities.push('Huts and Sheds');
		} else if($( "#serviceType option:selected" ).text() == 'Tower Construction'){
			amenities.push('Tower Construction');
		} else if($( "#serviceType option:selected" ).text() == 'Pole Structures'){
			amenities.push('Pole Structures');
		}
		
		$.each($("#applicationAmenity option:selected"), function(idx){     
			amenities.push($(this).text());
		});
		
		
		var result="";
		$.each(amenities, function(idx, value){            
			//console.log('is even?', $(this).text(), idx, );
			var isEven=(parseInt(idx)%2 === 0);
			if(isEven){
				result= result+ (result?"</div><div class='form-group'>":"<div class='form-group'>");
			}
			result=result+getTemplateComplie(value, isEven);
        });
		result=result+"</div>";
		$('#amenitiesInputs').html(result);
		roundOfDecimalPlaces();
		patternvalidation();
	}
	
	function getTemplateComplie(value, isFirstPosition){
		var templateStr="";
		
		switch(value) {
		    case "Well":
		        templateStr=$('#well-template').html();
		        break;
		    case "Compound Wall":
		    	templateStr=$('#compound-template').html();
		        break;
		    case "Shutter or Door Conversion/Erection under rule 100 or 101":
		    	templateStr=$('#shutter-template').html();
		        break;
		    case "Roof Conversion under rule 100 or 101":
		    	templateStr=$('#roof-template').html();
		        break;
		    case "Tower Construction":
		    	templateStr=$('#tower-template').html();
		        break;
		    case "Pole Structures":
		    	templateStr=$('#poles-template').html();
		        break;
		    case "Huts and Sheds":
		    	templateStr=$('#sheds-template').html();
		        break;
		}
		
		return templateStr.replace(/{className}/g, isFirstPosition?'col-sm-3':'col-sm-2');
	}
	
	$('#existingAppPlan').hide();
	$('#constDiv').hide();
	$('#isexistingApprovedPlan').on('change', function(){ 
		   if(this.checked) // if changed state is "CHECKED"
		    {
			   $('.removemandatory').find("span").addClass( "mandatory" );
			   $('#existingAppPlan').show();
			   $('#feeAmountRecieptNo').attr('required', true);
			   $('#approvedReceiptDate').attr('required', true);
			   $('#approvedFeeAmount').attr('required', true);
			   $('#revisedPermitNumber').attr('required', true);
		    } else if(!this.checked) { // if changed state is "CHECKED" 
			  $('.removemandatory').find("span").removeClass( "mandatory" );
			  $('#feeAmountRecieptNo').attr('required', false);
			  $('#approvedReceiptDate').attr('required', false);
			  $('#approvedFeeAmount').attr('required', false);
			  $('#revisedPermitNumber').attr('required', false);
			  $('#existingAppPlan').hide();
		    }
		});
	
	$('#isappForRegularization').on('change', function(){ 
		   if(this.checked) // if changed state is "CHECKED"
		    {
			   $('.constStages').find("span").addClass( "mandatory" );
			   $('#constStages').attr('required', true);
			   $('#constDiv').show();
			   $('#inprogress').hide();
			   $('#constStages').attr('required', true);
			   $('.workCommencementDate1').hide();
			   $('.workCompletionDate1').hide();
			   $('#constStages').change(function(){
					if($('#constStages option:selected').html()=="In Progress" ||  $(this).val()=="-1"){
						$('#inprogress').show();
						$('.workCommencementDate1').show();
						$('.workCompletionDate1').hide();
						$('.workCompletionDate').find("span").removeClass( "mandatory" );
						$('#workCompletionDate').removeAttr('required');
						$('.workCommencementDate').find("span").addClass( "mandatory" );
						$('#workCommencementDate').attr('required', true);
						$('.stateOfConstruction').find("span").addClass( "mandatory" );
						$('#stateOfConstruction').attr('required', true);
					} else if($('#constStages option:selected').html()=="Completed" ||  $(this).val()=="-1") {
						$('.workCommencementDate1').show();
						$('.workCompletionDate1').show();
						$('.workCommencementDate').find("span").addClass( "mandatory" );
						$('#workCommencementDate').attr('required', true);
						$('.workCompletionDate').find("span").addClass( "mandatory" );
						$('#workCompletionDate').attr('required', true);
						 $('#inprogress').hide();
					} else {
						$('#inprogress').hide();
						$('.workCommencementDate1').hide();
						$('.workCompletionDate1').hide();
					}
				});
		    } else if(!this.checked) // if changed state is "CHECKED"
		    {
			   	  $('#constStages').attr('required', false);
				  $('#stateOfConstruction').attr('required', false);
				  $('#constDiv').hide();
		    }
		});
	
	
	$('#constStages').change(function(){
		if($('#constStages option:selected').html()=="NotStarted" ||  $(this).val()=="-1"){
			 $('#stateOfConstruction').attr('required', true);
	 		$('#stateOfConstruction').append('<span class="mandatory">*</span>');
					
		}else if($('#constStages option:selected').html()=="Started"){	
			$('#stateOfConstruction').attr('required', false);
		}
	});

    $('#workCompletionDate').on('changeDate', function() {

        if (!$('#workCommencementDate').val()) {
            bootbox.alert("Please enter work starting date");
            $('#workCompletionDate').val('').datepicker("refresh");
        } else if ($('#workCommencementDate').val() && moment($('#workCompletionDate').val(),'DD/MM/YYYY').isSameOrBefore(moment($('#workCommencementDate').val(),'DD/MM/YYYY'))) {
        	bootbox.alert("Work completion date should be greater than the work starting date");
            $('#workCompletionDate').val('').datepicker("refresh");
        }
    });



    // on form load get occupancy details List
	$.ajax({
		url: "/bpa/application/getoccupancydetails",     
		type: "GET",
		dataType: "json",
		success: function (response) {
			occupancyResponse = arrayGroupByKey(response, 'id');
			mixedOccupancyResponse = response;
		}, 
		error: function (response) {
		}
	});
	
	$('#extentOfLand,#unitOfMeasurement').change(function(){
		var extentOfLand = $('#extentOfLand').val();
		var uom = $('#unitOfMeasurement').val();
		if(extentOfLand && uom && convertExtendOfLandToSqmts(extentOfLand, uom) > 1000000) {
			$('#extentOfLand').val('');
			$('#extentinsqmts').val('');
			bootbox.alert("Maximum allowed area of extend land upto 10 lakhs Sq.Mtrs only.");
		}
	});
	
	var previousIndex;
    var oneDayPermitPreviousVal;
    var landTypeDescIndex;
	$('#occupancyapplnlevel').focus(function () {
        // Store the current value on focus, before it changes
        previousIndex= this.selectedIndex;
        oneDayPermitPreviousVal = $('#isOneDayPermitApplication').is(':checked');
        landTypeDescIndex = $('#typeOfLand').val()
    }).change(function(e){
    	if($('#buildingAreaDetails tbody tr').length == 1 && ($('.occupancy').val() == '' || $('.floorDescription').val() == '')){
    		resetOccupancyDetails();
    	} else if($('#buildingAreaDetails tbody tr').length >= 1 && $('.occupancy').val() != '' && $('.floorDescription').val() != ''){
			
			var dropdown=e;
			
			bootbox
			.confirm({
				message : 'If you change occupancy type, filled floor wise details will be reset. Do you want continue ? ',
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
						$('#buildingAreaDetails').find('input').not('input[type=hidden]').val('');
						$('#buildingAreaDetails').find('select').val('');
						resetOccupancyDetails();
						setFloorCount();
						getOccupancyObject();
						$( ".plinthArea" ).trigger( "change" );
						$( ".carpetArea" ).trigger( "change" );
						$( ".floorArea" ).trigger( "change" );
						return true;
					} else {
						dropdown.target.selectedIndex = previousIndex;
						if(oneDayPermitPreviousVal) {
                            $('#isOneDayPermitApplication').prop('checked', true);
                            $('#typeOfLand').val(landTypeDescIndex);
						}
						showOnePermitOnPageLoad();
                        hideAndShowEdcrDetails();
						return true;
					}
				}
			});
		} else if($('#buildingAreaDetails tbody tr').length >1 && ($('.occupancy').val() == '' || $('.floorDescription').val() == '')){
    		resetOccupancyDetails();
    	}
	});
	
	// onchange of main occupancy reset floorwise occupancy details column value
	function resetOccupancyDetails() {
		$('#buildingAreaDetails tbody tr *[name$="occupancy"]').each(function(idx){
            var  selectBoxName = "buildingDetail[0].applicationFloorDetails["+idx+"].occupancy";
            var  selectBoxName1 = "buildingDetail[0].applicationFloorDetailsForUpdate["+idx+"].occupancy";
            clearExistingDropDownValues(selectBoxName);
            clearExistingDropDownValues(selectBoxName1);
			if($("#occupancyapplnlevel option:selected" ).text() == 'Mixed'){
				$('select[name="'+selectBoxName+'"]').append($("<option value=''>Select </option>"));
                $('select[name="'+selectBoxName1+'"]').append($("<option value=''>Select </option>"));
				$.each(mixedOccupancyResponse, function(index, occupancyObj) {
					if(occupancyObj.description != 'Mixed')
                        loadOccupancyDetails(selectBoxName, occupancyObj.id, occupancyObj.description);
                    	loadOccupancyDetails(selectBoxName1, occupancyObj.id, occupancyObj.description);
				});
			} else {
                loadOccupancyDetails(selectBoxName, $("#occupancyapplnlevel option:selected" ).val(), $("#occupancyapplnlevel option:selected" ).text());
                loadOccupancyDetails(selectBoxName1, $("#occupancyapplnlevel option:selected" ).val(), $("#occupancyapplnlevel option:selected" ).text());
			}
		});
	}

    function loadOccupancyDetails(selectBoxName,id,value){
        $('select[name="'+selectBoxName+'"]').append($('<option>').val(id).text(value));
    }

    function clearExistingDropDownValues(selectBoxName) {
        $('select[name="'+selectBoxName+'"]').empty();
	}

	$(document).on('blur', '.floorArea', function(e) {
		var isCitizenAcceptedForAdditionalFee = $('#isCitizenAcceptedForAdditionalFee').is(':checked');
		var seviceTypeName = $( "#serviceType option:selected" ).text();
		if(seviceTypeName && 'Alteration' != seviceTypeName && 'Addition or Extension' != seviceTypeName 
				&& 'Huts and Sheds' != seviceTypeName) {
			var rowObj = $(this).closest('tr');
			var occpancyObj = getOccupancyObject();
			if(occpancyObj && $("#occupancyapplnlevel option:selected" ).text() != 'Mixed') {
				var numOfTimesAreaPermissible = occpancyObj[0].numOfTimesAreaPermissible;
				var numOfTimesAreaPermWitAddnlFee = occpancyObj[0].numOfTimesAreaPermWitAddnlFee;
				var areaPermissibleWOAddnlFee = parseFloat(extentInSqmts) * parseFloat(numOfTimesAreaPermissible);
				var areaPermissibleWithAddnlFee = parseFloat(extentInSqmts) * parseFloat(numOfTimesAreaPermWitAddnlFee);
				totalFloorArea = parseFloat($("#sumOfFloorArea").val()).toFixed(2);
					if(areaPermissibleWithAddnlFee == 0) {
						if(parseFloat(totalFloorArea) > areaPermissibleWOAddnlFee.toFixed(2)){
							 bootbox.alert("For the occupancy type of " +occpancyObj[0].description+", maximum permissible area is "+areaPermissibleWOAddnlFee.toFixed(2)+" Sq.Mtrs, beyond of permissible area you are not allowed construct construction.");
							 $(rowObj).find('.floorArea').val('');
							 $( ".floorArea" ).trigger( "change" );
							 $( ".plinthArea" ).trigger( "change" );
				    		 $( ".carpetArea" ).trigger( "change" );
							return false;
						} else {
							return true;
						}
					} else if(parseFloat(totalFloorArea) > areaPermissibleWithAddnlFee.toFixed(2) && isCitizenAcceptedForAdditionalFee) {
						 bootbox.alert("For the occupancy type of " +occpancyObj[0].description+", maximum permissible area allowed with out addtional fee is "+areaPermissibleWOAddnlFee.toFixed(2)+" Sq.Mtrs and with addtional fee is "+areaPermissibleWithAddnlFee.toFixed(2)+" Sq.Mtrs, beyond of maximum permissible area of "+areaPermissibleWithAddnlFee.toFixed(2)+" Sq.Mtrs, you are not allowed construct construction.");
						 $(rowObj).find('.floorArea').val('');
						 $( ".floorArea" ).trigger( "change" );
						 $( ".plinthArea" ).trigger( "change" );
			    		 $( ".carpetArea" ).trigger( "change" );
						return false;
					} else if(parseFloat(totalFloorArea) > areaPermissibleWOAddnlFee.toFixed(2) && !isCitizenAcceptedForAdditionalFee) {
						bootbox
						.confirm({
							message : 'For the occupancy type of ' +occpancyObj[0].description+', maximum permissible area allowed with out addtional fee is '+areaPermissibleWOAddnlFee.toFixed(2)+' Sq.Mtrs, Do you want continue construction in additional area with addtional cost of Rs.5000 per Sq.Mtr.Are you ready pay additional amount ? , please select Yes / No ',
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
									$('#isCitizenAcceptedForAdditionalFee').prop('checked',true);
								} else {
									$(rowObj).find('.floorArea').val('');
									$('#isCitizenAcceptedForAdditionalFee').prop('checked',false);
									 $( ".floorArea" ).trigger( "change" );
									 $( ".plinthArea" ).trigger( "change" );
						    		 $( ".carpetArea" ).trigger( "change" );
									e.stopPropagation();
									e.preventDefault();
								}
							}
						});
					}
			}
		}
	});

	$('.for-calculation').change(function(){
		getOccupancyObject();
		$("#extentinsqmts").val(convertExtendOfLandToSqmts(extentOfLand, $("#unitOfMeasurement").val()));
		$("#extentinsqmtshdn").val(convertExtendOfLandToSqmts(extentOfLand, $("#unitOfMeasurement").val()));
	});
	
	// multi-select without pressing ctrl key
	$("select.tick-indicator").mousedown(function(e){
	    e.preventDefault();
	    
		var select = this;
	    var scroll = select.scrollTop;
	    
	    e.target.selected = !e.target.selected;
	    
	    $(this).trigger('change');
	    
	    setTimeout(function(){select.scrollTop = scroll;}, 0);
	    
	    $(select).focus();

	}).mousemove(function(e){e.preventDefault()});
	
	// trigger events on pageload
	$( "#isexistingApprovedPlan" ).trigger( "change" );
	$( "#isappForRegularization" ).trigger( "change" );
	$( "#constStages" ).trigger( "change" );
	$( ".serviceType" ).trigger( "change" );
	$( ".applicationAmenity" ).trigger( "change" );
	var serviceTypeName = $( "#serviceType option:selected" ).text();
    hideAndShowEdcrDetails();
	if('Addition or Extension' == serviceTypeName || 'Alteration' == serviceTypeName || 'New Construction' == serviceTypeName
			|| 'Amenities' == serviceTypeName) {

        	showOnePermitOnPageLoad();

		$('#occupancyapplnlevel').on('change', function() {
            $('.amenityHideShow').show();
            resetValuesForAmenitiesOfOneDayPermit();
			if($("#occupancyapplnlevel option:selected" ).text() == 'Residential'){
				$('#oneDayPermitSec').show(); 
				$('#isOneDayPermitApplication').prop('checked', false);
                $('#isOneDayPermitApplication').val(false);
				$('#oneDayPermitTypeOfLandSec').hide();
			} else {
                $('#typeOfLand').val('');
				$('#typeOfLand').removeAttr('required');
				$('#isOneDayPermitApplication').prop('checked', false);
				$('#oneDayPermitSec').hide();
                $('#isOneDayPermitApplication').val(false);
			}
            hideAndShowEdcrDetails();
		});
	}
	function hideAndShowEdcrDetails() {
        if( serviceTypeName && 'New Construction' == serviceTypeName && $("#occupancyapplnlevel option:selected" ) && $("#occupancyapplnlevel option:selected" ).text() == 'Residential') {
            $('#eDcrNumber').attr('required', true);
        	$('#eDcrNumber').show();
            $('.edcrApplnDetails').show();
        } else {
            $('#eDcrNumber').removeAttr('required');
            $('#eDcrNumber').hide();
            $('.edcrApplnDetails').hide();
        }
	}
	function showOnePermitOnPageLoad() {

        if('Amenities' == $( "#serviceType option:selected" ).text())
            $('.amenityHideShow').show();
        else
        	if($('#isOneDayPermitApplication').is(':checked'))
            	$('.amenityHideShow').hide();
        	else
                $('.amenityHideShow').show();

        if($("#occupancyapplnlevel option:selected" ).text() == 'Residential') {
            if($('#isOneDayPermitApplication').is(':checked')) {
                $('#oneDayPermitSec').show();
                $('#oneDayPermitTypeOfLandSec').show();
			} else {
                $('#oneDayPermitSec').show();
                $('#isOneDayPermitApplication').val(false);
                $('#oneDayPermitTypeOfLandSec').hide();
            }
        }
	}

    $('#isOneDayPermitApplication').click(function() {
        resetValuesForAmenitiesOfOneDayPermit();
	        if ($(this).is(':checked')) {
                if($('#isOneDayPermitApplication').val()) {
                    $('.amenityHideShow').hide();
                    if('Amenities'.localeCompare($( "#serviceType option:selected" ).text()) == 0)
                        $('.amenityHideShow').show();
                }
                $('#typeOfLand').prop('required', true);
                $('#oneDayPermitTypeOfLandSec').show();
                $('#isOneDayPermitApplication').val(true);
                if($('#zone').val()!='' && $('#electionBoundary').val()!='')
	        		validateSlotMappingForOneDayPermit($('#zone').val(), $('#electionBoundary').val());
	        } else {
                $('.amenityHideShow').show();
                $('#typeOfLand').val('');
                $('#oneDayPermitTypeOfLandSec').hide();
                $('#isOneDayPermitApplication').prop('checked', false);
                $('#typeOfLand').removeAttr('required');
                $('#isOneDayPermitApplication').val(false);
	        }
	  });
    
    $('#electionBoundary').on('change', function() {
		 if($('#zone').val()!='' && $('#isOneDayPermitApplication').is(':checked'))
			 validateSlotMappingForOneDayPermit($('#zone').val(), $('#electionBoundary').val());
	 });
});

function validateSlotMappingForOneDayPermit(zoneId, electionWardId){
	$.ajax({
		url: "/bpa/ajax/getOneDayPermitSlotByBoundary",   
		type: "GET",
		data: {
			zoneId : zoneId,
			electionWardId : electionWardId
		},
		dataType: "json",
		success: function (response) {
			if(response==false){
				$('#electionBoundary').val('');
				bootbox.alert("Slot Mapping Master Data not defined for selected Election Ward. Please Define the Mapping.");
				return false;
			} else
				return true;
		}, 
		error: function (response) {
		}
	});
}


function getOccupancyObject() {
	extentOfLand = $('#extentOfLand').val();
	extentInSqmts = $('#extentinsqmts').val();
	/*totalPlintArea = $('#totalPlintArea').val();*/
	var occpancyId = $('#occupancyapplnlevel').val();
		/*if(!occpancyId){
			bootbox.alert("Please select occpancy type");
			return false;
		}*/
	return occupancyResponse[occpancyId];
}

function arrayGroupByKey(arry, groupByKey){
	var resultData={};
	var result=arry.reduce(function(result, current) {
	   result[current[groupByKey]] = result[current[groupByKey]] || [];
	   result[current[groupByKey]].push(current);
	   return result;
	}, {});

	Object.keys(result).sort().forEach(function(key) {
	 resultData[key] = result[key];
	});

	return resultData;
}


function convertExtendOfLandToSqmts(extentOfLand,uom){
	var extentinsqmts;
	if(uom == 'ARE') {
		extentinsqmts = extentOfLand * 100;
	} else if(uom == 'HECTARE') {
		extentinsqmts = extentOfLand * 10000;
	} else {
		extentinsqmts = extentOfLand;
	}
	return extentinsqmts;
}

//Floor wise floor area validations
function validateFloorDetails(plinthArea) {
	var seviceTypeName = $( "#serviceType option:selected" ).text();
	if('Huts and Sheds' != seviceTypeName) {	
		var occpancyObj = getOccupancyObject();
		var rowObj = $('#buildingAreaDetails tbody tr');
		if(occpancyObj && $("#occupancyapplnlevel option:selected" ).text() != 'Mixed'){
			if(!extentOfLand){
				bootbox.alert("Please enter extend of land area value");
				$(rowObj).find('.floorArea').val('');
				$(rowObj).find('.plinthArea').val('');
				$( ".floorArea" ).trigger( "change" );
				$( ".plinthArea" ).trigger( "change" );
				return false;
			}
			/*if(!totalPlintArea){
				bootbox.alert("Please enter total builtup area value");
				return false;
			}*/
			var inputPlinthArea = parseFloat($(plinthArea).val()).toFixed(2);
			var permissibleAreaInPercentage = occpancyObj[0].permissibleAreaInPercentage;
			var permissibleAreaForFloor = parseFloat(extentInSqmts * permissibleAreaInPercentage / 100).toFixed(2);
			if(parseFloat(inputPlinthArea) > parseFloat(permissibleAreaForFloor)){
				$(plinthArea).val('');
				bootbox.alert("For type of " +occpancyObj[0].description+", each floor wise maximum permissable coverage area is " +permissibleAreaForFloor+" Sq.Mtrs, so beyond of maximum coverage area permission are not allowed.");
				 $( ".plinthArea" ).trigger( "change" );
		   		 $( ".floorArea" ).trigger( "change" );
		   		 $( ".carpetArea" ).trigger( "change" );
		   		$(rowObj).find('.plinthArea').focus();
				return false;
			}
			/*if(parseFloat($("#sumOfFloorArea").val()) > parseFloat(totalPlintArea)){
				$(plinthArea).val('');
				bootbox.alert("Sum of floor wise floor area "+parseFloat($("#sumOfFloorArea").val())+" Sq.Mtrs is exceeding the total builtup area "+parseFloat(totalPlintArea)+" Sq.Mtrs of you entered, please check and enter valid data.");
				return false;
			}*/
			$( ".floorArea" ).trigger( "change" );
			$( ".plinthArea" ).trigger( "change" );
			$( ".carpetArea" ).trigger( "change" );
		}
	}
}

function resetValuesForAmenitiesOfOneDayPermit() {
    $(".applicationAmenity").val('');
    $('#admissionfee').val(0);
    $('#serviceType,.applicationAmenity').trigger('change');
}

