package com.facebook.memory.data.structures;

public abstract class AnnotatableMemoryAddress extends MemoryAddress implements Annotatable {
  public AnnotatableMemoryAddress(long address) {
    super(address);
  }
}
