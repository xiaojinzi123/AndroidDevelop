package com.xiaojinzi.serverlog.bean

import androidx.annotation.Keep

/**
 * Android Log 日志的实体对象
 */
@Keep
class AndroidLogBean(val level: String, val tag:String?, val content: String?, val stackTraceMsg: String?)
