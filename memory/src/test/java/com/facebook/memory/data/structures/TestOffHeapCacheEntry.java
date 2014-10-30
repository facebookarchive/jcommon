package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;

public class TestOffHeapCacheEntry {

  private OffHeapCacheEntry cacheEntry;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 128 * 1024 * 1024;
    SlabFactory slabFactory = new ManagedSlabFactory();
    slab = slabFactory.create(slabSizeBytes);
    cacheEntry = OffHeapCacheEntry.allocate(slab);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test(groups = {"fast", "local"})
  public void testSanity() throws Exception {
    cacheEntry.setDataPointer(500)
      .setPrevious(99)
      .setNext(101);

    Assert.assertEquals(cacheEntry.getPrevious(), 99);
    Assert.assertEquals(cacheEntry.getNext(), 101);
    Assert.assertEquals(cacheEntry.getDataPointer(), 500);
  }
}
