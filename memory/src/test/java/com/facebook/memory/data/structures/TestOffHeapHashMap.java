package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.memory.Sizes;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.slabs.ShardedSlab;
import com.facebook.memory.slabs.ShardedSlabPool;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabPool;
import com.facebook.memory.slabs.ThreadLocalSlab;
import com.facebook.memory.slabs.ThreadLocalThenMostFreePolicy;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;

public class TestOffHeapHashMap {
  private OffHeapByteArrayHashMap byteArrayMap;
  private OffHeapByteArray value3;
  private OffHeapByteArray value2;
  private OffHeapByteArray value1;
  private OffHeapByteArray key3;
  private OffHeapByteArray key2;
  private OffHeapByteArray key1;
  private AtomicBoolean shouldEvict;
  private LruCachePolicy cachePolicy;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int numSlabs = 10;
    int slabSizeBytes = Sizes.MB.ov(16);
    int tlSlabSizeBytes = Sizes.MB.ov(1);
//    MapBucketAccessor bucketAccessor = new MapBucketAccessor();
    ManagedSlabFactory managedSlabFactory = new ManagedSlabFactory();
    SlabPool slabPool = ShardedSlabPool.create(managedSlabFactory, numSlabs, slabSizeBytes);
    ShardedSlab shardedSlab = new ShardedSlab(slabPool, ThreadLocalThenMostFreePolicy::new);

    slab = new ThreadLocalSlab(tlSlabSizeBytes, shardedSlab);

    MemoryViewController memoryViewController = new MemoryViewController(MemoryView32.factory(), slab);
    LinkedListKeyWrapper linkedListKeyWrapper = new LinkedListKeyWrapper(OffHeapByteArrayImpl::wrap, OffHeapByteArrayImpl::wrap);
    SizedOffHeapWrapper keyWrapper = OffHeapByteArrayImpl::wrap;
    SizedOffHeapWrapper valueWrapper = OffHeapByteArrayImpl::wrap;
    BucketAccessor bucketAccessor = new LinkedListBucketAccessor(
      slab, memoryViewController, keyWrapper, valueWrapper
    );

    shouldEvict = new AtomicBoolean(false);

    CacheEntryAccessor cacheEntryFactory = new LinkedListNodeCacheEntryAccessor();
//    CacheEntryAccessor cacheEntryFactory = new OffHeapCacheEntryAccessor(slab);
    cachePolicy = new LruCachePolicy(cacheEntryFactory, shouldEvict::get);
    byteArrayMap = new OffHeapByteArrayHashMap(1024, linkedListKeyWrapper, bucketAccessor, cachePolicy);
    key1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 1, 2}, slab);
    key2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{7, 8}, slab);
    key3 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{10, 100}, slab);
    value1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 0, 0, 101, 100}, slab);
    value2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{70}, slab);
    value3 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{121, 123}, slab);
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test(groups = {"fast", "local"})
  public void testContainsKey() throws Exception {
    Assert.assertFalse(byteArrayMap.containsKey(key1));
    byteArrayMap.put(key1, value1);
    Assert.assertTrue(byteArrayMap.containsKey(key1));
    Assert.assertFalse(byteArrayMap.containsKey(key2));
    Assert.assertFalse(byteArrayMap.containsKey(key3));
  }

  @Test(groups = {"fast", "local"})
  public void testRemove() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertTrue(byteArrayMap.containsKey(key1));
    byteArrayMap.remove(key1);
    Assert.assertFalse(byteArrayMap.containsKey(key1));
  }

  @Test(groups = {"fast", "local"})
  public void testGet() throws Exception {
    byteArrayMap.put(key1, value1);
    ByteArray readValue1 = byteArrayMap.get(key1);
    Assert.assertEquals(readValue1, value1);
  }

  @Test(groups = {"fast", "local"})
  public void testBucketCollision() throws Exception {
    OffHeapByteArray wrappedKey1 = new FixedHashCodeByteArray(key1, 23);
    OffHeapByteArray wrappedKey2 = new FixedHashCodeByteArray(key2, 23);

    byteArrayMap.put(wrappedKey1, value1);
    byteArrayMap.put(wrappedKey2, value2);
    Assert.assertEquals(byteArrayMap.get(wrappedKey1), value1);
    Assert.assertEquals(byteArrayMap.get(wrappedKey2), value2);

    Bucket bucket1 = byteArrayMap.getBucket(wrappedKey1);
    Bucket bucket2 = byteArrayMap.getBucket(wrappedKey2);

    Assert.assertEquals(bucket1, bucket2);
  }

  @Test(groups = {"fast", "local"})
  public void testLruPolicy() throws Exception {
    byteArrayMap.put(key1, value1);
    shouldEvict.set(true);
    byteArrayMap.get(key1);
    Assert.assertTrue(byteArrayMap.get(key1) == null);
  }

  @Test
  public void testSize() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertEquals(byteArrayMap.getSize(), 1);
    byteArrayMap.put(key2, value2);
    Assert.assertEquals(byteArrayMap.getSize(), 2);
  }

  @Test
  public void testOverWrite() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertEquals(byteArrayMap.getSize(), 1);
    Assert.assertEquals(byteArrayMap.get(key1), value1);
    byteArrayMap.put(key1, value2);
    Assert.assertEquals(byteArrayMap.getSize(), 1);
    Assert.assertEquals(byteArrayMap.get(key1), value2);
  }

  @Test
  public void testRemoveAffectsCachePolicy() throws Exception {
    byteArrayMap.put(key1, value1);
    byteArrayMap.put(key2, value2);
    byteArrayMap.put(key3, value3);

    // this will cause the oldest element to be removed after removal
    shouldEvict.set(true);
    byteArrayMap.remove(key2);

    Assert.assertEquals(byteArrayMap.getSize(), 1);
    Assert.assertEquals(byteArrayMap.get(key3), value3);
  }

  private static class FixedHashCodeByteArray extends OffHeapByteArrayImpl implements SizedOffHeapStructure {
    private final int hashCode;

    private FixedHashCodeByteArray(OffHeapByteArray offHeapByteArray, int hashCode) {
      super(offHeapByteArray.getAddress(), offHeapByteArray.getLength());
      this.hashCode = hashCode;
    }

    // override hashCode() and not equals on purpose; for testing we want to force hash collisions when keys are *not*
    // equal
    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
