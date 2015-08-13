package com.facebook.memory.data.structures;

public class LinkedListKeyWrapper implements OffHeapByteArrayWrapper {
  @Override
  public OffHeapByteArray wrap(long address) {
    return LinkedListBucketNode.wrap(address).getKey();
  }
}
