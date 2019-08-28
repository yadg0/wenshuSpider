$(function () {
    //设置分享控件悬浮位置
    Content.Content.SetFixedPosition("5px");
    //Content.Content.GetClickCount();

    //开启统计功能
});

function openApp() {
    $("#divtdcApp").show();
}

var Content = {
    Comment: {
        LeaveWords_Save: function () {
            var contents = $("#commentArea").val();
            if (contents != "") {
                $.ajax({
                    url: "/Content/ContentLeaveWords_Save",
                    type: "POST",
                    async: true,
                    data: { "caseName": $("#hidCaseName").val(), "caseNumber": $("#hidCaseNumber").val(), "docId": $("#hidDocID").val(), "courtID": $("#HidCourtID").val(), "leaveWords": $("#commentArea").val() },
                    success: function (res) {
                        if (res == "1") {
                            try {
                                toGridsum.GetFullPage($("#hidDocID").val(), "留言", ""); //国双;
                            } catch (e) { }
                            Lawyee.Tools.ShowMessage("留言成功！"); $("#commentArea").val(""); $('.divcontent_comment').hide(); $('#comment').find('img:first').attr('src', '/Assets/img/content/content_comment.jpg');
                        }
                        else if (res == "0") { Lawyee.Tools.ShowMessage("留言失败！"); }
                        else { Lawyee.Tools.ShowMessage("留言失败：" + res); }
                    }
                });
            } else {
                alert("留言内容不能为空");
            }
        },
        LeaveWords_Reset: function () {
            $("#commentArea").val("");
        }
    },
    Content: {
        InitPlugins: function () {
            //概要信息
            var dirData = {
                Elements: [],
                LegalBase: []
            };

            //二维码
            {
                var docId = $("#hidDocID").val();
                var court = $("#hidCourt").val();
                var caseNumber = $("#hidCaseNumber").val();
                var caseType = $("#hidCaseType").val();

                //初始化分享控件
                if (docId != "") {
                    //初始化关联文书和四网融合关联文书
                    $.ajax({
                        url: "/Content/GetDocRelationAndFuse",
                        type: "POST",
                        async: true,
                        data: { "docId": docId, "court": court, "caseNumber": caseNumber, "caseType": caseType },
                        success: function (data) {
                            if (data != "") {
                            //四网融合关联数据
                                dirData = {
                                    Elements: ["TsgkUrl", "ZxgkUrl"],
                                    tsgkUrl: eval("(" + data + ")").tsgkUrl,
                                    zxgkUrl: eval("(" + data + ")").zxgkUrl
                                };
                                if ($("#divTool_Relation").length > 0) {
                                    $("#divTool_Relation").ContentRelation({ data: dirData });
                                }
                                //关联文书关联数据
                                var relateFile = [];
                                relateFile = {
                                    RelateFile: eval("(" + data + ")").RelateFile.RelateFile
                                };
                                if (relateFile.RelateFile != undefined && relateFile.RelateFile.length > 0) {
                                    if ($("#divRelateFiles").length > 0) {
                                        $("#divRelateFiles").RelateFiles({ data: relateFile });
                                        $(".relatefiles_container").show();
                                        $(".div_tool_dir").css("top", "30%");
                                    }
                                } else {
                                    //隐藏关联文书div
                                    $(".div_tool_dir").css("top", "30%");
                                }
                            }
                        }
                    });
//                    //初始化摘要控件
//                    $.ajax({
//                        url: "/Content/GetSummary",
//                        type: "POST",
//                        async: true,
//                        data: { "docId": docId },
//                        success: function (data) {
//                            dirData = {
//                                Elements: ["RelateInfo", "LegalBase"],
//                                RelateInfo: eval("(" + data + ")").RelateInfo,
//                                LegalBase: eval("(" + data + ")").LegalBase
//                            };
//                            if ($("#divTool_Summary").length > 0) {
//                                $("#divTool_Summary").ContentSummary({ data: dirData });
//                            }
//                        }
//                    });
                   
                }
            }
        },
        GetClickCount: function () {
            //获取文书点击量
            var docId = $("#hidDocID").val();
            if (docId != "HtmlNotExist" && docId != "") {
                //全文正常访问时增加点击量
                $.post("/Content/GetClickCount", { "docId": docId }, function (data) {
                    var clickCount = $("#con_llcs");
                    if (data != undefined && data != "") {
                        var jsonData = $.parseJSON(data)
                        if (clickCount != undefined) {
                            clickCount.html("浏览：" + jsonData.TotalCount + "次");
                        }
                    }
                });
            }

        },
        //设置分享控件悬浮位置
        SetFixedPosition: function (shareB) {
            window.onscroll = function () {
                var heightToBottom = $(window).height() - $(".content_main").height() - ($(".content_main").offset().top - $(document).scrollTop());
                if (heightToBottom > 0) {
                    $("#divTool_Share").css("bottom", (heightToBottom + 5) + "px");
                } else {
                    $("#divTool_Share").css("bottom", shareB);
                }
                $("#divTool_Dir").css("top", "30%");
            }
        },
        KeyWordMarkRed: function () {
            var url = window.location.href;
            if (url.indexOf("&") > 0) {
                var keyWord = url.split("&")[1].split("=")[1];
                if (keyWord != undefined && keyWord != "") {
                    keyWord = decodeURI(keyWord);
                    //案件标题关键词标红
                    var $content = $("#contentTitle");
                    var contentHtml = $content.html();
                    var keyWordsArr = keyWord.split("|");
                    for (var i = 0; i < keyWordsArr.length; i++) {
                        eval('contentHtml = contentHtml.replace(/' + keyWordsArr[i] + '/g, "<span style=\'color:red\'>' + keyWordsArr[i] + '</span>")');
                    }
                    //案件全文关键词标红
                    $content.html(contentHtml);
                    $content = $("#DivContent");
                    contentHtml = $content.html();
                    for (var i = 0; i < keyWordsArr.length; i++) {
                        eval('contentHtml = contentHtml.replace(/' + keyWordsArr[i] + '/g, "<span style=\'color:red\'>' + keyWordsArr[i] + '</span>")');
                    }
                    $content.html(contentHtml);
                }
            }
        },
        CheckValidateCode: function () {
            if ($("#txtValidateCode").val() == "") { Lawyee.Tools.ShowMessage("请输入验证码！"); return false; }
            $.post("/Content/CheckValidateCode", { "ValidateCode": $("#txtValidateCode").val() }, function (data) {
                if (data == "1") {
                    window.parent.location.href = window.parent.location.href;
                } else if (data == "2") {
                    alert("验证码错误!");
                } else {
                    alert(data);
                }
            });
        }
    }
}
var lawyeeToolbar = {
    containerId: "Content",
    Save: {
        Html2Word: function () {
            debugger;
            var $content = $("#" + lawyeeToolbar.containerId);
            var contentTitle = $.trim($("#contentTitle").text());
            var content = "";
            var docid = $("#hidDocID").val();
            content += $content.html();
            content = content.replace(/\<img.*?\>/ig, '');
            content = content.replace(/\<input.*?\>/ig, '');
            var body = document.body;
            var formid = 'DownloadForm';
            var url = '/Content/GetHtml2Word';
            var node = document.getElementById(formid);
            if (node != null) {
                node.parentNode.removeChild(node);
            }
            var theForm = document.createElement('form');
            theForm.id = formid;
            theForm.action = url;
            theForm.method = 'post';

            node = document.createElement('input');
            node.type = 'hidden';
            node.id = 'htmlStr';
            node.name = 'htmlStr';
            node.value = encodeURIComponent(content);
            theForm.appendChild(node);
            node = document.createElement('input');
            node.type = 'hidden';
            node.id = 'htmlName';
            node.name = 'htmlName';
            node.value = encodeURIComponent(contentTitle);
            theForm.appendChild(node);
            node = document.createElement('input');
            node.type = 'hidden';
            node.id = 'DocID';
            node.name = 'DocID';
            node.value = docid;
            theForm.appendChild(node);
            body.appendChild(theForm);
            theForm.submit();
        },
        DownLoadCase: function () {
            debugger;
            var caseInfo = $("#hidDocID").val() + "|" + $("#hidCaseName").val() + "|";
            var thebody = document.body;
            var formid = 'DownloadForm';
            var url = '/CreateContentJS/CreateListDocZip.aspx?action=1';
            var theform = document.createElement('form');
            theform.id = formid;
            theform.action = url;
            theform.method = 'POST';

            //获取检索条件，作为压缩包名称
            var conditions = "";
            var theInput = document.createElement('input');
            theInput.type = 'hidden';
            theInput.id = 'conditions';
            theInput.name = 'conditions';
            theInput.value = encodeURI(conditions);
            theform.appendChild(theInput);

            var theInput = document.createElement('input');
            theInput.type = 'hidden';
            theInput.id = 'docIds';
            theInput.name = 'docIds';
            theInput.value = caseInfo;
            theform.appendChild(theInput);

            //验证码功能暂未启用
            var theInput = document.createElement('input');
            theInput.type = 'hidden';
            theInput.id = 'keyCode';
            theInput.name = 'keyCode';
            theInput.value = '';
            theform.appendChild(theInput);

            thebody.appendChild(theform);
            theform.submit();
        }
    },
    Print: {
        PrintHtml: function () {
            $("#" + lawyeeToolbar.containerId).printArea();
        }
    },
     RefYzm: function () {
        var guid = lawyeeToolbar.createGuid() + lawyeeToolbar.createGuid() + "-" + lawyeeToolbar.createGuid() + "-" + lawyeeToolbar.createGuid() + lawyeeToolbar.createGuid() + "-" + lawyeeToolbar.createGuid() + lawyeeToolbar.createGuid() + lawyeeToolbar.createGuid(); //CreateGuid();
        $("#txthidGuid").val(guid);
        $("#diverror").html("");
        $("#txtValidateCode").val("");
        $("#divYzmImg").html("<img alt='点击刷新验证码！' name='validateCode' id='ImgYzm' onclick='lawyeeToolbar.RefYzm()'  title='点击切换验证码' src='/ValiCode/CreateCode/?guid=" + guid + "' style='cursor: pointer;'  />");
    },
    Init: {
        Process: function () {
            //save
            $("#img_download").click(function () { lawyeeToolbar.Save.Html2Word(); })
            //print
            $("#img_print").click(function () { lawyeeToolbar.Print.PrintHtml() });
        }
    }
}