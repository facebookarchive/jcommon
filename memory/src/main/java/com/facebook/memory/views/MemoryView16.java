package com.facebook.memory.views;

public class MemoryView16 extends AbstractMemoryView<Short> {
  private final short size;
  private short current = 0;

  public MemoryView16(long address, short size) {
    super(address);
    this.size = size;
  }

  public static Factory factory() {
    return new Factory();
  }

  @Override
  protected Short current() {
    return current;
  }

  @Override
  protected long currentAsLong() {
    return current;
  }

  @Override
  protected Short size() {
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
  protected long adjustedAddress(Short value) {
    return getAddress() + value;
  }

  @Override
  protected Short incrementCurrent(int value) {
    return current += value;
  }
  
  public static class Factory {
    public MemoryView16 create(long address, short size) {
      return new MemoryView16(address, size);
    }

    public MemoryView16 create(MemoryView16 memoryView) {
      return new MemoryView16(memoryView.getAddress(), memoryView.size());
    }
  }
}
