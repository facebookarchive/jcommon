package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.Sizes;

public class TestAllocateOnlySlab {

  private RawSlab rawSlab;
  private AllocateOnlySlab slab;
  private int slabSizeBytes;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    slabSizeBytes = Sizes.MB.ov(64);

    rawSlab = new OffHeapSlab(slabSizeBytes);
    slab = new AllocateOnlySlab(rawSlab.getBaseAddress(), rawSlab, slabSizeBytes);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    rawSlab.freeSlab();
  }

  @Test
  public void testTryAllocateTooBig() throws Exception {
    Allocation allocation = slab.tryAllocate(slabSizeBytes + 1);

    Assert.assertEquals(allocation.getAddress(), slab.getBaseAddress());
    Assert.assertEquals(allocation.getSize(), slab.getSize());
  }
}
