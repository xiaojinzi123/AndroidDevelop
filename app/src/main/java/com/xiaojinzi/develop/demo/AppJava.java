package com.xiaojinzi.develop.demo;

import android.app.Application;
import android.util.Log;

import com.xiaojinzi.develop.Config;
import com.xiaojinzi.develop.DevelopHelper;
import com.xiaojinzi.serverlog.impl.ServerLog;
import com.xiaojinzi.serverlog.impl.ServerLogConfig;

public class AppJava extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DevelopHelper.INSTANCE.init(
                new Config.Builder()
                        .withDebug(false)
                        .withApplication(this)
                        .withMainAppIntentAction("android_develop_demo_app_main")
                        .withDevelopOpenIntentAction("android_develop_demo_develop_open")
                        .build());
        ServerLog.INSTANCE.init(
                new ServerLogConfig.Builder()
                        .withDebug(DevelopHelper.INSTANCE.isDevelop())
                        .withProductName("serverLogDemo")
                        .build()
        );
    }
}
