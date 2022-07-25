package com.xiaojinzi.serverlog.bean

import androidx.annotation.Keep

@Keep
class MessageFragment {
    var type: String? = null
    var uid: String? = null
    var index = 0
    var data: String? = null
    var totalCount = 0
}