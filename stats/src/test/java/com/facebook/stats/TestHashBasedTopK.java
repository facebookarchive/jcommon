package com.facebook.stats;

public class TestHashBasedTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new HashBasedTopK(k);
  }
}
