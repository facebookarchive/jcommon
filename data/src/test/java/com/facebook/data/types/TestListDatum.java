package com.facebook.data.types;

import com.google.common.collect.ImmutableList;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

public class TestListDatum {

  private ListDatum list1;
  private ListDatum list2;
  private ListDatum list3;
  private ListDatum list4;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    List<Datum> datumList1 = new ImmutableList.Builder<Datum>()
      .add(new StringDatum("a"))
      .add(new StringDatum("b"))
      .add(new StringDatum("c"))
      .build();
    list1 = new ListDatum(datumList1, ',');

     List<Datum> datumList2 = new ImmutableList.Builder<Datum>()
      .add(new StringDatum("a"))
      .add(new StringDatum("b"))
      .add(new StringDatum("d"))
      .build();
    list2 = new ListDatum(datumList2, ',');

    List<Datum> datumList3 = new ImmutableList.Builder<Datum>()
      .add(new StringDatum("1"))
      .add(new StringDatum("2"))
      .add(new StringDatum("3"))
      .build();
    list3 = new ListDatum(datumList3, ',');
    
    // identical to list1
    List<Datum> datumList4 = new ImmutableList.Builder<Datum>()
      .add(new StringDatum("a"))
      .add(new StringDatum("b"))
      .add(new StringDatum("c"))
      .build();
    list4 = new ListDatum(datumList4, ',');
  }
  
  @Test(groups = "fast")
  public void testCompare() throws Exception {
    // check equality
  	Assert.assertEquals(list1.compareTo(list1), 0);
  	Assert.assertEquals(list1.compareTo(list4), 0);
  	Assert.assertEquals(list2.compareTo(list2), 0);
  	Assert.assertEquals(list3.compareTo(list3), 0);
    // now check where difference is 1
  	Assert.assertEquals(list1.compareTo(list2), -1);
  	Assert.assertEquals(list2.compareTo(list1), 1);
    // now arbitrary delta
  	Assert.assertTrue(list1.compareTo(list3) > 0);
  	Assert.assertTrue(list3.compareTo(list1) < 0);
  }
  
  @Test(groups = "fast")
  public void testEquality() throws Exception {
    Assert.assertEquals(list1, list1);
    Assert.assertEquals(list1, list4);
    Assert.assertEquals(list4, list1);
    Assert.assertEquals(list2, list2);
    Assert.assertEquals(list3, list3);
  }
  
  @Test(groups = "fast")
  public void testAsString() throws Exception {
    Assert.assertEquals(list1.asString(), "a,b,c");
  }
  
  @Test(groups = "fast")
  public void testAsBytes() throws Exception {
  	Assert.assertEquals(list1.asBytes(), "a,b,c".getBytes("UTF-8"));
  }
  
  @Test(groups = "fast")
  public void testAsJsonString() throws Exception {
  	Assert.assertEquals(
      DatumUtils.buildJSON(list1).toString(), "[\"a\",\"b\",\"c\"]");
  }
  
  @Test(groups = "fast")
  public void testNullDatum() throws Exception {
    ArrayList<Datum> datumList = new ArrayList<Datum>();
    datumList.add(NullDatum.INSTANCE);
    datumList.add(NullDatum.INSTANCE);
    ListDatum nullList = new ListDatum(datumList);

    Assert.assertEquals(DatumUtils.buildJSON(nullList).toString(), "[null,null]");
  }
}
