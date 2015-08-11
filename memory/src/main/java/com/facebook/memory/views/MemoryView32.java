package com.facebook.memory.views;

import com.google.common.base.Preconditions;

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

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Override
  public MemoryView splice(long offset, long size) {
    Preconditions.checkArgument(size < Integer.MAX_VALUE);
    Preconditions.checkArgument(offset < Integer.MAX_VALUE);

    return new MemoryView32(adjustedAddress((int) offset), (int) size);
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
  protected void incrementCurrent(int value) {
    current += value;
  }

  public static class Factory extends AbstractMemoryViewFactory {
    private Factory() {}

    @Override
    public MemoryView wrap(long address, long size) {
      Preconditions.checkArgument(size <= Integer.MAX_VALUE);

      return new MemoryView32(address, (int) size);
    }
  }
}
