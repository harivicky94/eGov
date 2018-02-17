$(document)
		.ready(
				function() {
					$('.allservices').hide();
					$('#applType').attr('required', true);

					$('#applType')
							.change(
									function() {
										var applicationTypeName = $(
												"#applType option:selected")
												.text();
										if ('ONE_DAY_PERMIT'
												.localeCompare(applicationTypeName) == 0) {
											$('.allservices').show();
										} else if ('ALL_OTHER_SERVICES'
												.localeCompare(applicationTypeName) == 0) {
											$('.allservices').hide();
										}
									});

				});

$('#btnSearchForEdit').click(function() {
	if ($('#slotMappingform').valid()) {
		return true;
	} else {
		return false;
	}
});