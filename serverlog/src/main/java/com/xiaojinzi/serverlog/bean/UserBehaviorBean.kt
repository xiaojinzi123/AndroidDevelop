package com.xiaojinzi.serverlog.bean

import androidx.annotation.Keep

@Keep
class UserBehaviorBean(var eventId: String, var param: Map<String, String>)