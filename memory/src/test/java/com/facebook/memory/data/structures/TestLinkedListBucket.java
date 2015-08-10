package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.FailedAllocationException;
import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView32;

public class TestLinkedListBucket {
  private byte[] value2;
  private byte[] value1;
  private byte[] key2;
  private byte[] key1;
  private byte[] key3;

  private int size = 1024 * 100;
  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);

    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);
    key1 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    key2 = new byte[]{7, 8, 9, 10, 11};
    key3 = new byte[]{1, 2, 3, 4, 5, 6};
    value1 = new byte[]{0, 0, 0, 101, 100, 21, 22};
    value2 = new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20};
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testPutAndConstructors() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    Assert.assertEquals(list.size(), 0);
    list.put(key1, value1);
    Assert.assertEquals(list.size(), 1);
    list.put(key2, value2);
    Assert.assertEquals(list.size(), 2);

    Bucket list2 = LinkedListBucket.wrap(list.getAddress(), slab, MemoryView32.factory());
    Assert.assertEquals(list2.size(), 2);
  }

  @Test
  public void testPutsAndGets() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    list.put(key2, value2);
    Assert.assertEquals(list.get(key1).getByteArray(), value1);
    Assert.assertEquals(list.get(key2).getByteArray(), value2);
    Assert.assertNull(list.get(key3));
  }

  @Test
  public void testPutsAndRemoves1() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    list.put(key2, value2);
    list.put(key3, value1);
    Assert.assertEquals(list.size(), 3);
    list.remove(key2);
    Assert.assertEquals(list.size(), 2);
    Assert.assertNull(list.get(key2));
    list.put(key2, value2);
    Assert.assertEquals(list.size(), 3);
    Assert.assertEquals(list.get(key2).getByteArray(), value2);
  }


  @Test
  public void testPutsAndRemoves2() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    list.put(key2, value2);
    list.put(key3, value1);
    Assert.assertEquals(list.get(key1).getByteArray(), value1);
    list.remove(key1);
    Assert.assertEquals(list.size(), 2);
    Assert.assertNull(list.get(key1));
    list.put(key1, value2);
    Assert.assertEquals(list.size(), 3);
    Assert.assertEquals(list.get(key1).getByteArray(), value2);
  }

  @Test
  public void testPutsAndRemoves3() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    list.put(key2, value2);
    list.put(key3, value1);
    Assert.assertEquals(list.get(key1).getByteArray(), value1);
    list.remove(key3);
    Assert.assertEquals(list.size(), 2);
    Assert.assertNull(list.get(key3));
    list.put(key3, value2);
    Assert.assertEquals(list.size(), 3);
    Assert.assertEquals(list.get(key3).getByteArray(), value2);
  }

  @Test
  public void testRemoveNonExistantNode() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    list.put(key2, value2);
    Assert.assertFalse(list.remove(key3));
  }

  @Test
  public void testSingleNode() throws FailedAllocationException {
    Bucket list = LinkedListBucket.create(slab, MemoryView32.factory());
    list.put(key1, value1);
    Assert.assertFalse(list.remove(key2));
    Assert.assertTrue(list.remove(key1));
    Assert.assertEquals(list.size(), 0);
  }
}
