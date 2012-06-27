package com.facebook.stats;

public class TestArrayBasedIntegerTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new ArrayBasedIntegerTopK(keySpaceSize, k);
  }
}
