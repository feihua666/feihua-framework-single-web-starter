/**
 * Created by WZN on 2019/1/23.
 *
 */
var baseURL = "../../";
//工具集合Tools
window.T = {};

// 获取请求参数
// 使用示例
// location.href = http://localhost:8080/index.html?id=123
// T.p('id') --> 123;
var url = function (name) {
    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
    var r = window.location.search.substr(1).match(reg);
    if (r != null) return unescape(r[2]);
    return null;
};

T.p = url;

window.AjaxContentType = {
    'JSON': 'application/json;charset=utf-8',
    'URL': 'application/x-www-form-urlencoded',
    'FORM': 'multipart/form-data',
    'HTML': 'text/xml'
};
window.AjaxType = {'POST': 'POST', 'GET': 'GET', 'DELETE': 'DELETE', 'PUT': 'PUT'};

/**
 * httpUtil
 * ajax调用，get,post,put,delete，返回数据格式为json调用，返回数据格式为json
 *
 * @param url：请求地址
 * @param data：请求参数
 * @param data：1. application/x-www-form-urlencoded   2. application/json;charset=utf-8  3. multipart/form-data 4. text/xml
 * @param successCallback：请求成功（接口返回的状态为“成功”）时调用的函数
 */
window.httpUtil = {
    //get 异步
    get: function (option, successCallback, errorCallBack, completeCallBack) {
        option = option || {url: null, data: {}};
        errorCallBack = errorCallBack || function (e) {
        }
        completeCallBack = completeCallBack || function (c) {
        }
        // 返回ajax实例以便可以缓存它
        return $.ajax({
            url: baseURL + option.url || null,
            data: option.data || {},
            type: option.type || AjaxType.GET,
            dataType: option.dataType || 'json',
            cache: option.cache || false,
            success: function (result) {
                (successCallback)(result);
            },
            error: function (e) {
                (errorCallBack)(e);
            },
            complete: function (c) {
                (completeCallBack)(c);
            }
        });
    },
    //同步方法
    asyncGet: function (option, successCallback, errorCallBack, completeCallBack) {
        option = option || {url: null, data: {}};
        errorCallBack = errorCallBack || function (e) {
        }
        completeCallBack = completeCallBack || function (c) {
        }
        // 返回ajax实例以便可以缓存它
        return $.ajax({
            url: option.url || null,
            data: option.data || {},
            type: option.type || AjaxType.GET,
            dataType: option.dataType || 'json',
            cache: option.cache || false,
            async: false,
            success: function (result) {
                (successCallback)(result);
            },
            error: function (e) {
                (errorCallBack)(e);
            },
            complete: function (c) {
                (completeCallBack)(c);
            }
        });
    },
    //post  异步
    post: function (option, successCallback, errorCallBack, completeCallBack) {
        option = option || {};
        errorCallBack = errorCallBack || function (e) {
        }
        completeCallBack = completeCallBack || function (c) {
        }
        // 返回ajax实例以便可以缓存它
        return $.ajax({
            url: baseURL + option.url || '',
            data: option.data || {},
            type: option.type || AjaxType.POST,
            dataType: option.dataType || 'json',
            cache: option.cache || false,
            contentType: option.contentType || AjaxContentType.JSON,
            success: function (result) {
                (successCallback)(result);
            },
            error: function (e) {
                (errorCallBack)(e);
            },
            complete: function (c) {
                (completeCallBack)(c);
            }
        });
    },
    //put 异步
    put: function (option, successCallback, errorCallBack, completeCallBack) {
        option = option || {url: null, data: {}};
        errorCallBack = errorCallBack || function (e) {
        }
        completeCallBack = completeCallBack || function (c) {
        }
        // 返回ajax实例以便可以缓存它
        return $.ajax({
            url: baseURL + option.url || null,
            data: option.data || {},
            type: option.type || AjaxType.PUT,
            dataType: option.dataType || 'json',
            cache: option.cache || false,
            contentType: option.contentType || AjaxContentType.JSON,
            success: function (result) {
                (successCallback)(result);
            },
            error: function (e) {
                (errorCallBack)(e);
            },
            complete: function (c) {
                (completeCallBack)(c);
            }
        });
    },
    //delete  异步
    del: function (option, successCallback, errorCallBack, completeCallBack) {
        option = option || {url: null, data: {}};
        errorCallBack = errorCallBack || function (e) {
        }
        completeCallBack = completeCallBack || function (c) {
        }
        // 返回ajax实例以便可以缓存它
        return $.ajax({
            url: baseURL + option.url || null,
            data: option.data || {},
            type: option.type || AjaxType.DELETE,
            dataType: option.dataType || 'json',
            cache: option.cache || false,
            success: function (result) {
                (successCallback)(result);
            },
            error: function (e) {
                (errorCallBack)(e);
            },
            complete: function (c) {
                (completeCallBack)(c);
            }
        });
    }
}
//重写alert
window.alertMsg = function (that, result) {
    if(!result.msg) return;
    if (result.code == 0) {
        that.$message({
            type: 'success',
            message: result.msg
        });
    } else {
        that.$message.error(result.msg);
    }
}

/**
 * form表单序列化
 *
 * @return {}
 */
$.fn.serializeObject = function () {
    var o = {};
    var a = this.serializeArray();
    $.each(a, function () {
        if (this.value && $.trim(this.value) != '') {
            if (o[this.name]) {
                if (!o[this.name].push) {
                    o[this.name] = [o[this.name]];
                }
                o[this.name].push(this.value || '');
            } else {
                o[this.name] = $.trim(this.value || '');
            }
        }
    });

    return o;
};