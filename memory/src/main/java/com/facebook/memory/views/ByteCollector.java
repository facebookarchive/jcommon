package com.facebook.memory.views;

public interface ByteCollector {
  void collect(byte b);

  void collect(byte[] bytes);
}
