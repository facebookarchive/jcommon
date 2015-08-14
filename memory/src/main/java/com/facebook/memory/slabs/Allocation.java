package com.facebook.memory.slabs;

import com.google.common.base.Preconditions;

public class Allocation {
  private final long address;
  private final int size;

  public Allocation(long address, int size) {
    Preconditions.checkArgument(size >= 0);
    this.address = address;
    this.size = size;
  }

  public long getAddress() {
    return address;
  }

  public int getSize() {
    return size;
  }
}
