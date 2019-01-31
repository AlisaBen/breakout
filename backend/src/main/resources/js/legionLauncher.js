/**
 * Created by jackieliao on 2017/5/12.
 */
(function () {
    var id, xmlhttp;
    var protocol = 'https://'
    var locationHost = protocol + "prod.niuap.com"

//     var protocol = 'http://'
// var locationHost = protocol + "flowdev.neoap.com"
// var locationHost = protocol + "localhost:30335"

//get id from js src
    var src = document.getElementById("niuap-legion")?document.getElementById("niuap-legion").src:document.scripts;
    if(typeof(src)=="object" ){
        console.log(src.length);
        for(i=0;i<src.length;i++){
            if(src[i].src&&src[i].src.indexOf("legionLauncher.js?id=")!=-1){
                src=src[i].src
            }
        }
    }

    var args = src.substr(src.indexOf("?")+ 1).split("&")

    for (var i = 0; i < args.length; i++) {
        var j = args[i].indexOf("=");
        if (j > -1 && args[i].substr(0, j) == 'id') {
            id = args[i].substr(j + 1);
        }
    }

    sessionStorage.setItem("legion-appId", id)


// function loadXML(xmlMethod, url) {
//     xmlhttp = null;
//     if (window.XMLHttpRequest) {
//         // code for all new browsers
//         xmlhttp = new XMLHttpRequest()
//     } else if (window.ActiveXObject) {
//         // code for IE5 and IE6
//         xmlhttp = new ActiveXObject("Microsoft.XMLHTTP")
//     }
//     if (xmlhttp != null) {
//         xmlhttp.onreadystatechange = stateChange;
//         xmlhttp.open(xmlMethod, url, true);
//         xmlhttp.send(null)
//     } else {
//         console.error("Your browser does not support XMLHTTP,so legion can not use !!")
//     }
//
// }


// function stateChange() {
//     if (xmlhttp.readyState == 4) {
//         var response = JSON.parse(xmlhttp.responseText);
//         if (xmlhttp.status == 200) {
//             console.log("xmlhttp======", xmlhttp);
//             if (response.errCode == 0)
//             loadScript(locationHost + "/legion/static/js/es6-promise.auto.min.js", function () {
//                 console.log("es6-promise.auto.min.js");
//                 loadScript(locationHost + "/legion/static/js/fetch.min.js", function () {
//                     console.log("fetch.min.js");
//                 });
//                 loadScript(locationHost + "/legion/static/sjsout/collectfrontend-fastopt.js", function () {
//                     console.log("collectfrontend-fastopt.js is onload");
//                 });
//             });
//
//         } else {
//             console.warn("XMLHttpRequest error when connect legion ! ", response.msg)
//         }
//     }
// }


    function loadScript(url, callback) {
        var script = document.createElement("script");
        script.type = "text/javascript";

        //检测客户端类型
        if (script.readyState) {//IE
            script.onreadystatechange = function () {
                if (script.readyState === "loaded" || script.readyState === "complete") {
                    script.onreadystatechange = null;
                    callback();
                }
            }
        } else {//其他浏览器
            script.onload = function () {
                callback();
            }
        }

        script.src = url;
        document.getElementsByTagName("body")[0].appendChild(script);
    }


//run begin here
// loadXML("get", locationHost + "/legion/user/geJsId?jsId=" + id);

//判断此页面是否支持fetch和Promise
    if (window.fetch && window.Promise) {
        loadScript(locationHost + "/yilia/static/sjsout/collectfrontend-opt.js", function () {
            console.log("collectfrontend is onload");
        });
    } else if (!window.fetch && !window.Promise) {
        loadScript(locationHost + "/yilia/static/js/es6-promise.auto.min.js", function () {
            console.log("es6-promise.auto.min.js");
            loadScript(locationHost + "/yilia/static/js/fetch.min.js", function () {
                console.log("fetch.min.js");
            });
            loadScript(locationHost + "/yilia/static/sjsout/collectfrontend-opt.js", function () {
                console.log("collectfrontend is onload");
            })
        })
    } else if (!window.fetch && window.Promise) {
        loadScript(locationHost + "/yilia/static/js/fetch.min.js", function () {
            console.log("fetch.min.js");
        });
        loadScript(locationHost + "/yilia/static/sjsout/collectfrontend-opt.js", function () {
            console.log("collectfrontend is onload");
        });
    } else {
        loadScript(locationHost + "/yilia/static/js/es6-promise.auto.min.js", function () {
            console.log("es6-promise.auto.min.js");
        });
        loadScript(locationHost + "/yilia/static/sjsout/collectfrontend-opt.js", function () {
            console.log("collectfrontend is onload");
        });
    }
})()