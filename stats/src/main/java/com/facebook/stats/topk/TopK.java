package com.facebook.stats.topk;

import java.util.List;

/*
 * Interface for streaming top-k algorithms.
 */
public interface TopK<T extends Comparable<T>> {
  public void add(T key, long count);
  public List<T> getTopK();
}
