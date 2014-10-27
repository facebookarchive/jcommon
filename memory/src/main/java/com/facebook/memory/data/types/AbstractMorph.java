package com.facebook.memory.data.types;

import com.facebook.memory.views.MemoryView;

public abstract class AbstractMorph<T> implements Morph<T> {
  private final String name;
  private final int offset;

  protected AbstractMorph(String name, int offset) {
    this.name = name;
    this.offset = offset;
  }

  public abstract int getSize();

  public abstract T get(int offset, MemoryView memoryView);

  public abstract void set(T data, int offset, MemoryView memoryView);

  @Override
  public T get(MemoryView memoryView) {
    return get(0, memoryView);
  }

  @Override
  public void set(T data, MemoryView memoryView) {
    set(data, 0, memoryView);
  }

  protected int getOffset() {
    return offset;
  }

  protected int getThisAddress(int morphOffset) {
    return morphOffset + getOffset();
  }
}
