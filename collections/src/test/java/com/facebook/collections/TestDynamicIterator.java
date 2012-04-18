package com.facebook.collections;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class TestDynamicIterator {
  @Test
  public void testAddSingle() throws InterruptedException {
    DynamicIterator<Integer> iterator = new DynamicIterator<Integer>();
    iterator.add(1);
    iterator.finish();

    assertTrue(iterator.hasNext());
    assertEquals(iterator.next().intValue(), 1);

    assertFalse(iterator.hasNext());
  }

  @Test
  public void testAddMultiple() throws InterruptedException {
    DynamicIterator<Integer> iterator = new DynamicIterator<Integer>();

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

  @Test
  public void testInterleaved() throws InterruptedException {
    DynamicIterator<Integer> iterator = new DynamicIterator<Integer>(100);

    List<Integer> expected = asList(1, 2, 3, 4, 5);
    for (int value : expected) {
      iterator.add(value);
      assertTrue(iterator.hasNext());
      assertEquals(iterator.next().intValue(), value);
    }
    iterator.finish();
    assertFalse(iterator.hasNext());
  }

  @Test(expectedExceptions = IllegalStateException.class,
        expectedExceptionsMessageRegExp = ".*already finished.*")
  public void testDoesNotAllowAddsAfterFinish() throws InterruptedException {
    DynamicIterator<Integer> iterator = new DynamicIterator<Integer>(100);

    iterator.add(1);
    iterator.add(2);
    iterator.finish();

    iterator.add(3);
  }

  @Test
  public void testMultipleWriters()
    throws ExecutionException, InterruptedException, TimeoutException {
    final DynamicIterator<Integer> iterator = new DynamicIterator<Integer>(100);

    ExecutorService executor = Executors.newCachedThreadPool(
      new ThreadFactoryBuilder().setDaemon(true).build());

    Future<Set<Integer>> consumer = executor.submit(
      new Callable<Set<Integer>>() {
        public Set<Integer> call() throws Exception {
          return Sets.newHashSet(iterator);
        }
      }
    );

    //noinspection unchecked
    executor.invokeAll(asList(
      producer(iterator, 0, 1, 2, 3, 4),
      producer(iterator, 5, 6, 7, 8, 9)
    ), 1, TimeUnit.SECONDS); // failsafe in case there's a deadlock

    iterator.finish();

    Set<Integer> result = consumer.get(1, TimeUnit.SECONDS);// failsafe in case there's a deadlock
    assertEquals(result, Sets.newHashSet(0, 1, 2, 3, 4, 5, 6, 7, 8, 9));
  }

  private Callable<Void> producer(final DynamicIterator<Integer> iterator, final int... values)
  {
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
