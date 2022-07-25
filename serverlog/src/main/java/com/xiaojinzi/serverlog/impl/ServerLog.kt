package com.xiaojinzi.serverlog.impl

import android.os.Build
import android.text.TextUtils
import android.util.Log
import androidx.annotation.NonNull
import com.google.gson.Gson
import com.xiaojinzi.serverlog.IServerLogService
import com.xiaojinzi.serverlog.ServerLogConstants
import com.xiaojinzi.serverlog.bean.MessageBean
import com.xiaojinzi.serverlog.bean.MessageBean.Companion.deviceNameBuild
import com.xiaojinzi.serverlog.bean.MessageBean.Companion.heartBeatBuild
import com.xiaojinzi.serverlog.bean.MessageBean.Companion.setProviderTypesBuild
import com.xiaojinzi.serverlog.bean.MessageFragment
import okhttp3.*
import java.util.*
import kotlin.math.min

object ServerLog : IServerLogService, Runnable {

    const val TAG = "ServerLogService"

    lateinit var mConfig: ServerLogConfig

    /*连接的状态*/
    internal enum class ConnectState {
        NORMAL, ING, SUCCESS, FAIL, CLOSED
    }

    var DEVICE_INFO = "<ProductName>: android_" +
            Build.VERSION.RELEASE + "_" + Build.BRAND + "_" + Build.MODEL

    // thread safe
    private val queue = Collections.synchronizedList(LinkedList<MessageBean<*>>())
    private val g = Gson()
    private var mWebSocket: WebSocket? = null

    // 保证各个线程使用的都是最新的. 线程安全
    // 这个状态一定保证绝对正确, 因为会使用这个状态来使用其他属性
    @Volatile
    private var connectState = ConnectState.NORMAL

    /*服务端分配的唯一 tag*/
    private var clientTag: String? = null

    fun init(@NonNull config: ServerLogConfig) {
        this.mConfig = config
        DEVICE_INFO = "${mConfig.productName}: android_" + Build.VERSION.RELEASE + "_" + Build.BRAND + "_" + Build.MODEL
        if (mConfig.debug) {
            // 开启轮训线程
            Thread(this).start()
            // 尝试连接
            startSocketConnect()
        }
    }

    fun isDebug(): Boolean {
        return mConfig.debug
    }

    private fun startSocketConnect() {
        if (!mConfig.debug) {
            return
        }
        if (mWebSocket != null) {
            return
        }
        if (connectState == ConnectState.ING) {
            return
        }
        connectState = ConnectState.ING
        val request = Request.Builder()
            .url(IServerLogService.SERVERLOG_WS_URL) // 弄一个特别的头
            .header(
                ServerLogConstants.DEVELOP_SERVERLOG_HEADER,
                ServerLogConstants.DEVELOP_SERVERLOG_HEADER
            )
            .build()
        OkHttpClient.Builder()
            .build()
            .newWebSocket(request, object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: Response) {
                    super.onOpen(webSocket, response)
                    Log.e(TAG, "onOpen")
                    mWebSocket = webSocket
                    connectState = ConnectState.SUCCESS
                    val setProviderMessageBean = setProviderTypesBuild(
                        MessageBean.TYPE_NETWORK, MessageBean.TYPE_NETWORK_PROCESSED,
                        MessageBean.TYPE_USER_BEHAVIOR, MessageBean.TYPE_ANDROID_LOG
                    )
                    doSend(setProviderMessageBean, true)
                    val clientNameMessageBean = deviceNameBuild(DEVICE_INFO)
                    doSend(clientNameMessageBean, true)
                }

                override
                fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    super.onFailure(webSocket, t, response)
                    Log.e(TAG, "onFailure")
                    t.printStackTrace()
                    connectState = ConnectState.FAIL
                    destroyWebSocket()
                }

                override
                fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosing(webSocket, code, reason)
                    Log.e(TAG, "onClosing")
                    connectState = ConnectState.CLOSED
                    destroyWebSocket()
                }

                override
                fun onMessage(webSocket: WebSocket, text: String) {
                    super.onMessage(webSocket, text)
                    Log.e(TAG, "onMessage")
                    val messageBean: MessageBean<String> =
                        g.fromJson<MessageBean<String>>(text, MessageBean::class.java)
                    if (MessageBean.TYPE_CLIENT_TAG == messageBean.type) {
                        clientTag = messageBean.data
                    }
                }

                override
                fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    super.onClosed(webSocket, code, reason)
                    Log.e(TAG, "onClosed")
                    connectState = ConnectState.CLOSED
                    destroyWebSocket()
                }
            })
    }

    private fun destroyWebSocket() {
        mWebSocket = null
        clientTag = null
    }

    override
    fun send(messageBean: MessageBean<*>) {
        doSend(messageBean, false)
    }

    private fun doSend(messageBean: MessageBean<*>, isAddToFirst: Boolean) {
        if (!mConfig.debug) {
            return
        }
        if (queue.size > IServerLogService.MAX_QUEUE_SIZE) {
            try {
                queue.removeAt(0)
            } catch (_: Exception) {
                // ignore
            }
        }
        if (isAddToFirst) {
            queue.add(0, messageBean)
        } else {
            queue.add(messageBean)
        }
        startSocketConnect()
    }

    override
    fun run() {
        while (true) {
            if (TextUtils.isEmpty(clientTag) || connectState != ConnectState.SUCCESS || queue.size == 0) {
                try {
                    Thread.sleep(1000)
                } catch (ignore: InterruptedException) {
                    // ignore
                }
                continue
            }
            try {
                val heartBeat = heartBeatBuild()
                val b = mWebSocket!!.send(g.toJson(heartBeat))
                if (!b) {
                    throw Exception("socket 发送失败")
                }
                val messageBean = queue.removeAt(0)
                Log.e(TAG, "发送了一个" + messageBean.type)
                // 拆分 json 进行发送
                val json = g.toJson(messageBean)
                // 分为几组
                var groupCount = json.length / IServerLogService.FRAGMENT_DATA_SIZE
                if (json.length % IServerLogService.FRAGMENT_DATA_SIZE != 0) {
                    groupCount++
                }
                if (groupCount == 1) {
                    mWebSocket!!.send(json)
                } else {
                    val uid = UUID.randomUUID().toString()
                    var startIndex = 0
                    var endIndex = 0
                    for (i in 0 until groupCount) {
                        val messageFragment = MessageFragment()
                        messageFragment.type = MessageBean.TYPE_DATA_FRAGMENT
                        messageFragment.uid = uid
                        messageFragment.index = i
                        messageFragment.totalCount = groupCount
                        startIndex = i * IServerLogService.FRAGMENT_DATA_SIZE
                        endIndex = startIndex + min(
                            IServerLogService.FRAGMENT_DATA_SIZE,
                            json.length - startIndex
                        )
                        messageFragment.data = json.substring(startIndex, endIndex)
                        mWebSocket!!.send(g.toJson(messageFragment))
                    }
                }
            } catch (ignore: Exception) {
                connectState = ConnectState.CLOSED
                startSocketConnect()
            }
        }
    }

}