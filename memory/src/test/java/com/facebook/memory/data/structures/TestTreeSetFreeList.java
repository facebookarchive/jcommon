package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import com.facebook.memory.Sizes;

public class TestTreeSetFreeList {
  private static final int BLOCK_SIZE = 64;
  private FreeList freeList;
  private int rangeSizeBytes;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    rangeSizeBytes = Sizes.MB.ov(64);
    freeList = new TreeSetFreeList(rangeSizeBytes);
  }

  @Test(groups = "fast")
  public void testAllocate() throws Exception {
    Assert.assertEquals(freeList.allocate(100), 0);
    Assert.assertEquals(freeList.allocate(100), 100);
    Assert.assertEquals(freeList.allocate(500), 200);
    Assert.assertEquals(freeList.allocate(1024), 700);
    Assert.assertEquals(freeList.allocate(1024), 1724);
  }

  @Test
  public void testBoundaryAllocation() throws Exception {
    Assert.assertEquals(freeList.allocate(Sizes.MB.ov(32)), 0);
    Assert.assertEquals(freeList.allocate(Sizes.MB.ov(32)), Sizes.MB.ov(32));
  }

  @Test(groups = "fast")
  public void testAllocateAndFreeContiguous() throws Exception {
    FreeList freeList = new TreeSetFreeList(1024 * 1024 * 1024);
    IntRange startRange = freeList.asRangeSet().iterator().next();
    int numIters = 100;

    for (int i = 0; i < numIters; i++) {
      long expectedAddress = (i * BLOCK_SIZE);
      Assert.assertEquals(freeList.allocate(BLOCK_SIZE), expectedAddress);
    }

    for (int i = 0; i < numIters; i++) {
      int address = (i * BLOCK_SIZE);
      freeList.free(address, BLOCK_SIZE);

      if (i < numIters - 1) {
        // {x, y}, {z, end}
        Assert.assertEquals(freeList.asRangeSet().size(), 2);
      } else {
        // {0, end} -- all merged
        Assert.assertEquals(freeList.asRangeSet().size(), 1);
      }
    }

    Set<IntRange> finalRanges = freeList.asRangeSet();
    Assert.assertEquals(finalRanges.iterator().next(), startRange);
  }

  @Test
  public void testExtend() throws Exception {
    freeList.extend(Sizes.MB.ov(64));

    Assert.assertEquals(freeList.allocate(Sizes.MB.ov(128)), 0);
  }

  @Test
  public void testFree() throws Exception {
    int sizeBytes = 1024;
    int offset = freeList.allocate(sizeBytes);

    freeList.free(offset, sizeBytes);

    Assert.assertEquals(freeList.asRangeSet().size(), 1);

    IntRange range = freeList.asRangeSet().iterator().next();

    Assert.assertEquals(range.size(), rangeSizeBytes);
  }
}
