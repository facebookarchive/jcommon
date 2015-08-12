package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
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
  private CapturingEvictionCallback evictionCallback;
  private LinkedListKeyWrapper linkedListKeyWrapper;
  private LinkedListValueWrapper linkedListValueWrapper;
  private BucketAccessor bucketAccessor;
  private int numberOfBuckets;
  private CacheEntryAccessor cacheEntryFactory;
  private LinkedListBucketAccessor bucketAccessorNoCache;
  private EvictionFunction evictionFunction;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int numSlabs = 10;
    int slabSizeBytes = Sizes.MB.ov(16);
    int tlSlabSizeBytes = Sizes.MB.ov(1);
    ManagedSlabFactory managedSlabFactory = new ManagedSlabFactory();
    SlabPool slabPool = ShardedSlabPool.create(managedSlabFactory, numSlabs, slabSizeBytes);
    ShardedSlab shardedSlab = new ShardedSlab(slabPool, ThreadLocalThenMostFreePolicy::new);

    slab = new ThreadLocalSlab(tlSlabSizeBytes, shardedSlab);

    MemoryViewController memoryViewController = new MemoryViewController(MemoryView32.factory(), slab);

    linkedListKeyWrapper = new LinkedListKeyWrapper(
      OffHeapByteArrayImpl::wrap,
      OffHeapByteArrayImpl::wrap
    );
    linkedListValueWrapper = new LinkedListValueWrapper(
      OffHeapByteArrayImpl::wrap,
      OffHeapByteArrayImpl::wrap
    );

    SizedOffHeapWrapper keyWrapper = OffHeapByteArrayImpl::wrap;
    SizedOffHeapWrapper valueWrapper = OffHeapByteArrayImpl::wrap;

    bucketAccessor = new LinkedListBucketAccessor(
      slab, memoryViewController, keyWrapper, valueWrapper, new LinkedListBucketNodeAccessorWithLruCache()
    );
    bucketAccessorNoCache = new LinkedListBucketAccessor(
      slab, memoryViewController, keyWrapper, valueWrapper, new LinkedListBucketNodeAccessorNoCache()
    );
    shouldEvict = new AtomicBoolean(false);
    evictionFunction = new EvictionFunction() {
      @Override
      public boolean shouldEvictOnNewEntry(
        SizedOffHeapStructure key, SizedOffHeapStructure value, OffHeapMap<?, ?> offHeapMap
      ) {
        return shouldEvict.get();
      }

      @Override
      public boolean shouldEvictOnUpdate(
        SizedOffHeapStructure oldValue, SizedOffHeapStructure newValue, OffHeapMap<?, ?> offHeapMap
      ) {
        return shouldEvict.get();
      }

      @Override
      public boolean shouldEvict(OffHeapMap<?, ?> offHeapMap) {
        return shouldEvict.get();
      }
    };
    cacheEntryFactory = new LinkedListNodeCacheEntryAccessor();
    cachePolicy = new LruCachePolicy(cacheEntryFactory);
    evictionCallback = new CapturingEvictionCallback();
    numberOfBuckets = 1024;
    byteArrayMap = new OffHeapByteArrayHashMap(
      numberOfBuckets,
      linkedListKeyWrapper,
      linkedListValueWrapper,
      bucketAccessor,
      cachePolicy,
      evictionFunction,
      evictionCallback
    );
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

  @Test
  public void testContainsKey() throws Exception {
    Assert.assertFalse(byteArrayMap.containsKey(key1));
    byteArrayMap.put(key1, value1);
    Assert.assertTrue(byteArrayMap.containsKey(key1));
    Assert.assertFalse(byteArrayMap.containsKey(key2));
    Assert.assertFalse(byteArrayMap.containsKey(key3));
  }

  @Test
  public void testRemove() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertTrue(byteArrayMap.containsKey(key1));
    byteArrayMap.remove(key1);
    Assert.assertFalse(byteArrayMap.containsKey(key1));
  }

  @Test
  public void testGet() throws Exception {
    byteArrayMap.put(key1, value1);
    ByteArray readValue1 = byteArrayMap.get(key1);
    Assert.assertEquals(readValue1, value1);
  }

  @Test
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
    Assert.assertEquals(byteArrayMap.get(wrappedKey1), value1);
    Assert.assertEquals(byteArrayMap.get(wrappedKey2), value2);
  }

  @Test
  public void testLruPolicy() throws Exception {
    byteArrayMap.put(key1, value1);
    byteArrayMap.put(key2, value2);
    shouldEvict.set(true);
    byteArrayMap.get(key1);
    Assert.assertTrue(byteArrayMap.get(key2) == null);
    Assert.assertTrue(byteArrayMap.get(key1) != null);
  }

  @Test
  public void testLength() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertEquals(byteArrayMap.getLength(), 1);
    byteArrayMap.put(key2, value2);
    Assert.assertEquals(byteArrayMap.getLength(), 2);
  }

  @Test
  public void testOverWrite() throws Exception {
    byteArrayMap.put(key1, value1);
    Assert.assertEquals(byteArrayMap.getLength(), 1);
    Assert.assertEquals(byteArrayMap.get(key1), value1);
    byteArrayMap.put(key1, value2);
    Assert.assertEquals(byteArrayMap.getLength(), 1);
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

    Assert.assertEquals(byteArrayMap.getLength(), 1);
    Assert.assertEquals(byteArrayMap.get(key3), value3);
  }

  @Test
  public void testSizeTrackingAddOnly() throws Exception {
    int expectedSize = 0;
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    byteArrayMap.put(key1, value1);
    expectedSize += key1.getSize() + value1.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    byteArrayMap.put(key2, value2);
    expectedSize += key2.getSize() + value2.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    byteArrayMap.put(key3, value3);
    expectedSize += key3.getSize() + value3.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
  }

  @Test
  public void testSizeTrackingAddExpireReplaceRemove() throws Exception {
    int expectedSize = 0;
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    // add key/value1
    byteArrayMap.put(key1, value1);
    expectedSize += key1.getSize() + value1.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    // add key2/value2
    byteArrayMap.put(key2, value2);
    expectedSize += key2.getSize() + value2.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    // replace key1/value1 with key1/value3
    byteArrayMap.put(key1, value3);
    expectedSize += value3.getSize() - value1.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    // this triggers eviction of key2/value2
    shouldEvict.set(true);
    byteArrayMap.get(key2); // read any key will trigger removal of key2
    expectedSize -= key1.getSize() + value3.getSize();
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
    shouldEvict.set(false);
    byteArrayMap.remove(key2);
    expectedSize -= key2.getSize() + value2.getSize();
    Assert.assertEquals(expectedSize, 0);
    Assert.assertEquals(byteArrayMap.getSize(), expectedSize);
  }

  @Test
  public void testEvictionCallback() throws Exception {
    byteArrayMap.put(key1, value1);
    byteArrayMap.put(key2, value2);
    shouldEvict.set(true);
    byteArrayMap.get(key1);
    shouldEvict.set(false);

    Assert.assertTrue(byteArrayMap.get(key2) == null);
    Assert.assertTrue(byteArrayMap.get(key1) != null);

    List<SizedOffHeapStructure> keyList = evictionCallback.getKeyList();
    List<SizedOffHeapStructure> valueList = evictionCallback.getValueList();

    Assert.assertEquals(keyList.size(), 1);
    Assert.assertEquals(valueList.size(), 1);
    Assert.assertEquals(keyList.get(0), key2);
    Assert.assertEquals(valueList.get(0), value2);
  }

  @Test
  public void testNoCachePolicy() throws Exception {
    // this creates a map that never evicts any entries
    OffHeapByteArrayHashMap offHeapMap = new OffHeapByteArrayHashMap(
      numberOfBuckets,
      linkedListKeyWrapper,
      linkedListValueWrapper,
      bucketAccessorNoCache,
      new NoOpCachePolicy(),
      new AlwaysKeepEvictionFunction(),
      evictionCallback
    );

    offHeapMap.put(key1, value1);
    offHeapMap.put(key2, value2);
    offHeapMap.put(key3, value3);
    Assert.assertEquals(offHeapMap.get(key1), value1);
    Assert.assertEquals(offHeapMap.get(key2), value2);
    Assert.assertEquals(offHeapMap.get(key3), value3);

    List<SizedOffHeapStructure> keyList = evictionCallback.getKeyList();
    List<SizedOffHeapStructure> valueList = evictionCallback.getValueList();

    Assert.assertEquals(keyList.size(), 0);
    Assert.assertEquals(valueList.size(), 0);
  }

  @Test
  public void testSizedBasedEviction() throws Exception {
    int totalSize =
      key1.getSize() + key2.getSize() + key3.getSize() + value1.getSize() + value2.getSize() + value3.getSize();
    int maxSize = totalSize - value3.getSize();
    // ^^^^ => eviction will happen when we try to add the 3rd item
    LruCachePolicy cachePolicy = new LruCachePolicy(cacheEntryFactory);
    OffHeapByteArrayHashMap offHeapMap = new OffHeapByteArrayHashMap(
      numberOfBuckets,
      linkedListKeyWrapper,
      linkedListValueWrapper,
      bucketAccessor,
      cachePolicy,
      new MaxMapSizeEvictionFunction(maxSize),
      evictionCallback
    );

    int currentSize = 0;

    offHeapMap.put(key1, value1);
    currentSize += key1.getSize() + value1.getSize();
    Assert.assertEquals(offHeapMap.getSize(), currentSize);
    offHeapMap.put(key2, value2);
    currentSize += key2.getSize() + value2.getSize();
    Assert.assertEquals(offHeapMap.getSize(), currentSize);
    // this will push us over the maxSize, and the oldest kvp, key1/value, will be evicted
    offHeapMap.put(key3, value3);
    // adjust expected size by adding kvp3 and removing kvp1
    currentSize += key3.getSize() + value3.getSize();
    currentSize -= key1.getSize() + value1.getSize();
    Assert.assertEquals(offHeapMap.getSize(), currentSize);
    // and make sure key1 is evicted
    Assert.assertNull(offHeapMap.get(key1));

    List<SizedOffHeapStructure> keyList = evictionCallback.getKeyList();
    List<SizedOffHeapStructure> valueList = evictionCallback.getValueList();

    Assert.assertEquals(keyList.size(), 1);
    Assert.assertEquals(valueList.size(), 1);
    Assert.assertEquals(keyList.get(0), key1);
    Assert.assertEquals(valueList.get(0), value1);
    // and key2 + key2 have values
    Assert.assertNotNull(offHeapMap.get(key2));
    Assert.assertNotNull(offHeapMap.get(key3));

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

  private static class CapturingEvictionCallback implements EvictionCallback {
    private final List<SizedOffHeapStructure> keyList = new ArrayList<>();
    private final List<SizedOffHeapStructure> valueList = new ArrayList<>();

    @Override
    public void keyEvicted(SizedOffHeapStructure key) { keyList.add(key); }

    @Override
    public void valueEvicted(SizedOffHeapStructure value) { valueList.add(value); }

    public List<SizedOffHeapStructure> getKeyList() { return keyList; }

    public List<SizedOffHeapStructure> getValueList() { return valueList; }
  }
}
