package com.facebook.collections.heaps;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.TreeSet;

public class TestParitionedHeap {

  private PartitionedHeap<IntRange> partitionedHeap;
  private IntRange intRange1;
  private IntRange intRange2;
  private IntRange intRange3;
  private IntRange intRange4;
  private IntRange intRange2049;
  private IntRange intRange1025;
  private IntRange intRange512;
  private IntRange intRange10241;
  private IntRange intRange10242;
  private IntRange intRange102401;
  private IntRange intRange102402;
  private IntRange intRange1026;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    NavigableSetFactory<IntRange> rangeSetFactory = () -> new TreeSet<>(IntRange.getSizeComparator());
    IntRange[] boundaries = new IntRange[3];

    // only the size of these ranges mattesr
    boundaries[0] = new IntRange(0, 1023);
    boundaries[1] = new IntRange(0, 10 * 1024);
    boundaries[2] = new IntRange(0, 100 * 1024);

    HeapPartition<IntRange>[] heapPartitions = HeapPartitions.createHeapParitions(boundaries, rangeSetFactory);

    partitionedHeap = new PartitionedHeap<>(heapPartitions, IntRange.getSizeComparator());

    // lands in shard 0, size 1024
    intRange1 = IntRange.make(0, 1023);
    // lands in shard 1, size 1025
    intRange2 = IntRange.make(0, 1024);
    // lands in shard 3, size 10241
    intRange3 = IntRange.make(0, 10 * 1024);
    // lands in shard 3 also
    intRange4 = IntRange.make(0, 100 * 1024);
    // query ranges
    intRange512 = IntRange.make(0, 511);
    intRange1025 = IntRange.make(0, 1024);
    intRange1026 = IntRange.make(0, 1025);
    intRange2049 = IntRange.make(0, 2048);
    intRange10241 = IntRange.make(0, 10 * 1024);
    intRange10242 = IntRange.make(0, 10 * 1024 + 1);
    intRange102401 = IntRange.make(0, 100 * 1024);
    intRange102402 = IntRange.make(0, 100 * 1024 + 1);

    partitionedHeap.add(intRange1);
    partitionedHeap.add(intRange2);
    partitionedHeap.add(intRange3);
    partitionedHeap.add(intRange4);
  }

  @Test
  public void testAddAndRemove() throws Exception {
    IntRange intRange = IntRange.make(0, 1024 * 1024);

    partitionedHeap.add(intRange);
    Assert.assertEquals(partitionedHeap.size(), 5);
    partitionedHeap.remove(intRange);
    Assert.assertEquals(partitionedHeap.size(), 4);
    Assert.assertEquals(partitionedHeap.last(), intRange4);
  }

  @Test
  public void testLast() throws Exception {
    Assert.assertEquals(partitionedHeap.last(), intRange4);
    Assert.assertTrue(partitionedHeap.remove(intRange4));
    Assert.assertEquals(partitionedHeap.last(), intRange3);
    Assert.assertTrue(partitionedHeap.remove(intRange3));
    Assert.assertEquals(partitionedHeap.last(), intRange2);
    Assert.assertTrue(partitionedHeap.remove(intRange2));
    Assert.assertEquals(partitionedHeap.last(), intRange1);
    Assert.assertTrue(partitionedHeap.remove(intRange1));
    Assert.assertNull(partitionedHeap.last());
    Assert.assertEquals(partitionedHeap.size(), 0);
  }

  @Test
  public void testRemove() throws Exception {
    Assert.assertTrue(partitionedHeap.remove(intRange1));
    Assert.assertFalse(partitionedHeap.remove(intRange1));
    Assert.assertTrue(partitionedHeap.remove(intRange2));
    Assert.assertFalse(partitionedHeap.remove(intRange2));
  }

  @Test
  public void testRemoveAll1() throws Exception {
    Assert.assertTrue(partitionedHeap.removeAll(Arrays.asList(intRange1, intRange2)));
    Assert.assertFalse(partitionedHeap.removeAll(Arrays.asList(intRange1, intRange2)));
  }

  @Test
  public void testRemoveAll2() throws Exception {
    Assert.assertTrue(partitionedHeap.removeAll(Arrays.asList(intRange1)));
    Assert.assertFalse(partitionedHeap.removeAll(Arrays.asList(intRange1, intRange2)));
    Assert.assertFalse(partitionedHeap.removeAll(Arrays.asList(intRange2)));
  }

  @Test
  public void testPollLast() throws Exception {
    Assert.assertEquals(partitionedHeap.pollLast(), intRange4);
    Assert.assertEquals(partitionedHeap.pollLast(), intRange3);
    Assert.assertEquals(partitionedHeap.pollLast(), intRange2);
    Assert.assertEquals(partitionedHeap.pollLast(), intRange1);
    Assert.assertNull(partitionedHeap.pollLast());
    Assert.assertEquals(partitionedHeap.size(), 0);
  }

  @Test
  public void testHigher() throws Exception {
    Assert.assertEquals(partitionedHeap.higher(intRange512), intRange1);
    Assert.assertEquals(partitionedHeap.higher(intRange1025), intRange3);
    Assert.assertEquals(partitionedHeap.higher(intRange2049), intRange3);
    Assert.assertEquals(partitionedHeap.higher(intRange10241), intRange4);
    Assert.assertEquals(partitionedHeap.higher(intRange10242), intRange4);
    Assert.assertEquals(partitionedHeap.higher(intRange102401), null);
    Assert.assertEquals(partitionedHeap.higher(intRange102402), null);
  }

  @Test
  public void testCeiling() throws Exception {
    Assert.assertEquals(partitionedHeap.ceiling(intRange512), intRange1);
    Assert.assertEquals(partitionedHeap.ceiling(intRange1025), intRange2);
    Assert.assertEquals(partitionedHeap.ceiling(intRange2049), intRange3);
    Assert.assertEquals(partitionedHeap.ceiling(intRange10241), intRange3);
    Assert.assertEquals(partitionedHeap.ceiling(intRange10242), intRange4);
    Assert.assertEquals(partitionedHeap.ceiling(intRange102401), intRange4);
    Assert.assertEquals(partitionedHeap.ceiling(intRange102402), null);
  }

  @Test
  public void testLower() throws Exception {
    Assert.assertEquals(partitionedHeap.lower(intRange512), null);
    Assert.assertEquals(partitionedHeap.lower(intRange1025), intRange1);
    Assert.assertEquals(partitionedHeap.lower(intRange1026), intRange2);
    Assert.assertEquals(partitionedHeap.lower(intRange2049), intRange2);
    Assert.assertEquals(partitionedHeap.lower(intRange10241), intRange2);
    Assert.assertEquals(partitionedHeap.lower(intRange10242), intRange3);
    Assert.assertEquals(partitionedHeap.lower(intRange102401), intRange3);
    Assert.assertEquals(partitionedHeap.lower(intRange102402), intRange4);
  }

  @Test
  public void testFloor() throws Exception {
    Assert.assertEquals(partitionedHeap.floor(intRange512), null);
    Assert.assertEquals(partitionedHeap.floor(intRange1025), intRange2);
    Assert.assertEquals(partitionedHeap.floor(intRange1026), intRange2);
    Assert.assertEquals(partitionedHeap.floor(intRange2049), intRange2);
    Assert.assertEquals(partitionedHeap.floor(intRange10241), intRange3);
    Assert.assertEquals(partitionedHeap.floor(intRange10242), intRange3);
    Assert.assertEquals(partitionedHeap.floor(intRange10242), intRange3);
    Assert.assertEquals(partitionedHeap.floor(intRange102401), intRange4);
    Assert.assertEquals(partitionedHeap.floor(intRange102402), intRange4);
  }
}
