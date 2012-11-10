/*
 * Copyright (C) 2012 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.facebook.collections;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Iterator;

public class TestLongArray {
  private LongArray arraySize4;
  private LongArray arraySize16;
  private LongArray array;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    array = new LongArray();
    array.append(1L);
    array.append(2L);
    array.append(3L);
    array.append(4L);
    array.append(5L);
    array.append(6L);
    arraySize4 = new LongArray(4);
    arraySize16 = new LongArray(16);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    array.append(1L);
    Assert.assertEquals(array.size(), 7);
    Assert.assertEquals(array.get(0).longValue(), 1L);
    array.append(2L);
    Assert.assertEquals(array.size(), 8);
    Assert.assertEquals(array.get(5).longValue(), 6L);
    Assert.assertEquals(array.get(7).longValue(), 2L);
  }

  @Test(groups = "fast", expectedExceptions = ArrayIndexOutOfBoundsException.class)
  public void testOutOfBounds() throws Exception {
    Assert.assertEquals(array.size(), 6);
    Assert.assertEquals(array.get(7).longValue(), 1L);
  }

  @Test(groups = "fast")
  public void testResize() throws Exception {
    Assert.assertEquals(arraySize4.size(), 0);
    Assert.assertEquals(arraySize4.capacity(), 4);
    arraySize4.append(0L);
    arraySize4.append(1L);
    arraySize4.append(2L);
    arraySize4.append(4L);
    arraySize4.append(8L);
    Assert.assertEquals(arraySize4.size(), 5);
    Assert.assertEquals(arraySize4.capacity(), 12);
    Assert.assertEquals(arraySize4.get(0).longValue(), 0);
    Assert.assertEquals(arraySize4.get(1).longValue(), 1);
    Assert.assertEquals(arraySize4.get(2).longValue(), 2);
    Assert.assertEquals(arraySize4.get(3).longValue(), 4);
    Assert.assertEquals(arraySize4.get(4).longValue(), 8);
  }

  @Test(groups = "fast")
  public void testSetNoSizeGrowth() throws Exception {
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.set(3, 10L);

    Assert.assertEquals(arraySize16.size(), 4);
    Assert.assertEquals(arraySize16.get(3).longValue(), 10L);
  }

  @Test(groups = "fast")
  public void testSetSizeGrowth() throws Exception {
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.append(1L);
    arraySize16.set(4, 10L);

    Assert.assertEquals(arraySize16.size(), 5);
    Assert.assertEquals(arraySize16.get(4).longValue(), 10L);
  }

  @Test(groups = "fast", expectedExceptions = ArrayIndexOutOfBoundsException.class)
  public void testSetOutOfBounds() throws Exception {
    array.set(array.capacity(), 100L);
  }

  @Test(groups = "fast")
  public void testRemove() throws Exception {
    array.remove(3);
    Assert.assertNull(array.get(3));
    Assert.assertEquals(array.size(), 5);
    assertArrayIs(array, 1, 2, 3, 5, 6);
  }

  @Test(groups = "fast")
  public void testIterator() throws Exception {
    Iterator<Long> iter = array.iterator();
    // hasNext() shouldn't be required
    Assert.assertEquals(iter.next().longValue(), 1L);
    // repeated hasNext() shouldn't change value
    iter.hasNext();
    iter.hasNext();
    iter.hasNext();
    Assert.assertEquals(iter.next().longValue(), 2L);
    iter.remove();
    Assert.assertEquals(iter.next().longValue(), 3L);
    Assert.assertEquals(iter.next().longValue(), 4L);
    Assert.assertEquals(iter.next().longValue(), 5L);
    Assert.assertEquals(iter.next().longValue(), 6L);
  }

  @Test(groups = "fast", expectedExceptions = IllegalStateException.class)
  public void testIteratorRemoveNoNext() throws Exception {
    Iterator<Long> iter = array.iterator();
  	iter.remove();
  }

  @Test(groups = "fast", expectedExceptions = IllegalStateException.class)
  public void testIteratorRepeatedRemove() throws Exception {
    Iterator<Long> iter = array.iterator();
  	iter.next();
    iter.remove();
    Assert.assertEquals(iter.next().longValue(), 2L);
    iter.remove();
    iter.remove();
  }

  private void assertArrayIs(LongArray array, long... values) {
    int i = 0;

    for (Long arrayValue : array) {
      Assert.assertEquals(arrayValue.longValue(), values[i]);
      i++;
    }
  }
}
