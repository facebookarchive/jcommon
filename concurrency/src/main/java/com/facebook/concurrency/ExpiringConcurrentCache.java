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

import org.joda.time.DateTimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.facebook.collections.TranslatingIterator;
import com.facebook.collectionsbase.Mapper;
import com.facebook.util.exceptions.ExceptionHandler;

public class ExpiringConcurrentCache<K, V, E extends Exception>
  implements ConcurrentCache<K, V, E> {
  private static final Logger LOG = LoggerFactory.getLogger(ExpiringConcurrentCache.class);

  private final ConcurrentCache<K, CacheEntry<V, E>, E> baseCache;
  private final long maxAgeMillis;
  private final ExecutorService executor;

  // track the last time a prune operation was performed. Objects 
  // will only be pruned after maxAgeMillis has passed
  private final AtomicLong lastPrune = new AtomicLong(
    DateTimeUtils.currentTimeMillis()
  );
  private final AtomicBoolean pruning = new AtomicBoolean(false);
  // an EvictionListener provides a memory efficient way for clients of this
  // object to receive information about both the key and value evicted.
  private final EvictionListener<K, V> evictionListener;

  public ExpiringConcurrentCache(
    ValueFactory<K, V, E> valueFactory,
    long maxAge,
    TimeUnit maxAgeUnit,
    EvictionListener<K, V> evictionListener,
    ExceptionHandler<E> exceptionHandler,
    ExecutorService executor
  ) {
    this.evictionListener = evictionListener;
    this.baseCache =
      new CoreConcurrentCache<>(
        new CacheEntryValueFactory(valueFactory),
        exceptionHandler
      );
    this.maxAgeMillis = maxAgeUnit.toMillis(maxAge);
    this.executor = executor;
  }

  public ExpiringConcurrentCache(
    ValueFactory<K, V, E> valueFactory,
    long maxAge,
    TimeUnit maxAgeUnit,
    EvictionListener<K, V> evictionListener,
    ExceptionHandler<E> exceptionHandler
  ) {
    this(
      valueFactory,
      maxAge,
      maxAgeUnit,
      evictionListener,
      exceptionHandler,
      Executors.newSingleThreadExecutor()
    );
  }

  /**
   * compatibility function for use with legacy implementations that use
   * Reapable to be notified of evictions
   * 
   * @param valueFactory
   * @param maxAge
   * @param maxAgeUnit
   * @param exceptionHandler
   * @param executor
   * @param <K>
   * @param <V>
   * @param <E>
   * @return
   */
  public static <K, V extends Reapable<? extends Exception>, E extends Exception>
  ExpiringConcurrentCache<K, V, E> createWithReapableValue(
    ValueFactory<K, V, E> valueFactory,
    long maxAge,
    TimeUnit maxAgeUnit,
    ExceptionHandler<E> exceptionHandler,
    ExecutorService executor
  ) {
    return new ExpiringConcurrentCache<>(
      valueFactory,
      maxAge,
      maxAgeUnit,
      (key, value) -> {
        try {
          value.shutdown();
        } catch (Throwable t) {
          LOG.error("error shutting down reapable", t);
        }
      },
      exceptionHandler,
      executor
    );
  }

  @Override
  public V get(K key) throws E {
    CacheEntry<V, E> cacheEntry = baseCache.get(key);

    CallableSnapshot<V, E> snapshot = cacheEntry.touch();

    // prune after getting the value
    pruneIfNeeded();

    return snapshot.get();
  }

  @Override
  public V put(K key, V value) throws E {
    pruneIfNeeded();
    
    CacheEntry<V, E> cacheEntry = new CacheEntry<>(value);
    CacheEntry<V, E> existingCacheEntry = baseCache.put(key, cacheEntry);

    return existingCacheEntry == null ? null : existingCacheEntry.getSnapshot().get();
  }

  @Override
  public V remove(K key) throws E {
    pruneIfNeeded();

    CacheEntry<V, E> cacheEntry = baseCache.remove(key);
    
    return cacheEntry == null ? null : cacheEntry.getSnapshot().get();
  }

  @Override
  public boolean removeIfError(K key) {
    return baseCache.removeIfError(key);
  }

  @Override
  public void clear() {
    baseCache.clear();
  }

  @Override
  public void prune() throws E {
    pruneIfNeeded();
  }

  @Override
  public int size() {
    return baseCache.size();
  }

  /**
   * non-blocking, thread-safe prune operation that only enters pruning block
   * after enough time has elapsed
   *
   * @throws E
   */
  private void pruneIfNeeded() {
    // only prune if sufficient time has elapsed and another thread isn't
    // already pruning
    if (DateTimeUtils.currentTimeMillis() - lastPrune.get() >= maxAgeMillis &&
      pruning.compareAndSet(false, true)) {
      try {
        Iterator<Map.Entry<K, CallableSnapshot<CacheEntry<V, E>, E>>> iterator =
          baseCache.iterator();

        while (iterator.hasNext()) {
          final K key;
          final CacheEntry<V, E> cacheEntry;
          try {
            Map.Entry<K, CallableSnapshot<CacheEntry<V, E>, E>> entry =
              iterator.next();
            key = entry.getKey();
            cacheEntry = entry.getValue().get();
          } catch (Exception e) {
            // We control the creation process, so should not get an exception
            throw new RuntimeException("CacheEntry create should not fail");
          }

          if (cacheEntry.hasExpired(maxAgeMillis)) {
            // remove the item from the cache
            iterator.remove();

            // do any shutdown() tasks asynchronously so we don't block access
            // to the cache
            executor.execute(
              () -> {
                // now reap the entry
                try {
                  V value = cacheEntry.getSnapshot().get();

                  try {
                    evictionListener.evicted(key, value);
                  } catch (Throwable t) {
                    LOG.error(
                      "Error reaping cache element-- may not be properly closed",
                      t
                    );
                  }
                } catch (Exception e) {
                  LOG.info(
                    "Unable to get cache value for key " + key
                  );
                  // still notify that key is evicted
                  evictionListener.evicted(key, null);
                }
              }
            );

          }
        }
      } finally {
        lastPrune.set(DateTimeUtils.currentTimeMillis());
        pruning.set(false);
      }
    }
  }

  @Override
  public Iterator<Map.Entry<K, CallableSnapshot<V, E>>> iterator() {
    return new TranslatingIterator<>(
      new ValueMapper(), baseCache.iterator()
    );
  }

  @Override
  public CallableSnapshot<V, E> getIfPresent(K key) {
    pruneIfNeeded();
    CallableSnapshot<CacheEntry<V, E>, E> snapshot =
      baseCache.getIfPresent(key);

    if (snapshot == null) {
      return null;
    } else {
      try {
        return snapshot.get().getSnapshot();
      } catch (Exception e) {
        throw new RuntimeException("this shouldn't happen", e);
      }
    }
  }

  private class ValueMapper implements
    Mapper<
      Map.Entry<K, CallableSnapshot<CacheEntry<V, E>, E>>,
      Map.Entry<K, CallableSnapshot<V, E>>
      > {
    @Override
    public Map.Entry<K, CallableSnapshot<V, E>> map(
      Map.Entry<K, CallableSnapshot<CacheEntry<V, E>, E>> input
    ) {
      CallableSnapshot<V, E> snapshot;
      try {
        snapshot = input.getValue().get().touch();
      } catch (Exception e) {
        // We control the creation process, so we should not get an exception
        throw new RuntimeException("CacheEntry create should not fail");
      }
      return new AbstractMap.SimpleImmutableEntry<>(
        input.getKey(),
        snapshot
      );
    }
  }

  private class CacheEntryValueFactory
    implements ValueFactory<K, CacheEntry<V, E>, E> {
    CallableSnapshotFunction<K, V, E> snapshotFunction;

    private CacheEntryValueFactory(ValueFactory<K, V, E> valueFactory) {
      snapshotFunction =
        new PrivateCallableSnapshotFunction<>(valueFactory);
    }

    @Override
    public CacheEntry<V, E> create(K input) {
      return new CacheEntry<>(snapshotFunction.apply(input));
    }
  }

  /**
   * a cache entry is a value and it's last accessed time (create, read).
   * The last accessed is used for expiring entire older than a configured
   * TTL by the cache
   *
   * @param <V> value type
   * @param <E> exception type
   */
  @SuppressWarnings({"unchecked"})
  private static class CacheEntry<V, E extends Exception> {
    // mtime guarded by this
    private long mtime = DateTimeUtils.currentTimeMillis();
    private volatile Object snapshotOrValue;

    private CacheEntry(V value) {
      this.snapshotOrValue = value;
    }

    private CacheEntry(CallableSnapshot<V, E> snapshot) {
      // if the snapshot indicates no error, store just the value. This
      // will save us about 24 bytes on a 64-bit box:  2 x 8 byte ptr and
      // the 8-byte overhead java adds for each object (saved by not
      // having the CallableSnapshot)
      if (snapshot.getException() == null) {
        try {
          snapshotOrValue = snapshot.get();
        } catch (Exception e) {
          LOG.error("this should NEVER be seen", e);
          snapshotOrValue = snapshot;
        }
      } else {
        snapshotOrValue = snapshot;
      }
    }

    public CallableSnapshot<V, E> getSnapshot() throws E {
      return getCallableSnapshot();
    }

    public synchronized CallableSnapshot<V, E> touch() {
      mtime = DateTimeUtils.currentTimeMillis();

      return getCallableSnapshot();
    }

    public synchronized boolean hasExpired(long maxAgeMillis) {
      return DateTimeUtils.currentTimeMillis() - mtime >= maxAgeMillis;
    }

    /**
     * we store either the result of the Callable if there is no 
     * exception (saves memory). Otherewise, we keep the whole CallableSnapshot
     *
     * @return
     */
    private CallableSnapshot<V, E> getCallableSnapshot() {
      if (snapshotOrValue instanceof PrivateCallableSnapshot) {
        return (CallableSnapshot<V, E>) snapshotOrValue;
      } else {
        // we can use NullExceptionHandler since we know
        // this is a value
        return new PrivateCallableSnapshot<>(
          new FixedValueCallable<>((V) snapshotOrValue),
          new NullExceptionHandler<>()
        );
      }
    }
  }

  // this private class is using it's class type as a boolean flag (to save
  // memory). If the value stored is of this type in the CacheEntry,
  // then it means we couldn't store the value and need to call 
  // CallableSnapshot.get(). By making it private, we guarantee that O
  // cannot be this type
  private static class PrivateCallableSnapshotFunction
    <I, O, E extends Exception>
    implements CallableSnapshotFunction<I, O, E> {

    private final ValueFactory<I, O, E> valueFactory;
    private final ExceptionHandler<E> exceptionHandler;

    private PrivateCallableSnapshotFunction(
      ValueFactory<I, O, E> valueFactory, ExceptionHandler<E> exceptionHandler
    ) {
      this.valueFactory = valueFactory;
      this.exceptionHandler = exceptionHandler;
    }

    private PrivateCallableSnapshotFunction(ValueFactory<I, O, E> valueFactory) {
      // We can cast exceptions because the value factory declares which type
      // of exceptions it can throw on creation
      this(valueFactory, new CastingExceptionHandler<>());
    }

    @Override
    public CallableSnapshot<O, E> apply(final I input) {
      return new PrivateCallableSnapshot<>(
        () -> valueFactory.create(input),
        exceptionHandler
      );
    }

  }

  private static class PrivateCallableSnapshot<V, E extends Exception>
    extends CallableSnapshot<V, E> {
    private PrivateCallableSnapshot(
      Callable<V> callable,
      ExceptionHandler<E> exceptionHandler
    ) {
      super(callable, exceptionHandler);
    }
  }
}
