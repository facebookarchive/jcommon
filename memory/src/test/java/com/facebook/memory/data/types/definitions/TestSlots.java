package com.facebook.memory.data.types.definitions;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

import com.facebook.memory.UnsafeAccessor;

public class TestSlots implements OffHeapStructure {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();

  private static final IntSlot INT_SLOT = new IntSlot();
  private static final LongSlot LONG_SLOT = new LongSlot();
  private static final PointerSlot POINTER_SLOT = new PointerSlot();
  private static final ByteArraySlot BYTE_ARRAY_SLOT_1 = new ByteArraySlot();
  private static final ByteArraySlot BYTE_ARRAY_SLOT_2 = new ByteArraySlot();

  private long address;
  private IntAccessor intAccessor;
  private LongAccessor longAccessor;
  private AbstractSlotAccessor pointerAccessor;
  private ByteArraySlotAccessor byteArraySlotAccessor1;
  private ByteArraySlotAccessor byteArraySlotAccessor2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    address = UNSAFE.allocateMemory(512);
    ByteArraySlotAccessor accessor = BYTE_ARRAY_SLOT_1.accessor(address);
    accessor.putLength(32);
    intAccessor = INT_SLOT.accessor(address);
    longAccessor = LONG_SLOT.accessor(intAccessor);
    pointerAccessor = POINTER_SLOT.accessor(longAccessor);
    byteArraySlotAccessor1 = BYTE_ARRAY_SLOT_1.accessor(pointerAccessor);
    byteArraySlotAccessor2 = BYTE_ARRAY_SLOT_2.accessor(byteArraySlotAccessor1);
  }

  @Test
  public void testDeclarations() throws Exception {
    Assert.assertEquals(intAccessor.getSlotOffset(), 0);
    Assert.assertEquals(longAccessor.getSlotOffset(), 4);
    Assert.assertEquals(pointerAccessor.getSlotOffset(), 12);
    Assert.assertEquals(byteArraySlotAccessor1.getSlotOffset(), 20);
    Assert.assertEquals(byteArraySlotAccessor2.getSlotOffset(), 56);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    UNSAFE.freeMemory(address);
  }

  @Override
  public long getAddress() {
    return 0;
  }
}
