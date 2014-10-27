package com.facebook.memory.views;

public class MemoryView32 extends AbstractMemoryView<Integer> {
  private final int size;
  private int current = 0;

  public MemoryView32(long address, int size) {
    super(address);
    this.size = size;
  }

  public static Factory factory() {
    return new Factory();
  }

  @Override
  protected Integer current() {
    return current;
  }

  @Override
  protected long currentAsLong() {
    return current;
  }

  @Override
  protected Integer size() {
    return size;
  }

  @Override
  protected long sizeAsLong() {
    return size;
  }

  @Override
  protected long maxSize() {
    return Integer.MAX_VALUE;
  }

  @Override
  protected long adjustedAddress(Integer value) {
    return getAddress() + value;
  }

  @Override
  protected Integer incrementCurrent(int value) {
    return current += value;
  }

  public static class Factory {
    public MemoryView32 create(long address, int size) {
      return new MemoryView32(address, size);
    }

    public MemoryView32 create(MemoryView32 memoryView) {
      return new MemoryView32(memoryView.getAddress(), memoryView.size());
    }
  }
}
