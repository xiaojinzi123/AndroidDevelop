package com.xiaojinzi.develop.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.xiaojinzi.develop.ComponentActivityStack;
import com.xiaojinzi.develop.R;

public class ErrorAct extends AppCompatActivity {

  private TextView tv_error;
  private Toolbar toolbar;
  private Button bt_reboot;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setTheme(R.style.ErrorViewTheme);
    setContentView(R.layout.develop_error_act);
    tv_error = findViewById(R.id.tv_error);
    toolbar = findViewById(R.id.toolbar);
    bt_reboot = findViewById(R.id.bt_reboot);
    onInit();
  }

  protected void onInit() {
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);
    String mainAppAction = getIntent().getStringExtra("mainAppAction");
    String errorMsg = getIntent().getStringExtra("errorMsg");
    tv_error.setText(errorMsg);

    bt_reboot.setOnClickListener(v -> {
      Intent intent = new Intent(mainAppAction);
      intent.addCategory(Intent.CATEGORY_DEFAULT);
      intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
      startActivity(intent);
      finish();
      Process.killProcess(Process.myPid());
    });
  }
}