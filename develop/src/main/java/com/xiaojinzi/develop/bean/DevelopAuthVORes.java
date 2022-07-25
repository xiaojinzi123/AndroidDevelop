package com.xiaojinzi.develop.bean;

import androidx.annotation.Keep;

@Keep
public class DevelopAuthVORes {

  private String content;
  private long startTime;
  private long validTime;
  private long endTime;

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public long getStartTime() {
    return startTime;
  }

  public void setStartTime(long startTime) {
    this.startTime = startTime;
  }

  public long getValidTime() {
    return validTime;
  }

  public void setValidTime(long validTime) {
    this.validTime = validTime;
  }

  public long getEndTime() {
    return endTime;
  }

  public void setEndTime(long endTime) {
    this.endTime = endTime;
  }

}
