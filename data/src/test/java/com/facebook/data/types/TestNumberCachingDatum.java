package com.facebook.data.types;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestNumberCachingDatum {

  private NumberCachingDatum doubleValueAsString;
  private NumberCachingDatum doubleValue;
  private NumberCachingDatum longValueAsString;
  private NumberCachingDatum longValue;
  private NumberCachingDatum intValueAsString;
  private NumberCachingDatum intValue;
  private NumberCachingDatum stringValue;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    intValue = new NumberCachingDatum(DatumFactory.toDatum(100));
    intValueAsString = new NumberCachingDatum(DatumFactory.toDatum("100"));
    longValue = new NumberCachingDatum(DatumFactory.toDatum(Long.MAX_VALUE));
    longValueAsString = new NumberCachingDatum(DatumFactory.toDatum(Long.MAX_VALUE));
    doubleValue = new NumberCachingDatum(DatumFactory.toDatum(Math.PI));
    doubleValueAsString =
      new NumberCachingDatum(DatumFactory.toDatum(String.format("%s", Math.PI)));
    stringValue = new NumberCachingDatum(DatumFactory.toDatum("smatchemo"));
  }

  @Test(groups = "fast")
  public void testIntegerLongCompatible() throws Exception {
    Assert.assertTrue(DatumType.isLongCompatible(intValue));
    Assert.assertTrue(DatumType.isLongCompatible(intValueAsString));
  }


  @Test(groups = "fast")
  public void testLongLongCompatible() throws Exception {
    Assert.assertTrue(DatumType.isLongCompatible(longValue));
    Assert.assertTrue(DatumType.isLongCompatible(longValueAsString));
  }

  @Test(groups = "fast")
  public void testDoubleLongCompatible() throws Exception {
    Assert.assertFalse(DatumType.isLongCompatible(doubleValue));
    Assert.assertFalse(DatumType.isLongCompatible(doubleValueAsString));
  }

  @Test(groups = "fast")
  public void testStringLongCompatible() throws Exception {
    Assert.assertFalse(DatumType.isLongCompatible(stringValue));
  }
}
