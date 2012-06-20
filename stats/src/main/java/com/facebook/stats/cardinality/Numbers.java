package com.facebook.stats.cardinality;

class Numbers {
  public static boolean isPowerOf2(long value) {
    return (value & value - 1) == 0;
  }
}
