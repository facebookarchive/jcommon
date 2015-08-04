package com.facebook.concurrency;

public class TaggedRunnable implements Runnable {
  private final Runnable runnable;
  private final String tag;

  public TaggedRunnable(String tag, Runnable runnable) {
    this.tag = tag;
    this.runnable = runnable;
  }

  @Override
  public void run() {
    runnable.run();
  }

  public String getTag() {
    return tag;
  }
}
