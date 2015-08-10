package com.facebook.memory.data.structures;

public class Ranges {
  public static IntRange make(int lower, int upper) {
    return new IntRange(lower, upper);
  }
}
