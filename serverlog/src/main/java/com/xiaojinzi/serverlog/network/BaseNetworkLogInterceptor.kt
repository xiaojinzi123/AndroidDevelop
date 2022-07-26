package com.xiaojinzi.serverlog.network;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.CallSuper;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.xiaojinzi.serverlog.ServerLogConstants;
import com.xiaojinzi.serverlog.bean.NetWorkLogInfoBean;
import com.xiaojinzi.serverlog.impl.ServerLog;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Set;

import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;

/**
 * 这个类不能删除, 因为字节码会创建这个类的
 */
@Keep
public abstract class BaseNetworkLogInterceptor implements Interceptor {

    private static final String CHARSET = "UTF-8";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Log.e(ServerLog.TAG, "拦截到一个请求 start");
        // 根据这个值控制是否输出日志
        if (!ServerLog.INSTANCE.isDebug()) {
            return chain.proceed(chain.request());
        }
        String ignoreHeader = chain.request().header(ServerLogConstants.DEVELOP_NETWORK_IGNORE_HEADER);
        if (!TextUtils.isEmpty(ignoreHeader)) {
            return chain.proceed(chain.request());
        }
        NetWorkLogInfoBean netWorkLogInfo = new NetWorkLogInfoBean();
        Request request = chain.request();
        readRequest(request, netWorkLogInfo);
        Response realResponse = chain.proceed(request);
        Response resultResponse = readResponse(realResponse, netWorkLogInfo);
        try {
            doSend(netWorkLogInfo);
        } catch (Exception ignore) {
            // ignore
        }
        Log.e(ServerLog.TAG, "拦截到一个请求 end");
        return resultResponse;
    }

    private void readRequest(@NonNull Request request, @NonNull NetWorkLogInfoBean info) {
        String url = request.url().toString();
        info.setReq_method(request.method());
        info.setReq_url(url);
        Headers headers = request.headers();
        info.getReq_headers().clear();
        if (headers != null) {
            Set<String> names = headers.names();
            if (names != null) {
                for (String name : names) {
                    String headerValue = headers.get(name);
                    info.getReq_headers().add(name + ": " + headerValue);
                }
            }
        }

        boolean isReadBody = false;
        String headerContextType = request.header("Content-Type");
        if(request.body() instanceof FormBody) {
            isReadBody = true;
        } else  {
            if (!TextUtils.isEmpty(headerContextType)) {
                assert headerContextType != null;
                for (String allow_content_type : ServerLog.mConfig.getNetworkLogAllowReadRequestBodyContentTypeSet()) {
                    if (headerContextType.toLowerCase().contains(allow_content_type)) {
                        isReadBody = true;
                        break;
                    }
                }
            }
        }

        if (!isReadBody) {
            info.setReq_body("the request body is not allow to read, content-type is '" + headerContextType + "'");
        } else {
            try {
                RequestBody requestBody = request.body();
                if (requestBody != null) {
                    Buffer bufferedSink = new Buffer();
                    requestBody.writeTo(bufferedSink);
                    info.setReq_body(bufferedSink.readString(Charset.forName(CHARSET)));
                }
            } catch (Exception ignore) {
                // ignore
            }
        }
    }

    public static Response readResponse(@NonNull Response response, @NonNull NetWorkLogInfoBean info) throws IOException {
        info.setRes_code(response.code());
        info.setRes_message(response.message());
        Headers headers = response.headers();
        info.getRes_headers().clear();
        if (headers != null) {
            Set<String> names = headers.names();
            if (names != null) {
                for (String name : names) {
                    String headerValue = headers.get(name);
                    info.getRes_headers().add(name + ": " + headerValue);
                }
            }
        }
        boolean isReadResponse = false;
        String headerContextType = response.header("Content-Type");
        if (!TextUtils.isEmpty(headerContextType)) {
            assert headerContextType != null;
            for (String allow_content_type : ServerLog.mConfig.getNetworkLogAllowReadResponseBodyContentTypeSet()) {
                if (headerContextType.toLowerCase().contains(allow_content_type)) {
                    isReadResponse = true;
                    break;
                }
            }
        }

        if (!isReadResponse) {
            info.setRes_body("非可读格式 Body, 不做展示");
            return response;
        }

        ResponseBody resultResponseBody;
        ResponseBody responseBody = response.body();
        info.setRes_body(responseBody.string());
        resultResponseBody = ResponseBody.create(response.body().contentType(), info.getRes_body());
        return response.newBuilder().body(resultResponseBody).build();
    }

    @CallSuper
    protected void doSend(@NonNull NetWorkLogInfoBean netWorkLogInfo) {
        Log.e(ServerLog.TAG, "BaseNetworkLogInterceptor.doSend");
    }

}
