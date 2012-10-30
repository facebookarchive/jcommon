package com.facebook.data.types;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestFloatDatum {
  private FloatDatum booleanValue;
  private FloatDatum byteValue;
  private FloatDatum shortValue;
  private FloatDatum floatValue1;
  private FloatDatum floatValue2;
  private FloatDatum floatValue3;
  private FloatDatum intValue;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    booleanValue = new FloatDatum(1);
    byteValue = new FloatDatum(100);
    shortValue = new FloatDatum(1000);
    intValue = new FloatDatum(100000);
    floatValue1 = new FloatDatum(123.456f);
    floatValue2 = new FloatDatum(123.456f);
    floatValue3 = new FloatDatum(890.123f);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(booleanValue.asBoolean(), true);
    Assert.assertEquals(byteValue.asByte(), 100);
    Assert.assertEquals(shortValue.asShort(), 1000);
    Assert.assertEquals(intValue.asInteger(), 100000);
    Assert.assertEquals(floatValue1.asInteger(), 123);
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(floatValue1.compareTo(floatValue1), 0);
    Assert.assertEquals(floatValue1.compareTo(floatValue2), 0);
    Assert.assertEquals(floatValue1.compareTo(floatValue3), -1);
    Assert.assertEquals(floatValue3.compareTo(floatValue1), 1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(floatValue1, floatValue1);
    Assert.assertEquals(floatValue1, floatValue2);
    Assert.assertFalse(floatValue1.equals(floatValue3));
    Assert.assertEquals(
      DatumFactory.toDatum(true),
      DatumFactory.toDatum(1.0f)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((byte) 100),
      DatumFactory.toDatum(100.0f)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((short) 100),
      DatumFactory.toDatum(100.0f)
    );
    Assert.assertEquals(
      DatumFactory.toDatum(100),
      DatumFactory.toDatum(100.0f)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((double) 100),
      DatumFactory.toDatum(100.0f)
    );
    Assert.assertEquals(
      DatumFactory.toDatum("100"),
      DatumFactory.toDatum(100.0f)
    );
  }
}