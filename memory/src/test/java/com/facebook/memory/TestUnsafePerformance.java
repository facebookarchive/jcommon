package com.facebook.memory;

import org.testng.annotations.Test;
import sun.misc.Unsafe;

import com.facebook.util.TimeUtil;

public class TestUnsafePerformance {
  @Test(groups = "fast")
  public void testGetPutInt() throws Exception {
    Unsafe unsafe = UnsafeAccessor.get();

    long address = unsafe.allocateMemory(512 * 1024 * 1024);
    int span = 100 * 1024 * 1024;

    for (int i = 0; i < span; i += Integer.BYTES) {
      unsafe.putInt(address + i, i % Integer.MAX_VALUE);
    }

    for (int i = 0; i < span; i += Integer.BYTES) {
      unsafe.getInt(address + i, i % Integer.MAX_VALUE);
    }

    unsafe.freeMemory(address);
  }
}
