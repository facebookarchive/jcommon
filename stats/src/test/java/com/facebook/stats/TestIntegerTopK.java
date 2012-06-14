package com.facebook.stats;

import java.util.Arrays;

import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

public abstract class TestIntegerTopK {
  protected abstract TopK<Integer> getInstance();

  @Test(groups = "fast")
  public void testTop3() {
    TopK<Integer> topK = getInstance();
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
    TopK<Integer> topK = getInstance();
    topK.add(0, -3);
  }

  @Test(groups = "fast",
        expectedExceptions = NullPointerException.class,
        expectedExceptionsMessageRegExp = "key can't be null")
  public void testNullKey() {
    TopK<Integer> topK = getInstance();
    topK.add(null, 1);
  }

  private static void assertTopK(TopK<Integer> topK, Integer... expected) {
    assertEquals(topK.getTopK(), Arrays.asList(expected));
  }
}
