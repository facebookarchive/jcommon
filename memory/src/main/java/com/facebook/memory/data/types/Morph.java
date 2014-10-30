package com.facebook.memory.data.types;

import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.ReadableMemoryView;

public interface Morph<T> {
  /**
   * @return size in bytes
   */
  public int getSize();
//  public List<Morph> getAsMorphList();
  public T get(ReadableMemoryView memoryView);
  public T get(int morphOffset, ReadableMemoryView memoryView);
  public void set(T data, MemoryView memoryView);
  public void set(T data, int morphOffset, MemoryView memoryView);
}
