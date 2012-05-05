package com.facebook.collections;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

public class TestRangeSet {
  private Set<Long> set;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    set = new RangeSet();
  }

  @Test(groups = "fast")
  public void testAdd() throws Exception {
    Assert.assertTrue(set.isEmpty());
    Assert.assertEquals(set.size(), 0);

    // Single
    Assert.assertTrue(set.add(0L));
    Assert.assertTrue(set.contains(0L));
    Assert.assertEquals(set.size(), 1);

    // Single Duplicate
    Assert.assertFalse(set.add(0L));
    Assert.assertEquals(set.size(), 1);

    // Forward contiguous
    Assert.assertTrue(set.add(1L));
    Assert.assertTrue(set.contains(1L));
    Assert.assertEquals(set.size(), 2);

    // Range Duplicate
    Assert.assertFalse(set.add(1L));
    Assert.assertEquals(set.size(), 2);

    // Reverse contiguous
    Assert.assertTrue(set.add(-1L));
    Assert.assertTrue(set.contains(-1L));
    Assert.assertEquals(set.size(), 3);

    // Forward fragment
    Assert.assertTrue(set.add(3L));
    Assert.assertTrue(set.contains(3L));
    Assert.assertEquals(set.size(), 4);

    // Reverse fragment
    Assert.assertTrue(set.add(-3L));
    Assert.assertTrue(set.contains(-3L));
    Assert.assertEquals(set.size(), 5);

    // Fragment merge
    Assert.assertTrue(set.add(2L));
    Assert.assertTrue(set.contains(2L));
    Assert.assertEquals(set.size(), 6);

    // Merge duplicate
    Assert.assertFalse(set.add(2L));
    Assert.assertEquals(set.size(), 6);
  }

  @Test(groups = "fast")
  public void testRemove() throws Exception {
    Assert.assertTrue(set.isEmpty());
    Assert.assertEquals(set.size(), 0);

    // Load values
    Assert.assertTrue(set.addAll(Arrays.asList(0L, 2L, 3L, 5L, 6L, 8L, 9L, 10L)));
    Assert.assertEquals(set.size(), 8);

    // Remove non-existant
    Assert.assertFalse(set.remove(-1L));
    Assert.assertFalse(set.remove(1L));
    Assert.assertFalse(set.remove(4L));
    Assert.assertEquals(set.size(), 8);

    // Remove singular
    Assert.assertTrue(set.remove(0L));
    Assert.assertFalse(set.contains(0L));
    Assert.assertEquals(set.size(), 7);

    // Remove range min
    Assert.assertTrue(set.remove(2L));
    Assert.assertFalse(set.contains(2L));
    Assert.assertEquals(set.size(), 6);

    // Remove range max
    Assert.assertTrue(set.remove(6L));
    Assert.assertFalse(set.contains(6L));
    Assert.assertEquals(set.size(), 5);

    // Remove causing range split
    Assert.assertTrue(set.remove(9L));
    Assert.assertFalse(set.contains(9L));
    Assert.assertEquals(set.size(), 4);
  }

  @Test(groups = "fast")
  public void testRemoveAll() throws Exception {
    Assert.assertTrue(set.isEmpty());
    Assert.assertEquals(set.size(), 0);

    // Load values
    Assert.assertTrue(set.addAll(Arrays.asList(0L, 2L, 3L, 5L, 6L, 8L, 9L, 10L)));
    Assert.assertEquals(set.size(), 8);

    // Remove non-existant values
    Assert.assertFalse(set.removeAll(Arrays.asList(11L, 12L, 13L, 15L, 16L)));
    Assert.assertEquals(set.size(), 8);

    // Remove some values
    Assert.assertTrue(set.removeAll(Arrays.asList(0L, 1L, 2L, 3L, 4L, 5L, 6L)));
    Assert.assertEquals(set.size(), 3);
  }

  @Test(groups = "fast")
  public void testClear() throws Exception {
    Assert.assertTrue(set.isEmpty());
    Assert.assertEquals(set.size(), 0);

    // Load values
    Assert.assertTrue(set.addAll(Arrays.asList(0L, 2L, 3L, 5L, 6L, 8L, 9L, 10L)));
    Assert.assertEquals(set.size(), 8);

    // Clear values
    set.clear();
    Assert.assertTrue(set.isEmpty());
    Assert.assertEquals(set.size(), 0);
  }

  @Test(groups = "fast")
  public void testIterator() throws Exception {
    Iterator<Long> iter;

    iter = set.iterator();
    Assert.assertFalse(iter.hasNext());

    set.add(1L);
    iter = set.iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == 1);
    Assert.assertFalse(iter.hasNext());

    set.add(2L);
    iter = set.iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == 1);
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == 2);
    Assert.assertFalse(iter.hasNext());

    set.add(-1L);
    iter = set.iterator();
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == -1);
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == 1);
    Assert.assertTrue(iter.hasNext());
    Assert.assertTrue(iter.next() == 2);
    Assert.assertFalse(iter.hasNext());
  }

  @Test(groups = "fast", expectedExceptions = NoSuchElementException.class)
  public void testIllegalIterator() throws Exception {
    Iterator<Long> iter;

    iter = set.iterator();
    Assert.assertFalse(iter.hasNext());
    iter.next();
  }

  @Test(groups = "fast")
  public void testStandard() throws Exception {
    Set<Long> standard = new HashSet<Long>();
    Random random = new Random(System.nanoTime());

    for (int idx = 0; idx < 1000000; idx++) {
      long value = random.nextLong() % 10000;
      if (random.nextInt(3) > 0) {
        Assert.assertEquals(set.add(value), standard.add(value));
      } else {
        Assert.assertEquals(set.remove(value), standard.remove(value));
      }
      Assert.assertEquals(set.size(), standard.size());
    }

    // All aspects of both sets should be the same
    Assert.assertEquals(set, standard);
  }
}
