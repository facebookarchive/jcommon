package com.facebook.stats;

import com.facebook.logging.Logger;
import com.facebook.logging.LoggerImpl;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public abstract class TestIntegerTopK {
  protected abstract TopK<Integer> getInstance(int keySpaceSize, int k);

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
    final int keySpaceSize = 10000;
    final int k = 100;
    final int maxAdd = 100;
    TopK<Integer> topK = getInstance(keySpaceSize, k);

    log().info("Timing add() performance with keySpaceSize = %s, k = %s", keySpaceSize, k);

    Random random = new Random();

    long totalTime = 0;
    long count = 0;
    long begin = System.nanoTime();
    while (System.nanoTime() - begin < TimeUnit.SECONDS.toNanos(5)) {
      long start = System.nanoTime();
      topK.add(random.nextInt(keySpaceSize), random.nextInt(maxAdd));

      if (System.nanoTime() - begin > TimeUnit.SECONDS.toNanos(1)) {
        // discard the first second of measurements
        totalTime += System.nanoTime() - start;
        ++count;
      }
    }

    log().info(
      "Processed %s entries in %s ms. Insertion rate = %s entries/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );
  }

  @Test(groups = "slow")
  public void testRetrievalTiming() {
    final int keySpaceSize = 10000;
    final int k = 100;
    final int maxAdd = 100;
    TopK<Integer> topK = getInstance(keySpaceSize, k);

    log().info("Timing getTopK() performance with keySpaceSize = %s, k = %s", keySpaceSize, k);

    Random random = new Random();

    long totalTime = 0;
    long count = 0;
    long begin = System.nanoTime();
    while (System.nanoTime() - begin < TimeUnit.SECONDS.toNanos(5)) {
      long start = System.nanoTime();
      topK.add(random.nextInt(keySpaceSize), random.nextInt(maxAdd));
      topK.getTopK();

      if (System.nanoTime() - begin > TimeUnit.SECONDS.toNanos(1)) {
        // discard the first second of measurements
        totalTime += System.nanoTime() - start;
        ++count;
      }
    }

    log().info(
      "Processed %s entries in %s ms. Retrieval rate = %s retrievals/s",
      count,
      TimeUnit.NANOSECONDS.toMillis(totalTime),
      count / (totalTime * 1.0 / TimeUnit.SECONDS.toNanos(1))
    );
  }

  private static void assertTopK(TopK<Integer> topK, Integer... expected) {
    assertEquals(topK.getTopK(), Arrays.asList(expected));
  }

  private Logger log() {
    return LoggerImpl.getLogger(getClass());
  }
}
