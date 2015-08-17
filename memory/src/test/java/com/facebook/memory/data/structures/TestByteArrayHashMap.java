package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import com.facebook.collections.bytearray.AbstractByteArray;
import com.facebook.collections.bytearray.ByteArray;
import com.facebook.collections.bytearray.ByteArrays;
import com.facebook.memory.Sizes;
import com.facebook.memory.slabs.ManagedSlabFactory;
import com.facebook.memory.slabs.ShardedSlab;
import com.facebook.memory.slabs.ShardedSlabPool;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabPool;
import com.facebook.memory.slabs.ThreadLocalSlab;
import com.facebook.memory.slabs.ThreadLocalThenMostFreePolicy;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;

public class TestByteArrayHashMap {
  private ByteArrayHashMap byteArrayMap;
  private ByteArray value3;
  private ByteArray value2;
  private ByteArray value1;
  private ByteArray key3;
  private ByteArray key2;
  private ByteArray key1;
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
    BucketAccessor bucketAccessor = new LinkedListBucketAccessor(memoryViewController, slab);
    OffHeapByteArrayWrapper keyWrapper = new LinkedListKeyWrapper();

    shouldEvict = new AtomicBoolean(false);

    CacheEntryAccessor cacheEntryFactory = new LinkedListNodeCacheEntryAccessor();
//    CacheEntryAccessor cacheEntryFactory = new OffHeapCacheEntryAccessor(slab);
    cachePolicy = new LruCachePolicy(cacheEntryFactory, shouldEvict::get);
    byteArrayMap = new ByteArrayHashMap(1024, keyWrapper, bucketAccessor, cachePolicy);
    key1 = ByteArrays.wrap(new byte[]{0, 1, 2});
    key2 = ByteArrays.wrap(new byte[]{7, 8});
    key3 = ByteArrays.wrap(new byte[]{10, 100});
    value1 = ByteArrays.wrap(new byte[]{0, 0, 0, 101, 100});
    value2 = ByteArrays.wrap(new byte[]{70});
    value3 = ByteArrays.wrap(new byte[]{121, 123});
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
    ByteArray wrappedKey1 = new FixedHashCodeByteArray(key1, 23);
    ByteArray wrappedKey2 = new FixedHashCodeByteArray(key2, 23);

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

  private static class FixedHashCodeByteArray extends AbstractByteArray{
    private final ByteArray byteArray;
    private final int hashCode;

    private FixedHashCodeByteArray(ByteArray byteArray, int hashCode) {
      this.byteArray = byteArray;
      this.hashCode = hashCode;
    }

    @Override
    public int getLength() {
      return byteArray.getLength();
    }

    @Override
    public byte getAdjusted(int pos) {
      return byteArray.getAdjusted(pos);
    }

    @Override
    public void putAdjusted(int pos, byte b) {
      byteArray.putAdjusted(pos, b);
    }

    @Override
    public boolean isNull() {
      return byteArray.isNull();
    }

    @Override
    public int compareTo(ByteArray o) {
      return byteArray.compareTo(o);
    }

    // override hashCode() and not equals on purpose; for testing we want to force hash collisions when keys are *not*
    // equal
    @Override
    public int hashCode() {
      return hashCode;
    }
  }
}
