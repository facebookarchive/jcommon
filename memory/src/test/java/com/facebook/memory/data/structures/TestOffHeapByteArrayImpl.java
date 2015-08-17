package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.Sizes;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;
import com.facebook.memory.slabs.ThreadLocalSlabFactory;

public class TestOffHeapByteArrayImpl {

  private Slab slab;
  private OffHeapByteArray offHeapByteArray;
  private int arrayLength;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    arrayLength = 32;
    SlabFactory slabFactory = new ThreadLocalSlabFactory(Sizes.MB.ov(1), new ManagedSlabFactory());

    slab = slabFactory.create(Sizes.MB.ov(32));
    offHeapByteArray = OffHeapByteArrayImpl.allocate(arrayLength, slab);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testLength() throws Exception {
    Assert.assertEquals(offHeapByteArray.getLength(), arrayLength);
  }

  @Test
  public void testReadWrite() throws Exception {
    for (int i = 0; i < arrayLength; i++) {
      offHeapByteArray.putAdjusted(i, (byte) i);
    }

    for (int i = 0; i < arrayLength; i++) {
      Assert.assertEquals(offHeapByteArray.getAdjusted(i), (byte)i);
    }
  }
}
