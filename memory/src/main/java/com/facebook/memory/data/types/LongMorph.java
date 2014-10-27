package com.facebook.memory.data.types;

import com.facebook.memory.views.MemoryView;

public class LongMorph extends AbstractMorph<Long> {
  public LongMorph(String name, int offset) {
    super(name, offset);
  }

  @Override
  public int getSize() {
    return Long.BYTES;
  }

  @Override
  public Long get(int offset, MemoryView memoryView) {
    return memoryView.getLong(offset + getOffset());
  }

  @Override
  public void set(Long data, int offset, MemoryView memoryView) {
    memoryView.putLong(offset + getOffset(), data);
  }
}
