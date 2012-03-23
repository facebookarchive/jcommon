package com.facebook.memory;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeAccessor {
  private static Unsafe UNSAFE = null;

  static {
    try {
      Field field = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
      field.setAccessible(true);
      UnsafeAccessor.UNSAFE = (sun.misc.Unsafe) field.get(null);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  public static Unsafe get() {
    return UnsafeAccessor.UNSAFE;
  }
}