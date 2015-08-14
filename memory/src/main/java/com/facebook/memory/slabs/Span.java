package com.facebook.memory.slabs;

import com.google.common.base.Preconditions;

public class Span {
  private static final Span EMPTY_SPAN = new Span(0,0);
  private final int offset;
  private final int size;

  private Span() {
    offset = 0;
    size = 0;
  }

  public Span(int offset, int size) {
    Preconditions.checkArgument(offset >= 0, "offset %d must be >= 0", offset);
    Preconditions.checkArgument(size > 0, "size %d must be > 0", size);
    this.offset = offset;
    this.size = size;
  }

  public static Span emptySpan() {
    return EMPTY_SPAN;
  }

  public int getOffset() {
    return offset;
  }

  public int getSize() {
    return size;
  }
}
