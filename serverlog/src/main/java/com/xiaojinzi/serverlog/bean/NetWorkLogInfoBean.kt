package com.xiaojinzi.serverlog.bean

import androidx.annotation.Keep
import java.util.*

@Keep
class NetWorkLogInfoBean {
    /**
     * 'req_method':'POST',
     * 'req_url':'www.baidu.com',
     * 'req_headers':['req_header1=1','req_header2=2','req_header3=3'],
     * 'req_body':'我是请求body,可以是一个json数据',
     * 'res_code':'200',
     * 'res_message':'ok',
     * 'res_headers':['res_header1=1','res_header2=2','res_header3=3'],
     * 'res_body':'我是响应body,可以是一个json数据'
     */
    var req_method: String? = null
    var req_url: String? = null
    var req_headers: List<String> = ArrayList()
    var req_body: String? = null
    var res_code = 0
    var res_message: String? = null
    var res_headers: List<String> = ArrayList()
    var res_body: String? = null
}