package com.facebook.memory.data.structures;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.FailedAllocationException;

public interface ByteArrayMap {
  ByteArray get(ByteArray key);

  void put(ByteArray key, ByteArray value) throws FailedAllocationException;

  boolean remove(ByteArray key);

  boolean containsKey(ByteArray key);

  int getSize();
}
