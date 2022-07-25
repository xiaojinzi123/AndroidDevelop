package com.xiaojinzi.develop.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Process;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.xiaojinzi.develop.ComponentActivityStack;
import com.xiaojinzi.develop.DevelopHelper;
import com.xiaojinzi.develop.R;

import java.io.File;

/**
 * 这个界面主要有几个作用
 * 1. 作为一个单独的进程的启动界面, 因为原进程会被杀死
 */
public class OpenDevelopAct extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.develop_open_develop_act);
        new Handler(Looper.getMainLooper()).postDelayed(() -> doReboot(), 1000);
    }

    private void doReboot() {
        try {
            String encryptText = getIntent().getStringExtra("encryptText");
            String mainAppAction = getIntent().getStringExtra("mainAppAction");
            if (!TextUtils.isEmpty(encryptText)) {
                DevelopHelper.INSTANCE.saveEncryptDevelopAuthData(getApplicationContext(), encryptText);
                Intent intent = new Intent(mainAppAction);
                intent.addCategory(Intent.CATEGORY_DEFAULT);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            finish();
        } catch (Exception ignore) {
            // ignore
        } finally {
            // 杀死自己
            Process.killProcess(Process.myPid());
        }
    }

}