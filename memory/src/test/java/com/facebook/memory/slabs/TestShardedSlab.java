package com.facebook.memory.slabs;

import com.beust.jcommander.internal.Lists;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.facebook.concurrency.ConcurrencyUtil;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.Sizes;

public class TestShardedSlab {

  private ShardedSlab slab;
  private int numThreads;
  private int allocationSize;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ManagedSlabFactory();
    numThreads = 512;
    allocationSize = Sizes.KB.ov(1);
    ShardedSlabPool slabPool = ShardedSlabPool.create(slabFactory, numThreads, Sizes.MB.ov(1));

    slab = new ShardedSlab(slabPool, ThreadLocalThenMostFreePolicy::new);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

//  @Test(groups = {"fast", "local"})
  public void testSpeed() throws Exception {
    List<Runnable> offHeap = Lists.newArrayList();
    AtomicReference<FailedAllocationException> error = new AtomicReference<>();
    for (int i = 0; i < numThreads; i++) {
      offHeap.add(
        () -> {
          for (int i1 = 0; i1 < Sizes.MB.ov(1); i1 += allocationSize) {
            try {
              Assert.assertTrue(slab.allocate(allocationSize) > 0);
            } catch (FailedAllocationException e) {
              Assert.fail(
                String.format(
                  "unable to allocate %d bytes in thread %s", allocationSize, Thread.currentThread().getId()
                )
              );
            }
          }
        }
      );
    }

    long offHeapTime = timeTasks(offHeap);
    List<Runnable> heapTasks = Lists.newArrayList();
//    for (int i = 0; i < numThreads; i++) {
//      heapTasks.add(
//        new Runnable() {
//          @Override
//          public void run() {
//            Object o = new Object();
//            Assert.assertTrue(o != null);
//          }
//        }
//      );
//    }
//    long heapTime = timeTasks(heapTasks);

//    System.err.println(String.format("%f ms vs %f ms", offHeapTime / 1000000.0, heapTime / 1000000.0));
    System.err.println(String.format("%f", offHeapTime / 1000000.0));
  }

  private long timeTasks(List<Runnable> tasks) {
    long start = System.nanoTime();
    ConcurrencyUtil.parallelRun(tasks, numThreads);
    long end = System.nanoTime();

    return end - start;
  }
}
