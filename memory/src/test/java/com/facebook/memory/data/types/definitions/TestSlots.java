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
  private static final PointerSlot POINTER_SLOT_1 = new PointerSlot();
  private static final PointerSlot POINTER_SLOT_2 = new PointerSlot();
  private static final ByteArraySlot BYTE_ARRAY_SLOT_1 = new ByteArraySlot();
  private static final ByteArraySlot BYTE_ARRAY_SLOT_2 = new ByteArraySlot();

  private long address;
  private IntAccessor intAccessor;
  private PointerAccessor pointerAccessor1;
  private PointerAccessor pointerAccessor2;
  private ByteArrayAccessor byteArrayAccessor1;
  private ByteArrayAccessor byteArrayAccessor2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    address = UNSAFE.allocateMemory(512);
    intAccessor = INT_SLOT.accessor(address);
    pointerAccessor1 = POINTER_SLOT_1.accessor(intAccessor);
    pointerAccessor2 = POINTER_SLOT_2.accessor(pointerAccessor1);
    byteArrayAccessor1 = BYTE_ARRAY_SLOT_1.create(pointerAccessor2, 32);
    byteArrayAccessor2 = BYTE_ARRAY_SLOT_2.create(byteArrayAccessor1, 32);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    UNSAFE.freeMemory(address);
  }

  @Test
  public void testOurDeclarations() throws Exception {
    Assert.assertEquals(intAccessor.getSlotOffset(), 0);
    Assert.assertEquals(pointerAccessor1.getSlotOffset(), 4);
    Assert.assertEquals(pointerAccessor2.getSlotOffset(), 12);
    Assert.assertEquals(byteArrayAccessor1.getSlotOffset(), 20);
    Assert.assertEquals(byteArrayAccessor2.getSlotOffset(), 56);
  }

  @Override
  public long getAddress() {
    return 0;
  }
}
