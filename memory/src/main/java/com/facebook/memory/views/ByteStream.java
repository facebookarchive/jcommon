package com.facebook.memory.views;

// byte type explicit to avoid auto unboxing/boxing
public interface ByteStream {
  boolean hasNext();
  byte nextByte();
}
