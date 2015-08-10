package com.facebook.memory.data.types.definitions;

import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.data.types.morphs.IntMorph;
import com.facebook.memory.data.types.morphs.PointerMorph;

public class Span {
  public static final Span DEF = new Span(0);

  private final PointerMorph dataAddress;
  private final IntMorph size;

  public Span(int offset) {
    dataAddress = new PointerMorph("dataAddress", offset);
    size = new IntMorph("size", offset + UnsafeAccessor.get().addressSize());
  }

  public PointerMorph dataAddress() {
    return dataAddress;
  }

  public IntMorph size() {
    return size;
  }

  public int getSize() {
    return dataAddress.getSize() + size.getSize();
  }
}
