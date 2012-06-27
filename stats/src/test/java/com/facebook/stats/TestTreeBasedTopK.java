package com.facebook.stats;

public class TestTreeBasedTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new TreeBasedTopK(k);
  }
}
