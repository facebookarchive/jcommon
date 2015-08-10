package com.facebook.memory.data.types.morphs;

import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.ReadableMemoryView;

public interface Morph<T> {
  /**
   * @return size in bytes
   */
  int getSize();
//  public List<Morph> getAsMorphList();
  T get(ReadableMemoryView memoryView);
  T get(int morphOffset, ReadableMemoryView memoryView);
  void set(T data, MemoryView memoryView);
  void set(T data, int morphOffset, MemoryView memoryView);
}
