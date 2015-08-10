package com.facebook.memory.data.types.morphs;

import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.ReadableMemoryView;

public class IntMorph extends AbstractMorph<Integer> {
  public IntMorph(String name, int offset) {
    super(name, offset);
  }

  @Override
  public int getSize() {
    return Integer.BYTES;
  }

  @Override
  public Integer get(int offset, ReadableMemoryView memoryView) {
    return memoryView.getInt(offset + getOffset());
  }

  @Override
  public void set(Integer data, int offset, MemoryView memoryView) {
    memoryView.putInt(offset + getOffset(), data);
  }
}
