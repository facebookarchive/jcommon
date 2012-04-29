package com.facebook.concurrency;

public class NoOp implements Runnable {
  public static final Runnable INSTANCE = new NoOp();

  private NoOp(){}

  @Override
  public void run() {
  }
}
