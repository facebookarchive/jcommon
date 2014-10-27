package com.facebook.memory;

public class AllocationContext {
  private final long key;

  public AllocationContext(long key) {
    this.key = key;
  }


  public long getKey() {
    return key;
  }
}
