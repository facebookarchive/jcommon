package com.facebook.memory.views;

public interface BytesVisitor {
  public void visit(long address, byte b);
}
