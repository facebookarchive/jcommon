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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.testng.Assert;

public class ConcurrentCacheTestHelper<K, V> {
  private final ConcurrentCache<K, V, RuntimeException> cache;
  private final List<Throwable> exceptionList = Collections.synchronizedList(new ArrayList<>());

  public ConcurrentCacheTestHelper(ConcurrentCache<K, V, RuntimeException> cache) {
    this.cache = cache;
  }

  Thread clearInThread() {
    return doInThread(
        new Runnable() {
          @Override
          public void run() {
            cache.clear();
          }
        });
  }

  Thread getInThread(K key, V expectedValue) {
    return doInThread(
        new Runnable() {
          @Override
          public void run() {
            // we should get the expected value on a cache-miss
            Assert.assertEquals(cache.get(key), expectedValue);
          }
        });
  }

  Thread removeInThread(K key, V expectedValue) {
    return doInThread(
        new Runnable() {
          @Override
          public void run() {
            // we should get the expected value on a cache-miss
            Assert.assertEquals(cache.remove(key), expectedValue);
          }
        });
  }

  Thread doInThread(Runnable operation) {
    Thread t = new Thread(operation);

    // need to make sure we propagate the exception
    t.setUncaughtExceptionHandler(
        new Thread.UncaughtExceptionHandler() {
          @Override
          public void uncaughtException(Thread t, Throwable e) {
            exceptionList.add(e);
          }
        });
    t.start();

    return t;
  }

  List<Throwable> getExceptionList() {
    return exceptionList;
  }
}
