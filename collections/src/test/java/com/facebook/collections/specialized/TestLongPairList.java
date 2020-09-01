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
package com.facebook.collections.specialized;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLongPairList {

  private LongPairList primes;
  private LongPairList squares;
  private LongPairList cubes;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    primes = new LongPairList(2);
    primes.add(toTuple(1, 7));
    primes.add(toTuple(2, 5));
    primes.add(toTuple(3, 3));
    primes.add(toTuple(4, 2));

    squares = new LongPairList(2);
    squares.add(toTuple(10, 100));
    squares.add(toTuple(2, 4));
    squares.add(toTuple(3, 9));
    squares.add(toTuple(5, 25));
    squares.add(toTuple(1, 1));

    cubes = new LongPairList(2);
  }

  @Test(groups = "fast")
  public void testShrinkBoundaryCase1() throws Exception {
    cubes.add(toTuple(1, 1));
    cubes.shrink();
  }

  @Test(groups = "fast")
  public void testFindMin() throws Exception {
    assertTuplesEqual(primes.peek(), toTuple(1, 7));
    assertTuplesEqual(squares.peek(), toTuple(1, 1));
  }

  @Test(groups = "fast")
  public void testRemoveMin() throws Exception {
    long[] head = primes.poll();
    assertTuplesEqual(head, toTuple(1, 7));
    assertTuplesEqual(primes.peek(), toTuple(2, 5));

    head = squares.poll();
    assertTuplesEqual(head, toTuple(1, 1));
    assertTuplesEqual(squares.peek(), toTuple(2, 4));
  }

  @Test(groups = "fast")
  public void testInsertAndDelete() throws Exception {
    assertTuplesEqual(primes.poll(), toTuple(1, 7));
    primes.add(toTuple(0, 0));
    assertTuplesEqual(primes.poll(), toTuple(0, 0));
    assertTuplesEqual(primes.poll(), toTuple(2, 5));
    assertTuplesEqual(primes.poll(), toTuple(3, 3));
    assertTuplesEqual(primes.poll(), toTuple(4, 2));
    Assert.assertEquals(primes.size(), 0);

    squares.poll();
    squares.add(toTuple(0, 0));
    assertTuplesEqual(squares.poll(), toTuple(0, 0));
    assertTuplesEqual(squares.poll(), toTuple(2, 4));
    assertTuplesEqual(squares.poll(), toTuple(3, 9));
    assertTuplesEqual(squares.poll(), toTuple(5, 25));
    assertTuplesEqual(squares.poll(), toTuple(10, 100));
    Assert.assertEquals(squares.size(), 0);
  }

  @Test(groups = "fast")
  public void testInterleavedPollAdd() throws Exception {
    LongPairList list = new LongPairList(2);

    list.add(toTuple(10, 10));
    list.add(toTuple(1, 1));
    assertTuplesEqual(list.poll(), toTuple(1, 1));
    list.add(toTuple(4, 4));
    list.add(toTuple(5, 5));
    assertTuplesEqual(list.peek(), toTuple(4, 4));
    assertTuplesEqual(list.poll(), toTuple(4, 4));
    list.add(toTuple(3, 3));
    list.add(toTuple(2, 2));
    assertTuplesEqual(list.poll(), toTuple(2, 2));
    assertTuplesEqual(list.poll(), toTuple(3, 3));
    assertTuplesEqual(list.poll(), toTuple(5, 5));
    assertTuplesEqual(list.poll(), toTuple(10, 10));
    Assert.assertEquals(list.size(), 0);
    list.shrink();
  }

  @Test(groups = "fast")
  public void testFuu() throws Exception {
    LongPairList list = new LongPairList(2);

    list.add(toTuple(1, 7));
    list.add(toTuple(2, 5));
    list.poll();
    list.add(toTuple(1, 7));
    assertTuplesEqual(list.poll(), toTuple(1, 7));
  }

  @Test(groups = "fast")
  public void testIterator() throws Exception {
    assertContentsViaIterator(primes, toTuple(1, 7), toTuple(2, 5), toTuple(3, 3), toTuple(4, 2));
    primes.poll();

    assertContentsViaIterator(primes, toTuple(2, 5), toTuple(3, 3), toTuple(4, 2));

    assertContentsViaIterator(
        squares, toTuple(1, 1), toTuple(2, 4), toTuple(3, 9), toTuple(5, 25), toTuple(10, 100));
    squares.poll();

    assertContentsViaIterator(
        squares, toTuple(2, 4), toTuple(3, 9), toTuple(5, 25), toTuple(10, 100));
  }

  private void assertTuplesEqual(long[] actual, long[] exected) {
    Assert.assertTrue(Arrays.equals(actual, exected));
  }

  private void assertContentsViaIterator(LongPairList tupleList, long[]... results) {
    List<long[]> tmpList = new ArrayList<long[]>(tupleList.size());

    for (long[] tuple : tupleList) {
      tmpList.add(tuple);
    }

    Collections.sort(
        tmpList,
        new Comparator<long[]>() {
          @Override
          public int compare(long[] o1, long[] o2) {
            return Long.signum(o1[0] - o2[0]);
          }
        });

    int i = 0;

    for (long[] tuple : tmpList) {
      assertTuplesEqual(tuple, results[i]);
      i++;
    }
  }

  private long[] toTuple(long... items) {
    return items;
  }
}
