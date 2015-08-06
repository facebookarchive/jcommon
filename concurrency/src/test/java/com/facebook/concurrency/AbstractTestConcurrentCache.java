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

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.facebook.testing.TestUtils.waitUntilThreadBlocks;

public abstract class AbstractTestConcurrentCache {
  private static final String STD_KEY = "std";
  private static final String BLOCKING_KEY = "blocking";
  private static final String EXCEPTION_KEY = "exception";
  private static final String VALUE = "value";
  private ConcurrentCacheTestHelper<String, String> testHelper;
  private BlockingValueProducer<String, RuntimeException> stdProducer;
  private BlockingValueProducer<String, RuntimeException> blockingProducer;
  private BlockingValueProducer<String, RuntimeException> exceptionThrowingProducer;
  private ConcurrentCache<String, String, RuntimeException> cache;

  protected abstract ConcurrentCache<String, String, RuntimeException> createCache(
    ValueFactory<String, String, RuntimeException> valueFactory
  );

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    stdProducer = new BlockingValueProducer<>(VALUE);
    blockingProducer = new BlockingValueProducer<>(VALUE, true);
    exceptionThrowingProducer =
      new BlockingValueProducer<>(
        VALUE, true, new RuntimeException("I take exception to this")
      );
    cache = createCache(
      input -> {
        switch (input) {
          case STD_KEY:
            return stdProducer.call();
          case BLOCKING_KEY:
            return blockingProducer.call();
          case EXCEPTION_KEY:
            return exceptionThrowingProducer.call();
          default:
            return input;
        }
      }
    );
    testHelper = new ConcurrentCacheTestHelper<>(cache);
  }

  @Test
  public void testProducerThrowsException() throws Exception {
    // exception should be seen by all who try to do a get
    Thread t1 = testHelper.getInThread(EXCEPTION_KEY, VALUE);
    Thread t2 = testHelper.getInThread(EXCEPTION_KEY, VALUE);

    exceptionThrowingProducer.signal();

    t1.join();
    t2.join();

    try {
      cache.get(EXCEPTION_KEY);
      Assert.fail("expected exception");
    } catch (RuntimeException e) {
      Assert.assertEquals(
        testHelper.getExceptionList().size(), 2, "all threads did not see exceptions"
      );
      Assert.assertTrue(cache.getIfPresent(EXCEPTION_KEY) != null, "task not inserted");
    }
  }

  /**
   * case that while a value is being produced for a key, a removal happens.
   * Since remove blocks on
   */
  @Test
  public void testConcurrentGetAndRemove() throws Exception {
    Thread getThread = testHelper.getInThread(BLOCKING_KEY, VALUE);
    // wait until the task is inserted
    waitUntilThreadBlocks(getThread);
    // now start the remove
    Thread removeThread = testHelper.removeInThread(BLOCKING_KEY, VALUE);
    waitUntilThreadBlocks(removeThread);
    // let both proceed
    blockingProducer.signal();
    getThread.join();
    removeThread.join();

    Assert.assertEquals(blockingProducer.getCalledCount(), 1);
    Assert.assertFalse(cache.getIfPresent(VALUE) != null, "key should not exist");
  }

  //  @Test
  public void testConcurrentGetAndClear() throws Exception {
    // TOOD: see if we can fix this test;  we need a way to block
    // *after* we do the value insert; or we can do various tryAcquire on
    //  locks...
    Thread getThread = testHelper.getInThread(BLOCKING_KEY, VALUE);
    // wait until the task is inserted, but no value is produced
    waitUntilThreadBlocks(getThread);
    // now clear
    Thread clearThread = testHelper.clearInThread();
    clearThread.join();
    blockingProducer.signal();
    // let get proceed
    getThread.join();
    getThread.join();

    Assert.assertEquals(blockingProducer.getCalledCount(), 1);
    Assert.assertFalse(cache.getIfPresent(BLOCKING_KEY) != null, "key should not exist");

  }

  @Test
  public void testIterator() throws Exception {
    // sanity check that our iterator works as expected
    cache.get("fuu");
    cache.get("bar");
    cache.get("baz");
    cache.get("wombat");

    int i = 0;

    for (Map.Entry<String, CallableSnapshot<String, RuntimeException>> entry :
      cache) {
      // the value producer used returns the key as the value
      Assert.assertEquals(entry.getKey(), entry.getValue().get());
      i++;
    }

    Assert.assertEquals(i, 4);
  }

  @Test
  public void testCacheFlow() throws Exception {
    // not called yet
    Assert.assertEquals(stdProducer.getCalledCount(), 0);
    // we should get the expected value on a cache-miss
    Assert.assertEquals(cache.get(STD_KEY), VALUE);
    // and see 1 call to the factory
    Assert.assertEquals(stdProducer.getCalledCount(), 1);
    // now we have a cache hit
    Assert.assertEquals(cache.get(STD_KEY), VALUE);
    // that does not invoke the factory again 
    Assert.assertEquals(stdProducer.getCalledCount(), 1);
    // now remove the cache entry
    Assert.assertEquals(cache.remove(STD_KEY), VALUE);
    // produce again
    Assert.assertEquals(cache.get(STD_KEY), VALUE);
    // results in 2nd call
    Assert.assertEquals(stdProducer.getCalledCount(), 2);
  }

  /**
   * produces two threads doing get() at the same time.  Waits for both
   * to block and then asserts we only saw one factory.call()
   */
  @Test
  public void testConcurrentCacheHit() throws Throwable {
    // run each get in a separate thread
    Thread t1 = testHelper.getInThread(STD_KEY, VALUE);
    Thread t2 = testHelper.getInThread(STD_KEY, VALUE);

    // wait for both threads to block or terminate
    waitUntilThreadBlocks(t1);
    waitUntilThreadBlocks(t2);

    // signal the value factory to let one continue
    stdProducer.signal();

    //wait for threads
    t1.join();
    t2.join();

    // did either thread throw an exception?
    if (!testHelper.getExceptionList().isEmpty()) {
      throw testHelper.getExceptionList().get(0);
    }

    // only 1 call
    Assert.assertEquals(stdProducer.getCalledCount(), 1);
  }

  @Test
  public void testRemoveIfError() throws Exception {
    // allow the cache.get() to proceed below
    exceptionThrowingProducer.signal();
    try {
      cache.get(EXCEPTION_KEY);
      Assert.fail("expected exception");
    } catch (RuntimeException e) {
      // expected
    }
    // now call removeIfError() twice, with the first one   
    final AtomicInteger removeCount = new AtomicInteger(0);
    Runnable operation = () -> {
      if (cache.removeIfError(EXCEPTION_KEY)) {
        removeCount.incrementAndGet();
      }
    };
    // this really tests that get(error), removeIfError(), get(success), 
    // removeIfError() won't remove the successful get
    Thread t1 = testHelper.doInThread(operation);
    Thread t2 = testHelper.getInThread(BLOCKING_KEY, VALUE);
    waitUntilThreadBlocks(t1);
    waitUntilThreadBlocks(t2);
    blockingProducer.signal();
    // now there is a valid value, and this should not remove it  
    Thread t3 = testHelper.doInThread(operation);
    waitUntilThreadBlocks(t3);
    // should see only one remove
    Assert.assertEquals(removeCount.get(), 1);
  }

  @Test
  public void testRemoveBeforeValueSwap() throws Exception {
    // Initiate a value fetch
    Thread t1 = testHelper.getInThread(BLOCKING_KEY, VALUE);
    waitUntilThreadBlocks(t1);

    // Remove the cached value producer before it is swapped with its value
    cache.clear();

    // Now allow the original thread to finish producing the value and to do the
    // value swap
    blockingProducer.signal();
    t1.join();

    // There should not be a value present
    Assert.assertNull(cache.getIfPresent(BLOCKING_KEY));
    Assert.assertEquals(blockingProducer.getCalledCount(), 1);
  }
}
