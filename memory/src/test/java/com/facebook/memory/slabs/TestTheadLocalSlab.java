package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.Sizes;
import com.facebook.testing.ThreadHelper;
import com.facebook.util.TimeUtil;

public class TestTheadLocalSlab {

  private ThreadLocalSlab tlab1;
  private Slab baseSlab1;
  private ThreadHelper threadHelper;
  private Slab baseSlab2;
  private ThreadLocalSlab tlab2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ManagedSlabFactory();

    baseSlab1 = slabFactory.create(Sizes.MB.ov(128));
    tlab1 = new ThreadLocalSlab(Sizes.MB.ov(64), baseSlab1);
    baseSlab2 = slabFactory.create(Sizes.GB.ov(1));
    tlab2 = new ThreadLocalSlab(Sizes.MB.ov(1), baseSlab1);
    threadHelper = new ThreadHelper();
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
  public void testSpeed() throws Exception {
    TimeUtil.logElapsedTime(
      "tlab.allocate loop",
      () ->
      {
        while (tlab1.getFree() > Sizes.KB.ov(1)) {
          tlab1.allocate(Sizes.KB.ov(1));
        }
      }
    );
    TimeUtil.logElapsedTime(
      "new loop",
      () -> {
        for (int i = 0; i < 1024 * 1024; i++) {
          byte[] barr = new byte[1024];
        }
      }
    );
  }
}
