package com.facebook.collections;

import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class TestByteArray {
  private ByteArray byteArray1;
  private ByteArray nullArray;
  private ByteArray byteArray2;

  @BeforeMethod(alwaysRun = true)
  public void setUp() throws Exception {
    byteArray1 = ByteArray.wrap("a string".getBytes());
    byteArray2 = ByteArray.wrap("b string".getBytes());
    nullArray = ByteArray.wrap(null);
  }
  
  @Test(groups = "fast")
  public void testCompareTo() throws Exception {
  	Assert.assertEquals(byteArray1.compareTo(byteArray2), -1);
  	Assert.assertEquals(byteArray2.compareTo(byteArray1), 1);
  	Assert.assertEquals(byteArray1.compareTo(byteArray1), 0);
    Assert.assertEquals(byteArray1.compareTo(null), 1);
    Assert.assertEquals(byteArray1.compareTo(nullArray), 1);
    Assert.assertEquals(nullArray.compareTo(null), 1);
    Assert.assertEquals(nullArray.compareTo(byteArray1), -1);
  }
}
