package com.facebook.memory.data.structures;

public class MemoryAddress implements OffHeap {
  private final long address;

  public MemoryAddress(long address) {
    this.address = address;
  }

  @Override
  public long getAddress() {
    return address;
  }
}
