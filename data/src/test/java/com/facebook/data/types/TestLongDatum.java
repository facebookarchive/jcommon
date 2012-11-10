package com.facebook.data.types;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestLongDatum {
  private LongDatum booleanValue;
  private LongDatum byteValue;
  private LongDatum shortValue;
  private LongDatum intValue;
  private LongDatum longValue1;
  private LongDatum longValue2;
  private LongDatum longValue3;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    booleanValue = new LongDatum(1);
    byteValue = new LongDatum(100);
    shortValue = new LongDatum(1000);
    intValue = new LongDatum(1000000);
    longValue1 = new LongDatum(10000000000L);
    longValue2 = new LongDatum(10000000000L);
    longValue3 = new LongDatum(20000000000L);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(booleanValue.asBoolean(), true);
    Assert.assertEquals(byteValue.asByte(), 100);
    Assert.assertEquals(shortValue.asShort(), 1000);
    Assert.assertEquals(intValue.asInteger(), 1000000);
    Assert.assertEquals(intValue.asLong(), 1000000L);
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(longValue1.compareTo(longValue1), 0);
    Assert.assertEquals(longValue1.compareTo(longValue2), 0);
    Assert.assertEquals(longValue1.compareTo(longValue3), -1);
    Assert.assertEquals(longValue3.compareTo(longValue1), 1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(longValue1, longValue1);
    Assert.assertEquals(longValue1, longValue2);
    Assert.assertFalse(longValue1.equals(longValue3));
    Assert.assertEquals(
      DatumFactory.toDatum(true),
      DatumFactory.toDatum(1L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((byte) 100),
      DatumFactory.toDatum(100L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((short) 100),
      DatumFactory.toDatum(100L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum(100),
      DatumFactory.toDatum(100L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((float) 100),
      DatumFactory.toDatum(100L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum((double) 100),
      DatumFactory.toDatum(100L)
    );
    Assert.assertEquals(
      DatumFactory.toDatum("100"),
      DatumFactory.toDatum(100L)
    );
  }
}
