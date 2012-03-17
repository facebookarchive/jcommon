package com.facebook.collections;

public interface SimpleHeapFactory<T, S extends SimpleHeap<T>> {
  /**
   * 
   * @param initialSize hint to be used to size the heap initially
   * @return
   */
  public S create(int initialSize);
}
