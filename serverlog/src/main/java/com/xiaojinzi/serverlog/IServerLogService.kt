package com.xiaojinzi.serverlog

import com.xiaojinzi.serverlog.bean.MessageBean

interface IServerLogService {

    companion object {

        const val SERVERLOG_IP = "192.168.3.80"
        // const val SERVERLOG_PORT = "18080"
        const val SERVERLOG_PORT = "23457"
        const val SERVERLOG_IP_PORT = "$SERVERLOG_IP:$SERVERLOG_PORT"
        const val SERVERLOG_WS_URL = "ws://$SERVERLOG_IP_PORT/serverLog/client"
        const val DEVELOP_SERVERLOG_HEADER = "develop_serverLog"

        /*分片数据的最大长度*/
        const val FRAGMENT_DATA_SIZE = 1000

        /**
         * 最大的队列大小
         */
        const val MAX_QUEUE_SIZE = 300

    }

    /**
     * 发送数据
     */
    fun send(messageBean: MessageBean<*>)

}