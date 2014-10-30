package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sun.misc.Unsafe;

import java.util.ArrayList;
import java.util.List;

import com.facebook.memory.AllocationContext;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.UnsafeAccessor;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;

public class TestShardedSlabPool {
  private static final Unsafe UNSAFE = UnsafeAccessor.get();
  private ShardedSlabPool slabPool;
  private int chunkSize;
  private int numChunks;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int numSlabs = 33;
    int slabSizeBytes = Slabs.toIntBytes("32mb");
    long totalSizeBytes = numSlabs * (long)slabSizeBytes;

    slabPool = ShardedSlabPool.create(Slabs.managedSlabFactory(), numSlabs, slabSizeBytes);
    chunkSize = 256; //4 * 1024;
    numChunks = 1024 * 1024;

    long totalViewBytes = chunkSize * (long)numChunks;
//    Assert.assertTrue(
//      totalViewBytes < totalSizeBytes,
//      String.format( "view bytes [%d] > slab bytes [%d]", totalViewBytes, totalSizeBytes)
//    );
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slabPool.freeSlabPool();
  }

  @Test(groups = "fast")
  public void testAllocate() throws Exception {
    List<MemoryView> memoryViewList = allocateMemoryViewList(numChunks);

    Assert.assertEquals(memoryViewList.size(), numChunks);
  }

  @Test(groups = "fast")
  public void testAllocateLongAndPutGet() throws Exception {
    List<MemoryView> memoryViewList = allocateMemoryViewList(numChunks);

    for (MemoryView memoryView : memoryViewList) {
      Assert.assertEquals(memoryView.getSize(), chunkSize);

      for (int i = 0; i < chunkSize; i += Long.BYTES) {
        memoryView.putLong(i, i);
      }

      for (int i = 0; i < chunkSize; i += Long.BYTES) {
        Assert.assertEquals(memoryView.getLong(i), i);
      }
    }

  }

  @Test(groups = "fast")
  public void testAllocateIntAndPutGet() throws Exception {
    List<MemoryView> memoryViewList = allocateMemoryViewList(numChunks);
    for (MemoryView memoryView : memoryViewList) {
      Assert.assertEquals(memoryView.getSize(), chunkSize);

      for (int i = 0; i < chunkSize; i += Integer.BYTES) {
        memoryView.putInt(i, i);
      }

      for (int i = 0; i < chunkSize; i += Integer.BYTES) {
        Assert.assertEquals(memoryView.getInt(i), i);
      }
    }
  }

  @Test(groups = "fast")
  public void testAllocationToShard() throws Exception {
    List<MemoryView> memoryViewList = allocateMemoryViewList(numChunks);

    Assert.assertEquals(memoryViewList.size(), numChunks);

    for (MemoryView memoryView : memoryViewList) {

      // TODO: how to test what shard each memoryview is on?
    }
  }

  private List<MemoryView> allocateMemoryViewList(int count) throws FailedAllocationException {
    List<MemoryView> memoryViewList = new ArrayList<>(count);

    for (int i = 0; i < count; i++) {
      Slab slab = slabPool.getSlab(new AllocationContext(i));
      MemoryView memoryView = new MemoryViewController(MemoryView32.factory(), slab).allocate(chunkSize);

      memoryViewList.add(memoryView);
    }

    return memoryViewList;
  }
}
