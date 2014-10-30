package com.facebook.memory.data.structures;

import com.facebook.memory.FailedAllocationException;

public interface ByteArrayMap {
  byte[] get(byte[] key);

  void put(byte[] key, byte[] value) throws FailedAllocationException;

  boolean remove(byte[] key);

  boolean containsKey(byte[] key);

  int getSize();
}
