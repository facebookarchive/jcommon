package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

import com.facebook.collections.bytearray.ByteArray;
import com.facebook.collections.bytearray.ByteArrays;
import com.facebook.memory.data.types.definitions.FieldType;
import com.facebook.memory.data.types.definitions.Struct;
import com.facebook.memory.data.types.definitions.Structs;
import com.facebook.memory.slabs.ManagedSlab;
import com.facebook.memory.slabs.OffHeapSlab;
import com.facebook.memory.slabs.Slab;

public class TestLinkedListBucketNode {
  private ByteArray value2;
  private ByteArray value1;
  private ByteArray key3;
  private ByteArray key2;
  private ByteArray key1;

  private Slab slab;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    int size = 1024 * 100;
    OffHeapSlab offHeapSlab = new OffHeapSlab(size);
    slab = new ManagedSlab(offHeapSlab.getBaseAddress(), offHeapSlab, size);

    key1 = ByteArrays.wrap(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12});
    key2 = ByteArrays.wrap(new byte[]{7, 8, 9, 10, 11});
    key3 = ByteArrays.wrap(new byte[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 13});
    value1 = ByteArrays.wrap(new byte[]{0, 0, 0, 101, 100, 21, 22});
    value2 = ByteArrays.wrap(new byte[]{70, 80, 10, 120, 124, 10, 20, 30, 20});
  }

  @AfterMethod(alwaysRun = true)
  public void tearDown() throws Exception {
    slab.freeSlab();
  }

  @Test
  public void testSlots() throws Exception {
    Struct struct = Structs.getStruct(LinkedListBucketNode.class);
    List<Struct.Field> mergedFieldList = struct.getMergedFieldList();

    Assert.assertEquals(mergedFieldList.size(), 5);
    Assert.assertEquals(mergedFieldList.get(0).getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(mergedFieldList.get(1).getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(mergedFieldList.get(2).getFieldType(), FieldType.ADDRESS);
    Assert.assertEquals(mergedFieldList.get(3).getFieldType(), FieldType.BYTE_ARRAY);
    Assert.assertEquals(mergedFieldList.get(4).getFieldType(), FieldType.BYTE_ARRAY);
  }

  @Test
  public void testConstructors() throws Exception {
    LinkedListBucketNode node = LinkedListBucketNode.create(key1, value2, slab);
    Assert.assertTrue(node.keyEquals(key1));
    Assert.assertEquals(node.getValue(), value2);
    Assert.assertFalse(node.keyEquals(key3));

    LinkedListBucketNode nodeCopy = LinkedListBucketNode.wrap(node.getAddress());
    Assert.assertTrue(nodeCopy.keyEquals(key1));
    Assert.assertEquals(nodeCopy.getValue(), value2);
    Assert.assertEquals(nodeCopy.getSize(), node.getSize());
  }

  @Test
  public void testAddNode() throws Exception {
    LinkedListBucketNode node1 = LinkedListBucketNode.create(key1, value1, slab);
    LinkedListBucketNode node2 = LinkedListBucketNode.create(key2, value2, slab);
    Assert.assertEquals(node1.getSize(), 52);

    node1.setNext(node2.getAddress());

    LinkedListBucketNode node2Copy = node1.next();

    Assert.assertEquals(node2Copy.getAddress(), node2.getAddress());
    Assert.assertEquals(node2.getSize(), 46);
    Assert.assertEquals(node2Copy.getSize(), node2.getSize());

    ByteArray valueRead1 = node2.getValue();
    ByteArray valueRead2 = node2.getValue();

    Assert.assertEquals(valueRead1, valueRead2);
    Assert.assertTrue(node2.keyEquals(key2) && node2Copy.keyEquals(key2));
  }
}
