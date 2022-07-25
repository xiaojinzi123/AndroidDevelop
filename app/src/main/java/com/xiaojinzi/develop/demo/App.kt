package com.xiaojinzi.develop.demo

import android.app.Application
import com.xiaojinzi.develop.Config
import com.xiaojinzi.develop.DevelopHelper
import com.xiaojinzi.serverlog.impl.ServerLog
import com.xiaojinzi.serverlog.impl.ServerLogConfig
import okhttp3.OkHttp
import okhttp3.OkHttpClient
import okhttp3.Request

class App : Application() {

    override fun onCreate() {
        super.onCreate()
        DevelopHelper.init(
            Config.newBuilder()
                // 这个是项目的 debug 状态,
                .withDebug(BuildConfig.DEBUG)
                .withApplication(this)
                .withMainAppIntentAction("android_develop_demo_app_main")
                .withDevelopOpenIntentAction("android_develop_demo_develop_open")
                .withDevelopErrorIntentAction("android_develop_demo_develop_error")
                .build()
        )
        DevelopHelper.tryOpenDevelop(this)
        DevelopHelper.crashHandle()
        ServerLog.init(
            ServerLogConfig.newBuilder()
                .withDebug(DevelopHelper.isDevelop())
                .withProductName("serverLogDemo")
                .build()
        )

        Thread {
            val client = OkHttpClient().newBuilder().build()
            val call = client.newCall(
                request = Request.Builder().url("http://www.baidu.com").build()
            )
            call.execute()
        }.start()

    }

}
