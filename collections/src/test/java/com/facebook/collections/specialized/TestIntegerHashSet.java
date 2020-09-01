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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.HashSet;
import java.util.Set;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestIntegerHashSet {

  private IntegerHashSet defaultSet;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    defaultSet = new IntegerHashSet(2, Integer.MAX_VALUE);
    defaultSet.add(0);
    defaultSet.add(1);
    defaultSet.add(2);
    defaultSet.add(4);
    defaultSet.add(8);
  }

  @Test(groups = "fast")
  public void testRemove() throws Exception {
    Assert.assertTrue(defaultSet.remove(0));
    Assert.assertTrue(defaultSet.remove(2L));
  }

  @Test(groups = "fast")
  public void testContains() throws Exception {
    Assert.assertTrue(defaultSet.contains(0));
    Assert.assertTrue(defaultSet.contains(8L));
  }

  @Test(
      groups = "fast",
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*23.*")
  public void testMaxCapacity() throws Exception {
    int maxCapacity = 23;
    IntegerHashSet set = new IntegerHashSet(2, maxCapacity);

    for (int i = 0; i <= maxCapacity; i++) {
      set.add(i);
    }
  }

  @Test(
      groups = "fast",
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*23.*")
  public void testMaxCapacityWithAdAll() throws Exception {
    int maxCapacity = 23;
    IntegerHashSet set = new IntegerHashSet(2, maxCapacity);
    Set<Long> addSet = new HashSet<>();

    for (int i = 0; i <= maxCapacity; i++) {
      addSet.add((long) i);
    }

    set.addAll(addSet);
  }

  @Test(
      groups = "fast",
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*23.*")
  public void testMaxCapacityHonoredWithAdd() throws Exception {
    int maxCapacity = 23;
    IntegerHashSet set = new IntegerHashSet(2, maxCapacity);

    try {
      for (int i = 0; i <= maxCapacity; i++) {
        set.add(i);
      }
    } catch (IllegalStateException e) {
      Assert.assertEquals(set.size(), 23);

      throw e;
    }
  }

  @Test(
      groups = "fast",
      expectedExceptions = IllegalStateException.class,
      expectedExceptionsMessageRegExp = ".*23.*")
  public void testMaxCapacityHonoredWithAddAll() throws Exception {
    int maxCapacity = 23;
    IntegerHashSet set = new IntegerHashSet(2, maxCapacity);
    Set<Long> addSet = new HashSet<>();

    try {
      for (int i = 0; i <= maxCapacity; i++) {
        addSet.add((long) i);
      }

      set.addAll(addSet);
    } catch (IllegalStateException e) {
      Assert.assertEquals(set.size(), 23);

      throw e;
    }
  }

  @Test(groups = "fast")
  public void testResize() {
    IntegerHashSet set = new IntegerHashSet(2, Integer.MAX_VALUE);

    set.add(1);
    set.add(2);
    set.add(3);
    set.add(4);
    Assert.assertEquals(set.size(), 4);
  }

  @Test(groups = "fast")
  public void testHasChangedRemoveAll() throws Exception {
    ImmutableSet<Object> removeSet = ImmutableSet.builder().add(0).add(1).build();

    defaultSet.removeAll(removeSet);
    Assert.assertEquals(defaultSet.hasChanged(), true);
  }

  @Test(groups = "fast")
  public void testHasChangedRetainAll() throws Exception {
    ImmutableSet<Object> retainSet = ImmutableSet.builder().add(0).add(1).build();

    defaultSet.removeAll(retainSet);
    Assert.assertEquals(defaultSet.hasChanged(), true);
  }

  @Test(groups = "fast")
  public void testHasChangedClear() throws Exception {
    defaultSet.clear();
    Assert.assertEquals(defaultSet.hasChanged(), true);
  }

  @Test(groups = "fast")
  public void testZero() {
    IntegerHashSet set = new IntegerHashSet(1, Integer.MAX_VALUE);

    set.add(0L);
    set.add(1L);
    Assert.assertEquals(set.size(), 2);
    Assert.assertTrue(set.contains(0));
    Assert.assertTrue(set.contains(1));

    set.remove(0);
    Assert.assertEquals(set.size(), 1);
    Assert.assertFalse(set.contains(0));
    Assert.assertTrue(set.contains(1));

    set.addAll(ImmutableList.of(0L));
    Assert.assertEquals(set.size(), 2);
    Assert.assertTrue(set.contains(0));
    Assert.assertTrue(set.contains(1));

    set.retainAll(ImmutableList.of(0));
    Assert.assertEquals(set.size(), 1);
    Assert.assertTrue(set.contains(0));
    Assert.assertFalse(set.contains(1));

    set.add(1);
    Assert.assertEquals(set.size(), 2);
    Assert.assertTrue(set.contains(0));
    Assert.assertTrue(set.contains(1));

    set.removeAll(ImmutableList.of(0));
    Assert.assertEquals(set.size(), 1);
    Assert.assertFalse(set.contains(0));
    Assert.assertTrue(set.contains(1));
  }
}
