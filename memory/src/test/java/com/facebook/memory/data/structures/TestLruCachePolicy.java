package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.MemoryConstants;
import com.facebook.memory.slabs.ManagedSlabFactory;
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
  private LruCachePolicy cachePolicyOverlaidEntries;
  private LinkedListBucketNode node1;
  private LinkedListBucketNode node2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 64 * 1024 * 1024;
    ManagedSlabFactory slabFactory = new ManagedSlabFactory();
    ShardedSlabPool slabPool = ShardedSlabPool.create(slabFactory, 16, slabSizeBytes);

    slab = new ShardedSlab(slabPool, ThreadLocalThenMostFreePolicy::new);
    viewController = new MemoryViewController(MemoryView32.factory(), slab);

    OffHeapCacheEntryAccessor cacheEntryFactory = new OffHeapCacheEntryAccessor(slab);

    cachePolicy = new LruCachePolicy(cacheEntryFactory);
    data1 = viewController.allocate(Long.BYTES);
    data1.putLong(0, 101L);
    data2 = viewController.allocate(Long.BYTES);
    data2.putLong(0, Long.MAX_VALUE - 101L);

    OffHeapByteArray key1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{1, 2, 3}, slab);
    OffHeapByteArray value1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{'a', 'b', 'c'}, slab);
    OffHeapByteArray key2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{4, 5, 6}, slab);
    OffHeapByteArray value2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{'d', 'e', 'f'}, slab);
    SizedOffHeapWrapper offHeapByteArrayWrapper = OffHeapByteArrayImpl::wrap;

    node1 = LinkedListBucketNode.create(
      slab,
      key1,
      value1,
      offHeapByteArrayWrapper,
      offHeapByteArrayWrapper
    );
    node2 = LinkedListBucketNode.create(
      slab,
      key2,
      value2,
      offHeapByteArrayWrapper,
      offHeapByteArrayWrapper
    );
    cachePolicyOverlaidEntries = new LruCachePolicy(new LinkedListNodeCacheEntryAccessor());
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testAdd() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2, false);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());
  }

  @Test
  public void testAddAndRemove1() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2, false);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction2.getCachePolicyKey(),false);

    Assert.assertEquals(cacheAction3.getTokenToEvict(), data1.getAddress());
  }

  @Test
  public void testAddAndRemove2() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2, false);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction1.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());
  }

  @Test
  public void testAddAndRemoveAllEntries() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction2 = cachePolicy.addEntry(data2, false);

    Assert.assertEquals(cacheAction2.getTokenToEvict(), data1.getAddress());
    CacheAction cacheAction3 = cachePolicy.removeEntry(cacheAction1.getCachePolicyKey(), false);
    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());
    CacheAction cacheAction4 = cachePolicy.removeEntry(cacheAction2.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction4.getTokenToEvict(), MemoryConstants.NO_ADDRESS);
  }

  @Test
  public void testAddAndUpdate() throws Exception {
    CacheAction cacheAction1 = cachePolicy.addEntry(data1, false);
    CacheAction cacheAction2 = cachePolicy.addEntry(data2, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), data1.getAddress());

    CacheAction cacheAction3 = cachePolicy.updateEntry(cacheAction1.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction3.getTokenToEvict(), data2.getAddress());

    CacheAction cacheAction4 = cachePolicy.updateEntry(cacheAction2.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction4.getTokenToEvict(), data1.getAddress());
  }

  @Test
  public void testOverlayLinkedList() throws Exception {
    // this this does basic sanity check that LruCachePolicy will work with other CacheEntryAccessor impls, in
    // particular the LinkedListNode
    CacheAction cacheAction1 = cachePolicyOverlaidEntries.addEntry(node1, false);
    CacheAction cacheAction2 = cachePolicyOverlaidEntries.addEntry(node2, false);

    Assert.assertEquals(cacheAction1.getTokenToEvict(), node1.getAddress());

    CacheAction cacheAction3 = cachePolicyOverlaidEntries.updateEntry(cacheAction1.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction3.getTokenToEvict(), node2.getAddress());

    CacheAction cacheAction4 = cachePolicyOverlaidEntries.updateEntry(cacheAction2.getCachePolicyKey(), false);

    Assert.assertEquals(cacheAction4.getTokenToEvict(), node1.getAddress());
  }
}
