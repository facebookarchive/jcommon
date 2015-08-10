package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import com.facebook.collections.ByteArray;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.ManagedSlabFactory;
import com.facebook.memory.Sizes;
import com.facebook.memory.ThreadLocalSlabFactory;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.slabs.SlabFactory;
import com.facebook.memory.views.MemoryView32;
import com.facebook.memory.views.MemoryViewController;

public class TestByteArrayHashMap {
  private ByteArrayHashMap byteArrayMap;
  private byte[] value3;
  private byte[] value2;
  private byte[] value1;
  private byte[] key3;
  private byte[] key2;
  private byte[] key1;
  private ByteArray wrappedValue1;
  private ByteArray wrappedValue2;
  private ByteArray wrappedValue3;
  private AtomicBoolean shouldEvict;
  private LruCachePolicy cachePolicy;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int slabSizeBytes = 128 * 1024 * 1024;
    MapBucketAccessor bucketAccessor = new MapBucketAccessor();
    SlabFactory slabFactory = new ThreadLocalSlabFactory(Sizes.MB.ov(1), new ManagedSlabFactory());

    slab = slabFactory.create(slabSizeBytes);

    MemoryViewController memoryViewController = new MemoryViewController(MemoryView32.factory(), slab);

    shouldEvict = new AtomicBoolean(false);

    OffHeapCacheEntryFactory cacheEntryFactory = new OffHeapCacheEntryFactory(slab) {
      @Override
      public OffHeapCacheEntry create(OffHeap entryAddress) throws FailedAllocationException {
        return super.create(entryAddress);
      }
    };

    cachePolicy = new LruCachePolicy(cacheEntryFactory, memoryViewController, shouldEvict::get);
    byteArrayMap = new ByteArrayHashMap(1024, bucketAccessor.createKeyAccessor(), bucketAccessor, cachePolicy);
    key1 = new byte[]{0, 1, 2};
    key2 = new byte[]{7, 8};
    key3 = new byte[]{10, 100};
    value1 = new byte[]{0, 0, 0, 101, 100};
    value2 = new byte[]{70};
    value3 = new byte[]{121, 123};
    wrappedValue1 = ByteArray.wrap(value1);
    wrappedValue2 = ByteArray.wrap(value2);
    wrappedValue3 = ByteArray.wrap(value3);
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
    byte[] readValue1 = byteArrayMap.get(key1);
    Assert.assertEquals(ByteArray.wrap(readValue1), ByteArray.wrap(value1));
  }

  @Test(groups = {"fast", "local"})
  public void testBucketCollision() throws Exception {
    ByteArray wrappedKey1 = new FixedHashCodeByteArray(ByteArray.wrap(key1), 23);
    ByteArray wrappedKey2 = new FixedHashCodeByteArray(ByteArray.wrap(key2), 23);

    byteArrayMap.put(wrappedKey1, value1);
    byteArrayMap.put(wrappedKey2, value2);

    Assert.assertEquals(ByteArray.wrap(byteArrayMap.get(wrappedKey1)), wrappedValue1);
    Assert.assertEquals(ByteArray.wrap(byteArrayMap.get(wrappedKey2)), wrappedValue2);
    Assert.assertTrue(byteArrayMap.getBucket(wrappedKey1) == byteArrayMap.getBucket(wrappedKey2));
  }

  @Test(groups = {"fast", "local"})
  public void testLruPolicy() throws Exception {
    byteArrayMap.put(key1, value1);
    shouldEvict.set(true);
    byteArrayMap.get(key1);
    Assert.assertTrue(byteArrayMap.get(key1) == null);
  }

  private static class FixedHashCodeByteArray extends ByteArray {
    private final ByteArray byteArray;
    private final int hashCode;

    private FixedHashCodeByteArray(ByteArray byteArray, int hashCode) {
      this.byteArray = byteArray;
      this.hashCode = hashCode;
    }

    @Override
    public byte[] getArray() {
      return byteArray.getArray();
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
