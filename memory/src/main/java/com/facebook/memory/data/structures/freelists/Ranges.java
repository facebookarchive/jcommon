package com.facebook.memory.data.structures.freelists;

import com.facebook.collections.orderedset.IntRange;

public class Ranges {
  public static IntRange make(int lower, int upper) {
    return new IntRange(lower, upper);
  }
}
