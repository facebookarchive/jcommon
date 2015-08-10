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
import com.facebook.memory.slabs.ThreadLocalThenMostFreePolicy;
import com.facebook.memory.views.MemoryView;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;

public class TestLruCachePolicy {

  private LruCachePolicy cachePolicy;
  private MemoryViewController viewController;
  private MemoryView data2;
  private MemoryView data1;
  private ShardedSlab slab;
  private AtomicBoolean shouldEvict;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 64 * 1024 * 1024;
    ManagedSlabFactory slabFactory = new ManagedSlabFactory();
    ShardedSlabPool slabPool = ShardedSlabPool.create(slabFactory, 16, slabSizeBytes);
    slab = new ShardedSlab( slabPool, new ThreadLocalThenMostFreePolicy(slabPool));
    shouldEvict = new AtomicBoolean(false);

    viewController = new MemoryViewController(MemoryView32.factory(), slab);
    OffHeapCacheEntryFactory cacheEntryFactory = new OffHeapCacheEntryFactory(slab);
    cachePolicy = new LruCachePolicy( cacheEntryFactory, viewController, shouldEvict::get);

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
    CacheAction cacheAction1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemove1() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction2.getCachePolicyKey());

    Assert.assertEquals(cacheAction3.getTokenToEvict(), data1.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemove2() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction1.getCachePolicyKey());

    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndRemoveAllEntries() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());
    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction1.getCachePolicyKey());
    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());
    CacheAction cacheAction4 = cachePolicy.removeEntry(cacheAction2.getCachePolicyKey());

    Assert.assertEquals(cacheAction4.getTokenToEvict(), MemoryConstants.NO_ADDRESS);
  }

  @Test(groups = {"fast", "local"})
  public void testAddAndUpdate() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1);
    CacheAction cacheAction2 = cachePolicy.addEntry(data2);
    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());
    CacheAction cacheAction3 = cachePolicy.updateEntry(cacheAction1.getCachePolicyKey());
    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());
    CacheAction cacheAction4 = cachePolicy.updateEntry(cacheAction2.getCachePolicyKey());
    Assert.assertEquals(cacheAction4.getTokenToEvict(), data1.getAddress());
  }
}
