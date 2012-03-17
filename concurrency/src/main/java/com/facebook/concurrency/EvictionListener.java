package com.facebook.concurrency;


/**
 * 
 * @param <K>
 * @param <V>
 */
public interface EvictionListener<K, V> {
  /**
   * notifies a listener that a key/value pair has been evicted. No guarantee
   * is made that the key/value pair is not re-inserted by the time this 
   * occurs (ie, an eviction occurred in the past)
   * @param key key that was evicted 
   * @param value value evicted. May be null in the case that either null
   * was inserted, or an exception was thrown producing the value
   */
  void evicted(K key, V value);
}
