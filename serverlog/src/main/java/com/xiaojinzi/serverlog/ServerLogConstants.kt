package com.xiaojinzi.serverlog

object ServerLogConstants {
    // ServerLog 的 websocket 的连接的一个特殊的头
    const val DEVELOP_SERVERLOG_HEADER = "develop_serverLog"

    // 当一个请求有这个标志的时候, 就会被忽略
    const val DEVELOP_NETWORK_IGNORE_HEADER = "develop_network_ignore"
}