$(document)
    .ready(
        function() {
            var fileformatsinclude = [ 'dxf'];

            jQuery('.upload-file')
                .change(
                    function(e) {

                    });

            function validateUploadFile(val) {
                var isValid = true;
                /* validation for file upload */
                var myfile = val;
                var ext = myfile.split('.').pop();
                isValid = validate_file(fileformatsinclude, ext,
                    jQuery(val));

                var fileInput = jQuery(this);
                var maxSize = 12582912; // file size in
                // bytes(2MB)
                if (fileInput.get(0).files.length) {
                    var fileSize = this.files[0].size; // in
                    // bytes
                    var charlen = (this.value
                        .split('/').pop().split(
                            '\\').pop()).length;
                    if (charlen > 50) {
                        bootbox
                            .alert('Document name should not exceed 50 characters!');
                        fileInput.replaceWith(fileInput
                            .val('').clone(true));
                        return false;
                    } else if (fileSize > maxSize) {
                        bootbox
                            .alert('File size should not exceed 12 MB!');
                        fileInput.replaceWith(fileInput
                            .val('').clone(true));
                        return false;
                    }
                }
                return isValid;
            }
            function validate_file(fileformat, ext, obj) {
                if (jQuery.inArray(ext.toLowerCase(), fileformat) == -1) {
                    bootbox.alert("Please upload " + fileformat
                        + " format documents only");
                    obj.val('');
                    return false;
                }
                return true;
            }

            $('#fileTrigger').click(function(){
                $('.upload-msg').addClass('hide');
                $("#myfile").trigger('click');
                this.blur();
                this.focus();
            });

            $('#myfile').change(function(){
                console.log(this.files[0]);
                $('#fileTrigger').hide().parent().find('p, .fileActions').removeClass('hide');
                $('#fileName').html(this.files[0].name);
            });

            $('#fileDelete').click(function(){
                $('.fileSection').find('p').addClass('hide');
                $('#fileTrigger').show();
                $('.fileActions').addClass('hide');
            });

            $('#fileUpload').click(function(){
                $('.fileSection').find('p').addClass('hide');
                $('#fileTrigger').show();
                $('.fileActions').addClass('hide');
            });

            $('#buttonSubmit').click(function(e) {
                if ($('#edcrApplicationform').valid()) {
                    return true;
                } else {
                    return false;
                }
            });

            $(document).on('change',"#applicationAmenity",function (){
                var amenities = [];
                $.each($("#applicationAmenity option:selected"), function(idx){
                    amenities.push($(this).text());
                });
                $('#amenities').val(amenities);
            });

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
					url : "//edcr/edcrapplication/ajaxsearch/"+$('#mode').val(),      
					type: "POST",
					"data":  getFormData(jQuery('form'))
				},
				"fnRowCallback": function (row, data, index) {
						$(row).on('click', function() {
				console.log(data.id);
				window.open('//edcr/edcrapplication/'+ $('#mode').val() +'/'+data.id,'','width=800, height=600');
			});
				 },
				"sPaginationType" : "bootstrap",
				"bDestroy" : true,
				"sDom" : "<'row'<'col-xs-12 hidden col-right'f>r>t<'row'<'col-xs-3'i><'col-xs-3 col-right'l><'col-xs-3 col-right'<'export-data'T>><'col-xs-3 text-right'p>>",
				"aLengthMenu" : [ [ 10, 25, 50, -1 ], [ 10, 25, 50, "All" ] ],
				"oTableTools" : {
					"sSwfPath" : "../../../../../../egi/resources/global/swf/copy_csv_xls_pdf.swf",
					"aButtons" : [ "xls", "pdf", "print" ]
				},
				aaSorting: [],				
				columns : [ { 
"data" : "applicationNumber", "sClass" : "text-left"} ,{ 
"data" : "dcrNumber", "sClass" : "text-left"} ,{ 
"data" : "applicationDate", "sClass" : "text-left"}]				
			});
			}
