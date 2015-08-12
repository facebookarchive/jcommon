package com.facebook.memory.data.structures;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestIntRange {
  private IntRange intRange1;
  private IntRange intRange2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    intRange1 = new IntRange(0, 100);
    intRange2 = new IntRange(0, 100);
  }

  @Test
  public void testShave1() throws Exception {
    IntRange shavedRange = intRange1.shave(50);
    Assert.assertEquals(shavedRange.getLower(), 50);

  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testNegativeValue() throws Exception {
    new IntRange(-1, 10);
  }

  @Test(expectedExceptions = {IllegalArgumentException.class})
  public void testInvalidRange() throws Exception {
    new IntRange(100, 10);
  }
}
