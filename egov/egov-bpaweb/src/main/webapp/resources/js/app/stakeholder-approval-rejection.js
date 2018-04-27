$(document)
    .ready(
        function () {

            $('#btnApprove').click(function (e) {
                bootbox
                    .confirm({
                        message: 'Please confirm, do you really want to approve the application?. Please make sure submitted details are correct before approval.',
                        buttons: {
                            'cancel': {
                                label: 'No',
                                className: 'btn-danger'
                            },
                            'confirm': {
                                label: 'Yes',
                                className: 'btn-primary'
                            }
                        },
                        callback: function (result) {
                            if (result) {
                                if ($('#stakeHolder').valid()) {
                                    $('#stakeHolderStatus').val($('#btnApprove').val());
                                    $('.loader-class').modal('show', {
                                        backdrop: 'static'
                                    });
                                    document.forms[0].submit();
                                } else {
                                    e.preventDefault();
                                }
                            } else {
                                e.stopPropagation();
                                e.preventDefault();
                            }
                        }
                    });
                return false;
            });

            $('#btnReject').click(function (e) {
                bootbox
                    .confirm({
                        message: 'Please confirm, do you really want to rejection this application ?',
                        buttons: {
                            'cancel': {
                                label: 'No',
                                className: 'btn-danger'
                            },
                            'confirm': {
                                label: 'Yes',
                                className: 'btn-primary'
                            }
                        },
                        callback: function (result) {
                            if (result) {
                                if (!$('#comments').val()) {
                                    bootbox.alert('Please enter rejection comments.');
                                    return true;
                                } else if ($('#stakeHolder').valid()) {
                                    $('#stakeHolderStatus').val($('#btnReject').val());
                                    $('.loader-class').modal('show', {
                                        backdrop: 'static'
                                    });
                                    document.forms[0].submit();
                                } else {
                                    e.preventDefault();
                                }
                            } else {
                                e.stopPropagation();
                                e.preventDefault();
                            }
                        }
                    });
                return false;
            });

        });