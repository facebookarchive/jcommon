package com.facebook.stats;

public class TestTreeBasedIntegerTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance(int keySpaceSize, int k) {
    return new TreeBasedIntegerTopK(keySpaceSize, k);
  }

}
