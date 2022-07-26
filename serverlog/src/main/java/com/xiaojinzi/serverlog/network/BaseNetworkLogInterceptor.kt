package com.xiaojinzi.serverlog.network

import android.text.TextUtils
import android.util.Log
import androidx.annotation.CallSuper
import androidx.annotation.Keep
import com.xiaojinzi.serverlog.ServerLogConstants
import com.xiaojinzi.serverlog.bean.NetWorkLogInfoBean
import com.xiaojinzi.serverlog.impl.ServerLog
import com.xiaojinzi.serverlog.impl.ServerLog.isDebug
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import java.io.IOException
import java.nio.charset.Charset

/**
 * 这个类不能删除, 因为字节码会创建这个类的
 */
@Keep
abstract class BaseNetworkLogInterceptor : Interceptor {
    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response {
        Log.e(ServerLog.TAG, "拦截到一个请求 start")
        // 根据这个值控制是否输出日志
        if (!isDebug()) {
            return chain.proceed(chain.request())
        }
        val ignoreHeader: String? = chain.request().header(
            name = ServerLogConstants.DEVELOP_NETWORK_IGNORE_HEADER
        )
        if (!TextUtils.isEmpty(ignoreHeader)) {
            return chain.proceed(chain.request())
        }
        val netWorkLogInfo = NetWorkLogInfoBean()
        val request: Request = chain.request()
        readRequest(request, netWorkLogInfo)
        val realResponse: Response = chain.proceed(request)
        val resultResponse = readResponse(realResponse, netWorkLogInfo)
        try {
            doSend(netWorkLogInfo)
        } catch (ignore: Exception) {
            // ignore
        }
        Log.e(ServerLog.TAG, "拦截到一个请求 end")
        return resultResponse
    }

    private fun readRequest(request: Request, info: NetWorkLogInfoBean) {
        val url = request.url.toString()
        info.req_method = request.method
        info.req_url = url
        val headers = request.headers
        info.req_headers.clear()
        val names = headers.names()
        for (name in names) {
            val headerValue = headers[name]
            info.req_headers.add("$name: $headerValue")
        }
        val headerContentMediaType = request.body?.contentType()
        val isReadBody = if (headerContentMediaType == null) {
            false
        } else {
            ServerLog
                .mConfig
                .networkLogAllowReadRequestBodyContentTypeSet
                .any {
                    "${headerContentMediaType.type}/${headerContentMediaType.subtype}".equals(
                        other = it,
                        ignoreCase = true
                    )
                }
        }
        if (!isReadBody) {
            info.req_body =
                "the request body is not allow to read, content-type is '${headerContentMediaType?.type}/${headerContentMediaType?.subtype}'"
        } else {
            try {
                val requestBody = request.body
                if (requestBody != null) {
                    val bufferedSink = Buffer()
                    requestBody.writeTo(bufferedSink)
                    info.req_body = bufferedSink.readString(Charset.forName(CHARSET))
                }
            } catch (ignore: Exception) {
                // ignore
            }
        }
    }

    @CallSuper
    protected open fun doSend(netWorkLogInfo: NetWorkLogInfoBean) {
        Log.e(ServerLog.TAG, "BaseNetworkLogInterceptor.doSend")
    }

    companion object {
        private const val CHARSET = "UTF-8"

        @Throws(IOException::class)
        fun readResponse(response: Response, info: NetWorkLogInfoBean): Response {
            info.res_code = response.code
            info.res_message = response.message
            val headers = response.headers
            info.res_headers.clear()
            val names = headers.names()
            for (name in names) {
                val headerValue = headers[name]
                info.res_headers.add("$name: $headerValue")
            }
            val headerContentMediaType = response.body?.contentType()
            val isReadResponse = if (headerContentMediaType == null) {
                false
            } else {
                ServerLog
                    .mConfig
                    .networkLogAllowReadResponseBodyContentTypeSet
                    .any {
                        "${headerContentMediaType.type}/${headerContentMediaType.subtype}".equals(
                            other = it,
                            ignoreCase = true
                        )
                    }
            }
            if (!isReadResponse) {
                info.res_body = "非可读格式 Body, 不做展示"
                return response
            }

            info.res_body = response.body?.string()
            val resultResponseBody: ResponseBody? =
                info.res_body?.toResponseBody(contentType = response.body?.contentType())
            return response.newBuilder().body(body = resultResponseBody).build()

        }
    }
}