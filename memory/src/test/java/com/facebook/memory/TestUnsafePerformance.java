package com.facebook.memory;

import org.testng.Assert;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

public class TestUnsafePerformance {
  @Test(groups = "fast")
  public void testGetPutInt() throws Exception {
    Unsafe unsafe = UnsafeAccessor.get();

    long address = unsafe.allocateMemory(128 * 1024 * 1024);

    Assert.assertTrue(address > 0);

    try {
      int span = 120 * 1024 * 1024;

      for (int i = 0; i < span; i += Integer.BYTES) {
        unsafe.putInt(address + i, i % Integer.MAX_VALUE);
      }

      for (int i = 0; i < span; i += Integer.BYTES) {
        Assert.assertEquals(unsafe.getInt(address + i), i % Integer.MAX_VALUE);
      }
    } finally {
      unsafe.freeMemory(address);
    }

  }
}
