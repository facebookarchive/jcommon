package com.facebook.memory.data.types.definitions;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class TestFieldType {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();
  private long address;
  private int storedSize;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    storedSize = 128;
    address = UNSAFE.allocateMemory(16);
    UNSAFE.putInt(address, storedSize);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    UNSAFE.freeMemory(address);
  }

  @Test
  public void testDeclartions() throws Exception {
    Assert.assertEquals(SlotType.INT.getSlotSizeFunction().getSize(address), Integer.BYTES);
    Assert.assertEquals(SlotType.ADDRESS.getSlotSizeFunction().getSize(address), UNSAFE.addressSize());
    Assert.assertEquals(SlotType.BYTE_ARRAY.getSlotSizeFunction().getSize(address), storedSize + Integer.BYTES);
  }
}
