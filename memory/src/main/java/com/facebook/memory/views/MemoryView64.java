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
  public MemoryView splice(long offset, long size) {

    return new MemoryView64(adjustedAddress(offset), size);
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
  protected void incrementCurrent(int value) {
    current += value;
  }

  public static class Factory extends AbstractMemoryViewFactory {
    private Factory() {}

    @Override
    public MemoryView wrap(long address, long size) {
      return new MemoryView64(address, size);
    }
  }
}