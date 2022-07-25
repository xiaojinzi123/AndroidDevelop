package com.xiaojinzi.develop;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public class Strings {

  public static String requireNotEmpty(@Nullable String target) {
    return requireNotEmpty(target, null);
  }

  public static String requireNotEmpty(@Nullable String target, @Nullable String message) {
    if (TextUtils.isEmpty(target)) {
      if (TextUtils.isEmpty(message)) {
        throw new IllegalArgumentException();
      } else {
        throw new IllegalArgumentException(message);
      }
    }
    return target;
  }
}
