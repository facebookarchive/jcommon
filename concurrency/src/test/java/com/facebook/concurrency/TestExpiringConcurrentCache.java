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

import com.facebook.collections.Pair;
import com.facebook.testing.MockExecutor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.joda.time.DateTimeUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestExpiringConcurrentCache {
  private ExpiringConcurrentCache<String, ReapableString, RuntimeException> legacyCache;
  private static final String KEY = "key";
  private ReapableString reapableValue1;
  private String value1;
  private BlockingValueProducer<ReapableString, RuntimeException> producer;
  private ConcurrentCacheTestHelper<String, ReapableString> testHelper;
  private MockExecutor mockExecutor;
  private ExpiringConcurrentCache<String, String, RuntimeException> cache;
  private List<Pair<String, String>> evicted;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    value1 = "value1";
    reapableValue1 = new ReapableString(value1);
    producer = new BlockingValueProducer<>(reapableValue1);
    mockExecutor = new MockExecutor();
    // arbitrary time for now
    DateTimeUtils.setCurrentMillisFixed(0);
    legacyCache =
        ExpiringConcurrentCache.createWithReapableValue(
            new ValueFactory<String, ReapableString, RuntimeException>() {
              @Override
              public ReapableString create(String input) throws RuntimeException {
                return producer.call();
              }
            },
            30,
            TimeUnit.MILLISECONDS,
            RuntimeExceptionHandler.INSTANCE,
            mockExecutor);
    evicted = new ArrayList<>();
    cache =
        new ExpiringConcurrentCache<>(
            new ValueFactory<String, String, RuntimeException>() {
              @Override
              public String create(String input) throws RuntimeException {
                return producer.call().toString();
              }
            },
            30,
            TimeUnit.MILLISECONDS,
            new EvictionListener<String, String>() {
              @Override
              public void evicted(String key, String value) {
                evicted.add(new Pair<>(key, value));
              }
            },
            RuntimeExceptionHandler.INSTANCE,
            mockExecutor);
    testHelper = new ConcurrentCacheTestHelper<>(legacyCache);
  }

  @Test(groups = "fast")
  public void testExpiration() throws Exception {
    // add a value to the cache
    Assert.assertEquals(legacyCache.get(KEY), reapableValue1);
    Assert.assertEquals(producer.getCalledCount(), 1);
    // advance time to close to the expiration
    DateTimeUtils.setCurrentMillisFixed(29);
    // still in cache
    Assert.assertTrue(legacyCache.getIfPresent(KEY) != null, "key should be in cache");
    // now 30ms passed, should expire
    DateTimeUtils.setCurrentMillisFixed(30);
    Assert.assertFalse(legacyCache.getIfPresent(KEY) != null, "key should NOT be in cache");
    mockExecutor.drain();
    // and value1.shutdown was called
    Assert.assertEquals(reapableValue1.getShutdownCalled(), 1);
  }

  @Test(groups = "fast")
  public void testEvictionListener() throws Exception {
    // add a value to the cache
    Assert.assertEquals(cache.get(KEY), value1);
    Assert.assertEquals(producer.getCalledCount(), 1);
    DateTimeUtils.setCurrentMillisFixed(30);
    cache.prune();
    // key is removed from the cache
    Assert.assertEquals(cache.size(), 0);
    // and this will execute the eviction callback
    mockExecutor.drain();
    Assert.assertEquals(evicted.size(), 1);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    DateTimeUtils.setCurrentMillisSystem();
  }

  private static class ReapableString implements Reapable<RuntimeException> {
    private final String value;
    private final AtomicLong shutdownCalled = new AtomicLong(0);

    private ReapableString(String value) {
      this.value = value;
    }

    @Override
    public void shutdown() throws RuntimeException {
      shutdownCalled.incrementAndGet();
    }

    public long getShutdownCalled() {
      return shutdownCalled.get();
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }

      ReapableString that = (ReapableString) o;

      if (!Objects.equals(value, that.value)) {
        return false;
      }

      return true;
    }

    @Override
    public int hashCode() {
      return value != null ? value.hashCode() : 0;
    }

    @Override
    public String toString() {
      return value;
    }
  }
}
