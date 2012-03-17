package com.facebook.concurrency;

import java.util.Map;

/**
 * Subset of Map relevant to doing caching.  Provides an iterator that
 * allows removal of entries from the cache
 * 
 * @param <K> key to use in the cache
 * @param <V> value of the cache
 * @param <E> exception that may be thrown when creating the value for the 
 *  cache
 */
public interface ConcurrentCache<K, V, E extends Exception> 
  extends Iterable<Map.Entry<K, CallableSnapshot<V, E>>>{
  /**
   * Returns a value associated with the specified key, creating a new value
   * if no previous mapping existed.
   * 
   * NOTE: on exception, the exception will be cached as the result for all
   * future gets(...) on this key until this key is explicitly cleared with a
   * call to remove(...) or removeIfError(...). 
   * 
   * @param key - key to use in the cache
   * @return - returns the value in the cache for the specified key, creating a
   * new value if one did not previously exist
   * @throws E - user specified value creation exception
   */
  public V get(K key) throws E;

  /**
   * atomic insert that will replace any existing value
   * 
   * @param key
   * @param value
   * @return existing value for key, or null if none is present
   * @throws E
   */
  public V put(K key, V value) throws E;

  /**
   * Removes the specified key from the cache, and returns the associated
   * result, which may either be a return value or a value creation exception.
   * 
   * @param key - key to remove from the cache
   * @return removed entry, null if not present
   * @throws E - may throw a creation exception if the create failed, but
   * will still remove the cached result
   */
  public V remove(K key) throws E;

  /**
   * Removes the key if the cached result is an exception
   * 
   * @param key - key to remove from the cache
   * @return true iff the element was removed
   */
  public boolean removeIfError(K key);

  /**
   * This checks if there is a cached result associated with the key. If so,
   * it returns a CallableSnapshot, null otherwise
   * 
   * @param key - key to check
   * @return CallableSnapshot or null if not present
   */
  public CallableSnapshot<V, E> getIfPresent(K key);
  
  /**
   * Unconditional clear operation on the cache
   */
  public void clear();

  /**
   * Optionally may apply heuristic to prune the cache.
   */
  public void prune() throws E;

  /**
   * 
   * @return The number of keys stored in the map
   */
  public int size();
  
}
