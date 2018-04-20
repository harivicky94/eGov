$(document)
		.ready(
				function() {
					$('#btnApprove').click(function() {
						if ($('#stakeHolder').valid()) {
						return true;
						} else {
						return false;
						}
					});
					
					$('#btnReject').click(function() {
						if ($('#stakeHolder').valid()) {
						return true;
						} else {
						return false;
						}
					});
					$('#btnReject').click(function() {
					    if(!$('#comments').val()) {
					    	bootbox.alert('Please enter rejection comments.');
					    	return false;
					    } else{
					    	return true;
					    }
					});
					$('#btnApprove').click(function() {
						 $('#stakeHolderStatus').val( $('#btnApprove').val());
						 $('#stakeHolderStatus').val();
					});
					$('#btnReject').click(function() {
						 $('#stakeHolderStatus').val( $('#btnReject').val());
					});
				
				});