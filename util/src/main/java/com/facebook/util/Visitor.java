package com.facebook.util;

/**
 * simple visitor object for type T
 *
 * @param <T>
 *
 */
public interface Visitor<T> {
  /**
   *
   * @param element element being examined
   * @return return true to continue visiting to next element
   */
  public boolean visit(T element);
}
