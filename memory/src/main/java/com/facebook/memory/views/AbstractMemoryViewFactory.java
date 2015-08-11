package com.facebook.memory.views;

public abstract class AbstractMemoryViewFactory implements MemoryViewFactory {
  @Override
  public abstract MemoryView wrap(long address, long size);

  @Override
  public MemoryView wrap(ReadableMemoryView memoryView) {
    return wrap(memoryView.getAddress(), memoryView.getSize());
  }

  @Override
  public ReadableMemoryView wrapByte(long address) {
    return wrap(address, Byte.BYTES);
  }

  @Override
  public ReadableMemoryView wrapShort(long address) {
    return wrap(address, Short.BYTES);
  }

  @Override
  public ReadableMemoryView wrapInt(long address) {
    return wrap(address, Integer.BYTES);
  }

  @Override
  public ReadableMemoryView wrapLong(long address) {
    return wrap(address, Long.BYTES);
  }
}
