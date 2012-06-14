package com.facebook.stats;

public class TestTreeBasedTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance() {
    return new TreeBasedTopK(3);
  }
}
