package com.facebook.memory.views;

// byte type explicit to avoid auto unboxing/boxing if we used an Iterator<Byte>
public interface ByteStream {
  boolean hasNext();
  byte nextByte();
}
