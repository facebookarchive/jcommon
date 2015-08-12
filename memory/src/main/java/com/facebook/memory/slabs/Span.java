package com.facebook.memory.slabs;

public class Span {
  private static final Span EMPTY_SPAN = new Span(0, 0);
  private final int offset;
  private final int size;

  public Span(int offset, int size) {
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
