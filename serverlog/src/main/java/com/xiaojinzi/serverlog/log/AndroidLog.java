package com.xiaojinzi.serverlog.log;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.xiaojinzi.serverlog.bean.AndroidLogBean;
import com.xiaojinzi.serverlog.bean.MessageBean;
import com.xiaojinzi.serverlog.impl.ServerLog;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class AndroidLog {

    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    public static final String V = "VERBOSE";
    public static final String D = "DEBUG";
    public static final String I = "INFO";
    public static final String W = "WARN";
    public static final String E = "ERROR";

    public static void doSend(@NonNull String level, @Nullable String tag, @Nullable String content, @Nullable Throwable tr) {
        if (ServerLog.TAG.equals(tag)) {
            return;
        }
        try {
            String stackTraceMsg = null;
            if (tr != null) {
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                // 打印到 pw 这个流中, 最终会到 sw 流中
                tr.printStackTrace(pw);
                stackTraceMsg = sw.toString();
                pw.close();
                sw.close();
            }
            AndroidLogBean androidLogBean = new AndroidLogBean(level, tag, content, stackTraceMsg);
            MessageBean<AndroidLogBean> messageBean = new MessageBean();
            messageBean.setType(MessageBean.TYPE_ANDROID_LOG);
            messageBean.setData(androidLogBean);
            ServerLog.INSTANCE.send(messageBean);
        } catch (Exception ignore) {
            // ignore
        }
    }

    public static int v(@Nullable String tag, @NonNull String msg) {
        doSend(V, tag, msg, null);
        return Log.v(tag, msg);
    }

    public static int v(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        doSend(V, tag, msg, tr);
        return Log.v(tag, msg, tr);
    }

    public static int d(@Nullable String tag, @NonNull String msg) {
        doSend(D, tag, msg, null);
        return Log.d(tag, msg);
    }

    public static int d(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        doSend(D, tag, msg, tr);
        return Log.d(tag, msg, tr);
    }

    public static int i(@Nullable String tag, @NonNull String msg) {
        doSend(I, tag, msg, null);
        return Log.i(tag, msg);
    }

    public static int i(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        doSend(I, tag, msg, tr);
        return Log.i(tag, msg, tr);
    }

    public static int w(@Nullable String tag, @NonNull String msg) {
        doSend(W, tag, msg, null);
        return Log.w(tag, msg);
    }

    public static int w(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        doSend(W, tag, msg, tr);
        return Log.w(tag, msg, tr);
    }

    public static int w(@Nullable String tag, @Nullable Throwable tr) {
        doSend(W, tag, null, tr);
        return Log.w(tag, tr);
    }

    public static boolean isLoggable(@Nullable String var0, int var1) {
        return Log.isLoggable(var0, var1);
    }

    public static int e(@Nullable String tag, @NonNull String msg) {
        doSend(E, tag, msg, null);
        return Log.e(tag, msg);
    }

    public static int e(@Nullable String tag, @Nullable String msg, @Nullable Throwable tr) {
        doSend(E, tag, msg, tr);
        return Log.e(tag, msg, tr);
    }

    @NonNull
    public static String getStackTraceString(@Nullable Throwable tr) {
        return Log.getStackTraceString(tr);
    }

    public static int println(int priority, @Nullable String tag, @NonNull String msg) {
        return Log.println(priority, tag, msg);
    }

}
