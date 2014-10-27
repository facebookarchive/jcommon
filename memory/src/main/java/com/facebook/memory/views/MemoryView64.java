package com.facebook.memory.views;

public class MemoryView64 extends AbstractMemoryView<Long> {
  private final long size;
  private long current = 0;

  public MemoryView64(long address, long size) {
    super(address);
    this.size = size;
  }

  public static Factory factory() {
    return new Factory();
  }

  @Override
  protected Long current() {
    return current;
  }

  @Override
  protected long currentAsLong() {
    return current;
  }

  @Override
  protected Long size() {
    return size;
  }

  @Override
  protected long sizeAsLong() {
    return size;
  }

  @Override
  protected long maxSize() {
    return Long.MAX_VALUE;
  }

  @Override
  protected long adjustedAddress(Long value) {
    return getAddress() + value;
  }

  @Override
  protected Long incrementCurrent(int value) {
    return current += value;
  }

  public static class Factory {
    public MemoryView64 create(long address, long size) {
      return new MemoryView64(address, size);
    }

    public MemoryView64 create(MemoryView memoryView) {
      return new MemoryView64(memoryView.getAddress(), memoryView.getSize());
    }
  }
}
