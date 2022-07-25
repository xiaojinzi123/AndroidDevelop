package com.xiaojinzi.develop;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * 注册的声明周期回调,用于取消一些调用,这些调用在界面销毁之后
 */
class ComponentLifecycleCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        ComponentActivityStack.getInstance().pushActivity(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        // ignore
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // ignore
    }

    @Override
    public void onActivityPaused(Activity activity) {
        // ignore
    }

    @Override
    public void onActivityStopped(Activity activity) {
        // ignore
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        // ignore
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        ComponentActivityStack.getInstance().removeActivity(activity);
    }

}