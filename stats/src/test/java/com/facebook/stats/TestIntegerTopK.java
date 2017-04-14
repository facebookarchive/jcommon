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
package com.facebook.stats;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import com.facebook.logging.Logger;
import com.facebook.stats.topk.TopK;

import static org.testng.Assert.assertEquals;

public abstract class TestIntegerTopK {
  // few hundred millis each test to keep tests short
  private static final long TEST_TIME_NANOS = TimeUnit.MILLISECONDS.toNanos(1250);
  private static Logger LOG;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    LOG = getLogger();
  }

  protected abstract TopK<Integer> getInstance(int keySpaceSize, int k);

  /**
   * defers logger creation so we log based on subclass name; may do efficient caching
   *
   * @return Logger
   */
  protected abstract Logger getLogger();

  @Test(groups = "fast")
  public void testTop3() {
    TopK<Integer> topK = getInstance(10, 3);

    assertTopK(topK);
    topK.add(1, 3);
    assertTopK(topK, 1);
    topK.add(2, 2);
    assertTopK(topK, 1, 2);
    topK.add(3, 8);
    assertTopK(topK, 3, 1, 2);
    topK.add(4, 1);
    assertTopK(topK, 3, 1, 2);
    topK.add(4, 3);
    assertTopK(topK, 3, 4, 1);
    topK.add(2, 3);
    assertTopK(topK, 3, 2, 4);
  }

  @Test(groups = "fast",
        expectedExceptions = IllegalArgumentException.class,
        expectedExceptionsMessageRegExp = "count to add must be non-negative, got -3")
  public void testAddNegative() {
    TopK<Integer> topK = getInstance(10, 3);
    topK.add(0, -3);
  }

  @Test(groups = "fast",
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "key can't be null")
  public void testNullKey() {
    TopK<Integer> topK = getInstance(10, 3);
    topK.add(null, 1);
  }

  @Test(groups = "slow")
  public void testInsertionTiming() {
    int keySpaceSize = 10000;
    int k = 100;
    int maxAdd = 100;
    TopK<Integer> topK = getInstance(keySpaceSize, k);

    LOG.info("Timing add() performance with keySpaceSize = %d, k = %d", keySpaceSize, k);

    Random random = new Random(0);
    long totalTime = 0;
    long count = 0;
    long begin = System.nanoTime();

    while (System.nanoTime() - begin < TEST_TIME_NANOS) {
      long start = System.nanoTime();

      topK.add(random.nextInt(keySpaceSize), random.nextInt(maxAdd));

      if (System.nanoTime() - begin > TimeUnit.SECONDS.toNanos(1)) {
        // discard the first second of measurements
        totalTime += System.nanoTime() - start;
        ++count;
      }
    }

    LOG.info(
      "Processed %d entries in %d ms. Insertion rate = %f entries/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );
  }

  @Test(groups = "slow")
  public void testRetrievalTiming() {
    int keySpaceSize = 10000;
    int k = 100;
    int maxAdd = 100;
    TopK<Integer> topK = getInstance(keySpaceSize, k);

    LOG.info("Timing getTopK() performance with keySpaceSize = %d, k = %d", keySpaceSize, k);

    Random random = new Random(0);
    long totalTime = 0;
    long count = 0;
    long begin = System.nanoTime();

    while (System.nanoTime() - begin < TEST_TIME_NANOS) {

      topK.add(random.nextInt(keySpaceSize), random.nextInt(maxAdd));

      long start = System.nanoTime();

      topK.getTopK();

      if (System.nanoTime() - begin > TimeUnit.SECONDS.toNanos(1)) {
        // discard the first second of measurements
        totalTime += System.nanoTime() - start;
        ++count;
      }
    }

    LOG.info(
      "Processed %d entries in %d ms. Retrieval rate = %f retrievals/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );
  }

  private static void assertTopK(TopK<Integer> topK, Integer... expected) {
    assertEquals(topK.getTopK(), Arrays.asList(expected));
  }
}
