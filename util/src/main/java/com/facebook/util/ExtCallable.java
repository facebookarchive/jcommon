package com.facebook.util;

public interface ExtCallable<V, E extends Throwable>{
  public V call() throws E;
}
