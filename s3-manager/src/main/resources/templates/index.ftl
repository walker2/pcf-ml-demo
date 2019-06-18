<!DOCTYPE html>
<html>
<head>
    <title>S3 browser</title>
    <meta charset="utf-8"/>
    <meta http-equiv="X-UA-Compatible" content="IE=edge"/>
    <meta name="viewport" content="width=device-width, initial-scale=1"/>

    <link rel="stylesheet" type="text/css" href="http://axicon.axisj.com/axicon/axicon.css">
    <link rel="stylesheet" type="text/css" href="http://dev.axisj.com/ui/kakao/AXJ.min.css"/>
    <script type="text/javascript" src="http://code.jquery.com/jquery-1.11.3.min.js"></script>
    <script type="text/javascript" src="http://dev.axisj.com/dist/AXJ.min.js"></script>
    <style>
        html, body {
            margin: 0px;
            padding: 0px;
            height: 100%;
            overflow: hidden;
            font-size: 12px;
        }

        h1 {
            font-size: 24px;
            padding: 10px;
            margin: 0px;
        }

        .block {
            padding: 10px;
        }

        .AXUpload5QueueBox_list.allowDrop {
            background-image: url(images/drop_icon.png);
            background-repeat: no-repeat;
            background-position: center center;
        }

        .AXUpload5QueueBox_list .AXUploadItem .AXUploadTit {
            white-space: nowrap;
        }
    </style>
</head>
<body>

<table cellpadding="0" cellspacing="0" style="height:100%;width:100%;">
    <tr>
        <td valign="top">
            <div class="block">
                <h1>
                    <i class="axi axi-file"></i> Uploaded list
                    <div data-ax5select="select1" data-ax5select-config="{}"></div>
                </h1>
                <div id="uploaded-list"></div>
            
            </div>
        </td>
        <td valign="top" width="30%" style="border-left: 2px solid #d8d8d8;">
            <div class="block">
                <h1>
                    <i class="axi axi-ion-gear-b"></i> File information
                </h1>
                <div id="file-view">

                </div>
            </div>
        </td>
    </tr>
    <tr>
        <td valign="top" colspan="2" height="250" style="border-top: 2px solid #d8d8d8;background: #eee;">
            <div class="block">

                <div class="AXUpload5" id="AXUpload5"></div>

                <div style="height:10px;"></div>

                <div id="uploadQueueBox" class="AXUpload5QueueBox_list" style="height:188px;"></div>
            </div>

        </td>
    </tr>
</table>

<script type="text/javascript">
    
    $(document.body).ready(function(){
        
    });
    
    var fnObj = {
        pageStart: function () {
            this.grid.init();
            this.view.init();
            this.upload.init();
        },
        query: function () {
            this.grid.query();
        },
        pageResize: function () {
            var body_height = axf.clientHeight();
            this.grid.target_dom.css({height: body_height - 360});
            this.grid.target.resetHeight();
        },
        grid: {
            target_dom: "",
            target: new AXGrid(),
            init: function () {
                this.target_dom = $("#uploaded-list");
                this.target.setConfig({
                    targetID: "uploaded-list",
                    theme: "AXGrid",
                    colGroup: [
                        {key: "bucketName", label: "BucketName", width: "100", align: "center"},
                        {key: "key", label: "Key", width: "200"},
                        {
                            key: "size", label: "Size", width: "80", align: "right", formatter: function () {
                                return this.value.byte();
                            }
                        },
                        {
                            key: "lastModified",
                            label: "LastModified",
                            width: "100",
                            align: "center",
                            formatter: function () {
                                return this.value.date().print();
                            }
                        },
                        {key: "storageClass", label: "StorageClass", width: "100"},
                        {
                            key: "owner", label: "Owner", width: "80", formatter: function () {
                                return this.value.displayName;
                            }
                        },
                        {key: "etag", label: "Etag", width: "200"}
                    ],
                    body: {
                        onclick: function () {
                            fnObj.view.set(this.item);
                        }
                    },
                    page: {
                        paging: false
                    }
                });
                //this.query();
            },
            query: function () {
                $.ajax({
                    url: "/api/aws/s3/list?bucket=" + $('[data-ax5select="select1"]').ax5select("getValue")[0].value
                })
                    .done(function (data) {
                        fnObj.grid.target.setList(data);
                    });
            }
        },
        view: {
            target: "",
            init: function () {
                this.target = $("#file-view");
            },
            set: function (item) {
                this.item = item;
                var po = [];


                po.push('<table cellpadding="0" cellspacing="0" class="AXFormTable">');
                po.push('	<colgroup>');
                po.push('		<col width="100" />');
                po.push('		<col />');
                po.push('	</colgroup>');
                po.push('	<tbody>');
                po.push('		<tr>');
                po.push('			<th>BucketName</th>');
                po.push('			<td>' + item.bucketName);
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>Name</th>');
                po.push('			<td>' + item.key);
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>Size</th>');
                po.push('			<td>' + item.size.byte());
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>LastModified</th>');
                po.push('			<td>' + item.lastModified.date());
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>StorageClass</th>');
                po.push('			<td>' + item.storageClass);
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>Owner</th>');
                po.push('			<td>' + Object.toJSON(item.owner));
                po.push('			</td>');
                po.push('		<tr>');
                po.push('		<tr>');
                po.push('			<th>Etag</th>');
                po.push('			<td>' + item.etag);
                po.push('			</td>');
                po.push('		<tr>');
                po.push('	</tbody>');
                po.push('</table>');

                po.push('<div style="padding: 10px;">');
                po.push('<button class="AXButton Classic" onclick="fnObj.view.download();"><i class="axi axi-cloud-download"></i>&nbsp;&nbsp;Download</button>');
                po.push('</div>');
                po.push('<div style="padding: 10px;">');
                po.push('<button class="AXButton Classic" onclick="fnObj.view.delete();"><i class="axi axi-cloud-download2"></i>&nbsp;&nbsp;Delete</button>');
                po.push('</div>');

                this.target.html(po.join(''));
            },
            download: function () {
                location.href = ('/api/aws/s3/download?key=' + this.item.key);
            },
            delete: function () {
                $.ajax({
                    url: "/api/aws/s3/delete?key=" + this.item.key,
                    type: "POST"
                })
                    .done(function (data) {
                        toast.push(data);
                        fnObj.grid.query();
                    });
            }
        },
        upload: {
            target: new AXUpload5(),
            init: function () {
                var target = this.target;
                this.target.setConfig({
                    targetID: "AXUpload5",
                    targetButtonClass: "Classic",
                    uploadFileName: "file",
                    file_types: "*/*",
                    dropBoxID: "uploadQueueBox",
                    queueBoxID: "uploadQueueBox",

                    // --------- e
                    onClickUploadedItem: function () {
                        window.open(this.uploadedPath.dec() + this.saveName.dec(), "_blank", "width=500,height=500");
                    },

                    uploadMaxFileSize: (1000 * 1024 * 1024),
                    uploadMaxFileCount: 10,
                    uploadUrl: "/api/aws/s3/upload",
                    uploadPars: {},
                    deleteUrl: "/api/aws/s3/delete",
                    deletePars: {},

                    buttonTxt: '<i class="axi axi-cloud-upload"></i>&nbsp;&nbsp; Upload',

                    fileKeys: {
                        name: "name",
                        type: "type",
                        saveName: "saveName",
                        fileSize: "fileSize",
                        uploadedPath: "uploadedPath",
                        thumbPath: "thumbUrl"
                    },

                    onbeforeFileSelect: function () {
                        trace(this);
                        return true;
                    },

                    onUpload: function () {
                        //trace(this);
                        //trace(myUpload.uploadedList);
                        //trace("onUpload");
                    },
                    onComplete: function () {
                        //trace(this);
                        toast.push(target.uploadedList.length + " file uploaded.");

                        target.setUploadedList([]);
                        $("#uploadQueueBox").empty();

                        fnObj.grid.query();
                        // $("#uploadCancelBtn").get(0).disabled = true;
                    },
                    onStart: function () {
                        //trace(this);
                        //trace("onStart");
                        // $("#uploadCancelBtn").get(0).disabled = false;
                    },
                    onDelete: function () {
                        trace(this);
                        //trace("onDelete");
                    },
                    onError: function (errorType, extData) {
                        if (errorType === "html5Support") {
                            //dialog.push('The File APIs are not fully supported in this browser.');
                        } else if (errorType === "fileSize") {
                            //trace(extData);
                            alert("You can not upload files that exceed the file size. \n(" + extData.name + " : " + extData.size.byte() + ")");
                        } else if (errorType === "fileCount") {
                            alert("Items that exceed the number of uploads will not be uploaded");
                        }
                    }
                });
                // changeConfig


            }
        }
    };

    $(document.body).ready(function () {
        $.ajax({
            url: '/api/aws/s3/list_buckets'
        }).done(function (data) {
            var html = '';
            var len = data.length;
            var options = [];
            options.push({value: '', text: ''});
            for (var i = 0; i < len; i++) {
                options.push({value: data[i].name, text: data[i].name});
            }
            $('[data-ax5select]').ax5select({
                    options: options,
                    onStateChanged: function () {
                        //console.log(value);
                        //console.log(this);
                        //console.log($('[data-ax5select="select1"]').ax5select("getValue"));
                    }
            });
            $('[data-ax5select]').change(function() {
                fnObj.query();
            });
            console.log($('[data-ax5select="select1"]').ax5select("getValue")[0].value)
            fnObj.pageStart();
            fnObj.pageResize();
            fnObj.query();
        });
    });
    $(window).resize(function () {
        fnObj.pageResize();
    });
</script>
<link rel="stylesheet" type="text/css" href="https://cdn.rawgit.com/ax5ui/ax5ui-select/master/dist/ax5select.css" />
<script src="https://cdnjs.cloudflare.com/ajax/libs/jquery/1.12.4/jquery.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/ax5ui/ax5core/master/dist/ax5core.min.js"></script>
<script type="text/javascript" src="https://cdn.rawgit.com/ax5ui/ax5ui-select/master/dist/ax5select.min.js"></script>
</body>
</html>