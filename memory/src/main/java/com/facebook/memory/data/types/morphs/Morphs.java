package com.facebook.memory.data.types.morphs;

import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class Morphs {
  public static final int ADDRESS_SIZE = UnsafeAccessor.get().addressSize();

  private Morphs() {
    throw new AssertionError();
  }
}
