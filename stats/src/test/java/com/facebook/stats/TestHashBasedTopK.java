package com.facebook.stats;

public class TestHashBasedTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance() {
    return new HashBasedTopK(3);
  }
}
