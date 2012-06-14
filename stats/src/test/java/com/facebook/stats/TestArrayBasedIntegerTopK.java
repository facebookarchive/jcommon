package com.facebook.stats;

public class TestArrayBasedIntegerTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance() {
    return new ArrayBasedIntegerTopK(10, 3);
  }
}
