package com.facebook.memory.data.types.morphs;

import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.ReadableMemoryView;

public class ByteMorph extends AbstractMorph<Byte> {
  protected ByteMorph(String name, int offset) {
    super(name, offset);
  }

  @Override
  public int getSize() {
    return Byte.BYTES;
  }

  @Override
  public Byte get(int offset, ReadableMemoryView memoryView) {
    return memoryView.getByte(getThisAddress(offset));
  }

  @Override
  public void set(Byte data, int offset, MemoryView memoryView) {
    memoryView.putByte(getThisAddress(offset), data);
  }
}
