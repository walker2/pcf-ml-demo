$(function () {
    $('#task_type').change(function() {
        var option = $(this).val();
        if (option === "RECOGNITION") {
            $('#option_choose').html('<div class="form-group"><label for="all_files">Files:</label><select id="all_files" name="files" class="form-control" multiple="multiple"></select></div>');
            getFiles();
        }
        if (option === "RELEARN") {
            $('#option_choose').html('<div class="form-group"><label for="train_buck">Train bucket:</label><select id="train_buck" name="train_buck" class="form-control"></select></div>'
            + '<div class="form-group"><label for="valid_buck">Validation bucket:</label><select id="valid_buck" name="valid_buck" class="form-control"></select></div>');
            getBuckets();
        }
    });
    $('.execution').each(function(i, obj) {
        var execution = $(this).text();
        if (execution === "RUNNING") {
            $(this).css("color", "#00FF7F");
        }
        if (execution === "STOPPED") {
            $(this).css("color", "#FA8072");
        }
        if (execution === "DONE") {
            $(this).css("color", "#EEE8AA");
        }
    });

    $('.collapse').each(function(i, obj) {
        var collapse = $(this).text();
        var json = JSON.parse(collapse);
        console.log(json);
        $(this).html(prettyPrintJson.toHtml(json));
    });
    function refresh() {
        setTimeout(function () {
            location.reload()
        }, 3000);
    }

    //run job once
    $(".btnRun").click(function () {
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/runJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "name": $("#name_" + jobId).text(),
                "type": $("#type_" + jobId).text()
            },
            success: function (res) {
                if (res.valid) {
                    refresh();
                    $.toast({
                        title: 'Run ' + $("#name_" + jobId).text() + ' job',
                        type: 'success',
                        delay: 3000
                    });
                } else {
                    alert(res.msg);
                }
            }
        });
    });

    //pause job
    $(".btnPause").click(function () {
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/pauseJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "name": $("#name_" + jobId).text(),
                "type": $("#type_" + jobId).text()
            },
            success: function (res) {
                if (res.valid) {
                    refresh();
                    $.toast({
                        title: 'Paused ' + $("#name_" + jobId).text() + ' job',
                        type: 'success',
                        delay: 3000
                    });
                } else {
                    alert(res.msg);
                }
            }
        });
    });


    //resume job
    $(".btnResume").click(function () {
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/resumeJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "name": $("#name_" + jobId).text(),
                "type": $("#type_" + jobId).text()
            },
            success: function (res) {
                if (res.valid) {
                    refresh();

                    $.toast({
                        title: 'Resumed ' + $("#name_" + jobId).text() + ' job',
                        type: 'success',
                        delay: 3000
                    });
                } else {
                    alert(res.msg);
                }
            }
        });
    });

    //delete job
    $(".btnDelete").click(function () {
        var jobId = $(this).parent().data("id");
        $.ajax({
            url: "/api/deleteJob?t=" + new Date().getTime(),
            type: "POST",
            data: {
                "name": $("#name_" + jobId).text(),
                "type": $("#type_" + jobId).text()
            },
            success: function (res) {
                if (res.valid) {

                    refresh();
                    $.toast({
                        title: 'Deleted ' + $("#name_" + jobId).text() + ' job',
                        type: 'success',
                        delay: 3000
                    });

                } else {
                    alert(res.msg);
                }
            }
        });
    });

    // update cron expression
    $(".btnEdit").click(
        function () {
            $("#myModalLabel").html("cron edit");
            var jobId = $(this).parent().data("id");
            $("#jobId").val(jobId);
            $("#edit_name").val($("#name_" + jobId).text());
            $("#edit_group").val($("#group_" + jobId).text());
            $("#edit_cron").val($("#cron_" + jobId).text());
            $("#edit_status").val($("#status_" + jobId).text());
            $("#edit_desc").val($("#desc_" + jobId).text());

            $('#edit_name').attr("readonly", "readonly");
            $('#edit_group').attr("readonly", "readonly");
            $('#edit_desc').attr("readonly", "readonly");

            $("#myModal").modal("show");
        });

    $("#save").click(
        function () {
            $.ajax({
                url: "/api/saveOrUpdate?t=" + new Date().getTime(),
                type: "POST",
                data: $('#mainForm').serialize(),
                success: function (res) {
                    if (res.valid) {
                        refresh();
                        $.toast({
                            title: 'Job saved',
                            type: 'success',
                            delay: 3000
                        });
                    } else {
                        alert(res.msg);
                    }
                }
            });
        });


    // create job
    $("#createBtn").click(
        function () {
            //getFiles();
            $("#myModalLabel").html("create job");

            $("#jobId").val("");
            $("#edit_name").val("");
            $("#edit_group").val("");
            $("#edit_cron").val("");
            $("#edit_status").val("NORMAL");
            $("#edit_desc").val("");

            $('#edit_name').removeAttr("readonly");
            $('#edit_group').removeAttr("readonly");
            $('#edit_desc').removeAttr("readonly");

            $("#myModal").modal("show");
        });

    function getFiles() {
        $.ajax({
            url: '/api/s3/list?bucket='
        }).done(function (data) {
            var html = '';
            var len = data.length;
            for (var i = 0; i < len; i++) {
                html += '<option value="' + data[i].key + '">' + data[i].key + '</option>';
            }
            $('#all_files').append(html).multiselect();
        });
    }

    function getBuckets() {
        $.ajax({
            url: '/api/s3/list_buckets'
        }).done(function (data) {
            var html = '';
            var len = data.length;
            for (var i = 0; i < len; i++) {
                html += '<option value="' + data[i].name + '">' + data[i].name + '</option>';
            }
            $('#train_buck').append(html);
            $('#valid_buck').append(html);
        });
    }


});