/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.concurrency;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

import com.facebook.collections.TranslatingIterator;
import com.facebook.collectionsbase.Mapper;
import com.facebook.util.exceptions.ExceptionHandler;

@SuppressWarnings({"unchecked"})
public class CoreConcurrentCache<K, V, E extends Exception>
  implements ConcurrentCache<K, V, E> {
  private final ConcurrentMap<K, Object> cache;
  private final ValueFactory<K, V, E> valueFactory;
  private final ExceptionHandler<E> exceptionHandler;

  /**
   * allows subclasses to provide an alternative cache implementation
   * 
   * @param valueFactory
   * @param exceptionHandler
   * @param cache - any ConcurrentMap impl will suffice
   */
  protected CoreConcurrentCache(
    ValueFactory<K, V, E> valueFactory,
    ExceptionHandler<E> exceptionHandler,
    ConcurrentMap<K, Object> cache
  ) {
    this.valueFactory = valueFactory;
    this.exceptionHandler = exceptionHandler;
    this.cache = cache;
  }

  public CoreConcurrentCache(
    ValueFactory<K, V, E> valueFactory, ExceptionHandler<E> exceptionHandler
  ) {
    this(valueFactory, exceptionHandler, new ConcurrentHashMap<>());
  }

  @Override
  public V get(final K key) throws E {
    Object value = cache.get(key);

    // if there isn't entry, do a thread-safe insert into the cache, 
    // and create if necessary 
    if (value == null) {
      AtomicReference<Object> valueRef = new AtomicReference<>();
      value = new PrivateFutureHelper<>(
        () -> {
          V producedValue = valueFactory.create(key);

          // we place our value into the map in place of the factory if and
          // only if it is still mapped to the same private future helper
          CoreConcurrentCache.this.cache.replace(
            key, valueRef.get(), producedValue
          );

          return producedValue;
        },
        exceptionHandler
      );
      valueRef.set(value);

      Object existingValue = cache.putIfAbsent(key, value);

      // did another thread insert a value into the cache before us?  If so,
      // use it
      if (existingValue != null) {
        value = existingValue;
      }
    }

    return decodeValue(value);
  }

  @Override
  public V put(K key, V value) throws E {
    Object existingValue = cache.put(key, value);
    
    return decodeValue(existingValue);
  }

  @Override
  public V remove(K key) throws E {
    Object value = cache.remove(key);

    if (value == null) {
      return null;
    } else {
      return decodeValue(value);
    }
  }

  @Override
  public boolean removeIfError(K key) {
    Object value = cache.get(key);

    if (value != null && 
      value instanceof PrivateFutureHelper &&
      ((FutureHelper<V, E>)value).isError()
      ) {
      cache.remove(key, value);
      
      return true;
    }

    return false;
  }

  @Override
  public void clear() {
    cache.clear();
  }

  @Override
  public void prune() {
    // no-op 
  }

  @Override
  public int size() {
    return cache.size();
  }

  @Override
  public Iterator<Map.Entry<K, CallableSnapshot<V, E>>> iterator() {
    return new TranslatingIterator<>(
      new ValueMapper(),
      cache.entrySet().iterator()
    );
  }

  @Override
  public CallableSnapshot<V, E> getIfPresent(K key) {
    Object value = cache.get(key);

    if (value == null) {
      return null;
    } else {
      return new CallableSnapshot<>(
        new CallableFutureHelper(value),
        new CastingExceptionHandler<E>()
      );
    }
  }

  /**
   * executes a FutureHelper to get a value from a cache entry if need be
   * 
   * @param value cache entry to decode
   * @return actual value in the cache
   * @throws E on error producing the value
   */
  private V decodeValue(Object value) throws E {
    if (value instanceof PrivateFutureHelper) {
      return ((FutureHelper<V, E>) value).safeGet();
    } else {
      return (V)value;
    }
  }

  private class ValueMapper implements
    Mapper<Map.Entry<K, Object>, Map.Entry<K, CallableSnapshot<V, E>>> {
    @Override
    public Map.Entry<K, CallableSnapshot<V, E>> map(Map.Entry<K, Object> input) {
      return new AbstractMap.SimpleImmutableEntry<>(
        input.getKey(),
        new CallableSnapshot<>(
          new CallableFutureHelper(input.getValue()), 
          new CastingExceptionHandler<E>() // OK to cast b/c know exception type
        )
      );
    }
  }

  private class CallableFutureHelper implements Callable {
    private final Object value;

    private CallableFutureHelper(Object value) {
      this.value = value;
    }

    @Override
    public V call() throws Exception {
      return decodeValue(value);
    }
  }

  /**
   * this is a marker class only. Effectively we are using the class type of
   * this object in our cache to indicate that we need to call 
   * FutureHelper.safeGet() to produce a value. Obviously, being a private
   * class, no one can create a value of this type, so...
   * 
   * @param <V2>
   * @param <E2>
   */
  private static class PrivateFutureHelper<V2, E2 extends Exception> 
    extends FutureHelper<V2, E2>{
    private PrivateFutureHelper(
      Callable<V2> callable,
      ExceptionHandler<E2> exceptionHandler
    ) {
      super(callable, exceptionHandler);
    }
  }
}
