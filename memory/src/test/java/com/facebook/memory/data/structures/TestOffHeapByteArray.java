package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.collections.bytearray.ByteArrays;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.Sizes;
import com.facebook.memory.slabs.ThreadLocalSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;

public class TestOffHeapByteArray {

  private ByteArray offHeapByteArray;
  private ByteArray heapByteBuffer;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ThreadLocalSlabFactory(Sizes.MB.ov(1), new ManagedSlabFactory());
    Slab slab = slabFactory.create(Sizes.MB.ov(128));
    byte[] baseBytes = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10};
    offHeapByteArray = OffHeapByteArrayImpl.fromHeapByteArray(baseBytes, slab);
    heapByteBuffer = ByteArrays.wrap(baseBytes);
  }

  @Test
  public void testEquals() throws Exception {
    Assert.assertEquals(offHeapByteArray, offHeapByteArray);
    Assert.assertEquals(offHeapByteArray, heapByteBuffer);
    Assert.assertEquals(heapByteBuffer, offHeapByteArray);
  }

  @Test
  public void testIterator() throws Exception {

  }
}
