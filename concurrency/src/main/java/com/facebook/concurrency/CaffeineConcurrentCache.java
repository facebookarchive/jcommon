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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.base.Preconditions;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import com.facebook.util.exceptions.ExceptionHandler;

public class CaffeineConcurrentCache<K, V, E extends Exception> implements ConcurrentCache<K, V, E> {
  private final LoadingCache<K, CallableSnapshot<V, E>> cache;
  private final ExceptionHandler<E> exceptionHandler;
  private final ConcurrentMap<K, CallableSnapshot<V, E>> cacheAsMap;

  /**
   * callers may pass in a Cache object configured appropriately
   *
   * @param valueFactory
   * @param exceptionHandler
   * @param cacheBuilder     - result of Caffeine.newBuilder() + any config (caller may customize the cache)
   */
  public CaffeineConcurrentCache(
    ValueFactory<K, V, E> valueFactory,
    ExceptionHandler<E> exceptionHandler,
    Caffeine<Object, Object> cacheBuilder
  ) {
    this.exceptionHandler = exceptionHandler;
    this.cache = cacheBuilder.build(new CacheValueLoader<>(valueFactory, exceptionHandler));
    cacheAsMap = cache.asMap();
  }

  public CaffeineConcurrentCache(
    ValueFactory<K, V, E> valueFactory, ExceptionHandler<E> exceptionHandler
  ) {
    this(valueFactory, exceptionHandler, Caffeine.newBuilder());
  }

  @Override
  public V get(final K key) throws E {
    return cache.get(key).get();
  }

  @Override
  public V put(K key, V value) throws E {
    CallableSnapshot<V, E> putResult = cacheAsMap.put(key, new CallableSnapshot<>(() -> value, exceptionHandler));

    return putResult == null ? null : putResult.get();
  }

  @Override
  public V remove(K key) throws E {
    CallableSnapshot<V, E> removeResult = cacheAsMap.remove(key);

    return removeResult == null ? null : removeResult.get();
  }

  @Override
  public boolean removeIfError(K key) {
    CallableSnapshot<V, E> snapshot = cache.getIfPresent(key);

    if (snapshot != null && snapshot.getException() != null) {
      cacheAsMap.remove(key, snapshot);

      return true;
    }

    return false;
  }

  @Override
  public void clear() {
    cache.invalidateAll();
  }

  @Override
  public void prune() {
    cache.cleanUp();
  }

  @Override
  public int size() {
    long sizeInLong = cache.estimatedSize();
    Preconditions.checkState(sizeInLong < Integer.MAX_VALUE, "overflow on cache size");

    return (int) sizeInLong;
  }

  @Override
  public Iterator<Map.Entry<K, CallableSnapshot<V, E>>> iterator() {
    Iterator<Map.Entry<K, CallableSnapshot<V, E>>> iterator = cacheAsMap.entrySet().iterator();

    return iterator;
  }

  @Override
  public CallableSnapshot<V, E> getIfPresent(K key) {
    return cache.getIfPresent(key);
  }

  private static class CacheValueLoader<K, V, E extends Exception> implements CacheLoader<K, CallableSnapshot<V, E>> {
    private final ValueFactory<K, V, E> valueFactory;
    private final ExceptionHandler<E> exceptionHandler;

    private CacheValueLoader(ValueFactory<K, V, E> valueFactory, ExceptionHandler<E> exceptionHandler) {
      this.valueFactory = valueFactory;
      this.exceptionHandler = exceptionHandler;
    }

    @Override
    public CallableSnapshot<V, E> load(K key) {
      try {
        V value = valueFactory.create(key);

        return new CallableSnapshot<>(() -> value, exceptionHandler);
      } catch (Exception e) {
        return CallableSnapshot.createWithException(exceptionHandler.handle(e));
      }
    }
  }
}
