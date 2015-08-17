package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.collections.bytearray.ByteArrays;
import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView32;

public class TestLinkedListBucket {
  private ByteArray value2;
  private ByteArray value1;
  private ByteArray key2;
  private ByteArray key1;
  private ByteArray key3;

  private int size = 1024 * 100;
  private Slab slab;
  private MemoryView32.Factory memoryViewFactory;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);

    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);
    key1 = ByteArrays.wrap(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    key2 = ByteArrays.wrap(new byte[]{7, 8, 9, 10, 11});
    key3 = ByteArrays.wrap(new byte[]{1, 2, 3, 4, 5, 6});
    value1 = ByteArrays.wrap(new byte[]{0, 0, 0, 101, 100, 21, 22});
    value2 = ByteArrays.wrap(new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20});
    this.memoryViewFactory = MemoryView32.factory();
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testPutAndConstructors() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, memoryViewFactory);
    Assert.assertEquals(list.size(), 0);
    list.put(key1, value1);
    Assert.assertEquals(list.size(), 1);
    list.put(key2, value2);
    Assert.assertEquals(list.size(), 2);

    Bucket list2 = LinkedListBucket.wrap(list.getAddress(), slab, memoryViewFactory);
    Assert.assertEquals(list2.size(), 2);
  }

  @Test
  public void testPutsAndGets() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, memoryViewFactory);
    list.put(key1, value1);
    list.put(key2, value2);
    Assert.assertEquals(list.get(key1).getByteArray(), value1);
    Assert.assertEquals(list.get(key2).getByteArray(), value2);
    Assert.assertNull(list.get(key3));
  }

  @Test
  public void testPutsAndRemoves1() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.size(), 3);
    bucket.remove(key2);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key2));
    bucket.put(key2, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key2).getByteArray(), value2);
  }


  @Test
  public void testPutsAndRemoves2() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.get(key1).getByteArray(), value1);
    bucket.remove(key1);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key1));
    bucket.put(key1, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key1).getByteArray(), value2);
  }

  @Test
  public void testPutsAndRemoves3() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    bucket.put(key3, value1);
    Assert.assertEquals(bucket.get(key1).getByteArray(), value1);
    bucket.remove(key3);
    Assert.assertEquals(bucket.size(), 2);
    Assert.assertNull(bucket.get(key3));
    bucket.put(key3, value2);
    Assert.assertEquals(bucket.size(), 3);
    Assert.assertEquals(bucket.get(key3).getByteArray(), value2);
  }

  @Test
  public void testRemoveNonExistantNode() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory);
    bucket.put(key1, value1);
    bucket.put(key2, value2);
    Assert.assertFalse(bucket.remove(key3));
  }

  @Test
  public void testSingleNode() throws FailedAllocationException {
    Bucket bucket = LinkedListBucket.create(slab, memoryViewFactory);
    bucket.put(key1, value1);
    Assert.assertFalse(bucket.remove(key2));
    Assert.assertTrue(bucket.remove(key1));
    Assert.assertEquals(bucket.size(), 0);
  }

  @Test
  public void testEmptyBucket() throws Exception {
    Bucket sourceBucket = LinkedListBucket.create(slab, memoryViewFactory);
    Assert.assertEquals(sourceBucket.size(), 0);

    long address = sourceBucket.getAddress();

    Bucket wrappedBucket = LinkedListBucket.wrap(address, slab, memoryViewFactory);

    Assert.assertEquals(wrappedBucket.size(), 0);
  }
}
