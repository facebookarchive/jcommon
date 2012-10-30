package com.facebook.collections;

import com.facebook.collectionsbase.Lists;
import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.List;

public class TestLists {

  private List<String> list1;
  private List<String> list2;
  private List<String> list3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    // should be list2 > list3 > list1
    list1 = new ImmutableList.Builder<String>()
      .add("a")
      .add("b")
      .add("c")
      .build();
    list2 = new ImmutableList.Builder<String>()
      .add("a")
      .add("b")
      .add("d")
      .build();
    list3 = new ImmutableList.Builder<String>()
      .add("a")
      .add("b")
      .add("c")
      .add("d")
      .build();
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
