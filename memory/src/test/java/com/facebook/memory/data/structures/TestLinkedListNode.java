package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;
import com.facebook.memory.views.MemoryView32;

public class TestLinkedListNode {
  private byte[] value2;
  private byte[] value1;
  private byte[] key3;
  private byte[] key2;
  private byte[] key1;

  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int size = 1024 * 100;
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);
    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);

    key1 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12};
    key2 = new byte[]{7, 8, 9, 10, 11};
    key3 = new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13};
    value1 = new byte[]{0, 0, 0, 101, 100, 21, 22};
    value2 = new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20};
  }

  @Test
  public void testConstructors() throws Exception {
    LinkedListNode node = LinkedListNode.create(key1, value2, MemoryView32.factory(), slab);
    Assert.assertTrue(node.keyEquals(key1));
    Assert.assertEquals(node.getValue(), value2);
    Assert.assertFalse(node.keyEquals(key3));

    LinkedListNode nodeCopy = new LinkedListNode(node.getAddress(), MemoryView32.factory());
    Assert.assertTrue(nodeCopy.keyEquals(key1));
    Assert.assertEquals(nodeCopy.getValue(), value2);
    Assert.assertEquals(nodeCopy.getSize(), node.getSize());
  }

  @Test
  public void testAddNode() throws Exception {
    LinkedListNode node = LinkedListNode.create(key1, value1, MemoryView32.factory(), slab);
    LinkedListNode node2 = LinkedListNode.create(key2, value2, MemoryView32.factory(), slab);
    node.setNext(node2.getAddress());
    LinkedListNode node2Copy = node.next();
    Assert.assertEquals(node2Copy.getAddress(), node2.getAddress());
    Assert.assertEquals(node2.getValue(), node2.getValue());
    Assert.assertTrue(node2.keyEquals(key2) && node2Copy.keyEquals(key2));
  }
}
