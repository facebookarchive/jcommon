package com.facebook.stats;

public class TestTreeBasedIntegerTopK extends TestIntegerTopK {
  protected TopK<Integer> getInstance() {
    return new TreeBasedIntegerTopK(10, 3);
  }
}
