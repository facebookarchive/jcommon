package com.facebook.memory.views;

public interface ByteCollector {
  public void collect(byte b);

  public void collect(byte[] bytes);
}
