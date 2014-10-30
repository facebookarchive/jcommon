package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.MemoryConstants;
import com.facebook.memory.slabs.ShardedSlab;
import com.facebook.memory.slabs.ShardedSlabPool;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;
import com.facebook.util.ExtCallable;

public class TestLruCachePolicy {

  private LruCachePolicy cachePolicy;
  private MemoryViewController viewController;
  private MemoryView data2;
  private MemoryView data1;
  private ShardedSlab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 64 * 1024 * 1024;
    ManagedSlabFactory slabFactory = new ManagedSlabFactory();
    slab = new ShardedSlab(ShardedSlabPool.create(slabFactory, 16, slabSizeBytes));
    final AtomicBoolean shouldEvict = new AtomicBoolean(false);

    viewController = new MemoryViewController(MemoryView32.factory(), slab);
    OffHeapCacheEntryFactory cacheEntryFactory = new OffHeapCacheEntryFactory(slab);
    cachePolicy = new LruCachePolicy(
      cacheEntryFactory, viewController, new ExtCallable<Boolean, RuntimeException>() {
      @Override
      public Boolean call() throws RuntimeException {
        return shouldEvict.get();
      }
    });

    data1 = viewController.allocate(Long.BYTES);

    data1.putLong(0, 101L);

    data2 = viewController.allocate(Long.BYTES);

    data2.putLong(0, Long.MAX_VALUE - 101L);

  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test(groups = {"fast", "local"})
  public void testAdd() throws Exception {
    cachePolicy.addEntry(data1);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
    cachePolicy.addEntry(data2);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemove1() throws Exception {
    cachePolicy.addEntry(data1);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());

    CachePolicyKey policyKey2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());

    cachePolicy.removeEntry(policyKey2);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemove2() throws Exception {
    CachePolicyKey policyKey1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());

    cachePolicy.addEntry(data2);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());

    cachePolicy.removeEntry(policyKey1);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data2.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemoveAllEntries() throws Exception {
    CachePolicyKey policyKey1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());

    CachePolicyKey policyKey2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
    cachePolicy.removeEntry(policyKey1);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data2.getAddress());
    cachePolicy.removeEntry(policyKey2);

    Assert.assertEquals(cachePolicy.getTokenToRemove(), MemoryConstants.NO_ADDRESS);
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndUpdate() throws Exception {
    CachePolicyKey policyKey1 = cachePolicy.addEntry(data1);
    CachePolicyKey policyKey2 = cachePolicy.addEntry(data2);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
    cachePolicy.updateEntry(policyKey1);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data2.getAddress());
    cachePolicy.updateEntry(policyKey2);
    Assert.assertEquals(cachePolicy.getTokenToRemove(), data1.getAddress());
  }
}
