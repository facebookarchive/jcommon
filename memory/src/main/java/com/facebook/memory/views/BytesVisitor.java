package com.facebook.memory.views;

/**
 * visitor pattern for Slabs
 */
public interface BytesVisitor {
  void visit(long address, byte b);
}
