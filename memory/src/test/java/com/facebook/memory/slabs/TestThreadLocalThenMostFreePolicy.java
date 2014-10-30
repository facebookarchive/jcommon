package com.facebook.memory.slabs;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.Sizes;

public class TestThreadLocalThenMostFreePolicy {

  private ThreadLocalThenMostFreePolicy policy;
  private ShardedSlabPool slabPool;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    SlabFactory slabFactory = new ManagedSlabFactory();
    slabPool = ShardedSlabPool.create(slabFactory, 2, Sizes.MB.ov(3));
    policy = new ThreadLocalThenMostFreePolicy(slabPool);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slabPool.freeSlabPool();
  }

  @Test(groups = {"fast", "local"})
  public void testAllocation() throws Exception {
    long address = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(2)));

    Assert.assertTrue(address > 0);
  }

  @Test(groups = {"fast", "local"})
  public void testAllocationMax() throws Exception {
    long address = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(3)));

    Assert.assertTrue(address > 0);
  }

  @Test(groups = {"fast", "local"})
  public void testAllocateAcrossSlabs1() throws Exception {
    long address1 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(2)));
    long address2 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(2)));

    Assert.assertTrue(address1 > 0);
    Assert.assertTrue(address2 > 0);
  }

  @Test(groups = {"fast", "local"})
  public void testAllocateAcrossSlabsMax() throws Exception {
    long address1 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(2)));
    long address2 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(2)));
    long address3 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(1)));
    long address4 = policy.allocate(new SizeSlabAllocationFunction(Sizes.MB.ov(1)));

    Assert.assertTrue(address1 > 0);
    Assert.assertTrue(address2 > 0);
    Assert.assertTrue(address3 > 0);
    Assert.assertTrue(address4 > 0);
  }
}
