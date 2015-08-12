package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.Sizes;
import com.facebook.testing.ThreadHelper;
import com.facebook.util.TimeUtil;

public class TestThreadLocalSlab {

  private ThreadLocalSlab tlab1;
  private Slab baseSlab1;
  private ThreadHelper threadHelper;
  private ThreadLocalSlab tlab2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ManagedSlabFactory();

    baseSlab1 = slabFactory.create(Sizes.MB.ov(128));
    tlab1 = new ThreadLocalSlab(Sizes.MB.ov(64), baseSlab1);
    tlab2 = new ThreadLocalSlab(Sizes.MB.ov(1), baseSlab1);
    threadHelper = new ThreadHelper();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    baseSlab1.freeSlab();
  }

  @Test
  public void testAllocate() throws Exception {
    tlab1.allocate(Sizes.KB.ov(10));
    Assert.assertEquals(tlab1.getUsed(), Sizes.KB.ov(10));
    Assert.assertEquals(tlab1.getFree(), baseSlab1.getSize() - Sizes.KB.ov(10));
  }

  @Test
  public void testTwoThreads() throws Exception {
    Runnable pureAllocationTask = () -> {
      try {
        long address = tlab1.allocate(Sizes.MB.ov(1));
      } catch (FailedAllocationException e) {
        throw new RuntimeException(e);
      }
    };
    // allocate this tlab
    pureAllocationTask.run();
    Assert.assertEquals(baseSlab1.getFree(), Sizes.MB.ov(64));
    // allocate one other thread
    threadHelper.doInThread(pureAllocationTask::run).join();
    // base slab should be full
    Assert.assertEquals(baseSlab1.getFree(), 0);
    // but our tlab should have 63MB
    Assert.assertEquals(tlab1.getFree(), Sizes.MB.ov(63));
  }

  @Test
  public void testSingleThread() throws Exception {
    // tests swapping in of a new buffer when the current is exhausted
    Assert.assertTrue(tlab1.allocate(Sizes.MB.ov(63)) > 0);
    Assert.assertTrue(tlab1.allocate(Sizes.MB.ov(1)) > 0);
    Assert.assertEquals(baseSlab1.getFree(), Sizes.MB.ov(64));
    Assert.assertEquals(tlab1.getFree(), Sizes.MB.ov(64));
    Assert.assertTrue(tlab1.allocate(Sizes.MB.ov(1)) > 0);
    Assert.assertEquals(tlab1.getFree(), Sizes.MB.ov(63));
    Assert.assertEquals(baseSlab1.getFree(), 0);
  }

  @Test
  public void testNoRefreshAtBoundary() throws Exception {
    // allocates 1 MB from backing and 512 from that;
    // returns 512 KB
    // then allocates 1 MB from backing
    tlab2.allocate(Sizes.KB.ov(512));
    tlab2.allocate(Sizes.KB.ov(512));
    Assert.assertEquals(baseSlab1.getUsed(), Sizes.MB.ov(1));
    Assert.assertEquals(tlab2.getUsed(), Sizes.MB.ov(1));
  }

  @Test
  public void testRefresh() throws Exception {
    // allocates 1 MB from backing and 512 from that;
    // returns 512 KB
    // then allocates 1 MB from backing
    tlab2.allocate(Sizes.KB.ov(512));
    tlab2.allocate(Sizes.KB.ov(513));
    Assert.assertEquals(baseSlab1.getUsed(), Sizes.MB.ov(1) + Sizes.KB.ov(512)); // 1.5 KB
    // the tlab perspective only sees 1 MB used
    Assert.assertEquals(tlab2.getUsed(), Sizes.MB.ov(1) + Sizes.KB.ov(1));
  }

  @Test
  public void testDirectToBackingSlab() throws Exception {
    long address = tlab2.allocate(Sizes.MB.ov(96));

    Assert.assertTrue(address > 0);
    // size of allocation only since tlab is lazily allocated
    Assert.assertEquals(baseSlab1.getUsed(), Sizes.MB.ov(96));
  }

  @Test
  public void testSpeed() throws Exception {
    int size = Sizes.KB.ov(10);
    int numAlloc = TimeUtil.logElapsedTime(
      "tlab.allocate loop",
      () ->
      {
        int count = 0;
        while (tlab2.getFree() > size) {
          tlab2.allocate(size);
          count++;
        }

        return count;
      }
    );

    TimeUtil.logElapsedTime(
      "new loop",
      () -> {
        for (int i = 0; i < numAlloc; i++) {
          byte[] barr = new byte[size];
        }
      }
    );
  }
}
