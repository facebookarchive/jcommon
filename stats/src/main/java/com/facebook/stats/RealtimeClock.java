package com.facebook.stats;

class RealtimeClock implements Clock {
  @Override
  public long getMillis() {
    return System.currentTimeMillis();
  }
}
