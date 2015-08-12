package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView32;

public class TestLinkedListBucket {
  private SizedOffHeapStructure value2;
  private SizedOffHeapStructure value1;
  private SizedOffHeapStructure key2;
  private SizedOffHeapStructure key1;
  private SizedOffHeapStructure key3;

  private int size = 1024 * 100;
  private Slab slab;
  private MemoryView32.Factory memoryViewFactory;
  private SizedOffHeapWrapper keyWrapper;
  private SizedOffHeapWrapper valueWrapper;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);

    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);
    key1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, slab);
    key2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{7, 8, 9, 10, 11}, slab);
    key3 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{1, 2, 3, 4, 5, 6}, slab);
    value1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 0, 0, 101, 100, 21, 22}, slab);
    value2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20}, slab);
    memoryViewFactory = MemoryView32.factory();
    valueWrapper = OffHeapByteArrayImpl::wrap;
    keyWrapper = OffHeapByteArrayImpl::wrap;
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testPutAndConstructors() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    Assert.assertEquals(list.size(), 0);
    list.put(key1, value1);
    Assert.assertEquals(list.size(), 1);
    list.put(key2, value2);
    Assert.assertEquals(list.size(), 2);

    Bucket list2 = LinkedListBucket.wrap(
      list.getAddress(),
      slab,
      memoryViewFactory,
      valueWrapper,
      keyWrapper
    );
    Assert.assertEquals(list2.size(), 2);
  }

  @Test
  public void testPutsAndGets() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    list.put(key1, value1);
    list.put(key2, value2);
    Assert.assertEquals(list.get(key1).getAnnotatedValue().getValue(), value1);
    Assert.assertEquals(list.get(key2).getAnnotatedValue().getValue(), value2);
    Assert.assertNull(list.get(key3));
  }

  @Test
  public void testPutsAndRemoves1() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.size(), 3);
    bucket.remove(key2);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key2));
    bucket.put(key2, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key2).getAnnotatedValue().getValue(), value2);
  }

  @Test
  public void testPutsAndRemoves2() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.get(key1).getAnnotatedValue().getValue(), value1);
    bucket.remove(key1);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key1));
    bucket.put(key1, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key1).getAnnotatedValue().getValue(), value2);
  }

  @Test
  public void testPutsAndRemoves3() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.get(key1).getAnnotatedValue().getValue(), value1);
    bucket.remove(key3);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key3));
    bucket.put(key3, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key3).getAnnotatedValue().getValue(), value2);
  }

  @Test
  public void testRemoveNonExistantNode() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.get(key3);
    Assert.assertFalse(bucket.remove(key3));
  }

  @Test
  public void testSingleNode() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    bucket.put(key1, value1);
    Assert.assertFalse(bucket.remove(key2));
    Assert.assertTrue(bucket.remove(key1));
    Assert.assertEquals(bucket.size(), 0);
  }

  @Test
  public void testEmptyBucket() throws Exception {
    Bucket sourceBucket = LinkedListBucket.create(slab, memoryViewFactory, valueWrapper, keyWrapper);
    Assert.assertEquals(sourceBucket.size(), 0);

    long address = sourceBucket.getAddress();

    Bucket wrappedBucket = LinkedListBucket.wrap(address, slab, memoryViewFactory, valueWrapper, keyWrapper);

    Assert.assertEquals(wrappedBucket.size(), 0);
  }
}
