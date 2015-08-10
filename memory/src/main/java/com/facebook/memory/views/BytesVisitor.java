package com.facebook.memory.views;

public interface BytesVisitor {
  void visit(long address, byte b);
}
