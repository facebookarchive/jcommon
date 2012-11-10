package com.facebook.data.types;

import com.facebook.data.types.BooleanDatum;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestBooleanDatum {
  private BooleanDatum trueBooleanDatum1;
  private BooleanDatum trueBooleanDatum2;
  private BooleanDatum falseBooleanDatum;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    trueBooleanDatum1 = new BooleanDatum(true);
    trueBooleanDatum2 = new BooleanDatum(true);
    falseBooleanDatum = new BooleanDatum(false);
  }

  @Test(groups = "fast")
  public void testSanity() throws Exception {
    Assert.assertEquals(trueBooleanDatum1.asInteger(), 1);
    Assert.assertEquals(falseBooleanDatum.asInteger(), 0);
  }

  @Test(groups = "fast")
  public void testCompare() throws Exception {
    Assert.assertEquals(trueBooleanDatum1.compareTo(trueBooleanDatum1), 0);
    Assert.assertEquals(trueBooleanDatum1.compareTo(trueBooleanDatum2), 0);
    Assert.assertEquals(trueBooleanDatum1.compareTo(falseBooleanDatum), 1);
    Assert.assertEquals(falseBooleanDatum.compareTo(trueBooleanDatum2), -1);
  }

  @Test(groups = "fast")
  public void testEquals() throws Exception {
    Assert.assertEquals(trueBooleanDatum1, trueBooleanDatum1);
    Assert.assertEquals(trueBooleanDatum1, trueBooleanDatum2);
    Assert.assertFalse(trueBooleanDatum1.equals(falseBooleanDatum));
  }
}
