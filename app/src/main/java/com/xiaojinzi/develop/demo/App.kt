package com.xiaojinzi.develop.demo

import android.app.Application
import com.xiaojinzi.develop.Config
import com.xiaojinzi.develop.DevelopHelper
import com.xiaojinzi.serverlog.impl.ServerLog
import com.xiaojinzi.serverlog.impl.ServerLogConfig

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
    }

}
