package com.facebook.collections.specialized;

import com.facebook.util.ExtRunnable;
import com.facebook.util.TimeUtil;
import com.facebook.util.digest.LongMurmur3Hash;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class TestColtHashSet {

  private ColtLongHashSet set;
  private int numElements;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    numElements = 10;
    set = new ColtLongHashSet(numElements);
  }

  @Test(groups = "fast")
  public void testAdd() throws Exception {
    Assert.assertEquals(set.size(), 0);
    Assert.assertTrue(set.add(100L));
    Assert.assertTrue(set.contains(100L));
    Assert.assertEquals(set.size(), 1);
    Assert.assertFalse(set.add(100L));
    Assert.assertEquals(set.size(), 1);
    Assert.assertTrue(set.add(200L));
    Assert.assertTrue(set.contains(100L));
    Assert.assertTrue(set.contains(200L));
    Assert.assertEquals(set.size(), 2);
  }

  @Test(groups = "fast")
  public void testIsEmpty() throws Exception {
    Assert.assertTrue(set.isEmpty());
    Assert.assertTrue(set.add(100L));
    Assert.assertFalse(set.isEmpty());
  }

  @Test(groups = "fast")
  public void testNotThere() throws Exception {
    Assert.assertEquals(set.size(), 0);
    Assert.assertTrue(set.add(100L));
    Assert.assertFalse(set.contains(10L));
  }

  @Test(groups = "fast")
  public void testRemove() throws Exception {
    Assert.assertTrue(set.add(1L));
    Assert.assertTrue(set.remove(1L));
    Assert.assertFalse(set.remove(1L));
  }


  @Test(groups = "fast")
  public void testResize() throws Exception {
    fillSet();
    Assert.assertEquals(set.size(), numElements);
  }

  @Test(groups = "fast")
  public void testConcurrentModifcation() throws Exception {
    set.add(1L);
    Iterator<Long> iterator = set.iterator();
    Assert.assertTrue(iterator.hasNext());
    set.add(1L);
    Assert.assertTrue(iterator.hasNext());
    set.add(2L);
    // only changing the set should cause this
    try {
      iterator.hasNext();
    } catch (Exception e) {
      Assert.assertTrue(e instanceof ConcurrentModificationException);
    }

  }

  @Test(groups = "fast")
  public void testIterator() throws Exception {
    set.add(1L);
    set.add(2L);
    set.add(3L);
    Iterator<Long> iterator = set.iterator();

    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(iterator.next().longValue(), 3L);
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(iterator.next().longValue(), 2L);
    Assert.assertTrue(iterator.hasNext());
    Assert.assertEquals(iterator.next().longValue(), 1L);
    Assert.assertFalse(iterator.hasNext());
  }

  @Test(groups = "fast")
  public void testIteratorWhenFull() throws Exception {
    fillSet();

    int count = 0;
    Iterator<Long> iterator = set.iterator();

    while (iterator.hasNext()) {
      iterator.next();
      iterator.remove();
      count++;
    }

    Assert.assertEquals(set.size(), 0);
    Assert.assertEquals(count, numElements);

    Iterator<Long> iterator2 = set.iterator();

    // now an iterator shouldn't have any values in it
    Assert.assertFalse(iterator2.hasNext());
  }

  @Test(groups = "fast")
  public void testIterateAndRemove() throws Exception {
    Set<Long> answer = new HashSet<Long>();
    answer.add(1L);
    answer.add(2L);
    set.add(1L);
    set.add(2L);

    Iterator<Long> iterator = set.iterator();

    if (iterator.hasNext()) {
      // remove from answer for check below
      answer.remove(iterator.next());
      iterator.remove();
    }

    Assert.assertTrue(iterator.hasNext());
    // check that whatever value we removed, the other one is still present
    Assert.assertEquals(
      iterator.next().longValue(),
      answer.iterator().next().longValue()
    );
  }

  @Test(groups = "fast")
  public void testRepeatedRemove() throws Exception {
    set.add(1L);

    Iterator<Long> iterator = set.iterator();
    iterator.next();
    iterator.remove();

    try {
      iterator.remove();
      Assert.fail("expected exception");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalStateException);
    }
  }

  @Test(groups = "fast")
  public void testRemoveWithoutNext() throws Exception {
    set.add(1L);

    Iterator<Long> iterator = set.iterator();

    try {
      iterator.remove();
      Assert.fail("expected exception");
    } catch (Exception e) {
      Assert.assertTrue(e instanceof IllegalStateException);
    }
  }

  @Test(groups = "fast")
  public void testStress() throws Exception {
    // sanity test that exercises LongHashSet2 and LongLinkedList
    int inserts = 100000;
    int maxSize = 8000;
    SampledSet<Long> set = new SampledSetImpl<>(
      maxSize, new LongMurmur3Hash(), new LongHashSetFactory(maxSize)
    );
    timeAdds("custom-long-hash-set-2", set, inserts);
  }

  private static void timeAdds(String tag, final Set<Long> set, final int numAdds)
    throws Exception {
    TimeUtil.logElapsedTime(tag, new ExtRunnable<Exception>() {
      @Override
      public void run() throws Exception {
        Random random = new Random();
        for (int i = 0; i < numAdds; i++) {
          set.add(Math.abs(random.nextLong()));
        }
      }
    });
  }

  private void fillSet() {
    for (int i = 0; i < numElements; i++) {
      Assert.assertTrue(set.add((long) i));
    }
  }

  private void emptySet() {
    for (int i = 0; i < numElements; i++) {
      Assert.assertTrue(set.remove((long) i));
    }

    Assert.assertEquals(set.size(), 0);
    Assert.assertTrue(set.isEmpty());
  }

  private void checkFullSet() {
    Assert.assertEquals(set.size(), numElements);

    for (int i = 0; i < numElements; i++) {
      Assert.assertTrue(set.contains((long) i));
    }
  }
}
