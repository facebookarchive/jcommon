package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Set;

import com.facebook.collections.heaps.IntRange;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.Sizes;
import com.facebook.memory.data.structures.freelists.FreeList;
import com.facebook.memory.data.structures.freelists.TreeSetFreeList;

public class TestTreeSetFreeList {
  private static final int BLOCK_SIZE = 64;
  private FreeList freeList;
  private int rangeSizeBytes;
  private int rangeEnd;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    rangeSizeBytes = Sizes.MB.ov(64);
    rangeEnd = rangeSizeBytes - 1;
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
  public void testAllocateAndFreeLoop() throws Exception {
    int totalToAllocate = Sizes.MB.ov(128);
    FreeList freeList = new TreeSetFreeList(totalToAllocate);
    int allocated = 0;
    int currentRound = Sizes.MB.ov(1);
    int allocationSize = Sizes.KB.ov(10);

    while (allocated + allocationSize < totalToAllocate) {
      freeList.allocate(allocationSize);
      allocated += allocationSize;
      currentRound -= allocationSize;

      // this introduces what should be a non-overlapping free() call so that the total allocated should not change
      if (currentRound < 0) {
        int toFree = -currentRound;
        freeList.free(allocated - toFree, toFree);
        freeList.allocate(toFree);
      }
    }

    Assert.assertEquals(allocated, totalToAllocate - freeList.getSize());
  }

  @Test
  public void testExtend() throws Exception {
    freeList.extend(Sizes.MB.ov(64));

    Assert.assertEquals(freeList.allocate(Sizes.MB.ov(128)), 0);
  }

  @Test
  public void testEqualSizeAdds() throws Exception {
    FreeList freeList = new TreeSetFreeList(300);
    // once had a bug where comparator didn't break size ties in a TreeSet => 2 equally sized ranges were not allowed
    freeList.allocate(100);
    freeList.allocate(100);
    freeList.allocate(100);
    freeList.free(0, 100);
    freeList.free(200, 100);

    Assert.assertEquals(freeList.getSize(), 200);
  }

  @Test
  public void testFree() throws Exception {
    int sizeBytes = 1024;
    int offset = freeList.allocate(sizeBytes);

    freeList.free(offset, sizeBytes);

    Assert.assertEquals(freeList.asRangeSet().size(), 1);

    IntRange range = freeList.asRangeSet().iterator().next();

    Assert.assertEquals(range.getSize(), rangeSizeBytes);
  }

  @Test
  public void testAllocateAll() throws Exception {
    freeList.allocate(rangeSizeBytes);

    Assert.assertEquals(freeList.asRangeSet().size(), 0);
    Assert.assertEquals(freeList.getSize(), 0);
  }

  @Test
  public void testAllocateFailAllocate() throws Exception {
    Assert.assertTrue(freeList.allocate(rangeSizeBytes - 100) >= 0);

    try {
      freeList.allocate(rangeSizeBytes - 200);
    } catch (FailedAllocationException e) {
      // this tests that the 100 bytes left are still available after a failure
      Assert.assertTrue(freeList.allocate(100) >= 0);

      return;
    }

    Assert.fail("expected an exception");
  }
}
