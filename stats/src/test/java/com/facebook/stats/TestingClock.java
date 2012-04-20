package com.facebook.stats;

import java.util.concurrent.TimeUnit;

public class TestingClock implements Clock {
  private long time;

  public long getMillis() {
    return time;
  }

  public void setTime(long millis) {
    time = millis;
  }

  public void increment(long delta, TimeUnit unit) {
    time += unit.toMillis(delta);
  }
}
