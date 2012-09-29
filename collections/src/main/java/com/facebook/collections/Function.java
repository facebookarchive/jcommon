package com.facebook.collections;

/**
 * full function capability: take an input, product an output, and throw checked exceptions or even
 * Error types if necessary
 *
 * @param <K>
 * @param <V>
 * @param <E>
 */
public interface Function<K, V, E extends Throwable> {
  public V execute(K input) throws E;
}
