package com.facebook.memory.data.types.morphs;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.ReadableMemoryView;

public class PointerMorph extends AbstractMorph<Long> {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  public PointerMorph(String name, int offset) {
    super(name, offset);
  }

  @Override
  public int getSize() {
    return UNSAFE.addressSize();
  }

  @Override
  public Long get(int offset, ReadableMemoryView memoryView) {
    return memoryView.getPointer(getThisAddress(offset));
  }

  @Override
  public void set(Long data, int offset, MemoryView memoryView) {
    memoryView.putPointer(getThisAddress(offset), data);
  }
}
