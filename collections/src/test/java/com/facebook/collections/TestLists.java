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

import com.facebook.collectionsbase.Lists;
import com.google.common.collect.ImmutableList;
import java.util.List;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLists {

  private List<String> list1;
  private List<String> list2;
  private List<String> list3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    // should be list2 > list3 > list1
    list1 = new ImmutableList.Builder<String>().add("a").add("b").add("c").build();
    list2 = new ImmutableList.Builder<String>().add("a").add("b").add("d").build();
    list3 = new ImmutableList.Builder<String>().add("a").add("b").add("c").add("d").build();
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(Lists.compareLists(list1, list1), 0);
    Assert.assertEquals(Lists.compareLists(list1, list2), -1);
    Assert.assertEquals(Lists.compareLists(list1, list3), -1);
    Assert.assertEquals(Lists.compareLists(list2, list1), 1);
    Assert.assertEquals(Lists.compareLists(list2, list2), 0);
    Assert.assertEquals(Lists.compareLists(list2, list3), 1);
    Assert.assertEquals(Lists.compareLists(list3, list1), 1);
    Assert.assertEquals(Lists.compareLists(list3, list2), -1);
    Assert.assertEquals(Lists.compareLists(list3, list3), 0);
  }
}
