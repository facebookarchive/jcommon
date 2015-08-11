package com.facebook.memory.views;

import com.google.common.base.Preconditions;

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

  @SuppressWarnings("NumericCastThatLosesPrecision")
  @Override
  public MemoryView splice(long offset, long size) {
    Preconditions.checkArgument(size < Short.MAX_VALUE);
    Preconditions.checkArgument(offset < Short.MAX_VALUE);

    return new MemoryView16(adjustedAddress((short) offset), (short) size);
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
  protected void incrementCurrent(int value) {
    current += value;
  }

  public static class Factory extends AbstractMemoryViewFactory {
    private Factory() {}

    @Override
    public MemoryView wrap(long address, long size) {
      Preconditions.checkArgument(size < Short.MAX_VALUE);

      return new MemoryView16(address, (short) size);
    }
  }
}
