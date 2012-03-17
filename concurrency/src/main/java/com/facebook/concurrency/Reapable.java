package com.facebook.concurrency;

public interface Reapable<E extends Exception> {
  public void shutdown() throws E;
}
