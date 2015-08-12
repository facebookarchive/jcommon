package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import com.facebook.memory.data.types.definitions.SizedOffHeapStructure;
import com.facebook.memory.data.types.definitions.SlotTypes;
import com.facebook.memory.data.types.definitions.Struct;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;

public class TestLinkedListBucketNode {
  private SizedOffHeapStructure value1;
  private SizedOffHeapStructure value2;
  private SizedOffHeapStructure key1;
  private SizedOffHeapStructure key2;
  private SizedOffHeapStructure key3;

  private Slab slab;
  private SizedOffHeapWrapper keyWrapper;
  private SizedOffHeapWrapper valueWrapper;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int size = 1024 * 1024;
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);
    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);

    key1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, slab);
    key2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{7, 8, 9, 10, 11}, slab);
    key3 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13}, slab);
    value1 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{0, 0, 0, 101, 100, 21, 22}, slab);
    value2 = OffHeapByteArrayImpl.fromHeapByteArray(new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20}, slab);
    keyWrapper = OffHeapByteArrayImpl::wrap;
    valueWrapper = OffHeapByteArrayImpl::wrap;
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testSlots() throws Exception {
    Struct struct = Structs.getStruct(LinkedListBucketNode.class);
    List<Struct.SlotInfo> mergedSlotInfoList = struct.getMergedSlotInfoList();

    Assert.assertEquals(mergedSlotInfoList.size(), 3);
    Assert.assertEquals(mergedSlotInfoList.get(0).getSlotType(), SlotTypes.ADDRESS);
    Assert.assertEquals(mergedSlotInfoList.get(1).getSlotType(), SlotTypes.ADDRESS);
    Assert.assertEquals(mergedSlotInfoList.get(2).getSlotType(), SlotTypes.ADDRESS);
  }

  @Test
  public void testConstructors() throws Exception {
    LinkedListBucketNode node = LinkedListBucketNode.create(
      slab, key1, value2, keyWrapper, valueWrapper
    );
    Assert.assertTrue(node.keyEquals(key1));
    Assert.assertEquals(node.getValue(), value2);
    Assert.assertFalse(node.keyEquals(key3));

    LinkedListBucketNode nodeCopy = LinkedListBucketNode.wrap(
      node.getAddress(),
      keyWrapper,
      valueWrapper
    );
    Assert.assertTrue(nodeCopy.keyEquals(key1));
    Assert.assertEquals(nodeCopy.getValue(), value2);
    Assert.assertEquals(nodeCopy.getSize(), node.getSize());
  }

  @Test
  public void testSetNext() throws Exception {
    LinkedListBucketNode node1 = LinkedListBucketNode.create(slab, key1, value1, keyWrapper, valueWrapper);
    LinkedListBucketNode node2 = LinkedListBucketNode.create(slab, key2, value2, keyWrapper, valueWrapper);
    node1.setNextAddress(node2.getAddress());

    Assert.assertEquals(node1.nextNode().getAddress(), node2.getAddress());
  }

  @Test
  public void testAddNode() throws Exception {
    LinkedListBucketNode node1 = LinkedListBucketNode.create(slab, key1, value1, keyWrapper, valueWrapper);
    LinkedListBucketNode node2 = LinkedListBucketNode.create(slab, key2, value2, keyWrapper, valueWrapper);
    //  = nextPtr(8) + keyPtr (8) + valuePtr (8) + keyLen(4) + keyData (13)
    // + valueLength (4) + valueData (7)
    Assert.assertEquals(node1.getTotalSize(), 52);

    node1.setNextAddress(node2.getAddress());

    LinkedListBucketNode node2Copy = node1.nextNode();

    Assert.assertEquals(node2Copy.getAddress(), node2.getAddress());
    // similar computation for node2 with different lengths of key and value data
    Assert.assertEquals(node2.getTotalSize(), 46);
    Assert.assertEquals(node2Copy.getTotalSize(), node2.getTotalSize());

    SizedOffHeapStructure valueRead1 = node2.getValue();
    SizedOffHeapStructure valueRead2 = node2Copy.getValue();

    Assert.assertEquals(valueRead1, valueRead2);
    Assert.assertTrue(node2.keyEquals(key2) && node2Copy.keyEquals(key2));
  }

  @Test
  public void testReplaceValue() throws Exception {
    LinkedListBucketNode node1 = LinkedListBucketNode.create(
      slab,
      key1,
      value1,
      keyWrapper,
      valueWrapper
    );

    node1.replaceValue(value2);

    Assert.assertEquals(node1.getValue(), value2);
  }
}
