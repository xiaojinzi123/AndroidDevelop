package com.xiaojinzi.serverlog.bean

import androidx.annotation.Keep
import java.util.*

/**
 * app 和 web 通信的格式,服务器通过其中"targetAppTag"和"targetWebTag"找出目标设备,发送过去
 * time   : 2018/05/20
 *
 * @author : xiaojinzi 30212
 */
@Keep
class MessageBean<T> {

    companion object {

        // 心跳
        const val TYPE_HEARTBEAT = "heartbeat"

        // 设置设备名称
        const val TYPE_SET_CLIENT_NAME = "setClientName"

        // 唯一标志, 服务端生成下发
        const val TYPE_CLIENT_TAG = "clientTag"

        // 表示设置数据提供的类型
        const val TYPE_SET_PROVIDER_TYPES = "setProviderTypes"

        /*表示数据分片传送*/
        const val TYPE_DATA_FRAGMENT = "dataFragment"

        // 埋点数据
        const val TYPE_USER_BEHAVIOR = "userBehavior"

        // 表示原始拦截器
        const val TYPE_NETWORK = "network"

        // 处理过的, 基本表示最后一个日志拦截器
        const val TYPE_NETWORK_PROCESSED = "networkProcessed"

        // Android 的 log
        const val TYPE_ANDROID_LOG = "androidLog"

        @JvmStatic
        fun setProviderTypesBuild(vararg types: String): MessageBean<*> {
            val result: MessageBean<List<String>> = MessageBean()
            result.type = TYPE_SET_PROVIDER_TYPES
            result.data = listOf(*types)
            return result
        }

        @JvmStatic
        fun deviceNameBuild(name: String): MessageBean<*> {
            val result: MessageBean<String> = MessageBean()
            result.type = TYPE_SET_CLIENT_NAME
            result.data = name
            return result
        }

        @JvmStatic
        fun heartBeatBuild(): MessageBean<*> {
            val result: MessageBean<*> = MessageBean<Any?>()
            result.type = TYPE_HEARTBEAT
            return result
        }

    }

    // 表示需要支持的操作
    var type: String? = null

    // 真正发送出去的数据,最终会转化成 json 数据传出去
    var data: T? = null

}