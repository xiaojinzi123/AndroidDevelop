package com.xiaojinzi.develop.bean;

import androidx.annotation.Keep;

@Keep
public class DevelopAuthVOReq {

  private String content;
  private long startTime;
  private long validTime;

  public DevelopAuthVOReq(String content, long startTime, long validTime) {
    this.content = content;
    this.startTime = startTime;
    this.validTime = validTime;
  }

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

}
