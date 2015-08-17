package com.facebook.memory.data.structures;

public class CachePolicyKey extends MemoryAddress implements OffHeap {
  public CachePolicyKey(long address) {
    super(address);
  }
}
