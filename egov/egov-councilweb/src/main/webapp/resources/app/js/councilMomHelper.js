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

jQuery('#btnsearch').click(function(e) {
		
		callAjaxSearch();
	});

function getFormData($form){
    var unindexed_array = $form.serializeArray();
    var indexed_array = {};

    $.map(unindexed_array, function(n, i){
        indexed_array[n['name']] = n['value'];
    });

    return indexed_array;
}


function callAjaxSearch() {
	drillDowntableContainer = jQuery("#resultTable");		
	jQuery('.report-section').removeClass('display-hide');
		reportdatatable = drillDowntableContainer
			.dataTable({
				ajax : {
					url : "/council/councilmom/searchcreated-mom/"+$('#mode').val(),      
					type: "POST",
					beforeSend : function() {
						$('.loader-class').modal('show', {
							backdrop : 'static'
						});
					},
					"data" : getFormData(jQuery('form')),
					complete : function() {
						$('.loader-class').modal('hide');
					}
				},
				"bDestroy" : true,
				"autoWidth": false,
				"sDom" : "<'row'<'col-xs-12 hidden col-right'f>r>t<'row'<'col-xs-3'i><'col-xs-3 col-right'l><'col-xs-3 col-right'<'export-data'T>><'col-xs-3 text-right'p>>",
				"aLengthMenu" : [ [ 10, 25, 50, -1 ], [ 10, 25, 50, "All" ] ],
				"oTableTools" : {
					"sSwfPath" : "../../../../../../egi/resources/global/swf/copy_csv_xls_pdf.swf",
					"aButtons" : [{"sExtends" : "xls"},
						           {"sExtends" :"pdf"},
						           {"sExtends" : "print"}]
				},
				aaSorting: [],				
				columns : [ { 
"data" : "meetingType", "sClass" : "text-left"} ,{ 
"data" : "meetingNumber", "sClass" : "text-left"} ,{ 
"data" : "meetingDate", "sClass" : "text-left"},{
"data" : "meetingLocation", "sClass" : "text-left"},{
"data" : "meetingTime", "sClass" : "text-left"}
,{ "data" : null, "sClass" : "text-center", "target":-1,
	
    sortable: false,
    "render": function ( data, type, full, meta ) {
    	 var mode = $('#mode').val();
       	 if(mode == 'edit')
           	 return '<button type="button" class="btn btn-xs btn-secondary edit"><i class="fa fa-pencil" aria-hidden="true"></i>&nbsp;&nbsp;Edit</button>';
            else
           	return '<button type="button" class="btn btn-xs btn-secondary view"><i class="fa fa-eye" aria-hidden="true"></i>&nbsp;&nbsp;View</button>';
        }
}
,{ "data": "id", "visible":false }
]				
			});
			}



$("#resultTable").on('click','tbody tr td  .view',function(event) {
	var id = reportdatatable.fnGetData($(this).parent().parent(),6);
	window.open('/council/councilmom/'+ $('#mode').val() + '/'+id,'','width=800, height=600,scrollbars=yes');
});

$("#resultTable").on('click','tbody tr td  .edit',function(event) {
	var id = reportdatatable.fnGetData($(this).parent().parent(),6);
	window.open('/council/councilmom/new' + '/'+id,'','width=800, height=600,scrollbars=yes');
	
});

$(document).ready(function() {
	
	jQuery( ".dateval" ).datepicker({ 
   	 format: 'dd/mm/yyyy',
   	 autoclose:true,
        onRender: function(date) {
     	    return date.valueOf() < now.valueOf() ? 'disabled' : '';
     	  }
	  }).on('changeDate', function(ev) {
		  var electiondate = jQuery('#electionDate').val();
		  var oathdate = jQuery('#oathDate').val();
		  if(electiondate && oathdate){
			  DateValidation1(electiondate , oathdate);
		  }
		 
	  }).data('datepicker');
	
	function DateValidation1(start , end){
	    if (start != "" && end != "") {
			var stsplit = start.split("/");
			var ensplit = end.split("/");
			
			start = stsplit[1] + "/" + stsplit[0] + "/" + stsplit[2];
			end = ensplit[1] + "/" + ensplit[0] + "/" + ensplit[2];
			
			return ValidRange(start, end);
		}else{
			return true;
		}
	}

	$('#agendaTable tbody').on('blur','tr .validnum',function() {
		var rowObj = $(this).closest('tr');
    	validateUniqueDetails(rowObj.index(), $(rowObj).find('.validnum').val(),'agendaTable');
    	validateSumotoDetails(rowObj.index(), $(rowObj).find('.validnum').val(),'agendaTable');
    	validateResolutionNumber($(this));
    });
    
    $('#sumotoTable tbody').on('blur','tr .validnum',function(e) {
    	var rowObj = $(this).closest('tr');
    	validateSumotoDetails(rowObj.index(), $(rowObj).find('.validnum').val(),'sumotoTable');
    	validateUniqueDetails(rowObj.index(), $(rowObj).find('.validnum').val(),'sumotoTable');
    	validateResolutionNumber($(this));
    });
    
});

function validateResolutionNumber(resolutionNumber){
	var resolutionNo = resolutionNumber.val();
	if(resolutionNo) {
		$.ajax({
			url: "/council/councilmom/checkUnique-resolutionNo",      
			type: "GET",
			data: {
				resolutionNumber : resolutionNo, 
			},
			dataType: "json",
			success: function (response) { 
				if(!response) {
						$(resolutionNumber).val('');
						bootbox.alert("Entered Resolution Number already exists. Please Enter Unique Number.");
				}
			}, 
			error: function (response) {
				$(resolutionNumber).val('');
				bootbox.alert("connection validation failed");
			}
		});
	}	
}


function validateUniqueDetails(idx, resno,tablename) {
	if (resno) {
		$('#agendaTable tbody tr')
				.each(
						function(index) {
							if (idx === index && tablename=='agendaTable')
								return;
							var resolutionNo = $(this).find(
									'*[name$="resolutionNumber"]').val();

							if (resno && resno === resolutionNo) {
								if(tablename=='agendaTable'){
								$('#agendaTable tbody tr:eq(' + idx + ')')
										.find('.validnum').val('');
								}
								else
									{
									$('#sumotoTable tbody tr:eq(' + idx + ')')
									.find('.validnum').val('');
									}
								bootbox.alert("Duplicate Resolution Number.Please enter different resolution number");
								return false;
							}
						});
	}
}


function validateSumotoDetails(idx, resno,tablename) {
	if (resno) {
		$('#sumotoTable tbody tr')
				.each(
						function(index) {
							if (idx === index && tablename=='sumotoTable')
								return;
							var resolutionNo = $(this).find(
									'*[name$="resolutionNumber"]').val();

							if (resno && resno === resolutionNo) {
								if(tablename=='sumotoTable'){
								$('#sumotoTable tbody tr:eq(' + idx + ')')
										.find('.validnum').val('');
								}
								else
									{
									$('#agendaTable tbody tr:eq(' + idx + ')')
									.find('.validnum').val('');
									}
								bootbox.alert("Duplicate Resolution Number.Please enter different resolution number");
								return false;
							}
						});
	}
}
