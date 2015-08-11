package com.facebook.memory.data.types.definitions;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestSlots implements OffHeapStructure {
  private static final IntSlot INT_SLOT = new IntSlot();
  private static final LongSlot LONG_SLOT = new LongSlot();
  private static final PointerSlot POINTER_SLOT = new PointerSlot();
  private static final ByteArraySlot BYTE_ARRAY_SLOT = new ByteArraySlot();

  @Test
  public void testDeclarations() throws Exception {
    Assert.assertEquals(INT_SLOT.getOffset(), 0);
    Assert.assertEquals(LONG_SLOT.getOffset(), 4);
    Assert.assertEquals(POINTER_SLOT.getOffset(), 12);
    Assert.assertEquals(BYTE_ARRAY_SLOT.getOffset(), 20);
  }

  @Override
  public long getAddress() {
    return 0;
  }
}
