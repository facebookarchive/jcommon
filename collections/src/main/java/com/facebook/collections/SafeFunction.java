package com.facebook.collections;

/**
 * the point of this is to restore the utility and equivalence of guava's "Function"
 * in the case that we have code that users our "Function".  The only exception you can throw there
 * or here is a runtime.  The parent, you can declare a checked exception which is useful.
 *
 * @param <K>
 * @param <V>
 */
public interface SafeFunction<K, V> extends Function<K, V, RuntimeException> {
  public V execute(K input);
}
