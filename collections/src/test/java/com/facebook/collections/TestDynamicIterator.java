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
package com.facebook.collections;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestDynamicIterator {

  private DynamicIterator<Integer> iterator;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    iterator = new DynamicIterator<Integer>(100);
  }

  @Test(groups = "fast")
  public void testAddSingle() throws InterruptedException {
    iterator.add(1);
    iterator.finish();

    assertTrue(iterator.hasNext());
    assertEquals(iterator.next().intValue(), 1);

    assertFalse(iterator.hasNext());
  }

  @Test(groups = "fast")
  public void testAddMultiple() throws InterruptedException {

    List<Integer> expected = asList(1, 2, 3, 4, 5);
    for (Integer value : expected) {
      iterator.add(value);
    }
    iterator.finish();

    for (int value : expected) {
      assertTrue(iterator.hasNext());
      assertEquals(iterator.next().intValue(), value);
    }

    assertFalse(iterator.hasNext());
  }

  @Test(groups = "fast")
  public void testInterleaved() throws InterruptedException {
    List<Integer> expected = asList(1, 2, 3, 4, 5);
    for (int value : expected) {
      iterator.add(value);
      assertTrue(iterator.hasNext());
      assertEquals(iterator.next().intValue(), value);
    }
    iterator.finish();
    assertFalse(iterator.hasNext());
  }

  @Test(
      groups = "fast",
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*already finished.*")
  public void testDoesNotAllowAddsAfterFinish() throws InterruptedException {
    iterator.add(1);
    iterator.add(2);
    iterator.finish();

    iterator.add(3);
  }

  @Test(groups = "fast")
  public void testMultipleWriters()
      throws ExecutionException, InterruptedException, TimeoutException {

    ExecutorService executor =
        Executors.newCachedThreadPool(new ThreadFactoryBuilder().setDaemon(true).build());

    Future<Set<Integer>> consumer =
        executor.submit(
            new Callable<Set<Integer>>() {
              public Set<Integer> call() throws Exception {
                return Sets.newHashSet(iterator);
              }
            });

    //noinspection unchecked
    executor.invokeAll(
        asList(producer(iterator, 0, 1, 2, 3, 4), producer(iterator, 5, 6, 7, 8, 9)),
        1,
        TimeUnit.SECONDS); // failsafe in case there's a deadlock

    iterator.finish();

    Set<Integer> result = consumer.get(1, TimeUnit.SECONDS); // failsafe in case there's a deadlock
    assertEquals(result, Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
  }

  @Test(groups = "fast", expectedExceptions = NoSuchElementException.class)
  public void testNoSuchElement() throws Exception {
    iterator.add(1);
    iterator.finish();
    iterator.next();
    iterator.next();
  }

  private Callable<Void> producer(final DynamicIterator<Integer> iterator, final int... values) {
    return new Callable<Void>() {
      public Void call() throws InterruptedException {
        for (final int value : values) {
          iterator.add(value);
        }
        return null;
      }
    };
  }
}
