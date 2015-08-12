package com.facebook.memory.data.structures;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class TestFixedSizeOffHeapByteArray {
  private static final Unsafe unsafe = UnsafeAccessor.get();
  private long storedAddress;
  private FixedSizeOffHeapByteArray byteArray;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int storedSize = 256;

    storedAddress = unsafe.allocateMemory(storedSize);
    byteArray = FixedSizeOffHeapByteArray.wrap(storedAddress, storedSize);
  }

  @Test
  public void testReadWrite() throws Exception {

  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    if (storedAddress != 0) {
      unsafe.freeMemory(storedAddress);
    }
  }
}
