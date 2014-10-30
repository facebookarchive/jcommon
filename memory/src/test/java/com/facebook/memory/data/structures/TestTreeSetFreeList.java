package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Set;

import com.facebook.memory.data.structures.FreeList;
import com.facebook.memory.data.structures.IntRange;
import com.facebook.memory.data.structures.TreeSetFreeList;

public class TestTreeSetFreeList {
  private static final int BLOCK_SIZE = 64;

  @Test(groups = "fast")
  public void testAllocate() throws Exception {
    FreeList freeList = new TreeSetFreeList(0L, 1024 * 1024 * 1024);

    Assert.assertEquals(freeList.allocate(100), 0);
    Assert.assertEquals(freeList.allocate(100), 100);
    Assert.assertEquals(freeList.allocate(500), 200);
    Assert.assertEquals(freeList.allocate(1024), 700);
    Assert.assertEquals(freeList.allocate(1024), 1724);
  }

  @Test(groups = "fast")
  public void testAllocateAndFreeContiguous() throws Exception {
    FreeList freeList = new TreeSetFreeList(0L, 1024 * 1024 * 1024);
    IntRange startRange = freeList.asRangeSet().iterator().next();
    int numIters = 100;

    for (int i = 0; i < numIters; i++) {
      Assert.assertEquals(freeList.allocate(BLOCK_SIZE), i * BLOCK_SIZE);
    }

    for (int i = 0; i < numIters; i++) {
      freeList.free(i * BLOCK_SIZE, BLOCK_SIZE);

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
}